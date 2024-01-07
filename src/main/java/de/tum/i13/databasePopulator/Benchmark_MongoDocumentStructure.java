package de.tum.i13.databasePopulator;

import de.tum.i13.databasePopulator.Mongo.MongoQueries;
import de.tum.i13.databasePopulator.Mongo.MongoQueriesAllInOneDocument;
import de.tum.i13.databasePopulator.PSQL.PSQL;
import de.tum.i13.databasePopulator.PSQL.Queries;
import org.bson.types.ObjectId;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class Benchmark_MongoDocumentStructure {
    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        List<Integer> groupSizesToTest = List.of(1,2,5,10,20,50,100,200,500,1000,1000,2000,3000,5000,10000);
//        List<Integer> groupSizesToTest = List.of();
        for (int g: groupSizesToTest
        ) {
            System.out.println();
            System.out.println("TESTING Groupsize:"+g);
            test(g);
        }
    }



    public static void test(int groups) throws InterruptedException, ClassNotFoundException, SQLException {
        restartBenchmarkResults();
        int noReadOps = 1000;
        int noWriteOps = 1000;

        long delay = 10;
        // populate the DB
        System.out.println("setting up the DB");
        int maxbenchmarks = 100;
        int batchsize = 100;

        System.out.println("setting up the MONGO1 - PSQL copy");

        MongoQueries mq1 = new MongoQueries();
        mq1.deleteAllData();
        mq1.populateDB(groups, maxbenchmarks, batchsize);

        System.out.println("Database setup complete");


        System.out.println("setting up the MONGO2 - everything in 1 document");
        MongoQueriesAllInOneDocument mq2 = new MongoQueriesAllInOneDocument();
        mq2.deleteAllData();
        mq2.populateDB(groups, maxbenchmarks, batchsize);

        System.out.println("Database setup complete");


        // perform some benchmarks
        // READ

        System.out.println("Benchmarking Mongo1 READ");
        Thread.sleep(500);
        Random r = new Random(42);
        for (int i = 0; i < noReadOps; i++) {
            int group = r.nextInt(groups + (groups / 10));
            String token = "token-" + group;

            startMeasurement("MONGO1-checkIfGroupExists");
            mq1.checkIfGroupExists(token);
            endMeasurement("MONGO1-checkIfGroupExists");
            Thread.sleep(delay);

            startMeasurement("MONGO1-getGroupIdFromToken");
            ObjectId id = mq1.getGroupIdFromToken(token);
            endMeasurement("MONGO1-getGroupIdFromToken");
            Thread.sleep(delay);

            startMeasurement("MONGO1-getBenchmarksByGroupId");
            mq1.getBenchmarksByGroupId(id,false);
            endMeasurement("MONGO1-getBenchmarksByGroupId");
            Thread.sleep(delay);

            startMeasurement("MONGO1-getEvaluationResults");
            mq1.getEvaluationResults(false);
            endMeasurement("MONGO1-getEvaluationResults");
            Thread.sleep(delay);
        }

        Thread.sleep(500);
        System.out.println("Benchmarking Mongo2 READ");
        r = new Random(42);
        for (int i = 0; i < noReadOps; i++) {
            int group = r.nextInt(groups + (groups / 10));
            String token = "token-" + group;

            startMeasurement("MONGO2-checkIfGroupExists");
            mq2.checkIfGroupExists(token);
            endMeasurement("MONGO2-checkIfGroupExists");
            Thread.sleep(delay);

            startMeasurement("MONGO2-getGroupIdFromToken");
            ObjectId id = mq2.getGroupIdFromToken(token);
            endMeasurement("MONGO2-getGroupIdFromToken");
            Thread.sleep(delay);

            startMeasurement("MONGO2-getBenchmarksByGroupId");
            mq2.getBenchmarksByGroupId(id,false);
            endMeasurement("MONGO2-getBenchmarksByGroupId");
            Thread.sleep(delay);

            startMeasurement("MONGO2-getEvaluationResults");
            mq2.getEvaluationResults(false);
            endMeasurement("MONGO2-getEvaluationResults");
            Thread.sleep(delay);
        }



        // WRITE

        Thread.sleep(delay);

        System.out.println("Benchmarking Mongo1 WRITE");

        int totalBenchmarks = 10000000;
        r = new Random(42);
        for (int i = groups; i < noWriteOps + groups; i++) {


            ObjectId groupIdMongo = new ObjectId();

            startMeasurement("MONGO1-insertGroup");
            mq1.insertGroup(groupIdMongo, "group-" + i, "token-" + i);
            endMeasurement("MONGO1-insertGroup");
            Thread.sleep(delay);

            long benchmarkId = totalBenchmarks++;

            startMeasurement("MONGO1-insertBenchmarkStarted");
            mq1.insertBenchmarkStarted(benchmarkId, groupIdMongo, "benchmark-" + totalBenchmarks, batchsize, "Evaluation");
            endMeasurement("MONGO1-insertBenchmarkStarted");
            Thread.sleep(delay);
            long a = r.nextLong();

            startMeasurement("MONGO1-insertLatency");
            mq1.insertLatency(totalBenchmarks, 1, a, a, a, a, a);
            endMeasurement("MONGO1-insertLatency");
            Thread.sleep(delay);

            double d = r.nextDouble();

            startMeasurement("MONGO1-insertBenchmarkResult");
            mq1.insertBenchmarkResult(totalBenchmarks, 42, batchsize, d, d, batchsize, d, d, "dummy data");
            endMeasurement("MONGO1-insertBenchmarkResult");
            Thread.sleep(delay);

        }

        System.out.println("Benchmarking Mongo2 WRITE");

        totalBenchmarks = 10000000;
        r = new Random(42);
        for (int i = groups; i < noWriteOps + groups; i++) {


            ObjectId groupIdMongo = new ObjectId();

            startMeasurement("MONGO2-insertGroup");
            mq2.insertGroup(groupIdMongo, "group-" + i, "token-" + i);
            endMeasurement("MONGO2-insertGroup");
            Thread.sleep(delay);

            long benchmarkId = totalBenchmarks++;

            startMeasurement("MONGO2-insertBenchmarkStarted");
            mq2.insertBenchmarkStarted(benchmarkId, groupIdMongo, "benchmark-" + totalBenchmarks, batchsize, "Evaluation");
            endMeasurement("MONGO2-insertBenchmarkStarted");
            Thread.sleep(delay);
            long a = r.nextLong();

            startMeasurement("MONGO2-insertLatency");
            mq2.insertLatency(totalBenchmarks, 1, a, a, a, a, a);
            endMeasurement("MONGO2-insertLatency");
            Thread.sleep(delay);

            double d = r.nextDouble();

            startMeasurement("MONGO2-insertBenchmarkResult");
            mq2.insertBenchmarkResult(totalBenchmarks, 42, batchsize, d, d, batchsize, d, d, "dummy data");
            endMeasurement("MONGO2-insertBenchmarkResult");
            Thread.sleep(delay);

        }

        printAvg();
    }
}
