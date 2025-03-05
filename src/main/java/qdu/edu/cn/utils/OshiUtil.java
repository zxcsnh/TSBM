package qdu.edu.cn.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.util.Util;

public class OshiUtil {
    private static final Logger logger = LoggerFactory.getLogger(OshiUtil.class);

    private CentralProcessor processor;
    private GlobalMemory memory;
    private List<HWDiskStore> diskStores;
    private SystemInfo si;

    private double totalLastReadBytes = 0L;
    private double totalLastWriteBytes = 0L;
    private long[] lastCpuTicks;
    private long delay = 0L;
    private long lastTime = 0L;

    public OshiUtil(long Delay) {
        try {
            lastTime = System.nanoTime();
            delay = Delay;
            si = new SystemInfo();
            processor = si.getHardware().getProcessor();
            memory = si.getHardware().getMemory();
            diskStores = si.getHardware().getDiskStores();
            lastCpuTicks = processor.getSystemCpuLoadTicks();
            for (HWDiskStore disk : diskStores) {
                totalLastReadBytes += disk.getReadBytes();
                totalLastWriteBytes += disk.getWriteBytes();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize SystemInfo", e);
        }
    }

    public void monitor(String resultFile)
    {
        try {
            start();
            String result;
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
            Write.writeLine(resultFile, "start at "+formattedDate);
            while(true){
                result = start();
                Write.writeLine(resultFile, result);
            }
        } catch (Exception e) {
            logger.error("Monitor Error",e);
        }
    }

    public String start() {
        initializeSystemInfo();
        try {
            long toWait = delay - (System.nanoTime() - lastTime) / 1_000_000;
            if (toWait > 0L) {
                Util.sleep(delay);
            }
            lastTime = System.nanoTime();
            String res = "";
            res += getCpuLoad();
            res += getMemoryLoad();
            res += getDisksIO();
            return res;
        } catch (Exception e) {
            logger.error("Error in test method", e);
            return "";
        }
    }

    private void initializeSystemInfo() {
        if (si == null) {
            si = new SystemInfo();
        }
        try {
            si = new SystemInfo();
            processor = si.getHardware().getProcessor();
            memory = si.getHardware().getMemory();
            diskStores = si.getHardware().getDiskStores();
        } catch (Exception e) {
            logger.error("Failed to initialize SystemInfo", e);
        }
    }

    public String getCpuLoad() {
        try {
            double cpuLoad = processor.getSystemCpuLoadBetweenTicks(lastCpuTicks);
            lastCpuTicks = processor.getSystemCpuLoadTicks();

            return String.format("%.1f,", cpuLoad * 100);
        } catch (Exception e) {
            logger.error("Failed to get CPU load", e);
            return "-1,";
        }
    }
    public String getMemoryLoad() {
        try {
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            // throw new Exception("This is a custom exception!");
            return String.format("%.1f,", memoryUsage);
        } catch (Exception e) {
            logger.error("Failed to get memory load", e);
            return "-1,";
        }
    }

    public String getDisksIO() {
        try {
            long totalReadBytes = 0;
            long totalWriteBytes = 0;

            for (HWDiskStore disk : diskStores) {
                totalReadBytes += disk.getReadBytes();
                totalWriteBytes += disk.getWriteBytes();
            }
            double readMB = ((totalReadBytes - totalLastReadBytes) / (1024.0 * 1024 )) * 1000 / delay;
            double writeMB = ((totalWriteBytes - totalLastWriteBytes) / (1024.0 * 1024)) * 1000 / delay;

            totalLastReadBytes = totalReadBytes;
            totalLastWriteBytes = totalWriteBytes;

            return String.format("%.2f,%.2f", readMB, writeMB);
        } catch (Exception e) {
            logger.error("Failed to get disk I/O", e);
            return "-1";
        }
    }
}
