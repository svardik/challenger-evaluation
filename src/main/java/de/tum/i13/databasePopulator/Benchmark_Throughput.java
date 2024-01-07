package de.tum.i13.databasePopulator;

import de.tum.i13.databasePopulator.Mongo.MongoQueries;
import de.tum.i13.databasePopulator.PSQL.PSQL;
import de.tum.i13.databasePopulator.PSQL.Queries;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.SQLException;
import java.util.*;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class Benchmark_Throughput {
    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        List<Integer> groupSizesToTest = List.of(1,2,5,10,20,50,100,200,500,1000);
//        List<Integer> groupSizesToTest = List.of(1,5);

        for (int gs: groupSizesToTest
        ) {
            System.out.println("\n\n");
            System.out.println("Group size:"+gs);
            test(gs);
        }
    }



    public static void test(int groupsize) throws InterruptedException, ClassNotFoundException, SQLException {
        restartBenchmarkResults();
        int noOps = 10000;

        // populate the DB
        System.out.println("setting up the DB");
        int maxbenchmarks = 100;
        int batchsize = 100;
        String url = "jdbc:postgresql://127.0.0.1:5432/bandency?user=bandency&password=bandency-high-5";
        var connectionPool = new PSQL(url);
        // We do this here to test the DB connection
        connectionPool.getConnection();
        Queries q = new Queries(connectionPool);
        System.out.println("setting up the PSQL");
        q.deleteAllData();
        q.populateDB(groupsize, maxbenchmarks, batchsize);

        System.out.println("setting up the MONGO");

        MongoQueries mq = new MongoQueries();
        mq.deleteAllData();
        mq.populateDB(groupsize, maxbenchmarks, batchsize);

        System.out.println("Database setup complete");

        // perform some benchmarks
        // READ

        System.out.println("Collecting object IDs");
        List<UUID> uuids = new ArrayList<>();
        List<ObjectId> objectIds = new ArrayList<>();


        System.out.println("creating List of group tokens to test");
        List<String> tokens = new ArrayList<>();
        Random r = new Random(42);
        for (int i = 0; i < noOps; i++) {
            int group = r.nextInt(groupsize + (groupsize / 10));
            String token = "token-" + group;
            tokens.add(token);

            int groupIdIndex = r.nextInt(groupsize);
            uuids.add(q.groupIds.get(groupIdIndex));
            objectIds.add(mq.groupIds.get(groupIdIndex));
        }

        System.out.println("-----Benchmarking READ Ops-------");


        startMeasurement("PSQL-checkIfGroupExists");
        for (int i = 0; i < noOps; i++) {
            q.checkIfGroupExists(tokens.get(i));
        }
        endMeasurement("PSQL-checkIfGroupExists");
        Thread.sleep(1000);

        startMeasurement("MONGO-checkIfGroupExists");
        for (int i = 0; i < noOps; i++) {
            mq.checkIfGroupExists(tokens.get(i));
        }
        endMeasurement("MONGO-checkIfGroupExists");
        Thread.sleep(1000);

        // getGroupIdFromToken
        startMeasurement("PSQL-getGroupIdFromToken");
        for (int i = 0; i < noOps; i++) {
            q.getGroupIdFromToken(tokens.get(i));
        }
        endMeasurement("PSQL-getGroupIdFromToken");
        Thread.sleep(1000);

        startMeasurement("MONGO-getGroupIdFromToken");
        for (int i = 0; i < noOps; i++) {
            mq.getGroupIdFromToken(tokens.get(i));
        }
        endMeasurement("MONGO-getGroupIdFromToken");
        Thread.sleep(1000);

        // getBenchmarkResultsByGroupId
        startMeasurement("PSQL-getBenchmarksByGroupId");
        for (int i = 0; i < noOps; i++) {
            q.getBenchmarksByGroupId(uuids.get(i), true);
        }
        endMeasurement("PSQL-getBenchmarksByGroupId");
        Thread.sleep(1000);

        startMeasurement("MONGO-getBenchmarksByGroupId");
        for (int i = 0; i < noOps; i++) {
            mq.getBenchmarksByGroupId(objectIds.get(i), true);
        }
        endMeasurement("MONGO-getBenchmarksByGroupId");

        System.out.println("----- WRITE -----");

        List<ObjectId> objectIdsInsert = new ArrayList<>();
        List<UUID> uuidInsert = new ArrayList<>();
        List<String> groupNameInsert = new ArrayList<>();
        List<String> groupTokenInsert = new ArrayList<>();
        List<Long> benchmarkIdInsert = new ArrayList<>();
        List<String> benchmarkNameInsert = new ArrayList<>();
        List<Long> latencyInsert = new ArrayList<>();
        List<Double> benchmarkResultInsert = new ArrayList<>();

        long totalBenchmarks = 10000000;
        r = new Random(42);
        for (int i = groupsize; i < noOps + groupsize; i++) {
            objectIdsInsert.add(new ObjectId());
            uuidInsert.add(new UUID(r.nextLong(), r.nextLong()));

            groupNameInsert.add("group-" + i);
            groupTokenInsert.add("token-" + i);

            benchmarkIdInsert.add(totalBenchmarks++);
            benchmarkNameInsert.add("benchmark-" + totalBenchmarks);

            latencyInsert.add(r.nextLong());
            benchmarkResultInsert.add(r.nextDouble());
        }


        // insert Group
        startMeasurement("PSQL-insertGroup");
        for (int i = 0; i < noOps; i++) {
            q.insertGroup(uuidInsert.get(i), groupNameInsert.get(i), groupTokenInsert.get(i));
        }
        endMeasurement("PSQL-insertGroup");
        Thread.sleep(1000);

        startMeasurement("MONGO-insertGroup");
        for (int i = 0; i < noOps; i++) {
            mq.insertGroup(objectIdsInsert.get(i), groupNameInsert.get(i), groupTokenInsert.get(i));
        }
        endMeasurement("MONGO-insertGroup");
        Thread.sleep(1000);

        // insertBenchmarkStarted
        startMeasurement("PSQL-insertBenchmarkStarted");
        for (int i = 0; i < noOps; i++) {
            q.insertBenchmarkStarted(benchmarkIdInsert.get(i), uuidInsert.get(i), benchmarkNameInsert.get(i), batchsize, "Evaluation");
        }
        endMeasurement("PSQL-insertBenchmarkStarted");
        Thread.sleep(1000);

        startMeasurement("MONGO-insertBenchmarkStarted");
        for (int i = 0; i < noOps; i++) {
            mq.insertBenchmarkStarted(benchmarkIdInsert.get(i), objectIdsInsert.get(i), benchmarkNameInsert.get(i), batchsize, "Evaluation");
        }
        endMeasurement("MONGO-insertBenchmarkStarted");
        Thread.sleep(1000);


        // insertLatency
        startMeasurement("PSQL-insertLatency");
        for (int i = 0; i < noOps; i++) {
            q.insertLatency(benchmarkIdInsert.get(i), 1, latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i));
        }
        endMeasurement("PSQL-insertLatency");
        Thread.sleep(1000);

        startMeasurement("MONGO-insertLatency");
        for (int i = 0; i < noOps; i++) {
            mq.insertLatency(benchmarkIdInsert.get(i), 1, latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i), latencyInsert.get(i));
        }
        endMeasurement("MONGO-insertLatency");
        Thread.sleep(1000);

        //insertBenchmarkResult
        startMeasurement("PSQL-insertBenchmarkResult");
        for (int i = 0; i < noOps; i++) {
            q.insertBenchmarkResult(benchmarkIdInsert.get(i), 42, batchsize, benchmarkResultInsert.get(i), benchmarkResultInsert.get(i), batchsize, benchmarkResultInsert.get(i), benchmarkResultInsert.get(i), "dummy data");
        }
        endMeasurement("PSQL-insertBenchmarkResult");
        Thread.sleep(1000);

        startMeasurement("MONGO-insertBenchmarkResult");
        for (int i = 0; i < noOps; i++) {
            mq.insertBenchmarkResult(benchmarkIdInsert.get(i), 42, batchsize, benchmarkResultInsert.get(i), benchmarkResultInsert.get(i), batchsize, benchmarkResultInsert.get(i), benchmarkResultInsert.get(i), "dummy data");
        }
        endMeasurement("MONGO-insertBenchmarkResult");
        Thread.sleep(1000);
        printAvg();

    }


}
