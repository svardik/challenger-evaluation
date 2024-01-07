package de.tum.i13;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking;
import de.tum.i13.challenge.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class Main {
    // 0 = no calculation
    // 1 = 10ms calculation
    // 2 = 100ms calculation
    static int mode = 0;
    public static void main(String[] args) {
        setPrefix("GRPC-");
        System.out.println("Starting grpc benchmark with "+args[0]);
        ManagedChannel channel = ManagedChannelBuilder
                //.forAddress("challenge.msrg.in.tum.de", 5023)
                .forAddress("127.0.0.1", 5023) //in case it is used internally
                .usePlaintext()
                .build();


        var challengeClient = ChallengerGrpc.newBlockingStub(channel) //for demo, we show the blocking stub
                .withMaxInboundMessageSize(100 * 1024 * 1024)
                .withMaxOutboundMessageSize(100 * 1024 * 1024);

        BenchmarkConfiguration bc = BenchmarkConfiguration.newBuilder()
                .setBenchmarkName("Testrun " + new Date().toString())
                .addQueries(Query.Q1)
                .addQueries(Query.Q2)
//                .setToken("vctmqcboxqbnbwulepbtcypwnwgmwmyg") //mongoDB
                .setToken(args[0]) //PSQL
                .setBenchmarkType("evaluation") //Benchmark Type for evaluation
                //.setBenchmarkType("test") //Benchmark Type for testing
                .build();

        startMeasurement("createBenchmark");
        //Create a new Benchmark
        Benchmark newBenchmark = challengeClient.createNewBenchmark(bc);
        endMeasurement("createBenchmark");

        startMeasurement("startBenchmark");
        //Start the benchmark
        challengeClient.startBenchmark(newBenchmark);
        endMeasurement("startBenchmark");

        //Process the events
        int cnt = 0;
        while(true) {
            startMeasurement("getNextBatch");
            Batch batch = challengeClient.nextBatch(newBenchmark);

            if (batch.getLast()) { //Stop when we get the last batch
                endMeasurement("getNextBatch","getNextBatch-last");
                //System.out.println("Received lastbatch, finished!");
                break;
            } else {
                endMeasurement("getNextBatch");
            }

            //process the batch of events we have
            var q1Results = calculateOutliers(batch);

            ResultQ1 q1Result = ResultQ1.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllEntries(q1Results)
                    .build();

            startMeasurement("q1Result");
            //return the result of Q1
            challengeClient.resultQ1(q1Result);
            endMeasurement("q1Result");

            var centroidsOut = calculateCentroidsOut(batch);
            var centroidsIn = calculateCentroidsIn(batch);

            ResultQ2 q2Result = ResultQ2.newBuilder()
                    .setBenchmarkId(newBenchmark.getId()) //set the benchmark id
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllCentroidsIn(centroidsIn)
                    .addAllCentroidsOut(centroidsOut)
                    .build();

            startMeasurement("q2Result");
            challengeClient.resultQ2(q2Result);
            endMeasurement("q2Result");
//            System.out.println("Processed batch #" + cnt);
            ++cnt;

            if(cnt > 100) { //for testing you can stop early, in an evaluation run, run until getLast() is True.
                break;
            }
        }

        startMeasurement("endBenchmark");
        challengeClient.endBenchmark(newBenchmark);
        endMeasurement("endBenchmark");
       // System.out.println("ended Benchmark");

//        PerformanceBenchmarking.printAll();
        channel.shutdown();
    }

    private static List<Outliers> calculateOutliers(Batch batch) {

        Outliers outlier1 = Outliers.newBuilder()
                .setModel("a")
                .addAllIntervals(List.of("a1","a2","a3"))
                .build();
        Outliers outlier2 = Outliers.newBuilder()
                                .setModel("b")
                .addAllIntervals(List.of("b1","b2","b3"))
                .build();
        Outliers outlier3 = Outliers.newBuilder()
                .setModel("c")
                .addAllIntervals(List.of("c1","c2","c3"))
                .build();

        return List.of(outlier1,outlier2,outlier3);
    }

    private static List<Long> calculateCentroidsOut(Batch batch){
        return List.of(1L, 2L, 3L, 4L);
    }

    private static List<Long> calculateCentroidsIn(Batch batch){
        return List.of(1L, 2L, 3L, 4L);
    }
}
