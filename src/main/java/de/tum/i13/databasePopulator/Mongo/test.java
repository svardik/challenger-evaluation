package de.tum.i13.databasePopulator.Mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.SQLException;

import static com.mongodb.client.model.Filters.eq;

public class test {
    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        MongoQueriesAllInOneDocument mq2 = new MongoQueriesAllInOneDocument();
        mq2.deleteAllData();
        mq2.populateDB(2, 2, 1);
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");

            Document doc = groupsCollection.find()
                    .first();
            System.out.println(doc.toJson());



        } catch (Exception e){
            System.out.println(e);
        }
    }
}
