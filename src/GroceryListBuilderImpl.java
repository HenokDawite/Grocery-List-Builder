package src;
import java.util.*;

/**
 * Implementation of the GroceryListBuilder interface 
 * This class uses purchase history to create intelligent shopping suggestions
 * based on purchase patterns, frequency, and item characteristics.
 * @author Henok Dawite & Arjun Sisodia
 */
public class GroceryListBuilderImpl implements GroceryListBuilder {
    // Tracks how many times each item has been purchased overall
    private Map<String, Integer> itemFrequency; 
    
    // Records which items were bought in each specific week
    private Map<Integer, List<String>> weeklyItems; 
    
    // Stores the most recent week when each item was purchased
    private Map<String, Integer> lastPurchaseWeek; 
    
    // Maintains complete purchase history by week for each item
    private Map<String, List<Integer>> purchaseWeeks; 
    
    // Priority queue for efficiently retrieving most frequent items
    private PriorityQueue<ItemFrequency> frequentItemsQueue; 
    
    // Organizes items by category (produce, dairy, etc.)
    private Map<String, String> itemCategories; 
    
    // Tracks items that may spoil or expire quickly
    private Set<String> timeSensitiveItems; 
    
    // Tracks the current week number for calculations
    private int currentWeek; 
    
    /**
     * Initializes the grocery list builder with empty data structures
     * to track purchase history and item information.
     */
    public GroceryListBuilderImpl() {
        // Initialize HashMap to track purchase frequency of items
        itemFrequency = new HashMap<>();
        
        // Initialize HashMap to track items purchased in each week
        weeklyItems = new HashMap<>();
        
        // Initialize HashMap to track when each item was last purchased
        lastPurchaseWeek = new HashMap<>();
        
        // Initialize HashMap to track all purchase dates for each item
        purchaseWeeks = new HashMap<>();
        
        // Initialize priority queue with custom comparator - higher frequency items get higher priority
        frequentItemsQueue = new PriorityQueue<>((a, b) -> b.frequency - a.frequency);
        
        // Initialize HashMap to track item categories
        itemCategories = new HashMap<>();
        
        // Initialize HashSet to track perishable items
        timeSensitiveItems = new HashSet<>();
        
        // Initialize current week to 1 (beginning)
        currentWeek = 1;
    }

    /**
     * Helper class to store items with their purchase frequencies.
     * Used for prioritizing items in the frequency queue.
     */
    private class ItemFrequency {
        // The name of the item
        String item;
        
        // How many times the item has been purchased
        int frequency;
        
        /**
         * Creates a new ItemFrequency instance.
         * 
         * @param item The name of the item
         * @param frequency How many times this item has been purchased
         */
        public ItemFrequency(String item, int frequency) {
            this.item = item;
            this.frequency = frequency;
        }
    }

    @Override
    public void addItem(String item, int week) {
        // Increment the frequency counter for this item
        itemFrequency.put(item, itemFrequency.getOrDefault(item, 0) + 1);

        // Add this item to the list of items for this week
        weeklyItems.putIfAbsent(week, new ArrayList<>());
        weeklyItems.get(week).add(item);
        
        // Update the last purchase week for this item
        lastPurchaseWeek.put(item, week);
        
        // Add this week to the purchase history for this item
        purchaseWeeks.putIfAbsent(item, new ArrayList<>());
        purchaseWeeks.get(item).add(week);
        
        // Update the current week tracker if this purchase is in a newer week
        if (week > currentWeek) {
            currentWeek = week;
        }

        // Refresh the priority queue with current frequency data
        updateFrequentItemsQueue();
    }

