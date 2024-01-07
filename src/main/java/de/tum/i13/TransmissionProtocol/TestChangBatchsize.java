package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.Query;

import java.util.List;

public class TestChangBatchsize {

    public static void main(String[] args) throws Exception {
        RESTClient restClient = new RESTClient();

        GRPCClient grpcClient = new GRPCClient();
        String token = "token-"+1;


        long resultCalculationDelay = 0;

        long delayBetweenBenchmarks = 100;
        long delayBetweenOps = 0;

        List<String> queriesString = List.of("q1","q2");
        List<Query> queries = List.of(Query.Q1,Query.Q2);



//                restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);
                restClient.changeBatchSize(10,10);
        grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay);


        restClient.changeBatchSize(2,10);
        grpcClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queries, delayBetweenOps, resultCalculationDelay);

//        restClient.runBenchmarkingSequence(token, "Evaluation", "test" + token, queriesString, delayBetweenOps, resultCalculationDelay);

    }
}
