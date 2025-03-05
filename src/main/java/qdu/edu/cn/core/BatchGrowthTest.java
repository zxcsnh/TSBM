package qdu.edu.cn.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.util.Util;
import qdu.edu.cn.Config;
import qdu.edu.cn.utils.GenerateData;
import qdu.edu.cn.utils.Progress;
import qdu.edu.cn.utils.Write;

public class BatchGrowthTest {
    private static final Logger logger = LoggerFactory.getLogger(BatchGrowthTest.class);

    public static void writeA(Adapter adapter, String resultPath) {
        final int MAXDEVICES = Config.MAXDEVICES;
        final int BATCHSIZE = Config.BATCHSIZE;
        ExecutorService executor = null;
        CompletionService<Long> cs;
        try {
            executor = Executors.newFixedThreadPool(Config.FARMS);
            cs = new ExecutorCompletionService<>(executor);
    
            int timeIndex = 0;
            for (int i = 50; i <= MAXDEVICES; i = i + 50, timeIndex++) {
                long execTime = 0;
                int errors = 0;
                for (int k = 1; k <= BATCHSIZE; k++) {
                    long timeBegin = System.currentTimeMillis();
                    for (int j = 1; j <= Config.FARMS; j++) {
                        final int farm = j;
                        final int device = i;
                        final long ts = Config.BATCHSTARTA + timeIndex * BATCHSIZE * 5000 + (k - 1) * 5000;
    
                        cs.submit(() -> {
                            try {
                                StringBuffer data = GenerateData.generate(ts, 1, 1, device, Config.SENSORS, farm);
                                return adapter.insert(data.toString());
                            } catch (Exception e) {
                                logger.error("Error generating or inserting data for farm " + farm, e);
                                return -1L; // Return -1 in case of failure
                            }
                        });
                    }
    
                    boolean error = false;
                    for (int j = 1; j <= Config.FARMS; j++) {
                        try {
                            if (cs.take().get() < 0) {
                                error = true;  // Timeout or failure
                            }
                        } catch (Exception e) {
                            logger.error("Error processing result for batch " + k + " of size " + i, e);
                        }
                    }
    
                    long timeEnd = System.currentTimeMillis();
                    if (error) {
                        errors++;
                        if (timeEnd - timeBegin < 5000) {
                            Util.sleep(5000 - (timeEnd - timeBegin));  // Ensure a 5-second gap
                        }
                        continue;
                    }
    
                    execTime += timeEnd - timeBegin;
                    long pps = i * Config.FARMS * Config.SENSORS * 1000 / (timeEnd - timeBegin);
                    Progress.progressBar(BATCHSIZE, k,
                            "BatchA-" + i + "-" + k + ":  " + pps + ";");
    
                    // if (timeEnd - timeBegin < 5000) {
                    //     Util.sleep(5000 - (timeEnd - timeBegin));
                    // }

                }
                System.out.println("");
    
                // Write the result
                if (execTime <= 0) {
                    Write.writeLine(resultPath, "BatchA-" + i + ":  -1,-1");
                } else {
                    execTime = execTime / (BATCHSIZE - errors);
                    long pps = i * Config.FARMS * Config.SENSORS * 1000 / execTime;
                    if (errors <= 0) {
                        Write.writeLine(resultPath, "BatchA-" + i + ":  " + pps + "," + execTime);
                    } else {
                        Write.writeLine(resultPath, "BatchA-" + i + ":  " + pps + "," + execTime + ",-1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error occurred during writeA execution.", e);
        } finally {
            // Ensure the resources are cleaned up properly
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();  // Forcefully shutdown if tasks are not finished
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();  // Restore interrupted status
                    logger.error("Executor service interrupted during shutdown", e);
                }
            }
        }
    }
    
    public static void writeB(Adapter[] adapters, String resultPath) {
        final int MAXDEVICES = Config.MAXDEVICES;
        final int BATCHSIZE = Config.BATCHSIZE;
        ExecutorService executor = null;
        CompletionService<List<Long>> cs;

        List<Long>[] results = new ArrayList[Config.FARMS];
        try {
            executor = Executors.newFixedThreadPool(Config.FARMS);
            cs = new ExecutorCompletionService<>(executor);
    
            int timeIndex = 0;
            for (int i = 50; i <= MAXDEVICES; i = i + 50, timeIndex++) {
                for (int j = 1; j <= Config.FARMS; j++) {
                    final int farm = j;
                    final int device = i;
                    final long ts = Config.BATCHSTARTB + timeIndex * BATCHSIZE * 5000;
    
                    cs.submit(() -> {
                        try {
                            List<Long> list = new ArrayList<>();
                            for (int k = 1; k <= BATCHSIZE; k++) {
                                long timeBegin = System.currentTimeMillis();
                                StringBuffer data = GenerateData.generate(ts + (k - 1) * 5000, 1, 1, device, Config.SENSORS, farm);
                                long res = adapters[farm - 1].insert(data.toString());
                                list.add(res);
                                long timeEnd = System.currentTimeMillis();
                                long execTIme = timeEnd - timeBegin;
                                // if (execTIme < 5000) {
                                //     Util.sleep(5000 - timeout);
                                // }
                            }
                            return list;
                        } catch (Exception e) {
                            logger.error("Error generating or inserting data for farm " + farm, e);
                            return null; // Return null if failed
                        }
                    });
                }
    
                // Process results
                for (int j = 0; j < Config.FARMS; j++) {
                    try {
                        results[j] = cs.take().get();
                    } catch (Exception e) {
                        logger.error("Error processing result for batch " + i + " of size " + i, e);
                    }
                }
    
                long execTime = 0L;
                long errors = 0L;
                long pps = -1L;
                for (int j = 0; j < Config.FARMS; j++) {
                    if (results[j] == null) {
                        errors += BATCHSIZE;
                    } else {
                        for (Long result : results[j]) {
                            if (result < 0) {
                                errors++;
                            } else {
                                execTime += result;
                            }
                        }
                    }
                }
    
                errors = (Config.FARMS * BATCHSIZE) - errors;
                if (errors <= 0) {
                    Write.writeLine(resultPath, "BatchB-" + i + ":  -1,-1");
                } else {
                    execTime = execTime / errors;
                    pps = i * Config.FARMS * Config.SENSORS * 1000 / execTime;
                    if(errors != (Config.FARMS * BATCHSIZE)){
                        Write.writeLine(resultPath, "BatchB-" + i + ":  " + pps + "," + execTime + ",-1");
                    }else{
                        Write.writeLine(resultPath, "BatchB-" + i + ":  " + pps + "," + execTime);
                    }
                }
                Progress.progressBar((int) (MAXDEVICES/50), (int) (i/50),
                        "BatchB-" + i + ":  " + pps + ";");
            }
            System.out.println("");
        } catch (Exception e) {
            logger.error("Unexpected error occurred during writeB execution.", e);
        } finally {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                    logger.error("Executor service interrupted during shutdown", e);
                }
            }
        }
    }    
}

