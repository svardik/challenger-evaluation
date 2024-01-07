package de.tum.i13.TransmissionProtocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.*;

public class RESTProxyMain {
    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
    setPrefix("REST-PROXY");
        System.out.println("Starting REST-Proxy benchmark with "+args[0]);
    ProxyRESTClient apiClient = new ProxyRESTClient();

    ObjectNode benchmarkConfigNode = objectMapper.createObjectNode();
    benchmarkConfigNode.put("token",args[0]);
    benchmarkConfigNode.put("benchmark_name", args[0]);
    benchmarkConfigNode.put("benchmark_type", "evaluation");

    ArrayNode outlier1arr = objectMapper.createArrayNode();
    outlier1arr.add(0);
    outlier1arr.add(1);
    benchmarkConfigNode.set("queries",outlier1arr);

    String benchmarkConfig = objectMapper.writeValueAsString(benchmarkConfigNode);
    // Example usage:
    startMeasurement("createBenchmark");
    Response createResponse = apiClient.createBenchmark(benchmarkConfig);
    endMeasurement("createBenchmark");
    //        System.out.println(createResponse);
    String json = createResponse.readEntity(String.class);
//        System.out.println(json);

    Long benchmarkId = -1L;
        if (createResponse.getStatus() == 200) {
        benchmarkId = parseJsonToLong(json, "id").asLong();
    }
//        System.out.println(benchmarkId);


        ObjectNode benchmarkBodyNode = objectMapper.createObjectNode();
        benchmarkBodyNode.put("id",benchmarkId);
        String benchmarkBody = objectMapper.writeValueAsString(benchmarkBodyNode);

    startMeasurement("startBenchmark");
    Response startResponse = apiClient.startBenchmark(benchmarkBody);
    endMeasurement("startBenchmark");

//        System.out.println(startResponse);
        if (startResponse.getStatus() == 200) {
//            System.out.println("benchmark started");
    } else {
//            System.out.println(startResponse.getStatus());
//        System.out.println("error, benchmark not started");
    }

    boolean isLast = false;
        while (true) {

//            System.out.println("getting next batch");
        startMeasurement("getNextBatch");
        Response batchResponse = apiClient.getNextBatch(benchmarkBody);

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

            int seqId = parseJsonToLong(batchJson, "seq_id").asInt();

//                System.out.println("next batch receivedL: " + seqId + " last: " + isLast);
//                System.out.println("submitting the result");

            String dummyQ1String = generateDummyQ1String(benchmarkId,seqId);
            startMeasurement("q1Result");
            Response q1Response = apiClient.postBatchResultQ1(dummyQ1String);
            endMeasurement("q1Result");
//            System.out.println(q1Response);

//                System.out.println(q1Response);
            String dummyQ2String = generateDummyQ2String(benchmarkId,seqId);
            startMeasurement("q2Result");
            Response q2Response = apiClient.postBatchResultQ2(dummyQ2String);
            endMeasurement("q2Result");
//                System.out.println(q2Response);


        } else {
//                System.out.println("nextbatch error");
            break;
        }
    }

    startMeasurement("endBenchmark");
    Response endResponse =  apiClient.endBenchmark(benchmarkBody);
    endMeasurement("endBenchmark");
        if (endResponse.getStatus() == 200) {
//            System.out.println(endResponse);
        }
//


//    // Handle startResponse...
//        System.out.println(startResponse);
    // Call other API methods as needed...

    // Don't forget to close the client when done
        apiClient.close();
//        PerformanceBenchmarking.printAll();
//        PerformanceBenchmarking.printAvg();
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
