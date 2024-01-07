package de.tum.i13.BenchmarkingUtils;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.tinylog.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PerformanceBenchmarking {
    static Map<String,Long> startTimes = new HashMap<>();
    static Map<String, List<Long>> allBenchmarks = new TreeMap<>();
    static String prefix = "";

    public static void setPrefix(String prefix){
        PerformanceBenchmarking.prefix = prefix;
    }
    public static void startMeasurement(String name){
        Long startTime = System.nanoTime();
//        Logger.info("PERFORMANCE BENCHMARKING: starting "+name);
        startTimes.put(prefix+name,startTime);
    }

    public static void endMeasurement(String name){
        endMeasurement(name,name);
    }

    public static void endMeasurement(String name,String newName){
        Long endTime = System.nanoTime();
        Long start = startTimes.get(prefix+name);
        if (start == null){
//            Logger.error("No performance benchmark started: "+prefix+name);
        } else {
//            Logger.info("PERFORMANCE BENCHMARKING: "+prefix+name+" - "+((endTime - start)/1000000)+"ms");
            insertIntoAllBenchmarks(prefix+newName,endTime-start);
        }
    }

    private static void insertIntoAllBenchmarks(String name, long time){
        List<Long> benchmarks = allBenchmarks.getOrDefault(name,new ArrayList<>());
        benchmarks.add(time);
        allBenchmarks.put(name,benchmarks);
    }

    public static void printAll(){
        Gson gson = new Gson();
        String json = gson.toJson(allBenchmarks);
        System.out.println(json);
    }

    public static void printAvg(){
        for (Map.Entry<String,List<Long>> e: allBenchmarks.entrySet()
             ) {
            System.out.println(e.getKey()+"\t"+e.getValue().stream().mapToDouble(a->{return (double) a /1000000;}).average().orElse(-1.0)+"\t"+confidenceInterval(e.getValue()));
        }
    }

    public static void saveToCsv(){
        // Write map entries to CSV file
        try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"),'\t', CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            for (Map.Entry<String, List<Long>> entry : allBenchmarks.entrySet()) {
                List<String> csvData = new ArrayList<>();
                csvData.add(entry.getKey());
                for (Long value : entry.getValue()) {
                    csvData.add(String.valueOf(value));
                }
                writer.writeNext(csvData.toArray(new String[0]));
            }
            System.out.println("CSV file 'data.csv' has been created with the map entries.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String confidenceInterval(List<Long> data){
        double mean = calculateMean(data);
        double standardDeviation = calculateStandardDeviation(data);
        int sampleSize = data.size();

        double criticalValue = 1.96; // For 95% confidence interval (Z-value for 95%)

        double marginOfError = criticalValue * (standardDeviation / Math.sqrt(sampleSize));
        double lowerBound = (mean - marginOfError)/1000000;
        double upperBound = (mean + marginOfError)/1000000;

        return "[" + lowerBound + ", " + upperBound + "]";
    }

    // Method to calculate the mean
    public static double calculateMean(List<Long> data) {
        return data.stream().collect(Collectors.averagingLong(Long::longValue));
    }

    // Method to calculate the standard deviation
    public static double calculateStandardDeviation(List<Long> data) {
        double mean = calculateMean(data);
        double variance = data.stream().mapToDouble(num -> Math.pow(num - mean, 2)).sum() / data.size();
        return Math.sqrt(variance);
    }

    public static void restartBenchmarkResults(){
        startTimes = new HashMap<>();
        allBenchmarks = new TreeMap<>();
        prefix = "";
    }
}
