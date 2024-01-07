package de.tum.i13.databasePopulator.PSQL;

import de.tum.i13.challenge.Benchmark;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.endMeasurement;
import static de.tum.i13.BenchmarkingUtils.PerformanceBenchmarking.startMeasurement;

public class Queries {
    private final PSQL conn;

    public List<UUID> groupIds = new ArrayList<>();

    public Queries(PSQL connectionPool) {
        this.conn = connectionPool;
    }

    public PSQL getDb() {
        return this.conn;
    }


    // Overall database functionality
    public void populateDB(int groups, int maxBenchmarks, int batchSize) throws SQLException, ClassNotFoundException, InterruptedException {
        Random r = new Random(1337);
        long totalBenchmarks = 0;
        for (int i = 0; i < groups; i++) {

            UUID groupId = new UUID(r.nextLong(), r.nextLong());
            groupIds.add(groupId);
            insertGroup(groupId, "group-" + i, "token-" + i);

            for (int j = 0; j < r.nextInt(maxBenchmarks); j++) {
                totalBenchmarks++;
                insertBenchmarkStarted(totalBenchmarks, groupId, "benchmark-" + totalBenchmarks, batchSize, "Evaluation");
                //insertLatencyMeasurementStats(totalBenchmarks,42);
                for (int k = 0; k < batchSize; k++) {
                    insertLatency(totalBenchmarks, k, r.nextLong(), r.nextLong(), r.nextLong(), r.nextLong(), r.nextLong());
                }
                insertBenchmarkResult(totalBenchmarks, 42, batchSize, r.nextDouble(), r.nextDouble(), batchSize, r.nextDouble(), r.nextDouble(), "dummy data");
            }
        }
    }

