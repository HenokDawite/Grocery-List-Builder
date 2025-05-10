package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading grocery data from CSV files
 * 
 * @author Henok Dawite & Arjun Sisodia
 */
public class DataLoader {
    
    /**
     * Loads grocery data from a CSV file into the GroceryListBuilder
     * 
     * @param filePath Path to the CSV file
     * @param builder The GroceryListBuilder to load data into
     * @return The number of records loaded
     * @throws IOException If there is an error reading the file
     */
    public static int loadFromCSV(String filePath, GroceryListBuilder builder) throws IOException {
        int recordCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String item = parts[0].trim();
                    try {
                        int week = Integer.parseInt(parts[1].trim());
                        builder.addItem(item, week);
                        recordCount++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid week number for item: " + item);
                    }
                }
            }
        }
        
        return recordCount;
    }
    
    /**
     * Analyzes the data to identify potential time-sensitive items
     * 
     * @param filePath Path to the CSV file
     * @return Map of items and their purchase pattern score (higher score means more likely to be time-sensitive)
     * @throws IOException If there is an error reading the file
     */
    public static Map<String, Double> identifyTimeSensitiveItems(String filePath) throws IOException {
        Map<String, Map<Integer, Integer>> itemWeekFrequency = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String item = parts[0].trim();
                    try {
                        int week = Integer.parseInt(parts[1].trim());
                        
                        // Track frequency of each item per week
                        itemWeekFrequency.putIfAbsent(item, new HashMap<>());
                        Map<Integer, Integer> weekFreq = itemWeekFrequency.get(item);
                        weekFreq.put(week, weekFreq.getOrDefault(week, 0) + 1);
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid week number for item: " + item);
                    }
                }
            }
        }
        
        // Calculate pattern scores
        Map<String, Double> timeSensitiveScores = new HashMap<>();
        for (Map.Entry<String, Map<Integer, Integer>> entry : itemWeekFrequency.entrySet()) {
            String item = entry.getKey();
            Map<Integer, Integer> weekFrequency = entry.getValue();
            
            // Calculate regularity score (higher means more regular intervals)
            double regularityScore = calculateRegularityScore(weekFrequency);
            timeSensitiveScores.put(item, regularityScore);
        }
        
        return timeSensitiveScores;
    }
    
    /**
     * Calculates a regularity score for purchase patterns
     * A higher score indicates more regular purchase intervals
     * 
     * @param weekFrequency Map of weeks to purchase frequency
     * @return The regularity score
     */
    private static double calculateRegularityScore(Map<Integer, Integer> weekFrequency) {
        if (weekFrequency.size() <= 1) {
            return 0.0; // Need at least two weeks to calculate pattern
        }
        
        // Sort weeks
        Integer[] weeks = weekFrequency.keySet().toArray(new Integer[0]);
        java.util.Arrays.sort(weeks);
        
        // Calculate intervals
        int sumIntervals = 0;
        int countIntervals = 0;
        int prevWeek = weeks[0];
        
        for (int i = 1; i < weeks.length; i++) {
            int interval = weeks[i] - prevWeek;
            sumIntervals += interval;
            countIntervals++;
            prevWeek = weeks[i];
        }
        
        if (countIntervals == 0) {
            return 0.0;
        }
        
        double avgInterval = (double) sumIntervals / countIntervals;
        
        // Calculate variance of intervals
        double sumSquaredDiff = 0.0;
        prevWeek = weeks[0];
        
        for (int i = 1; i < weeks.length; i++) {
            int interval = weeks[i] - prevWeek;
            double diff = interval - avgInterval;
            sumSquaredDiff += diff * diff;
            prevWeek = weeks[i];
        }
        
        double variance = sumSquaredDiff / countIntervals;
        
        // Lower variance means more regular intervals
        // Inverse to get a score (higher = more regular)
        return weekFrequency.size() * (1.0 / (1.0 + variance));
    }
}