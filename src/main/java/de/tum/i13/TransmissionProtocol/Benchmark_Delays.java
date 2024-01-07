package de.tum.i13.TransmissionProtocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tum.i13.challenge.Query;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Queue;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.endMeasurement;

public class Benchmark_Delays {
    public static void main(String[] args) throws Exception {
        List<Long> delaysToTest = List.of(0L,1L,2L,5L,10L,20L,30L);

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

        restClient.changeBatchSize(100,10);
        for (Long delayBetweenOps: delaysToTest
             ) {
            System.out.println("Delay: "+delayBetweenOps);
            for (int i = 0; i < 100; i++) {
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
