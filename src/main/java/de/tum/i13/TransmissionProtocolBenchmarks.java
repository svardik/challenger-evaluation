package de.tum.i13;

import de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking;
import de.tum.i13.TransmissionProtocol.RESTMain;
import de.tum.i13.TransmissionProtocol.RESTProxyMain;

import java.util.Random;

public class TransmissionProtocolBenchmarks {
    public static void main(String[] args) throws Exception {
        long delayBetweenBenchmarks = 200;
        Random random = new Random(1337);


        for (int i = 0; i < 1000; i++) {
            int j = random.nextInt(100);
//            System.out.println("Token:");
            String[] params = new String[]{"token-"+j};
//            if (random.nextBoolean()){
//                Main.main(params);
//                Thread.sleep(delayBetweenBenchmarks);
//                RESTMain.main(params);
//            } else {}
                RESTMain.main(params);

                Thread.sleep(delayBetweenBenchmarks);

                Main.main(params);

                Thread.sleep(delayBetweenBenchmarks);

            RESTProxyMain.main(params);


            Thread.sleep(delayBetweenBenchmarks);
            System.out.println("benchmark nr: "+i);
        }
        PerformanceBenchmarking.printAvg();
        PerformanceBenchmarking.saveToCsv();

    }
}
