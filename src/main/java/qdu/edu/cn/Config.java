package qdu.edu.cn;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
    public static final long START = 1735660800000L;  // 2025-01-01 00:00:00
    public static final long THREADSTARTA = 1736265600000L;  // 2025-01-08 00:00:00
    public static final long THREADSTARTB = 1736870400000L;  // 2025-01-15 00:00:00
    public static final long BATCHSTARTA = 1737475200000L;  // 2025-01-22 00:00:00
    public static final long BATCHSTARTB = 1738080000000L;  // 2025-01-29 00:00:00
    public static final int SUM = 1200;
    public static final double MEAN = 20.0;
    public static final double STD_DEV = 5.0;
    public static int MAXFARMS = 32;
    public static int MAXDEVICES = 150;
    public static int BATCHSIZE = 5;
    public static long DELAY = 1000;
    public static int FARMS = 8;
    public static int DEVICES = 50;
    public static int SENSORS = 50;
    public static boolean ISIMPORT = true;
    public static boolean ISTHREAD = true;
    public static boolean ISBATCH = true;
    public static boolean ISMONITOR = true;
}

class JsonConfig {

    @JsonProperty("targetDB")
    private String targetDB;

    @JsonProperty("farms")
    private int farms;

    @JsonProperty("devices")
    private int devices;

    @JsonProperty("sensors")
    private int sensors;

    @JsonProperty("Import")
    private ImportConfig importConfig;

    @JsonProperty("batchsize")
    private int batchSize;

    @JsonProperty("MultiThread")
    private MultiThreadConfig multiThreadConfig;

    @JsonProperty("BatchGrowth")
    private BatchGrowthConfig batchGrowthConfig;

    @JsonProperty("DataBase")
    private Map<String, DatabaseConfig> databaseConfig;

    @JsonProperty("monitor")
    private MonitorConfig monitorConfig;

    // Getters and setters...
    public String getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(String targetDB) {
        this.targetDB = targetDB;
    }

    public int getFarms() {
        return farms;
    }

    public void setFarms(int farms) {
        this.farms = farms;
    }

    public int getDevices() {
        return devices;
    }

    public void setDevices(int devices) {
        this.devices = devices;
    }

    public int getSensors() {
        return sensors;
    }

    public void setSensors(int sensors) {
        this.sensors = sensors;
    }

    public ImportConfig getImportConfig() {
        return importConfig;
    }

    public void setImportConfig(ImportConfig importConfig) {
        this.importConfig = importConfig;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public MultiThreadConfig getMultiThreadConfig() {
        return multiThreadConfig;
    }

    public void setMultiThreadConfig(MultiThreadConfig multiThreadConfig) {
        this.multiThreadConfig = multiThreadConfig;
    }

    public BatchGrowthConfig getBatchGrowthConfig() {
        return batchGrowthConfig;
    }

    public void setBatchGrowthConfig(BatchGrowthConfig batchGrowthConfig) {
        this.batchGrowthConfig = batchGrowthConfig;
    }

    public Map<String, DatabaseConfig> getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Map<String, DatabaseConfig> databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public MonitorConfig getMonitorConfig() {
        return monitorConfig;
    }

    public void setMonitorConfig(MonitorConfig monitorConfig) {
        this.monitorConfig = monitorConfig;
    }

    // Inner classes for nested JSON objects
    public static class ImportConfig {
        @JsonProperty("isTest")
        private boolean isTest;
        @JsonProperty("threads")
        private int threads;
        @JsonProperty("counts")
        private int counts;

        // Getters and setters...
        public boolean isTest() {
            return isTest;
        }

        public void setTest(boolean test) {
            isTest = test;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getCounts() {
            return counts;
        }

        public void setCounts(int counts) {
            this.counts = counts;
        }
    }

    public static class MultiThreadConfig {
        @JsonProperty("isTest")
        private boolean isTest;
        @JsonProperty("maxfarms")
        private int maxFarms;

        // Getters and setters...
        public boolean isTest() {
            return isTest;
        }

        public void setTest(boolean test) {
            isTest = test;
        }

        public int getMaxFarms() {
            return maxFarms;
        }

        public void setMaxFarms(int maxFarms) {
            this.maxFarms = maxFarms;
        }
    }

    public static class BatchGrowthConfig {
        @JsonProperty("isTest")
        private boolean isTest;
        @JsonProperty("maxdevices")
        private int maxDevices;

        // Getters and setters...
        public boolean isTest() {
            return isTest;
        }

        public void setTest(boolean test) {
            isTest = test;
        }

        public int getMaxDevices() {
            return maxDevices;
        }

        public void setMaxDevices(int maxDevices) {
            this.maxDevices = maxDevices;
        }
    }

    public static class DatabaseConfig {
        @JsonProperty("name")
        private String name;
        @JsonProperty("classPath")
        private String classPath;
        @JsonProperty("ip")
        private String ip;
        @JsonProperty("port")
        private String port;
        @JsonProperty("username")
        private String username;
        @JsonProperty("password")
        private String password;

        // Getters and setters...
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClassPath() {
            return classPath;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class MonitorConfig {
        @JsonProperty("isOpen")
        private boolean isOpen;
        @JsonProperty("delay")
        private int delay;

        // Getters and setters...
        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen(boolean open) {
            isOpen = open;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}