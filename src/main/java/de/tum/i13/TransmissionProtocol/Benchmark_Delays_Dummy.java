package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.restartBenchmarkResults;

public class Benchmark_Delays_Dummy {
    public static void main(String[] args) throws Exception {
        List<Long> delaysToTest = List.of(0L);

        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();
        Random r = new Random(42);



        long resultCalculationDelay = 0;

        long delayBetweenBenchmarks = 100;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);

        for (Long delayBetweenOps: delaysToTest
        ) {
            System.out.println("Delay: "+delayBetweenOps);
            for (int i = 0; i < 10; i++) {
                grpcClient.runBenchmarkingSequenceDummy(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay,0);
                Thread.sleep(delayBetweenBenchmarks);
                restClient.runBenchmarkingSequenceDummy(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay,0);
                Thread.sleep(delayBetweenBenchmarks);
            }
            printAvg();
            restartBenchmarkResults();
        }

        // CLOSE
        grpcClient.close();
        restClient.close();
    }
}
