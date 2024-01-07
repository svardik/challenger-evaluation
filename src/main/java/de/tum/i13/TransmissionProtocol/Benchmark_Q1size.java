package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.restartBenchmarkResults;

public class Benchmark_Q1size {
    public static void main(String[] args) throws Exception {
//        List<Integer> batchsizeToTest = List.of(1,2,5,10,20,50,100,200,500,1000,2000,5000,10000);
//        List<Integer> q1SizeToTest = List.of(1,5,10,20,30,40,50,60,70,80,90,100);
        List<Integer> q1SizeToTest = List.of(5,10,25,50,75,100,150,200,250,300,400,500,600,700,800,900,1000);


        int batchsize = 100;
        int maxbatches = 100;


        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();

        Random r = new Random(42);

        long resultCalculationDelay = 5;
        long delayBetweenBenchmarks = 100;
        long delayBetweenOps = 0;

        int noOps = 50;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);

        restClient.changeBatchSize(batchsize,maxbatches);
        for (int q1size: q1SizeToTest
        ) {
            System.out.println("q1Size: "+q1size);
            for (int i = 0; i < noOps; i++) {
                grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay,q1size);
                Thread.sleep(delayBetweenBenchmarks);
                restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, q1size);
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
