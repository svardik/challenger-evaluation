package de.tum.i13.databasePopulator;

import de.tum.i13.databasePopulator.Mongo.MongoQueries;
import de.tum.i13.databasePopulator.PSQL.PSQL;
import de.tum.i13.databasePopulator.PSQL.Queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class ComplexReadBenchmarks {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        // populate the DB
        System.out.println("setting up the DB");
        int groups = 100;
        int maxbenchmarks = 100;
        int batchsize = 101;
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

//        System.out.println(mq.getBenchmarksByGroupId(mq.groupIds.get(2),true));
//        System.out.println(mq.getEvaluationResults(true));
//        System.out.println(            q.getEvaluationResults(true));


        int noBenchmarks = 1000;

        Random r = new Random(1337);




        r.setSeed(1337);
//        for (int i = 0; i < noBenchmarks ; i++) {
//            int group = r.nextInt(groups);
//
//            setPrefix("PSQL-");
//
//
//            startMeasurement("getBenchmarksByGroupId-no-string");
//            q.getBenchmarksByGroupId(q.groupIds.get(group),false);
//            endMeasurement("getBenchmarksByGroupId-no-string");
//
//            startMeasurement("getEvaluationResults-no-string");
//            q.getEvaluationResults(false);
//            endMeasurement("getEvaluationResults-no-string");
//            setPrefix("PSQL-");
//
//            setPrefix("MONGO-");
//            startMeasurement("getBenchmarksByGroupId-no-string");
//            mq.getBenchmarksByGroupId(mq.groupIds.get(group),false);
//            endMeasurement("getBenchmarksByGroupId-no-string");
//
//            startMeasurement("getEvaluationResults-no-string");
//            mq.getEvaluationResults(false);
//            endMeasurement("getEvaluationResults-no-string");
//
//        }

        r.setSeed(1337);
        for (int i = 0; i < noBenchmarks ; i++) {
            int group = r.nextInt(groups);

            setPrefix("PSQL-");
            startMeasurement("getBenchmarksByGroupId-string");
            q.getBenchmarksByGroupId(q.groupIds.get(group),true);
            endMeasurement("getBenchmarksByGroupId-string");


            startMeasurement("getEvaluationResults-string");
            q.getEvaluationResults(true);
            endMeasurement("getEvaluationResults-string");


            setPrefix("MONGO-");
            startMeasurement("getBenchmarksByGroupId-string");
            mq.getBenchmarksByGroupId(mq.groupIds.get(group),true);
            endMeasurement("getBenchmarksByGroupId-string");

            startMeasurement("getEvaluationResults-string");
            mq.getEvaluationResults(true);
            endMeasurement("getEvaluationResults-string");
        }

        printAvg();

    }
}
