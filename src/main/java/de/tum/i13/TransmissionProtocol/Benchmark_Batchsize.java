package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.restartBenchmarkResults;

public class Benchmark_Batchsize {
    public static void main(String[] args) throws Exception {
//        List<Integer> batchsizeToTest = List.of(1,2,5,10,20,50,100,200,500,1000,2000,5000,10000);
        List<Integer> batchsizeToTest = List.of(1,10,100,500,1000,1500,2000,2500,3000,3500,4000,4500,5000);

        int maxbatches = 100;

        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();

        Random r = new Random(42);



        long resultCalculationDelay = 0;

        long delayBetweenBenchmarks = 100;
        long delayBetweenOps = 5;

        int noOps = 100;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);

        for (int batchsize: batchsizeToTest
        ) {
            restClient.changeBatchSize(batchsize,maxbatches);
            System.out.println("Batchsize: "+batchsize);
            for (int i = 0; i < noOps; i++) {
                grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay);
                Thread.sleep(delayBetweenBenchmarks);
                restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);
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
