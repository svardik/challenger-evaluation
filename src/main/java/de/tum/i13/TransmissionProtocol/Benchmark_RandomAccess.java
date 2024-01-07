package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.printAvg;

public class Benchmark_RandomAccess {

    public static void main(String[] args) throws Exception {


        GRPCClient grpcClient = new GRPCClient();
        RESTClient restClient = new RESTClient();

        Random r = new Random(42);

        long resultCalculationDelay = 0;
        long delayBetweenOps = 5;

        long delayBetweenBenchmarks = 100;

        int j = r.nextInt(100);
//            System.out.println("Token:");
        String token = "token-"+j;

        int noSequences = 1000;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);

        restClient.changeBatchSize(100,10);

        for (int k = 0; k < noSequences ; k++) {

                grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay);
            Thread.sleep(delayBetweenBenchmarks);
                restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);
            Thread.sleep(delayBetweenBenchmarks);
        }


        printAvg();


        // CLOSE
        grpcClient.close();
        restClient.close();
    }

}
