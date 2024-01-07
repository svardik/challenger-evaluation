package de.tum.i13.TransmissionProtocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tum.i13.challenge.Outliers;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.endMeasurement;

public class RESTClient {
    private static final String BASE_URL = "http://localhost:5024/benchmark/";

    private final Client client;
    private final WebTarget target;

    static Random r = new Random(42);

    public RESTClient() {
        this.client = ClientBuilder.newClient();
        this.target = client.target(BASE_URL);
    }

    public Response createBenchmark(String token, String benchmarkType, String benchmarkName, List<String> queries) {
        WebTarget resource = target.path("/create-benchmark")
                .queryParam("token", token)
                .queryParam("benchmarkType", benchmarkType)
                .queryParam("benchmarkName", benchmarkName)
                .queryParam("queries", String.join(",", queries));

        return resource.request(MediaType.APPLICATION_JSON)
                .get();
    }


    public Response startBenchmark(String benchmarkId) {
        WebTarget resource = target.path(benchmarkId).path("/start-benchmark");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
    }

    public Response getNextBatch(String benchmarkId) {
        WebTarget resource = target.path(benchmarkId).path("/next-batch");
        return resource.request(MediaType.APPLICATION_JSON)
                .get();
    }

    public Response postBatchResultQ1(String benchmarkId, String result) {
        WebTarget resource = target.path(benchmarkId).path("/result-q1");
        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(result, MediaType.APPLICATION_JSON));
    }

    public Response postBatchResultQ2(String benchmarkId, String result) {
        WebTarget resource = target.path(benchmarkId).path("/result-q2");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(result, MediaType.APPLICATION_JSON));
    }

    public Response endBenchmark(String benchmarkId) {
        WebTarget resource = target.path(benchmarkId).path("/end-benchmark");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
    }


    public Response changeBatchSize(int batchsize, int maxbatches) {
        WebTarget resource = target.path("/change-batchsize")
                .queryParam("batchSize", batchsize)
                .queryParam("maxBatches", maxbatches);

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
    }

    public void close() {
        client.close();
    }


    public void runBenchmarkingSequence(String token, String benchmarkType, String benchmarkName, List<String> queries, long delayBetweenOps, long batchCalculationDelay) throws Exception {
        runBenchmarkingSequence(token, benchmarkType, benchmarkName, queries, delayBetweenOps, batchCalculationDelay, 0);
    }

    public void runBenchmarkingSequence(String token, String benchmarkType, String benchmarkName, List<String> queries, long delayBetweenOps, long batchCalculationDelay, int q1Size) throws Exception {
        setPrefix("REST-");
//        System.out.println("Starting REST benchmark with "+token);


        // Example usage:
        startMeasurement("createBenchmark");
        Response createResponse = createBenchmark(token, benchmarkType, benchmarkName, queries);

//        System.out.println(createResponse);
        String json = createResponse.readEntity(String.class);
//        System.out.println(json);

        Long benchmarkId = -1L;
        if (createResponse.getStatus() == 200) {
            benchmarkId = parseJsonToLong(json, "benchmark_id").asLong();
        }
        endMeasurement("createBenchmark");

        Thread.sleep(delayBetweenOps);
//        System.out.println(benchmarkId);

        startMeasurement("startBenchmark");
        Response startResponse = startBenchmark(benchmarkId.toString());


//        System.out.println(startResponse);
        if (startResponse.getStatus() == 201) {
//            System.out.println("benchmark started");
        } else {
            System.out.println("error, benchmark not started");
        }
        endMeasurement("startBenchmark");
        Thread.sleep(delayBetweenOps);

        boolean isLast = false;
        while (true) {

//            System.out.println("getting next batch");
            startMeasurement("getNextBatch");
            Response batchResponse = getNextBatch(benchmarkId.toString());

//            System.out.println(batchResponse);
            if (batchResponse.getStatus() == 200) {
                String batchJson = batchResponse.readEntity(String.class);

                isLast = parseJsonToLong(batchJson, "last").asBoolean();
                if (isLast) {
//                    System.out.println("Received lastbatch, finished!");
                    endMeasurement("getNextBatch", "getNextBatch-last");
                    break;
                } else {
                    endMeasurement("getNextBatch");
                }

                Thread.sleep(delayBetweenOps);


                int seqId = parseJsonToLong(batchJson, "seq_id").asInt();

//                System.out.println("next batch receivedL: " + seqId + " last: " + isLast);
//                System.out.println(batchJson);
//                System.out.println("submitting the result");
//                startMeasurement("generateQ1");
                String dummyQ1String = generateDummyQ1String(benchmarkId.toString(), seqId, q1Size);
//                endMeasurement("generateQ1");
                String dummyQ2String = generateDummyQ2String(benchmarkId.toString(), seqId);

                Thread.sleep(batchCalculationDelay);

                startMeasurement("q1Result");
                Response q1Response = postBatchResultQ1(benchmarkId.toString(), dummyQ1String);
                endMeasurement("q1Result");
                Thread.sleep(delayBetweenOps);
//                System.out.println(q1Response);

                startMeasurement("q2Result");
                Response q2Response = postBatchResultQ2(benchmarkId.toString(), dummyQ2String);
                endMeasurement("q2Result");
//                System.out.println(q2Response);
                Thread.sleep(delayBetweenOps);

            } else {
//                System.out.println("nextbatch error");
                break;
            }
        }

        startMeasurement("endBenchmark");
        Response endResponse = endBenchmark(benchmarkId.toString());
        endMeasurement("endBenchmark");
//        System.out.println(endResponse);
        Thread.sleep(delayBetweenOps);

    }

    public void runBenchmarkingSequenceDummy(String token, String benchmarkType, String benchmarkName, List<String> queries, long delayBetweenOps, long batchCalculationDelay, int q1Size) throws Exception {
        setPrefix("REST-");
//        System.out.println("Starting REST benchmark with "+token);


        // Example usage:
        startMeasurement("createBenchmark");
        Response createResponse = createBenchmark(token, benchmarkType, benchmarkName, queries);

//        System.out.println(createResponse);
        String json = createResponse.readEntity(String.class);
//        System.out.println(json);

        Long benchmarkId = -1L;
        if (createResponse.getStatus() == 200) {
            benchmarkId = parseJsonToLong(json, "benchmark_id").asLong();
        }
        endMeasurement("createBenchmark");

        Thread.sleep(delayBetweenOps);
//        System.out.println(benchmarkId);

        startMeasurement("startBenchmark");
        Response startResponse = startBenchmark(benchmarkId.toString());


//        System.out.println(startResponse);
        if (startResponse.getStatus() == 201) {
//            System.out.println("benchmark started");
        } else {
            System.out.println("error, benchmark not started");
        }
        endMeasurement("startBenchmark");
        Thread.sleep(delayBetweenOps);

        boolean isLast = false;
        for (int i = 0; i < 101; i++) {

//            System.out.println("getting next batch");
            startMeasurement("getNextBatch");
            Response batchResponse = getNextBatch(benchmarkId.toString());

//            System.out.println(batchResponse);
            if (batchResponse.getStatus() == 200) {
                String batchJson = batchResponse.readEntity(String.class);

                  if (i==100) {
//                    System.out.println("Received lastbatch, finished!");
                    endMeasurement("getNextBatch", "getNextBatch-last");
                    break;
                } else {
                    endMeasurement("getNextBatch");
                }

                Thread.sleep(delayBetweenOps);


                int seqId = 0;
//                System.out.println("next batch receivedL: " + seqId + " last: " + isLast);
//                System.out.println(batchJson);
//                System.out.println("submitting the result");
//                startMeasurement("generateQ1");
                String dummyQ1String = generateDummyQ1String(benchmarkId.toString(), seqId, q1Size);
//                endMeasurement("generateQ1");
                String dummyQ2String = generateDummyQ2String(benchmarkId.toString(), seqId);

                Thread.sleep(batchCalculationDelay);

                startMeasurement("q1Result");
                Response q1Response = postBatchResultQ1(benchmarkId.toString(), dummyQ1String);
                endMeasurement("q1Result");
                Thread.sleep(delayBetweenOps);
//                System.out.println(q1Response);

                startMeasurement("q2Result");
                Response q2Response = postBatchResultQ2(benchmarkId.toString(), dummyQ2String);
                endMeasurement("q2Result");
//                System.out.println(q2Response);
                Thread.sleep(delayBetweenOps);

            } else {
//                System.out.println("nextbatch error");
                break;
            }
        }

        startMeasurement("endBenchmark");
        Response endResponse = endBenchmark(benchmarkId.toString());
        endMeasurement("endBenchmark");
//        System.out.println(endResponse);
        Thread.sleep(delayBetweenOps);

    }


    private static JsonNode parseJsonToLong(String jsonString, String propertyName) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);

        // Assuming "benchmark_id" is present in the JSON
        JsonNode benchmarkIdNode = jsonNode.get(propertyName);

        if (benchmarkIdNode == null) {
            throw new IllegalArgumentException("Invalid or missing 'benchmark_id' in JSON");
        }

        return benchmarkIdNode;
    }

    private static String generateDummyQ2String(String benchmarkId, long batchSeqId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a Java object representing the structure
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("benchmark_id", benchmarkId);
        jsonNode.put("batch_seq_id", batchSeqId);

        List<Long> centroidsOut = List.of(1L, 2L, 3L, 4L);
        ArrayNode centroidsOutNode = objectMapper.createArrayNode();
        centroidsOut.forEach(centroidsOutNode::add);
        jsonNode.set("centroids_out", centroidsOutNode);

        List<Long> centroidsIn = List.of(1L, 2L, 3L, 4L);
        ArrayNode centroidsInNode = objectMapper.createArrayNode();
        centroidsIn.forEach(centroidsInNode::add);
        jsonNode.set("centroids_in", centroidsInNode);

        // Convert the object to a JSON string
        return objectMapper.writeValueAsString(jsonNode);
    }

    private static String generateDummyQ1String(String benchmarkId, long batchSeqId, int size) throws Exception {
        if (size == 0) {
            return generateDummyQ1String(benchmarkId, batchSeqId);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a Java object representing the structure
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("benchmark_id", benchmarkId);
        jsonNode.put("batch_seq_id", batchSeqId);

        List<ObjectNode> outlierList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ObjectNode outlier1 = objectMapper.createObjectNode();
            outlier1.put("model", String.valueOf(r.nextInt()));
            ArrayNode outlier1arr = objectMapper.createArrayNode();
            outlier1arr.add(String.valueOf(r.nextInt()));
            outlier1arr.add(String.valueOf(r.nextInt()));
            outlier1arr.add(String.valueOf(r.nextInt()));
            outlier1.set("intervals", outlier1arr);
            outlierList.add(outlier1);
        }


        ArrayNode entriesNode = objectMapper.createArrayNode();
        entriesNode.addAll(outlierList);
        jsonNode.set("entries", entriesNode);


        // Convert the object to a JSON string
        return objectMapper.writeValueAsString(jsonNode);
    }

    private static String generateDummyQ1String(String benchmarkId, long batchSeqId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a Java object representing the structure
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("benchmark_id", benchmarkId);
        jsonNode.put("batch_seq_id", batchSeqId);

        ObjectNode outlier1 = objectMapper.createObjectNode();
        outlier1.put("model", "a");
        ArrayNode outlier1arr = objectMapper.createArrayNode();
        outlier1arr.add("a1");
        outlier1arr.add("a2");
        outlier1arr.add("a3");
        outlier1.set("intervals", outlier1arr);

        ObjectNode outlier2 = objectMapper.createObjectNode();
        outlier2.put("model", "b");
        ArrayNode outlier2arr = objectMapper.createArrayNode();
        outlier2arr.add("b1");
        outlier2arr.add("b2");
        outlier2arr.add("b3");
        outlier2.set("intervals", outlier2arr);

        ObjectNode outlier3 = objectMapper.createObjectNode();
        outlier3.put("model", "c");
        ArrayNode outlier3arr = objectMapper.createArrayNode();
        outlier3arr.add("c1");
        outlier3arr.add("c2");
        outlier3arr.add("c3");
        outlier3.set("intervals", outlier3arr);

        ArrayNode entriesNode = objectMapper.createArrayNode();
        entriesNode.addAll(List.of(outlier1, outlier2, outlier3));
        jsonNode.set("entries", entriesNode);


        // Convert the object to a JSON string
        return objectMapper.writeValueAsString(jsonNode);
    }

}
