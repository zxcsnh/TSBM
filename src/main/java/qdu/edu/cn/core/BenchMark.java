package qdu.edu.cn.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.util.Util;
import qdu.edu.cn.Config;
import qdu.edu.cn.utils.OshiUtil;
import qdu.edu.cn.utils.Write;

public class BenchMark {
    private static final Logger logger = LoggerFactory.getLogger(BenchMark.class);
    public static void startMonitor(String File)
    {

    }
    public static void startTest(String path, String classPath, String dbName, String host, String port, String user, String pwd
        , int farms, int devices, int sensors) {
        MonitorThread monitor = new MonitorThread();
        Adapter adapter = null;
        try {
            adapter = (Adapter) Class.forName(classPath).getDeclaredConstructor().newInstance();
            adapter.initConnect(host,port,user,pwd);
        } catch (Exception e) {
            logger.error("Error initializing adapters", e);
        }
        String resultPath = path + "/result/";
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
        resultPath = resultPath + dbName + formattedDate + "/";
        String resultFile = resultPath + "result.txt";
        System.out.println(">>>>>>>>>>Start Test<<<<<<<<<<");
        if (Config.ISIMPORT){
            System.out.println(">>>>>>>>>>Start Import<<<<<<<<<<");
            monitor.beginMonitoring(resultPath+"ImportMonitor.txt", Config.DELAY);
            Write.writeLine(resultFile, "Import Test : pps,execTime(ms)");
            Write.writeLine(resultFile, "预计写入:"+Config.SUM*devices*farms*sensors);
            long execTime = ImportTest.start(adapter, resultPath, 8, 400, farms, devices, sensors);
            long pps = -1L;
            if(execTime <= 0){
                execTime = -1L;
            }else if(farms > 0 && devices > 0 && sensors > 0 && Config.SUM > 0){
                pps = (farms * devices * sensors * Config.SUM * 1000) / execTime;
            }
            Write.writeLine(resultFile, pps+","+execTime);
            monitor.stop();    
        }

        Adapter[] adapters = new Adapter[Config.MAXFARMS];
        try {
            for (int i = 0; i < Config.MAXFARMS; i++) {
                adapters[i] = (Adapter) Class.forName(classPath).getDeclaredConstructor().newInstance();
                adapters[i].initConnect(host,port,user,pwd);
            }
        } catch (Exception e) {
            logger.error("Error initializing adapters", e);
        }

        if (Config.ISTHREAD){
            System.out.println(">>>>>>>>>>Start MultiThread<<<<<<<<<<");
            monitor.beginMonitoring(resultPath+"MultiThreadMonitor.txt", Config.DELAY);
            System.out.println(">>>>>>>>>>Start ThreadA<<<<<<<<<<");
            Write.writeLine(resultFile, "预计写入:"+(2*Config.MAXFARMS-1)*Config.DEVICES*Config.SENSORS*Config.BATCHSIZE);
            Write.writeLine(resultFile, "MultiThreadTest Test A : pps,execTime(ms)");
            MultiThreadTest.writeA(adapter, resultFile);
    
    
            System.out.println(">>>>>>>>>>Start ThreadB<<<<<<<<<<");
            Write.writeLine(resultFile, "预计写入:"+(2*Config.MAXFARMS-1)*Config.DEVICES*Config.SENSORS*Config.BATCHSIZE);
            Write.writeLine(resultFile, "MultiThreadTest Test B : pps,execTime(ms)");
            MultiThreadTest.writeB(adapters, resultFile);
            monitor.stop();
        }

        if(Config.ISBATCH){
            System.out.println(">>>>>>>>>>Start BatchGrowth<<<<<<<<<<");
            monitor.beginMonitoring(resultPath+"BatchGrowthMonitor.txt", Config.DELAY);
            System.out.println(">>>>>>>>>>Start BatchA<<<<<<<<<<");
            Write.writeLine(resultFile, "预计写入:"+((Config.MAXDEVICES/50)*(Config.MAXDEVICES/50+1)/2)*Config.FARMS*Config.SENSORS*Config.BATCHSIZE);
            Write.writeLine(resultFile, "BatchGrowthTes Test A : pps,execTime(ms)");
            BatchGrowthTest.writeA(adapter, resultFile);
    
    
            System.out.println(">>>>>>>>>>Start BatchB<<<<<<<<<<");
            Write.writeLine(resultFile, "预计写入:"+((Config.MAXDEVICES/50)*(Config.MAXDEVICES/50+1)/2)*Config.FARMS*Config.SENSORS*Config.BATCHSIZE);
            Write.writeLine(resultFile, "BatchGrowthTes Test B : pps,execTime(ms)");
            BatchGrowthTest.writeB(adapters, resultFile);
            monitor.stop();
        }
    }
}
class MonitorThread implements Runnable {
    private volatile boolean running = true; // 使用 volatile 保证可见性
    private volatile String resultFile;
    private volatile long delay;
    public void beginMonitoring(String resultFile, long delay) {
        this.running = true;
        this.delay = delay;
        this.resultFile = resultFile;
        if(Config.ISMONITOR){
            new Thread(this).start();
        }
        
    }
    @Override
    public void run() {
        OshiUtil monitor = new OshiUtil(delay);
        String result;
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
        Write.writeLine(resultFile, "CPU(%),Memory(%),DiskRead(MB/s),DiskWrite(MB/s)");
        Write.writeLine(resultFile, "start at "+formattedDate);
        while(running){
            result = monitor.start();
            Write.writeLine(resultFile, result);
        }
        formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
        Write.writeLine(resultFile, "end at "+formattedDate);
    }

    public void stop() {
        if(Config.ISMONITOR){
            Util.sleep(delay);
        }
        running = false; // 线程会在下一次循环检查时自动退出
    }
}