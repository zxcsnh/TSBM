package qdu.edu.cn.core;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qdu.edu.cn.Config;
import qdu.edu.cn.utils.GenerateData;
import qdu.edu.cn.utils.Progress;
import qdu.edu.cn.utils.Write;

public class ImportTest {
    private static final Logger logger = LoggerFactory.getLogger(ImportTest.class);

    public static long start(Adapter adapter, String resultPath, int threads, int counts, int farms, int devices, int sensors) {
        try {
            int sum = 0;
            int count = counts / threads;
            long execTimeSum = 0;
            String File = resultPath + "Load.txt";
            StringBuffer[] datas = new StringBuffer[threads];
            for (int i = 0; i < threads; i++) {
                datas[i] = new StringBuffer();
            }
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CompletionService<Long> cs = new ExecutorCompletionService<>(executor);

            try {
                while (sum < Config.SUM) {
                    if (sum + counts > Config.SUM) {
                        counts = Config.SUM - sum;
                        threads = Math.max(1, counts / Math.max(1, count));  // 避免除零异常
                        count = counts / threads;
                    }

                    for (int i = 0; i < threads; i++) {
                        datas[i] = GenerateData.generate(Config.START + (sum + count * i) * 5000, count, farms, devices, sensors, 1);
                    }
                    sum += counts;

                    long beginTime = System.currentTimeMillis();
                    long executionTime = 1;

                    try {
                        for (int i = 0; i < threads; i++) {
                            final int index = i;
                            cs.submit(() -> adapter.insert(datas[index].toString()));
                        }
                        for (int i = 0; i < threads; i++) {
                            try {
                                if (cs.take().get() < 0) {
                                    logger.error("load data error");
                                    executionTime = -1;
                                }
                            } catch (ExecutionException e) {
                                logger.error("Execution error during importData: ", e);
                                executionTime = -1;
                            }
                        }
                        if (executionTime < 0) {
                            return -1;
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Thread interrupted: ", e);
                        return -1;  // 返回错误码
                    }

                    long endTime = System.currentTimeMillis();
                    executionTime = endTime - beginTime;

                    for (int i = 0; i < threads; i++) {
                        datas[i].setLength(0);
                    }

                    long pps = 0;
                    execTimeSum += executionTime;
                    if (executionTime > 0) {
                        pps = (counts * farms * devices * sensors * 1000L / executionTime);
                    }
                    Write.writeLine(File, pps + "," + executionTime);
                    Progress.progressBar(Config.SUM, sum, "pps:" + pps + ";   ");
                }
            } finally {
                if (executor != null && !executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
            System.out.println("");
            return execTimeSum;
        } catch (Exception e) {
            logger.error("Unexpected error in importData: ", e);
            return -1;
        }
    }
}