package de.tum.i13.TransmissionProtocol;

import de.tum.i13.challenge.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class GRPCClient {
    // 0 = no calculation
    // 1 = 10ms calculation
    // 2 = 100ms calculation


    ChallengerGrpc.ChallengerBlockingStub challengeClient;
    ManagedChannel channel;
    static Random r = new Random(42);

    public GRPCClient() {
        channel = ManagedChannelBuilder
                //.forAddress("challenge.msrg.in.tum.de", 5023)
                .forAddress("127.0.0.1", 5023) //in case it is used internally
                .usePlaintext()
                .build();


        challengeClient = ChallengerGrpc.newBlockingStub(channel) //for demo, we show the blocking stub
                .withMaxInboundMessageSize(100 * 1024 * 1024)
                .withMaxOutboundMessageSize(100 * 1024 * 1024);

    }

    public Benchmark createBenchmark(String token, String benchmarkType, String benchmarkName, List<Query> queries) {
        BenchmarkConfiguration bc = BenchmarkConfiguration.newBuilder()
                .setBenchmarkName(benchmarkName)
                .addAllQueries(queries)
//                .setToken("vctmqcboxqbnbwulepbtcypwnwgmwmyg") //mongoDB
                .setToken(token) //PSQL
                .setBenchmarkType(benchmarkType) //Benchmark Type for evaluation
                //.setBenchmarkType("test") //Benchmark Type for testing
                .build();
        return challengeClient.createNewBenchmark(bc);
    }

    public void startBenchmark(Benchmark benchmark) {
        challengeClient.startBenchmark(benchmark);
    }

    public Batch getNextBatch(Benchmark benchmark) {
        return challengeClient.nextBatch(benchmark);
    }

    public void postBatchResultQ1(ResultQ1 resultQ1) {
        challengeClient.resultQ1(resultQ1);
    }

    public void postBatchResultQ2(ResultQ2 resultQ2) {
        challengeClient.resultQ2(resultQ2);
    }

    public void endBenchmark(Benchmark benchmark) {
        challengeClient.endBenchmark(benchmark);
    }


    public void runBenchmarkingSequence(String token, String benchmarkType, String benchmarkName, List<Query> queries, long delayBetweenOps, long batchCalculationDelay) throws InterruptedException {
        runBenchmarkingSequence(token, benchmarkType, benchmarkName, queries, delayBetweenOps, batchCalculationDelay, 0);
    }

    public void runBenchmarkingSequence(String token, String benchmarkType, String benchmarkName, List<Query> queries, long delayBetweenOps, long batchCalculationDelay, int q1Size) throws InterruptedException {
        setPrefix("GRPC-");

        startMeasurement("createBenchmark");
        //Create a new Benchmark
        Benchmark newBenchmark = createBenchmark(token, benchmarkType, benchmarkName, queries);
        endMeasurement("createBenchmark");

        Thread.sleep(delayBetweenOps);

        startMeasurement("startBenchmark");
        //Start the benchmark
        startBenchmark(newBenchmark);
        endMeasurement("startBenchmark");

        Thread.sleep(delayBetweenOps);
        //Process the events
        int cnt = 0;
        while (true) {


            startMeasurement("getNextBatch");
            Batch batch = getNextBatch(newBenchmark);
//            System.out.println(batch);
            if (batch.getLast()) { //Stop when we get the last batch
                endMeasurement("getNextBatch", "getNextBatch-last");
                //System.out.println("Received lastbatch, finished!");
                break;
            } else {
                endMeasurement("getNextBatch");
            }
            Thread.sleep(delayBetweenOps);


//            startMeasurement("generateQ1");
            //process the batch of events we have
            var q1Results = calculateOutliers(batch, q1Size);
//            endMeasurement("generateQ1");
            var centroidsOut = calculateCentroidsOut(batch);
            var centroidsIn = calculateCentroidsIn(batch);


            ResultQ1 q1Result = ResultQ1.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllEntries(q1Results)
                    .build();
            ResultQ2 q2Result = ResultQ2.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllCentroidsIn(centroidsIn)
                    .addAllCentroidsOut(centroidsOut)
                    .build();

            Thread.sleep(batchCalculationDelay);

            startMeasurement("q1Result");
            //return the result of Q1
            postBatchResultQ1(q1Result);
            endMeasurement("q1Result");

            Thread.sleep(delayBetweenOps);

            startMeasurement("q2Result");
            postBatchResultQ2(q2Result);
            endMeasurement("q2Result");
            Thread.sleep(delayBetweenOps);
            ++cnt;
        }

        startMeasurement("endBenchmark");
        endBenchmark(newBenchmark);
        endMeasurement("endBenchmark");
        Thread.sleep(delayBetweenOps);
    }


    public void runBenchmarkingSequenceDummy(String token, String benchmarkType, String benchmarkName, List<Query> queries, long delayBetweenOps, long batchCalculationDelay, int q1Size) throws InterruptedException {
        setPrefix("GRPC-");

        startMeasurement("createBenchmark");
        //Create a new Benchmark
        Benchmark newBenchmark = createBenchmark(token, benchmarkType, benchmarkName, queries);
        endMeasurement("createBenchmark");

        Thread.sleep(delayBetweenOps);

        startMeasurement("startBenchmark");
        //Start the benchmark
        startBenchmark(newBenchmark);
        endMeasurement("startBenchmark");

        Thread.sleep(delayBetweenOps);
        //Process the events
        int cnt = 0;
        for (int i = 0; i < 101; i++) {
            startMeasurement("getNextBatch");
            Batch batch = getNextBatch(newBenchmark);
//            System.out.println(batch);
            if (i==100) { //Stop when we get the last batch
                endMeasurement("getNextBatch", "getNextBatch-last");
                //System.out.println("Received lastbatch, finished!");
                break;
            } else {
                endMeasurement("getNextBatch");
            }
            Thread.sleep(delayBetweenOps);

            ResultQ1 q1Result = ResultQ1.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(1) //set the sequence number
                    .addAllEntries(List.of())
                    .build();
            ResultQ2 q2Result = ResultQ2.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(1) //set the sequence number
                    .addAllCentroidsIn(List.of())
                    .addAllCentroidsOut(List.of())
                    .build();

            Thread.sleep(batchCalculationDelay);

            startMeasurement("q1Result");
            //return the result of Q1
            postBatchResultQ1(q1Result);
            endMeasurement("q1Result");

            Thread.sleep(delayBetweenOps);

            startMeasurement("q2Result");
            postBatchResultQ2(q2Result);
            endMeasurement("q2Result");
            Thread.sleep(delayBetweenOps);
            ++cnt;
        }

        startMeasurement("endBenchmark");
        endBenchmark(newBenchmark);
        endMeasurement("endBenchmark");
        Thread.sleep(delayBetweenOps);
    }

    public void close() {
        channel.shutdown();
    }


    private static List<Outliers> calculateOutliers(Batch batch) {

        Outliers outlier1 = Outliers.newBuilder()
                .setModel("a")
                .addAllIntervals(List.of("a1", "a2", "a3"))
                .build();
        Outliers outlier2 = Outliers.newBuilder()
                .setModel("b")
                .addAllIntervals(List.of("b1", "b2", "b3"))
                .build();
        Outliers outlier3 = Outliers.newBuilder()
                .setModel("c")
                .addAllIntervals(List.of("c1", "c2", "c3"))
                .build();

        return List.of(outlier1, outlier2, outlier3);
    }


    // 0=default dummmy value
    private static List<Outliers> calculateOutliers(Batch batch, int size) {
        if (size == 0) {
            return calculateOutliers(batch);
        }
        List<Outliers> list = new ArrayList<>();


        for (int i = 0; i < size; i++) {
            Outliers outlier = Outliers.newBuilder()
                    .setModel(String.valueOf(r.nextInt()))
                    .addAllIntervals(List.of(String.valueOf(r.nextInt()), String.valueOf(r.nextInt()), String.valueOf(r.nextInt())))
                    .build();
            list.add(outlier);
        }

        return list;
    }

    private static List<Long> calculateCentroidsOut(Batch batch) {
        return List.of(1L, 2L, 3L, 4L);
    }

    private static List<Long> calculateCentroidsIn(Batch batch) {
        return List.of(1L, 2L, 3L, 4L);
    }

}