    /**
     * Refreshes the priority queue with current frequency data.
     * Called whenever item frequencies change to maintain accuracy.
     */
    private void updateFrequentItemsQueue() {
        // Clear all existing entries in the priority queue
        frequentItemsQueue.clear();
        
        // Rebuild the queue with current frequency data for all items
        for (Map.Entry<String, Integer> entry : itemFrequency.entrySet()) {
            frequentItemsQueue.add(new ItemFrequency(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public List<String> getFrequentItems() {
        // Create list to hold the results
        List<String> frequentItems = new ArrayList<>();
        
        // Create a temporary copy of the queue to preserve the original
        PriorityQueue<ItemFrequency> tempQueue = new PriorityQueue<>(frequentItemsQueue);
        
        // Determine how many items to retrieve (at most 10)
        int count = Math.min(10, tempQueue.size());
        
        // Extract the top items from the queue
        for (int i = 0; i < count; i++) {
            if (!tempQueue.isEmpty()) {
                // Add the next most frequent item to the result list
                frequentItems.add(tempQueue.poll().item);
            }
        }
        
        // Return the list of most frequent items
        return frequentItems;
    }

    @Override
    public List<String> generateSuggestedList() {
        // Create list to hold suggestions
        List<String> suggestedList = new ArrayList<>();
        
        // Analyze purchase patterns for each item
        for (Map.Entry<String, List<Integer>> entry : purchaseWeeks.entrySet()) {
            // Get item name
            String item = entry.getKey();
            // Get all weeks when this item was purchased
            List<Integer> weeks = entry.getValue();
            
            // Skip items with insufficient data for pattern detection
            if (weeks.size() < 2) {
                continue;
            }
            
            // Sort the weeks chronologically for interval calculations
            Collections.sort(weeks);
            
            // Calculate the average interval between purchases
            int totalInterval = 0;
            for (int i = 1; i < weeks.size(); i++) {
                totalInterval += weeks.get(i) - weeks.get(i-1);
            }
            double avgInterval = (double) totalInterval / (weeks.size() - 1);
            
            // Get when this item was last purchased
            int lastWeek = lastPurchaseWeek.getOrDefault(item, 0);
            
            // Calculate how long it's been since the last purchase
            int weeksSinceLastPurchase = currentWeek - lastWeek;
            
            // Add to suggestions if:
            // 1. It's approaching the average purchase interval time, or
            // 2. It's a time-sensitive item that hasn't been bought recently
            if (weeksSinceLastPurchase >= avgInterval - 0.5 ||
                (timeSensitiveItems.contains(item) && weeksSinceLastPurchase >= 2)) {
                suggestedList.add(item);
            }
        }
        
        // Ensure the list has at least 5 items by adding frequent items if needed
        if (suggestedList.size() < 5) {
            // Create a temporary copy of the frequency queue to preserve the original
            PriorityQueue<ItemFrequency> tempQueue = new PriorityQueue<>(frequentItemsQueue);
            // Add items until we have at least 5 or run out of candidates
            while (suggestedList.size() < 5 && !tempQueue.isEmpty()) {
                // Get the next most frequent item
                String item = tempQueue.poll().item;
                // Only add if not already in list and we have enough purchase data
                if (!suggestedList.contains(item) && purchaseWeeks.get(item).size() >= 2) {
                    suggestedList.add(item);
                }
            }
        }
        
        // Return the final list of suggestions
        return suggestedList;
    }

    @Override
    public void rotateItems(int week) {
        // Update the current week if the provided week is newer
        if (week > currentWeek) {
            currentWeek = week;
        }
        
        // Look for time-sensitive items purchased 2 weeks ago
        if (weeklyItems.containsKey(week - 2)) {
            // Get all items purchased 2 weeks ago
            List<String> oldItems = weeklyItems.get(week - 2);
            // Check each item for rotation eligibility
            for (String item : oldItems) {
                // Only consider time-sensitive items
                if (timeSensitiveItems.contains(item)) {
                    // Get the last recorded purchase week for this item
                    Integer lastWeek = lastPurchaseWeek.get(item);
                    // Only rotate if it hasn't been purchased more recently
                    if (lastWeek != null && lastWeek <= week - 2) {
                        // Add the item to the current week to simulate repurchase
                        addItem(item, week);
                    }
                }
            }
        }
    }

    /**
     * Assigns a category to an item and automatically marks certain
     * perishable categories as time-sensitive.
     * 
     * @param item The grocery item name
     * @param category The category to assign to the item
     */
    public void addCategory(String item, String category) {
        // Store the category for this item
        itemCategories.put(item, category);
        
        // Automatically mark items in perishable categories as time-sensitive
        if (category.equals("Fruits") || 
            category.equals("Vegetables") || 
            category.equals("Dairy") ||
            category.equals("Bakery") ||
            category.equals("Deli") ||
            category.equals("Seafood")) {
            // Mark the item as time-sensitive
            markAsTimeSensitive(item);
        }
    }
    
    /**
     * Retrieves the category assigned to an item.
     * 
     * @param item The grocery item name
     * @return The item's category or null if uncategorized
     */
    public String getItemCategory(String item) {
        // Return the category, or null if not found
        return itemCategories.getOrDefault(item, null);
    }

    /**
     * Retrieves all items belonging to a specific category.
     * 
     * @param category The category to filter by
     * @return A list of all items in the requested category
     */
    public List<String> getItemsByCategory(String category) {
        // Create list to hold matching items
        List<String> items = new ArrayList<>();
        
        // Check each item's category for a match
        for (Map.Entry<String, String> entry : itemCategories.entrySet()) {
            if (entry.getValue().equals(category)) {
                // Add matching items to the results list
                items.add(entry.getKey());
            }
        }
        
        // Return the list of items in this category
        return items;
    }
    
    /**
     * Flags an item as time-sensitive, indicating it may spoil or expire quickly.
     * 
     * @param item The grocery item to mark as time-sensitive
     */
    public void markAsTimeSensitive(String item) {
        // Add this item to the set of time-sensitive items
        timeSensitiveItems.add(item);
    }

    /**
     * Removes the time-sensitive flag from an item.
     * 
     * @param item The grocery item to unmark as time-sensitive
     */
    public void unmarkAsTimeSensitive(String item) {
        // Remove this item from the set of time-sensitive items
        timeSensitiveItems.remove(item);
    }

    /**
     * Determines whether an item is flagged as time-sensitive.
     * 
     * @param item The grocery item to check
     * @return true if the item is marked as time-sensitive, false otherwise
     */
    public boolean isTimeSensitive(String item) {
        // Check if the item is in the time-sensitive set
        return timeSensitiveItems.contains(item);
    }
    
    /**
     * Retrieves all items purchased during a specific week.
     * 
     * @param week The week number to retrieve items for
     * @return A list of items purchased during that week
     */
    public List<String> getWeeklyItems(int week) {
        // Return the list of items for this week, or an empty list if none
        return weeklyItems.getOrDefault(week, new ArrayList<>());
    }
    
    /**
     * Retrieves all unique categories currently in use.
     * 
     * @return A set of all category names
     */
    public Set<String> getAllCategories() {
        // Create a set to store unique categories
        Set<String> categories = new HashSet<>();
        
        // Add each category to the set (duplicates automatically ignored)
        for (String category : itemCategories.values()) {
            categories.add(category);
        }
        
        // Return the set of all categories
        return categories;
    }
    
    /**
     * Retrieves all week numbers that contain purchase history.
     * 
     * @return A set of all week numbers with recorded purchases
     */
    public Set<Integer> getAllWeeks() {
        // Return the set of all weeks that have purchase records
        return weeklyItems.keySet();
    }
    
    /**
     * Gets the current week number being tracked.
     * 
     * @return The current week number
     */
    public int getCurrentWeek() {
        // Return the current week tracker value
        return currentWeek;
    }
    
    /**
     * Updates the current week number being tracked.
     * 
     * @param week The new week number to set
     */
    public void setCurrentWeek(int week) {
        // Update the current week tracker
        currentWeek = week;
    }
    
    /**
     * Calculates how often an item is typically purchased.
     * 
     * @param item The grocery item to analyze
     * @return The average number of weeks between purchases, or -1 if insufficient data
     */
    public double getAveragePurchaseInterval(String item) {
        // Get the purchase history for this item
        List<Integer> weeks = purchaseWeeks.get(item);
        // Check if we have enough data to calculate an interval
        if (weeks == null || weeks.size() <= 1) {
            return -1.0; // Return -1 to indicate no interval data
        }
        
        // Sort the weeks chronologically
        Collections.sort(weeks);
        
        // Calculate the total of all intervals
        int totalInterval = 0;
        for (int i = 1; i < weeks.size(); i++) {
            totalInterval += weeks.get(i) - weeks.get(i-1);
        }
        // Calculate and return the average interval
        return (double) totalInterval / (weeks.size() - 1);
    }

    /**
     * Retrieves the most recent week when an item was purchased.
     * 
     * @param item The grocery item to check
     * @return The week number of the most recent purchase, or -1 if never purchased
     */
    public int getLastPurchaseWeek(String item) {
        // Return the last purchase week, or -1 if not found
        return lastPurchaseWeek.getOrDefault(item, -1);
    }    
}