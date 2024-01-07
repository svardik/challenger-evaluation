package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;

public class Benchmark_Proxy_Throughput {

    public static void main(String[] args) throws Exception {
        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();
        ProxyRESTClient proxyRESTClient = new ProxyRESTClient();

        Random r = new Random(42);

        long resultCalculationDelay = 0;
        long delayBetweenOps = 0;

        long delayBetweenBenchmarks = 1000;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        int noIterations = 10;
        int noSequencesPerIteration = 1000;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);
        restClient.changeBatchSize(100,10);

        for (int k = 0; k < noIterations ; k++) {
            System.out.println("Benchmarking REST");
            for (int i = 0; i < noSequencesPerIteration; i++) {
                restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);
            }
            Thread.sleep(delayBetweenBenchmarks);

            System.out.println("Benchmarking GRPC");
            for (int i = 0; i < noSequencesPerIteration; i++) {
                grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay);

            }
            Thread.sleep(delayBetweenBenchmarks);

            System.out.println("Benchmarking Proxy");
            for (int i = 0; i < noSequencesPerIteration; i++) {
                proxyRESTClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);
            }
            Thread.sleep(delayBetweenBenchmarks);




        }


        printAvg();


        // CLOSE
        grpcClient.close();
        restClient.close();

}}
