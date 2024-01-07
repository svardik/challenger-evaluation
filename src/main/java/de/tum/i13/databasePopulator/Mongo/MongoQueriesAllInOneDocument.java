package de.tum.i13.databasePopulator.Mongo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoQueriesAllInOneDocument {


    public List<ObjectId> groupIds = new ArrayList<>();
    public void populateDB(int groups, int maxBenchmarks,int batchSize) throws SQLException, ClassNotFoundException, InterruptedException {
        Random r = new Random(1337);
        long totalBenchmarks = 0;
        for (int i = 0; i < groups; i++) {
            // ensure randomness consistency
            r.nextLong();
            r.nextLong();
            ObjectId groupId = new ObjectId();
            groupIds.add(groupId);
            insertGroup(groupId,"group-"+i,"token-"+i);

            for (int j = 0; j < r.nextInt(maxBenchmarks) ; j++) {
                totalBenchmarks++;
                insertBenchmarkStarted(totalBenchmarks,groupId,"benchmark-"+totalBenchmarks,batchSize,"Evaluation");

                for (int k = 0; k < batchSize; k++) {
                    insertLatency(totalBenchmarks,k,r.nextLong(),r.nextLong(),r.nextLong(),r.nextLong(),r.nextLong());
                }
                insertBenchmarkResult(totalBenchmarks,42,batchSize,r.nextDouble(),r.nextDouble(),batchSize,r.nextDouble(),r.nextDouble(),"dummy data");
            }
        }
    }

    public void deleteAllData() throws InterruptedException, ClassNotFoundException, SQLException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        db.getCollection("groups").drop();
    }

    // ------ write --------

    public void insertGroup(ObjectId id, String name, String token) throws SQLException, ClassNotFoundException, InterruptedException{
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> benchmarksCollection = db.getCollection("groups");
            Document benchmarkDocument = new Document()
                    .append("_id", id)
                    .append("groupname", name)
                    .append("password", "c8fb3e8874fe9705857e827b0532bfe6")
                    .append("groupemail", "svaral.matej@gmail.com")
                    .append("groupnick", name)
                    .append("groupapikey", token)
                    .append("benchmarks", new org.bson.BsonArray());

            benchmarksCollection.insertOne(benchmarkDocument);
        } finally {
            // Handle exceptions and close resources as needed

        }
    }

    public void insertBenchmarkStarted(long benchmarkId, ObjectId groupId, String benchmarkName, int batchSize, String bt) throws SQLException, ClassNotFoundException, InterruptedException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");

            // Update operation to add the benchmark details to the array of benchmarks for the group
            Document benchmarkDetails = new Document()
                    .append("_id", benchmarkId)
                    .append("timestamp", new Timestamp(System.currentTimeMillis()))
                    .append("benchmark_name", benchmarkName)
                    .append("benchmark_type", bt)
                    .append("batchsize", batchSize)
                    .append("latencies", new org.bson.BsonArray());

            groupsCollection.updateOne(
                    eq("_id",groupId),
                    Updates.addToSet("benchmarks", benchmarkDetails)
            );

        } finally {
            // Handle exceptions and close resources as needed

        }
    }


    public void insertLatency(long benchmarkId, long batchId, long startTime, long q1resultTime, long q1Latency, long q2resultTime, long q2Latency) throws SQLException, ClassNotFoundException, InterruptedException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");

        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");

            Document latencyDocument = new Document()
                    .append("batch_id", batchId)
                    .append("starttime", startTime)
                    .append("q1resulttime", q1resultTime)
                    .append("q1latency", q1Latency)
                    .append("q2resulttime", q2resultTime)
                    .append("q2latency", q2Latency);

            groupsCollection.updateOne(
                    Filters.and(
                            Filters.eq("benchmarks._id", benchmarkId)

                    ),
                    Updates.push("benchmarks.$.latencies", latencyDocument)
            );

        } finally {
            // Handle exceptions and close resources as needed

        }
    }


    public void insertBenchmarkResult(long benchmarkId, double seconds, long q1count, double q1Throughput, double q190Percentile, long q2count, double q2Throughput, double q290Percentile, String summary) throws SQLException, ClassNotFoundException, InterruptedException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
                MongoCollection<Document> groupsCollection = db.getCollection("groups");

                Document resultDocument = new Document()
                    .append("duration_sec", seconds)
                    .append("q1_count", q1count)
                    .append("q1_throughput", q1Throughput)
                    .append("q1_90percentile", q190Percentile)
                    .append("q2_count", q2count)
                    .append("q2_throughput", q2Throughput)
                    .append("q2_90percentile", q290Percentile)
                    .append("summary", summary);

            groupsCollection.updateOne(
                    Filters.and(
                            Filters.eq("benchmarks._id", benchmarkId)
                    ),
                    Updates.push("benchmarks.$.result", resultDocument)
            );
        } finally {

        }
    }

    // ------- read --------

    public boolean checkIfGroupExists(String token) {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");
            Document doc = groupsCollection.find(eq("groupapikey", token))
                    .first();
//            System.out.println("group exists"+(doc !=null));
            return doc !=null;
        } finally {
            // Handle exceptions and close resources as needed
        }
    }

    public static UUID getUUIDFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }


    public ObjectId getGroupIdFromToken(String token) {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");

            Document doc = groupsCollection.find(eq("groupapikey", token))
                    .first();
            if (doc!=null) {

                ObjectId id = doc.get("_id", ObjectId.class);
//                System.out.println(id.toByteArray().length);
//                System.out.println(Arrays.toString(id.toByteArray()));
//                System.out.println(Arrays.toString(Arrays.copyOf(id.toByteArray(), 16)));
//                return getUUIDFromByteArray(Arrays.copyOf(id.toByteArray(), 16));
                return id;
            }
            return null;
        } finally {
            // Handle exceptions and close resources as needed
        }
    }


    public String getBenchmarksByGroupId(ObjectId groupId,boolean buildString) throws InterruptedException, ClassNotFoundException, SQLException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");



            // Create a filter to find documents matching the groupId
            Document filter = new Document("_id", groupId);
            Document groupDoc = groupsCollection.find(filter).first();

            if (groupDoc==null){
                return null;
            }

            Object benchmarks = groupDoc.get("benchmarks");

