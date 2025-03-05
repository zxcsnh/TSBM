package qdu.edu.cn;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import qdu.edu.cn.core.BenchMark;
import qdu.edu.cn.utils.GenerateData;
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            String path = args[0];
            ObjectMapper objectMapper = new ObjectMapper();
            JsonConfig jsonConfig = objectMapper.readValue(new File(path+"/config.json"), JsonConfig.class);
            Config.FARMS = jsonConfig.getFarms();
            Config.DEVICES = jsonConfig.getDevices();
            Config.SENSORS = jsonConfig.getSensors();
            Config.MAXFARMS = jsonConfig.getMultiThreadConfig().getMaxFarms();
            Config.MAXDEVICES = jsonConfig.getBatchGrowthConfig().getMaxDevices();
            Config.BATCHSIZE = jsonConfig.getBatchSize();
            Config.DELAY = jsonConfig.getMonitorConfig().getDelay();
            Config.ISMONITOR = jsonConfig.getMonitorConfig().isOpen();
            Config.ISIMPORT = jsonConfig.getImportConfig().isTest();
            Config.ISTHREAD = jsonConfig.getMultiThreadConfig().isTest();
            Config.ISBATCH = jsonConfig.getBatchGrowthConfig().isTest();
            String dbName = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getName();
            String classPath = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getClassPath();
            String host = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getIp();
            String port = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getPort();
            String user = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getUsername();
            String pwd = jsonConfig.getDatabaseConfig().get(jsonConfig.getTargetDB()).getPassword();
            BenchMark.startTest(path+'/', classPath, dbName, host, port, user, pwd, Config.FARMS, Config.DEVICES, Config.SENSORS);
        } catch (IOException e) {
            logger.error("Failed to read config file;"+e);
        }

    }

    public static void saveDate() {
        Path dirPath = Paths.get("data");  // 定义文件夹路径
        Path filePath = Paths.get("data/example.txt");  // 定义文件路径
        long START = 1735660800000L;  // 2025-01-01 00:00:00
        int SUM = 500;
        int FARMS = 500;
        int DEVICES = 50;
        int SENSORS = 50;

        try {
            // 创建文件夹（如果不存在的话）
            if (Files.notExists(dirPath)) {
                Files.createDirectories(dirPath);  // 创建目录及其父目录
            }
            // 创建文件并写入数据（如果文件不存在则创建）
            Files.write(filePath, "".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("数据写入成功到文件: " + filePath);
            String content;
            for(int i = 0; i < SUM; i++) {
                content = GenerateData.generate(START + i * 5000, 1, FARMS, DEVICES, SENSORS, 1).toString();
                Files.write(filePath, content.getBytes(), StandardOpenOption.APPEND);
                // 计算进度百分比
                int progress = (i * 100) / SUM;
                // 构造进度条显示内容
                StringBuilder progressBar = new StringBuilder("[");
                int barLength = 50;  // 进度条长度
                int pos = (progress * barLength) / 100;
                // 填充进度条
                for (int j = 0; j < barLength; j++) {
                    if (j < pos) {
                        progressBar.append("#");  // 已完成部分
                    } else {
                        progressBar.append(" ");  // 未完成部分
                    }
                }
                progressBar.append("] ");
                progressBar.append(progress);
                progressBar.append("%");
                System.out.print("\r" + progressBar.toString());
            }
        } catch (IOException e) {
            logger.error("Failed to write data to file;"+e);
            System.exit(1);
        }
    }
}