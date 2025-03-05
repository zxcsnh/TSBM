package qdu.edu.cn.utils;
import java.util.Random;


public class GenerateData {
    private static final Random random = new Random();
    private static final double MEAN = 20.0;
    private static final double STD_DEV = 5.0;
    public static StringBuffer generate(long timestamp, int sum, int farms, int devices, int sensors,int farmName) {
        StringBuffer data = new StringBuffer();
        long ts = timestamp;
        int farm = farmName;
        for (int i = 0; i < sum; i++) {
            for(int j = 0;j < farms; j++){
                String fn = "f" + String.valueOf(farm+j);
                data.append(generateNormalDistributionData(ts + i * 5000, fn, devices, sensors));
            }
        }
        return data;
    }
    // 生成符合正态分布的随机数据
    public static StringBuffer generateNormalDistributionData(long ts, String farmName, int devices, int sensors) {
        StringBuffer data = new StringBuffer();
        for (int i = 1; i <= devices; i++) {
            String deviceName = "d" + i;
            data.append(ts);
            data.append(",");
            data.append(farmName);
            data.append(",");
            data.append(deviceName);
            data.append(",");
            for (int j = 1; j <= sensors; j++) {
                // nextGaussian() 生成标准正态分布的随机数，均值为0，标准差为1
                data.append(MEAN + random.nextGaussian() * STD_DEV);
                data.append(",");
            }
            data.append(System.lineSeparator());
        }
        return data;
    }
}
