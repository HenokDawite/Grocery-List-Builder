package src;
import java.util.*;

/**
 * Enhanced implementation of the GroceryListBuilder interface
 * This implementation improves the suggested list feature to consider purchase patterns,
 * not just frequency.
 * 
 * @author Henok Dawite & Arjun Sisodia
 */
public class GroceryListBuilderImpl implements GroceryListBuilder {
    private Map<String, Integer> itemFrequency; // Tracks item frequency
    private Map<Integer, List<String>> weeklyItems; // Maps weeks to items
    private Map<String, Integer> lastPurchaseWeek; // Tracks the week an item was last purchased
    private Map<String, List<Integer>> purchaseWeeks; // Tracks all weeks an item was purchased
    private PriorityQueue<ItemFrequency> frequentItemsQueue; // Queue for suggesting items
    private Map<String, String> itemCategories; // Maps items to their categories
    private Set<String> timeSensitiveItems; // Set of items that are time-sensitive
    private int currentWeek; // The current week
    
    /**
     * Constructor to initialize the GroceryListBuilder
     */
    public GroceryListBuilderImpl() {
        itemFrequency = new HashMap<>();
        weeklyItems = new HashMap<>();
        lastPurchaseWeek = new HashMap<>();
        purchaseWeeks = new HashMap<>();
        frequentItemsQueue = new PriorityQueue<>((a, b) -> b.frequency - a.frequency); // Higher frequency first
        itemCategories = new HashMap<>();
        timeSensitiveItems = new HashSet<>();
        currentWeek = 1;
    }

    /**
     * Inner class to represent an item and its frequency
     */
    private class ItemFrequency {
        String item;
        int frequency;
        
        public ItemFrequency(String item, int frequency) {
            this.item = item;
            this.frequency = frequency;
        }
    }

    @Override
    public void addItem(String item, int week) {
        // Update frequency
        itemFrequency.put(item, itemFrequency.getOrDefault(item, 0) + 1);

        // Update weekly items
        weeklyItems.putIfAbsent(week, new ArrayList<>());
        weeklyItems.get(week).add(item);
        
        // Update last purchase week
        lastPurchaseWeek.put(item, week);
        
        // Update purchase weeks history
        purchaseWeeks.putIfAbsent(item, new ArrayList<>());
        purchaseWeeks.get(item).add(week);
        
        // Update current week if this week is newer
        if (week > currentWeek) {
            currentWeek = week;
        }

        // Update priority queue
        updateFrequentItemsQueue();
    }

