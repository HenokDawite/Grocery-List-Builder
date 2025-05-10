package src;
import java.util.*;

/**
 * Implementation of the GroceryListBuilder interface
 * This class provides functionality for building and managing grocery lists.
 * 
 * @author Henok Dawite & Arjun Sisodia
 */
public class GroceryListBuilderImpl implements GroceryListBuilder {
    private Map<String, Integer> itemFrequency; // Tracks item frequency
    private Map<Integer, List<String>> weeklyItems; // Maps weeks to items
    private PriorityQueue<ItemFrequency> frequentItemsQueue; // Queue for suggesting items
    private Map<String, String> itemCategories; // Maps items to their categories
    private Set<String> timeSensitiveItems; // Set of items that are time-sensitive
    
    /**
     * Constructor to initialize the GroceryListBuilder
     */
    public GroceryListBuilderImpl() {
        itemFrequency = new HashMap<>();
        weeklyItems = new HashMap<>();
        frequentItemsQueue = new PriorityQueue<>((a, b) -> b.frequency - a.frequency); // Higher frequency first
        itemCategories = new HashMap<>();
        timeSensitiveItems = new HashSet<>();
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
        itemFrequency.put(item, itemFrequency.getOrDefault(item, 0) + 1);

        weeklyItems.putIfAbsent(week, new ArrayList<>());
        weeklyItems.get(week).add(item);

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
            frequentItems.add(tempQueue.poll().item);
        }
        
        return frequentItems;
    }

    @Override
    public List<String> generateSuggestedList() {
        List<String> suggestedList = new ArrayList<>();
        PriorityQueue<ItemFrequency> tempQueue = new PriorityQueue<>(frequentItemsQueue);
        
        // Get items purchased more than once
        while (!tempQueue.isEmpty()) {
            ItemFrequency itemFreq = tempQueue.poll();
            if (itemFreq.frequency > 1) {
                suggestedList.add(itemFreq.item);
            }
        }
        
        return suggestedList;
    }

    @Override
    public void rotateItems(int currentWeek) {
        // Remove items from time-sensitive items that are likely to be expired
        // and add them to the suggested list for the coming week
        if (weeklyItems.containsKey(currentWeek - 2)) {
            List<String> oldItems = weeklyItems.get(currentWeek - 2);
            for (String item : oldItems) {
                if (timeSensitiveItems.contains(item)) {
                    // Add to current week if it's time to repurchase
                    addItem(item, currentWeek);
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
}
