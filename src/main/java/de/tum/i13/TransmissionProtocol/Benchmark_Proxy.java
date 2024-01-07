package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;

public class Benchmark_Proxy {
    public static void main(String[] args) throws Exception {
        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();
        ProxyRESTClient proxyRESTClient = new ProxyRESTClient();

        Random r = new Random(42);


        long delayBetweenOps = 5;
        long resultCalculationDelay = 0;

        long delayBetweenBenchmarks = 100;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);
        restClient.changeBatchSize(100,10);

        for (int i = 0; i < 1000; i++) {
            restClient.runBenchmarkingSequence(token,"Evaluation","test"+token,queriesString,delayBetweenOps,resultCalculationDelay);
            Thread.sleep(delayBetweenBenchmarks);
            grpcClient.runBenchmarkingSequence(token,"Evaluation","test"+token,queries,delayBetweenOps,resultCalculationDelay);
            Thread.sleep(delayBetweenBenchmarks);
            proxyRESTClient.runBenchmarkingSequence(token,"Evaluation","test"+token,queriesString,delayBetweenOps,resultCalculationDelay);
            Thread.sleep(delayBetweenBenchmarks);
        }

        // CLOSE
        grpcClient.close();
        restClient.close();
        proxyRESTClient.close();

        printAvg();
    }
}
