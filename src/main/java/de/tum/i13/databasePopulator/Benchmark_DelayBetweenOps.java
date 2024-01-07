package de.tum.i13.databasePopulator;

import de.tum.i13.databasePopulator.Mongo.MongoQueries;
import de.tum.i13.databasePopulator.PSQL.PSQL;
import de.tum.i13.databasePopulator.PSQL.Queries;
import org.bson.types.ObjectId;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class Benchmark_DelayBetweenOps {
    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        List<Long> delaysToTest = List.of(0L,1L,2L,5L,10L,20L,30L);

        for (long l: delaysToTest
             ) {
            System.out.println();
            System.out.println("TESTING DELAY:"+l);
            test(l);
        }
    }



    public static void test(long delay) throws InterruptedException, ClassNotFoundException, SQLException {
        restartBenchmarkResults();
        int noReadOps = 1000;
        int noWriteOps = 1000;


        // populate the DB
        System.out.println("setting up the DB");
        int groups = 100;
        int maxbenchmarks = 100;
        int batchsize = 100;
        String url = "jdbc:postgresql://127.0.0.1:5432/bandency?user=bandency&password=bandency-high-5";
        var connectionPool = new PSQL(url);
        // We do this here to test the DB connection
        connectionPool.getConnection();
        Queries q = new Queries(connectionPool);
        System.out.println("setting up the PSQL");
        q.deleteAllData();
        q.populateDB(groups, maxbenchmarks, batchsize);

        System.out.println("setting up the MONGO");

        MongoQueries mq = new MongoQueries();
        mq.deleteAllData();
        mq.populateDB(groups, maxbenchmarks, batchsize);

        System.out.println("Database setup complete");

        // perform some benchmarks
        // READ

        System.out.println("Benchmarking PSQL READ");
        Random r = new Random(42);
        for (int i = 0; i < noReadOps; i++) {
            int group = r.nextInt(groups + (groups / 10));
            String token = "token-" + group;

            startMeasurement("PSQL-checkIfGroupExists");
            q.checkIfGroupExists(token);
            endMeasurement("PSQL-checkIfGroupExists");
            Thread.sleep(delay);
            startMeasurement("PSQL-getGroupIdFromToken");
            UUID uuid = q.getGroupIdFromToken(token);
            endMeasurement("PSQL-getGroupIdFromToken");
            Thread.sleep(delay);
            startMeasurement("PSQL-getBenchmarksByGroupId");
            q.getBenchmarksByGroupId(uuid,true);
            endMeasurement("PSQL-getBenchmarksByGroupId");
            Thread.sleep(delay);
        }

        System.out.println("Benchmarking Mongo READ");
        Thread.sleep(500);
        r = new Random(42);
        for (int i = 0; i < noReadOps; i++) {
            int group = r.nextInt(groups + (groups / 10));
            String token = "token-" + group;

            startMeasurement("MONGO-checkIfGroupExists");
            mq.checkIfGroupExists(token);
            endMeasurement("MONGO-checkIfGroupExists");
            Thread.sleep(delay);
            startMeasurement("MONGO-getGroupIdFromToken");
            ObjectId id = mq.getGroupIdFromToken(token);
            endMeasurement("MONGO-getGroupIdFromToken");
            Thread.sleep(delay);
            startMeasurement("MONGO-getBenchmarksByGroupId");
            mq.getBenchmarksByGroupId(id,true);
            endMeasurement("MONGO-getBenchmarksByGroupId");
            Thread.sleep(delay);
        }



        // WRITE

        Thread.sleep(delay);

        System.out.println("Benchmarking Mongo WRITE");

        int totalBenchmarks = 10000000;
        r = new Random(42);
        for (int i = groups; i < noWriteOps + groups; i++) {


            ObjectId groupIdMongo = new ObjectId();

            startMeasurement("MONGO-insertGroup");
            mq.insertGroup(groupIdMongo, "group-" + i, "token-" + i);
            endMeasurement("MONGO-insertGroup");
            Thread.sleep(delay);

            long benchmarkId = totalBenchmarks++;

            startMeasurement("MONGO-insertBenchmarkStarted");
            mq.insertBenchmarkStarted(benchmarkId, groupIdMongo, "benchmark-" + totalBenchmarks, batchsize, "Evaluation");
            endMeasurement("MONGO-insertBenchmarkStarted");
            Thread.sleep(delay);
            long a = r.nextLong();

            startMeasurement("MONGO-insertLatency");
            mq.insertLatency(totalBenchmarks, 1, a, a, a, a, a);
            endMeasurement("MONGO-insertLatency");
            Thread.sleep(delay);

            double d = r.nextDouble();

            startMeasurement("MONGO-insertBenchmarkResult");
            mq.insertBenchmarkResult(totalBenchmarks, 42, batchsize, d, d, batchsize, d, d, "dummy data");
            endMeasurement("MONGO-insertBenchmarkResult");
            Thread.sleep(delay);

        }

        System.out.println("Benchmarking PSQL WRITE");
        totalBenchmarks = 10000000;
        r = new Random(42);
        for (int i = groups; i < noWriteOps + groups; i++) {
            // PSQL
            UUID groupId = new UUID(r.nextLong(), r.nextLong());
            startMeasurement("PSQL-insertGroup");
            q.insertGroup(groupId, "group-" + i, "token-" + i);
            endMeasurement("PSQL-insertGroup");
            Thread.sleep(delay);

            long benchmarkId = totalBenchmarks++;
            startMeasurement("PSQL-insertBenchmarkStarted");
            q.insertBenchmarkStarted(benchmarkId, groupId, "benchmark-" + benchmarkId, batchsize, "Evaluation");
            endMeasurement("PSQL-insertBenchmarkStarted");
            Thread.sleep(delay);

            long a = r.nextLong();
            startMeasurement("PSQL-insertLatency");
            q.insertLatency(totalBenchmarks, 1, a, a, a, a, a);
            endMeasurement("PSQL-insertLatency");
            Thread.sleep(delay);

            double d = r.nextDouble();
            startMeasurement("PSQL-insertBenchmarkResult");
            q.insertBenchmarkResult(totalBenchmarks, 42, batchsize, d, d, batchsize, d, d, "dummy data");
            endMeasurement("PSQL-insertBenchmarkResult");
            Thread.sleep(delay);
        }
        printAvg();
    }


    // DELETE


}