    public void deleteAllData() throws InterruptedException, ClassNotFoundException, SQLException {
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("DELETE FROM groups;")) {
            pStmt.execute();
        }

        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("DELETE FROM querymetrics;")) {
            pStmt.execute();
        }

        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("DELETE FROM benchmarks;")) {
            pStmt.execute();
        }

        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("DELETE FROM public.benchmarkresults;")) {
            pStmt.execute();
        }
    }


    // ------ WRITE -------


    /*
    id = db.Column(UUID, primary_key=True)
    groupname = db.Column(db.Unicode())
    password = db.Column(db.Unicode())
    groupemail = db.Column(db.Unicode())
    groupnick = db.Column(db.Unicode())
    groupapikey = db.Column(db.String(255))
     */
    public void insertGroup(UUID id, String name, String token) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("INSERT INTO groups(" +
                        "id, groupname, password, groupemail, groupnick, groupapikey) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {

            pStmt.setObject(1, id);
            pStmt.setString(2, name);
            pStmt.setString(3, "");
            pStmt.setString(4, "svaral.matej@gmail.com");
            pStmt.setString(5, name);
            pStmt.setString(6, token);

            pStmt.execute();
        }
    }

    public void insertBenchmarkStarted(long benchmarkId, UUID groupId, String benchmarkName, int batchSize, String bt) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("INSERT INTO benchmarks(" +
                        "id, group_id, \"timestamp\", benchmark_name, benchmark_type, batchsize) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setObject(2, groupId);
            pStmt.setTimestamp(3, Timestamp.from(Instant.now()));
            pStmt.setString(4, benchmarkName);
            pStmt.setString(5, bt);
            pStmt.setLong(6, batchSize);

            pStmt.execute();
        }
    }

    public void insertLatencyMeasurementStats(long benchmarkId, double averageLatency) throws SQLException, ClassNotFoundException, InterruptedException {
        //delete in case there is already a measurement
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("DELETE FROM latencymeasurement where benchmark_id = ?")) {
            pStmt.setLong(1, benchmarkId);
            pStmt.execute();
        }

        //insert new metrics
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("INSERT INTO latencymeasurement(" +
                        "benchmark_id, \"timestamp\", avglatency) " +
                        "VALUES (?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setTimestamp(2, Timestamp.from(Instant.now()));
            pStmt.setDouble(3, averageLatency);
            pStmt.execute();
        }
    }


    public void insertLatency(long benchmarkId, long batchId, long startTime, long q1resultTime, long q1Latency, long q2resultTime, long q2Latency) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("INSERT INTO querymetrics(" +
                        "benchmark_id, batch_id, starttime, q1resulttime, q1latency, q2resulttime, q2latency) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            pStmt.setLong(1, benchmarkId);
            pStmt.setLong(2, batchId);
            pStmt.setLong(3, startTime);
            pStmt.setLong(4, q1resultTime);
            pStmt.setLong(5, q1Latency);
            pStmt.setLong(6, q2resultTime);
            pStmt.setLong(7, q2Latency);
            pStmt.execute();
        }
    }

    public void insertBenchmarkResult(long benchmarkId, double seconds, long q1count, double q1Throughput, double q190Percentile, long q2count, double q2Throughput, double q290Percentile, String summary) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement pStmt = this.conn
                .getConnection()
                .prepareStatement("INSERT INTO public.benchmarkresults(" +
                        "id, duration_sec, q1_count, q1_throughput, q1_90percentile, q2_count, q2_throughput, q2_90percentile, summary) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            pStmt.setLong(1, benchmarkId);
            pStmt.setDouble(2, seconds);

            pStmt.setLong(3, q1count);
            pStmt.setDouble(4, q1Throughput);
            pStmt.setDouble(5, q190Percentile);

            pStmt.setLong(6, q2count);
            pStmt.setDouble(7, q2Throughput);
            pStmt.setDouble(8, q290Percentile);

            pStmt.setString(9, summary);

            pStmt.execute();
        }
    }


    // ------ READ -------
    public boolean checkIfGroupExists(String token) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement preparedStatement = this.conn
                .getConnection()
                .prepareStatement("SELECT count(*) AS rowcount FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try (ResultSet r = preparedStatement.executeQuery()) {
                r.next();
                int count = r.getInt("rowcount");
                return count == 1;
            }
        }

    }

    public UUID getGroupIdFromToken(String token) throws SQLException, ClassNotFoundException, InterruptedException {
        try (PreparedStatement preparedStatement = this.conn
                .getConnection()
                .prepareStatement("SELECT id AS group_id FROM groups where groupapikey = ?")) {
            preparedStatement.setString(1, token);
            try (ResultSet r = preparedStatement.executeQuery()) {
                boolean hasElements = r.next();
                if (hasElements) {
                    return r.getObject("group_id", UUID.class);
                }
                return null;
            }
        }
    }

    // Frontend

    /*
    class Benchmarks(db.Model):
    __tablename__ = 'benchmarks'

    id = db.Column(BigInteger, primary_key=True)
    is_active = db.Column(Boolean)
    group_id = db.Column(UUID)
    timestamp = db.Column(TIMESTAMP)
    benchmark_name = db.Column(db.Unicode())
    benchmark_type = db.Column(db.Unicode())
    batchsize = db.Column(BigInteger)
     */

    public String getBenchmarksByGroupId(UUID groupId, boolean buildString) throws InterruptedException, ClassNotFoundException, SQLException {
        try (PreparedStatement preparedStatement = this.conn
                .getConnection()
                .prepareStatement("SELECT * FROM benchmarks where group_id = ?")) {
            preparedStatement.setObject(1, groupId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();

                if (buildString) {
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(metadata.getColumnName(i)).append(", ");
                    }
                    sb.append("\n");
                }

                while (rs.next()) {
                    if (buildString) {
                        for (int i = 1; i <= columnCount; i++) {
                            sb.append(rs.getString(i)).append(", ");
                        }
                        sb.append("\n");
                    }
                }
                return sb.toString();
            }
        }
    }


    public String getEvaluationResults(boolean buildString) throws InterruptedException, ClassNotFoundException, SQLException {
        try (PreparedStatement preparedStatement = this.conn
                .getConnection()
                .prepareStatement(
                        "SELECT DISTINCT ON(g.id) g.groupname, br.q1_90percentile, br.q1_throughput, br.q2_throughput, br.q2_90percentile, ((br.q1_90percentile + br.q2_90percentile) / 2) as avg90percentile, ((br.q1_throughput + br.q2_throughput) / 2) as avgthroughput from benchmarks as b " +
                                "join benchmarkresults as br " +
                                "on br.id = b.id " +
                                "join groups as g " +
                                "on g.id = b.group_id " +
                                "where b.benchmark_type like 'Evaluation' and br.q1_count >= 100 and br.q2_count >= 100 " +
                                "order by g.id, timestamp desc")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();

                if (buildString) {
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(metadata.getColumnName(i)).append(", ");
                    }
                    sb.append("\n");
                }

                while (rs.next()) {
                    if (buildString) {
                        for (int i = 1; i <= columnCount; i++) {
                            sb.append(rs.getString(i)).append(", ");
                        }
                        sb.append("\n");
                    } else {
                        rs.getString(1);
                    }
                }
                return sb.toString();
            }
        }
    }
}
