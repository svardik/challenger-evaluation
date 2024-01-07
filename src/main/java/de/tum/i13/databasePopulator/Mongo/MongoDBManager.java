package de.tum.i13.databasePopulator.Mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoDBManager {
    private static MongoClient mongoClient;
    private static final String DATABASE_NAME = "bandency"; // Change this to your DB name

    private MongoDBManager() {
    }

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
            mongoClient = new MongoClient(uri);
        }
        return mongoClient;
    }

    public static synchronized MongoDatabase getDatabase() {
        return getMongoClient().getDatabase(DATABASE_NAME);
    }

    public static synchronized MongoDatabase getDatabase(String name) {
        return getMongoClient().getDatabase(name);
    }
}