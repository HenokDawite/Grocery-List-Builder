package src;


/**
 * Represents a grocery item with its properties
 * 
 * @author Henok Dawite & Arjun Sisodia
 */
public class Item {
    private String name;
    private String category;
    private boolean timeSensitive;
    private int lastPurchasedWeek;
    
    /**
     * Constructor for creating a grocery item
     * 
     * @param name The name of the item
     * @param category The category the item belongs to
     * @param timeSensitive Whether the item is time-sensitive
     */
    public Item(String name, String category, boolean timeSensitive) {
        this.name = name;
        this.category = category;
        this.timeSensitive = timeSensitive;
        this.lastPurchasedWeek = -1; // Not purchased yet
    }
    
    /**
     * Constructor with default values
     * 
     * @param name The name of the item
     */
    public Item(String name) {
        this(name, "Uncategorized", false);
    }
    
    /**
     * Gets the name of the item
     * 
     * @return The item name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the item
     * 
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the category of the item
     * 
     * @return The item category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the category of the item
     * 
     * @param category The new category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Checks if the item is time-sensitive
     * 
     * @return true if time-sensitive, false otherwise
     */
    public boolean isTimeSensitive() {
        return timeSensitive;
    }
    
    /**
     * Sets whether the item is time-sensitive
     * 
     * @param timeSensitive The time-sensitive flag
     */
    public void setTimeSensitive(boolean timeSensitive) {
        this.timeSensitive = timeSensitive;
    }
    
    /**
     * Gets the week when the item was last purchased
     * 
     * @return The week number
     */
    public int getLastPurchasedWeek() {
        return lastPurchasedWeek;
    }
    
    /**
     * Sets the week when the item was last purchased
     * 
     * @param week The week number
     */
    public void setLastPurchasedWeek(int week) {
        this.lastPurchasedWeek = week;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Item other = (Item) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return name;
    }
}