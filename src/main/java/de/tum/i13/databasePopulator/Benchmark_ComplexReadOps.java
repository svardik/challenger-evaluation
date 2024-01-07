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

public class Benchmark_ComplexReadOps {
    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        List<Integer> groupSizesToTest = List.of(1,2,5,10,20,50,100,200,500,1000);
//        List<Integer> groupSizesToTest = List.of(1,5);

        for (int gs: groupSizesToTest
        ) {
            System.out.println("Group size:"+gs);
            test(gs);
        }
    }



    public static void test(int groupsize) throws InterruptedException, ClassNotFoundException, SQLException {
        restartBenchmarkResults();
        int noReadOps = 1000;
        long delay = 10;

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

//        System.out.println(q.getEvaluationResults(true));
        System.out.println("Benchmarking PSQL");
        for (int i = 0; i < noReadOps; i++) {
            startMeasurement("PSQL-getEvaluationResults");
            q.getEvaluationResults(true);
            endMeasurement("PSQL-getEvaluationResults");
            Thread.sleep(delay);
        }

//        System.out.println(mq.getEvaluationResults(true));
        System.out.println("Benchmarking Mongo");
        Thread.sleep(500);
        for (int i = 0; i < noReadOps; i++) {
            startMeasurement("MONGO-getEvaluationResults");
            mq.getEvaluationResults(true);
            endMeasurement("MONGO-getEvaluationResults");
            Thread.sleep(delay);
        }

        printAvg();
    }


}
