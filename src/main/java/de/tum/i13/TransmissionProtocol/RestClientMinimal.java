package de.tum.i13.TransmissionProtocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RestClientMinimal {
    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:5024/benchmark/");
        WebTarget resource = target.path("/create-benchmark")
                .queryParam("token", args[0])
                .queryParam("benchmarkType", "evaluation")
                .queryParam("benchmarkName", "REST benchmark")
                .queryParam("queries", "q1,q2");
        Response createResponse = resource.request(MediaType.APPLICATION_JSON)
                .get();
        String json = createResponse.readEntity(String.class);
        Long benchmarkId = -1L;
        if (createResponse.getStatus() == 200) {
            benchmarkId = parseJsonToLong(json, "benchmark_id").asLong();
        }
        WebTarget benchmarkStart = target.path(String.valueOf(benchmarkId)).path("/start-benchmark");
        Response startResponse = benchmarkStart.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        if (startResponse.getStatus() == 201) {
        }
        while (true) {
            WebTarget nextBatch = target.path(String.valueOf(benchmarkId)).path("/next-batch");
            Response batchResponse = nextBatch.request(MediaType.APPLICATION_JSON)
                    .get();
            if (batchResponse.getStatus() == 200) {
                String batchJson = batchResponse.readEntity(String.class);
                if (parseJsonToLong(batchJson, "last").asBoolean()) {
                    break;
                }
                int seqId = parseJsonToLong(batchJson, "seq_id").asInt();
                String Q1ResponseString = generateDummyQ1String(batchJson, benchmarkId, seqId);
                String Q2ResponseString = generateDummyQ2String(batchJson, benchmarkId, seqId);
                WebTarget q1Target = target.path(String.valueOf(benchmarkId)).path("/result-q1");
                Response q1Response = q1Target.request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Q1ResponseString, MediaType.APPLICATION_JSON));
                if (q1Response.getStatus() != 201) {
                }

                WebTarget q2Target = target.path(String.valueOf(benchmarkId)).path("/result-q1");
                Response q2Response = q2Target.request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(Q2ResponseString, MediaType.APPLICATION_JSON));
                if (q1Response.getStatus() != 201) {
                }
            }
        }
        WebTarget endBenchmark = target.path(String.valueOf(benchmarkId)).path("/end-benchmark");
        Response endResponse = endBenchmark.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        client.close();
    }
    private static JsonNode parseJsonToLong(String jsonString, String propertyName) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        JsonNode benchmarkIdNode = jsonNode.get(propertyName);
        if (benchmarkIdNode == null) {
            throw new IllegalArgumentException("Invalid or missing 'benchmark_id' in JSON");
        }
        return benchmarkIdNode;
    }
    private static String generateDummyQ1String(String benchmarkId, long batchSeqId, long size) throws Exception {
        return null;
    }
    private static String generateDummyQ2String(String benchmarkId, long batchSeqId, long size) throws Exception {
        return null;
    }
}
