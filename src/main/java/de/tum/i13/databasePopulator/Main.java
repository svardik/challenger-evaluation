package de.tum.i13.databasePopulator;

import de.tum.i13.databasePopulator.Mongo.MongoQueries;
import de.tum.i13.databasePopulator.PSQL.PSQL;
import de.tum.i13.databasePopulator.PSQL.Queries;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        String url = "jdbc:postgresql://127.0.0.1:5432/bandency?user=bandency&password=bandency-high-5";
        var connectionPool = new PSQL(url);
        // We do this here to test the DB connection
        connectionPool.getConnection();
        Queries q = new Queries(connectionPool);
        q.deleteAllData();
        q.populateDB(100,100,100);


        MongoQueries mq = new MongoQueries();
        mq.deleteAllData();
        mq.populateDB(100,100,100);
    }
}