//            System.out.println("Group with ID " + groupId + ": " + groupDoc.toJson());

            // Find documents in the collection that match the filter


            if (benchmarks instanceof List) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                // Iterate through the list of benchmarks
                for (Object benchmarkObj : (List<?>) benchmarks) {
                    if (benchmarkObj instanceof Document benchmark) {
                        if (buildString){
                            sb.append(benchmark.toJson());
                            sb.append(",\n");
                        }
                    }
                }
                sb.append("]");


                return sb.toString();
        }
        return null;
        }finally {

        }
    }



    public String getEvaluationResults(boolean buildString) throws InterruptedException, ClassNotFoundException, SQLException {
        MongoDatabase db = MongoDBManager.getDatabase("bandency2");
        try {
            MongoCollection<Document> groupsCollection = db.getCollection("groups");

            AggregateIterable<Document> result = groupsCollection.aggregate(Arrays.asList(
                    Aggregates.unwind("$benchmarks"),
                    Aggregates.match(
                            Filters.eq("benchmarks.benchmark_type", "Evaluation")),

                    Aggregates.match(Filters.and(
                            Filters.gte("benchmarks.result.q1_count", 100),
                            Filters.gte("benchmarks.result.q2_count", 100)
                    )),
                    Aggregates.sort(
                            Sorts.orderBy(
//                                    Sorts.ascending("g._id"),
                                    Sorts.descending("benchmarks.timestamp")
                            )
                    ),

                    Aggregates.group("$_id",
                            Accumulators.first("groupname", "$groupname"),
                            Accumulators.first("q1_90percentile", "$benchmarks.result.q1_90percentile"),
                            Accumulators.first("q1_throughput", "$benchmarks.result.q1_throughput"),
                            Accumulators.first("q2_90percentile", "$benchmarks.result.q2_90percentile"),
                            Accumulators.first("q2_throughput", "$benchmarks.result.q2_throughput"),
                            Accumulators.first("avg90percentile", new Document("$avg", new Document("$concatArrays",Arrays.asList("$benchmarks.result.q1_90percentile", "$benchmarks.result.q2_90percentile")))),
                            Accumulators.first("avgthroughput", new Document("$avg", new Document("$concatArrays",Arrays.asList("$benchmarks.result.q1_throughput", "$benchmarks.result.q2_throughput"))))
                    )
            ));


            // Find documents in the collection that match the filter
            StringBuilder sb = new StringBuilder();;

            sb.append("[");

            // Iterate through the aggregation result
            for (Document doc : result) {
                if (buildString){
                    sb.append(doc.toJson());
                    sb.append(",\n");
                }

            }
            sb.append("]");
            return sb.toString();
        }finally {

        }
    }

}
