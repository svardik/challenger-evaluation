package de.tum.i13.TransmissionProtocol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.endMeasurement;

public class ProxyRESTClient {
    private static final String BASE_URL = "http://localhost:5025/";

    private final Client client;
    private final WebTarget target;



    public ProxyRESTClient() {
        this.client = ClientBuilder.newClient();
        this.target = client.target(BASE_URL);
    }

    public Response createBenchmark(String body) {
        WebTarget resource = target.path("/create");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(body, MediaType.APPLICATION_JSON));
    }


    public Response startBenchmark(String benchmarkBody) {
        WebTarget resource = target.path("/start");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(benchmarkBody, MediaType.APPLICATION_JSON));
    }

    public Response getNextBatch(String benchmarkBody) {
        WebTarget resource = target.path("/next_batch");
        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(benchmarkBody, MediaType.APPLICATION_JSON));
    }

    public Response postBatchResultQ1(String resultBody) {
        WebTarget resource = target.path("/result_q1");
        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(resultBody, MediaType.APPLICATION_JSON));
    }

    public Response postBatchResultQ2(String resultBody) {
        WebTarget resource = target.path("/result_q2");
        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(resultBody, MediaType.APPLICATION_JSON));
    }

    public Response endBenchmark(String benchmarkBody) {
        WebTarget resource = target.path("/end");

        return resource.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(benchmarkBody, MediaType.APPLICATION_JSON));
    }

    public void close() {
        client.close();
    }


    public void runBenchmarkingSequence(String token, String benchmarkType, String benchmarkName, List<String> queries, long delayBetweenOps, long batchCalculationDelay) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        setPrefix("REST-PROXY");
//        System.out.println("Starting REST-Proxy benchmark with "+token);



        startMeasurement("createBenchmark");
        ObjectNode benchmarkConfigNode = objectMapper.createObjectNode();
        benchmarkConfigNode.put("token",token);
        benchmarkConfigNode.put("benchmark_name", benchmarkName);
        benchmarkConfigNode.put("benchmark_type", benchmarkType);

        ArrayNode outlier1arr = objectMapper.createArrayNode();
        if (queries.contains("q1")){
            outlier1arr.add(0);
        }
        if (queries.contains("q2")){
            outlier1arr.add(1);
        }


        benchmarkConfigNode.set("queries",outlier1arr);

        String benchmarkConfig = objectMapper.writeValueAsString(benchmarkConfigNode);
        // Example usage:

        Response createResponse = createBenchmark(benchmarkConfig);


        //        System.out.println(createResponse);
        String json = createResponse.readEntity(String.class);
//        System.out.println(json);

        Long benchmarkId = -1L;
        if (createResponse.getStatus() == 200) {
            benchmarkId = parseJsonToLong(json, "id").asLong();
        }

        endMeasurement("createBenchmark");
        Thread.sleep(delayBetweenOps);
//        System.out.println(benchmarkId);


        startMeasurement("startBenchmark");
        ObjectNode benchmarkBodyNode = objectMapper.createObjectNode();
        benchmarkBodyNode.put("id",benchmarkId);
        String benchmarkBody = objectMapper.writeValueAsString(benchmarkBodyNode);

        Response startResponse = startBenchmark(benchmarkBody);


//        System.out.println(startResponse);
        if (startResponse.getStatus() == 200) {
//            System.out.println("benchmark started");
        } else {
//            System.out.println(startResponse.getStatus());
//        System.out.println("error, benchmark not started");
        }
        endMeasurement("startBenchmark");
        Thread.sleep(delayBetweenOps);

        boolean isLast = false;
        while (true) {

//            System.out.println("getting next batch");
            startMeasurement("getNextBatch");
            Response batchResponse = getNextBatch(benchmarkBody);

//            System.out.println(batchResponse);
//            System.out.println(batchResponse.getStatus());
            if (batchResponse.getStatus() == 200) {
                String batchJson = batchResponse.readEntity(String.class);

                isLast = parseJsonToLong(batchJson, "last").asBoolean();
                if (isLast) {
//                    System.out.println("Received lastbatch, finished!");
                    endMeasurement("getNextBatch","getNextBatch-last");
                    break;
                } else {
                    endMeasurement("getNextBatch");
                }
                Thread.sleep(delayBetweenOps);

                int seqId = parseJsonToLong(batchJson, "seq_id").asInt();

//                System.out.println("next batch receivedL: " + seqId + " last: " + isLast);
//                System.out.println("submitting the result");

                String dummyQ1String = generateDummyQ1String(benchmarkId,seqId);
                String dummyQ2String = generateDummyQ2String(benchmarkId,seqId);

                Thread.sleep(batchCalculationDelay);

                startMeasurement("q1Result");
                Response q1Response = postBatchResultQ1(dummyQ1String);
                endMeasurement("q1Result");
                Thread.sleep(delayBetweenOps);
//            System.out.println(q1Response);

//                System.out.println(q1Response);

                startMeasurement("q2Result");
                Response q2Response = postBatchResultQ2(dummyQ2String);
                endMeasurement("q2Result");
                Thread.sleep(delayBetweenOps);
//                System.out.println(q2Response);


            } else {
//                System.out.println("nextbatch error");
                break;
            }
        }

        startMeasurement("endBenchmark");
        Response endResponse =  endBenchmark(benchmarkBody);
        endMeasurement("endBenchmark");
        Thread.sleep(delayBetweenOps);
        if (endResponse.getStatus() == 200) {
//            System.out.println(endResponse);
        }
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

    private static String generateDummyQ2String(long benchmarkId, long batchSeqId) throws Exception {
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

    private static String generateDummyQ1String(long benchmarkId, long batchSeqId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a Java object representing the structure
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("benchmark_id", benchmarkId);
        jsonNode.put("batch_seq_id", batchSeqId);

        ObjectNode outlier1 = objectMapper.createObjectNode();
        outlier1.put("model","a");
        ArrayNode outlier1arr = objectMapper.createArrayNode();
        outlier1arr.add("a1");
        outlier1arr.add("a2");
        outlier1arr.add("a3");
        outlier1.set("intervals",outlier1arr);

        ObjectNode outlier2 = objectMapper.createObjectNode();
        outlier2.put("model","b");
        ArrayNode outlier2arr = objectMapper.createArrayNode();
        outlier2arr.add("b1");
        outlier2arr.add("b2");
        outlier2arr.add("b3");
        outlier2.set("intervals",outlier2arr);

        ObjectNode outlier3 = objectMapper.createObjectNode();
        outlier3.put("model","c");
        ArrayNode outlier3arr = objectMapper.createArrayNode();
        outlier3arr.add("c1");
        outlier3arr.add("c2");
        outlier3arr.add("c3");
        outlier3.set("intervals",outlier3arr);

        ArrayNode entriesNode = objectMapper.createArrayNode();
        entriesNode.addAll(List.of(outlier1,outlier2,outlier3));
        jsonNode.set("entries",entriesNode);


        // Convert the object to a JSON string
        return objectMapper.writeValueAsString(jsonNode);
    }
}