    /**
     * Updates the priority queue with current frequency information
     */
    private void updateFrequentItemsQueue() {
        // Clear existing queue
        frequentItemsQueue.clear();
        
        // Add all items with their frequencies
        for (Map.Entry<String, Integer> entry : itemFrequency.entrySet()) {
            frequentItemsQueue.add(new ItemFrequency(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public List<String> getFrequentItems() {
        List<String> frequentItems = new ArrayList<>();
        PriorityQueue<ItemFrequency> tempQueue = new PriorityQueue<>(frequentItemsQueue);
        
        // Get top 10 items or all items if less than 10
        int count = Math.min(10, tempQueue.size());
        for (int i = 0; i < count; i++) {
            if (!tempQueue.isEmpty()) {
                frequentItems.add(tempQueue.poll().item);
            }
        }
        
        return frequentItems;
    }

    @Override
    public List<String> generateSuggestedList() {
        List<String> suggestedList = new ArrayList<>();
        
        // Consider purchase patterns, not just frequency
        for (Map.Entry<String, List<Integer>> entry : purchaseWeeks.entrySet()) {
            String item = entry.getKey();
            List<Integer> weeks = entry.getValue();
            
            // Skip items purchased less than twice
            if (weeks.size() < 2) {
                continue;
            }
            
            // Sort weeks to ensure correct interval calculation
            Collections.sort(weeks);
            
            // Calculate the average purchase interval
            int totalInterval = 0;
            for (int i = 1; i < weeks.size(); i++) {
                totalInterval += weeks.get(i) - weeks.get(i-1);
            }
            double avgInterval = (double) totalInterval / (weeks.size() - 1);
            
            // Get the last purchase week
            int lastWeek = lastPurchaseWeek.getOrDefault(item, 0);
            
            // Determine if it's time to suggest the item again
            int weeksSinceLastPurchase = currentWeek - lastWeek;
            
            // If time since last purchase is close to the average interval,
            // or if it's a time-sensitive item that might expire soon
            if (weeksSinceLastPurchase >= avgInterval - 0.5 ||
                (timeSensitiveItems.contains(item) && weeksSinceLastPurchase >= 2)) {
                suggestedList.add(item);
            }
        }
        
        // If suggested list is too small, add frequent items that have been purchased at least twice
        if (suggestedList.size() < 5) {
            PriorityQueue<ItemFrequency> tempQueue = new PriorityQueue<>(frequentItemsQueue);
            while (suggestedList.size() < 5 && !tempQueue.isEmpty()) {
                String item = tempQueue.poll().item;
                if (!suggestedList.contains(item) && purchaseWeeks.get(item).size() >= 2) {
                    suggestedList.add(item);
                }
            }
        }
        
        return suggestedList;
    }

    @Override
    public void rotateItems(int week) {
        // Update current week
        if (week > currentWeek) {
            currentWeek = week;
        }
        
        // Look for time-sensitive items from 2 weeks ago
        if (weeklyItems.containsKey(week - 2)) {
            List<String> oldItems = weeklyItems.get(week - 2);
            for (String item : oldItems) {
                if (timeSensitiveItems.contains(item)) {
                    // Check if not purchased recently
                    Integer lastWeek = lastPurchaseWeek.get(item);
                    if (lastWeek != null && lastWeek <= week - 2) {
                        // Add to current week if it's time to repurchase
                        addItem(item, week);
                    }
                }
            }
        }
    }

    /**
     * Adds a category to an item
     * 
     * @param item The grocery item
     * @param category The category to assign
     */
    public void addCategory(String item, String category) {
        itemCategories.put(item, category);
        
        // Automatically mark certain categories as time-sensitive
        if (category.equals("Fruits") || 
            category.equals("Vegetables") || 
            category.equals("Dairy") ||
            category.equals("Bakery") ||
            category.equals("Deli") ||
            category.equals("Seafood")) {
            markAsTimeSensitive(item);
        }
    }
    
    /**
     * Gets the category of an item
     * 
     * @param item The grocery item
     * @return The category of the item, or null if not categorized
     */
    public String getItemCategory(String item) {
        return itemCategories.getOrDefault(item, null);
    }

    /**
     * Gets all items in a specific category
     * 
     * @param category The category to filter by
     * @return List of items in the category
     */
    public List<String> getItemsByCategory(String category) {
        List<String> items = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : itemCategories.entrySet()) {
            if (entry.getValue().equals(category)) {
                items.add(entry.getKey());
            }
        }
        
        return items;
    }
    
    /**
     * Marks an item as time-sensitive
     * 
     * @param item The item to mark as time-sensitive
     */
    public void markAsTimeSensitive(String item) {
        timeSensitiveItems.add(item);
    }

    /**
     * Removes an item from the time-sensitive set
     * 
     * @param item The item to unmark as time-sensitive
     */
    public void unmarkAsTimeSensitive(String item) {
        timeSensitiveItems.remove(item);
    }

    /**
     * Checks if an item is time-sensitive
     * 
     * @param item The item to check
     * @return true if the item is time-sensitive, false otherwise
     */
    public boolean isTimeSensitive(String item) {
        return timeSensitiveItems.contains(item);
    }
    
    /**
     * Gets all items purchased in a specific week
     * 
     * @param week The week number
     * @return List of items purchased in that week
     */
    public List<String> getWeeklyItems(int week) {
        return weeklyItems.getOrDefault(week, new ArrayList<>());
    }
    
    /**
     * Gets all available categories
     * 
     * @return Set of all categories
     */
    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>();
        
        for (String category : itemCategories.values()) {
            categories.add(category);
        }
        
        return categories;
    }
    
    /**
     * Gets all week numbers that have grocery data
     * 
     * @return Set of week numbers
     */
    public Set<Integer> getAllWeeks() {
        return weeklyItems.keySet();
    }
    
    /**
     * Gets the current week
     * 
     * @return The current week number
     */
    public int getCurrentWeek() {
        return currentWeek;
    }
    
    /**
     * Sets the current week
     * 
     * @param week The week number to set
     */
    public void setCurrentWeek(int week) {
        currentWeek = week;
    }
    
    /**
     * Calculates the average purchase interval for an item
     * 
     * @param item The item to calculate for
     * @return The average interval between purchases, or -1 if insufficient data
     */
    public double getAveragePurchaseInterval(String item) {
        List<Integer> weeks = purchaseWeeks.get(item);
        if (weeks == null || weeks.size() <= 1) {
            return -1.0; // Return -1 to indicate no interval data
        }
        
        // Sort weeks to ensure correct interval calculation
        Collections.sort(weeks);
        
        int totalInterval = 0;
        for (int i = 1; i < weeks.size(); i++) {
            totalInterval += weeks.get(i) - weeks.get(i-1);
        }
        return (double) totalInterval / (weeks.size() - 1);
    }

    public int getLastPurchaseWeek(String item) {
        return lastPurchaseWeek.getOrDefault(item, -1);
    }    
}
