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
     * Imports grocery data from a CSV file into a GroceryListBuilder instance.
     * Skips the header row and processes each line to extract item names and week numbers.
     * 
     * @param filePath The location of the CSV file to be imported
     * @param builder The GroceryListBuilder that will store the imported data
     * @return The total number of successfully imported records
     * @throws IOException If the file cannot be accessed or read properly
     */
    public static int loadFromCSV(String filePath, GroceryListBuilder builder) throws IOException {
        // Initialize counter for successful imports
        int recordCount = 0;
        
        // Use try-with-resources to ensure file is properly closed after reading
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Variable to store each line from the file
            String line;
            // Flag to identify and skip the header row
            boolean isHeader = true;
            
            // Read the file line by line until end of file
            while ((line = reader.readLine()) != null) {
                // Check if this is the header line
                if (isHeader) {
                    // Skip header and set flag to false for subsequent lines
                    isHeader = false;
                    continue;
                }
                
                // Split the CSV line into parts using comma as delimiter
                String[] parts = line.split(",");
                // Ensure the line has at least 2 elements (item name and week)
                if (parts.length >= 2) {
                    // Extract item name and remove whitespace
                    String item = parts[0].trim();
                    try {
                        // Convert week string to integer, removing whitespace
                        int week = Integer.parseInt(parts[1].trim());
                        // Add the item to the builder with its week
                        builder.addItem(item, week);
                        // Increment the counter for successful imports
                        recordCount++;
                    } catch (NumberFormatException e) {
                        // Log error if week number is not a valid integer
                        System.err.println("Invalid week number for item: " + item);
                    }
                }
            }
        }
        
        // Return the total number of successfully imported records
        return recordCount;
    }
    
    /**
     * Analyzes purchase patterns to identify items that are bought at regular intervals.
     * Items with more regular purchase patterns receive higher scores, indicating they
     * might be time-sensitive (perishable items that should be purchased on schedule).
     * 
     * @param filePath The location of the CSV file containing purchase history
     * @return A map of items to their regularity scores (higher scores indicate more regular purchases)
     * @throws IOException If the file cannot be accessed or read properly
     */
    public static Map<String, Double> identifyTimeSensitiveItems(String filePath) throws IOException {
        // Map to store item purchase frequencies by week
        Map<String, Map<Integer, Integer>> itemWeekFrequency = new HashMap<>();
        
        // Use try-with-resources to ensure file is properly closed after reading
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Variable to store each line from the file
            String line;
            // Flag to identify and skip the header row
            boolean isHeader = true;
            
            // Read the file line by line until end of file
            while ((line = reader.readLine()) != null) {
                // Check if this is the header line
                if (isHeader) {
                    // Skip header and set flag to false for subsequent lines
                    isHeader = false;
                    continue;
                }
                
                // Split the CSV line into parts using comma as delimiter
                String[] parts = line.split(",");
                // Ensure the line has at least 2 elements (item name and week)
                if (parts.length >= 2) {
                    // Extract item name and remove whitespace
                    String item = parts[0].trim();
                    try {
                        // Convert week string to integer, removing whitespace
                        int week = Integer.parseInt(parts[1].trim());
                        
                        // Create a new map for this item if it doesn't exist yet
                        itemWeekFrequency.putIfAbsent(item, new HashMap<>());
                        // Get the frequency map for this item
                        Map<Integer, Integer> weekFreq = itemWeekFrequency.get(item);
                        // Increment the purchase count for this item in this week
                        weekFreq.put(week, weekFreq.getOrDefault(week, 0) + 1);
                        
                    } catch (NumberFormatException e) {
                        // Log error if week number is not a valid integer
                        System.err.println("Invalid week number for item: " + item);
                    }
                }
            }
        }
        
        // Map to store calculated pattern scores for each item
        Map<String, Double> timeSensitiveScores = new HashMap<>();
        // Calculate regularity scores for each item
        for (Map.Entry<String, Map<Integer, Integer>> entry : itemWeekFrequency.entrySet()) {
            // Get the item name
            String item = entry.getKey();
            // Get the week frequency map for this item
            Map<Integer, Integer> weekFrequency = entry.getValue();
            
            // Calculate how regular the purchase intervals are
            double regularityScore = calculateRegularityScore(weekFrequency);
            // Store the calculated score in the results map
            timeSensitiveScores.put(item, regularityScore);
        }
        
        // Return the map of item names to regularity scores
        return timeSensitiveScores;
    }
    
    /**
     * Calculates a score representing how regular the purchase intervals are for an item.
     * The score is based on the variance of intervals between purchases - lower variance
     * means more consistent intervals, resulting in a higher score.
     * 
     * @param weekFrequency A map tracking how many times an item was purchased in each week
     * @return A score where higher values indicate more regular purchase intervals
     */
    private static double calculateRegularityScore(Map<Integer, Integer> weekFrequency) {
        // Need at least two weeks to calculate a pattern
        if (weekFrequency.size() <= 1) {
            return 0.0; // Not enough data for pattern detection
        }
        
        // Convert weeks to an array for sorting
        Integer[] weeks = weekFrequency.keySet().toArray(new Integer[0]);
        // Sort weeks chronologically
        java.util.Arrays.sort(weeks);
        
        // Variables to track interval calculations
        int sumIntervals = 0;
        int countIntervals = 0;
        int prevWeek = weeks[0];
        
        // Calculate all intervals between consecutive purchases
        for (int i = 1; i < weeks.length; i++) {
            // Calculate the interval since previous purchase
            int interval = weeks[i] - prevWeek;
            // Add to running sum of intervals
            sumIntervals += interval;
            // Increment counter of intervals
            countIntervals++;
            // Update previous week for next iteration
            prevWeek = weeks[i];
        }
        
        // Handle case with no intervals (should not happen if size > 1)
        if (countIntervals == 0) {
            return 0.0;
        }
        
        // Calculate the average interval between purchases
        double avgInterval = (double) sumIntervals / countIntervals;
        
        // Calculate the variance of the intervals (how much they differ from average)
        double sumSquaredDiff = 0.0;
        prevWeek = weeks[0];
        
        // Iterate through weeks again to calculate variance
        for (int i = 1; i < weeks.length; i++) {
            // Calculate the interval since previous purchase
            int interval = weeks[i] - prevWeek;
            // Calculate difference from average interval
            double diff = interval - avgInterval;
            // Square the difference and add to sum
            sumSquaredDiff += diff * diff;
            // Update previous week for next iteration
            prevWeek = weeks[i];
        }
        
        // Calculate variance by dividing sum of squared differences by count
        double variance = sumSquaredDiff / countIntervals;
        
        // Calculate final score: 
        return weekFrequency.size() * (1.0 / (1.0 + variance));
    }
}