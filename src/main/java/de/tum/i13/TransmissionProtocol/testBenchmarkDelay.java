package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class testBenchmarkDelay {
        public static void main(String[] args) throws Exception {
            List<Long> delaysToTest = List.of(0L,1L,2L,5L,10L,30L);

            Random r = new Random(42);

            int noOps = 1000;
            int delayBetweenBenchmarks = 100;
            for (Long delayBetweenOps: delaysToTest
            ) {
                System.out.println("Delay: "+delayBetweenOps);
                for (int i = 0; i < noOps; i++) {
                    operation(42);
                    Thread.sleep(delayBetweenOps);
                }
                Thread.sleep(delayBetweenBenchmarks);
                printAvg();
                restartBenchmarkResults();

            }

        }
    public static void operation(int i) throws InterruptedException {
            startMeasurement("op1");
            Thread.sleep(1);
            endMeasurement("op1");
    }
}
