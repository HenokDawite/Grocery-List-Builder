package src;


/**
 * Represents a grocery item with its properties
 * 
 * @author Henok Dawite & Arjun Sisodia
 */
public class Item {
    // The name of the grocery item
    private String name;
    
    // The category the item belongs to (e.g., Produce, Dairy)
    private String category;
    
    // Whether the item is perishable/time-sensitive
    private boolean timeSensitive;
    
    // Week number when the item was most recently purchased
    private int lastPurchasedWeek;
    
    /**
     * Creates a new grocery item with all properties specified.
     * 
     * @param name The name of the grocery item
     * @param category The category the item belongs to (e.g., Produce, Dairy)
     * @param timeSensitive Whether the item has a short shelf life
     */
    public Item(String name, String category, boolean timeSensitive) {
        // Store the item name
        this.name = name;
        
        // Store the item category
        this.category = category;
        
        // Store the time-sensitivity flag
        this.timeSensitive = timeSensitive;
        
        // Initialize the last purchased week to -1 (not yet purchased)
        this.lastPurchasedWeek = -1;
    }
    
    /**
     * Creates a new grocery item with default values.
     * Sets category to "Uncategorized" and timeSensitive to false.
     * 
     * @param name The name of the grocery item
     */
    public Item(String name) {
        // Call the full constructor with default values
        this(name, "Uncategorized", false);
    }
    
    /**
     * Retrieves the name of the grocery item.
     * 
     * @return The item's name
     */
    public String getName() {
        // Return the stored name
        return name;
    }
    
    /**
     * Updates the name of the grocery item.
     * 
     * @param name The new name to assign
     */
    public void setName(String name) {
        // Update the stored name
        this.name = name;
    }
    
    /**
     * Retrieves the category the item belongs to.
     * 
     * @return The item's category
     */
    public String getCategory() {
        // Return the stored category
        return category;
    }
    
    /**
     * Updates the category of the grocery item.
     * 
     * @param category The new category to assign
     */
    public void setCategory(String category) {
        // Update the stored category
        this.category = category;
    }
    
    /**
     * Checks whether the item is time-sensitive (has a short shelf life).
     * 
     * @return true if the item is time-sensitive, false otherwise
     */
    public boolean isTimeSensitive() {
        // Return the time-sensitive flag
        return timeSensitive;
    }
    
    /**
     * Updates the time-sensitivity status of the item.
     * 
     * @param timeSensitive The new time-sensitive status
     */
    public void setTimeSensitive(boolean timeSensitive) {
        // Update the time-sensitive flag
        this.timeSensitive = timeSensitive;
    }
    
    /**
     * Retrieves the week number when the item was last purchased.
     * 
     * @return The week number, or -1 if never purchased
     */
    public int getLastPurchasedWeek() {
        // Return the stored last purchase week
        return lastPurchasedWeek;
    }
    
    /**
     * Updates the week when the item was last purchased.
     * 
     * @param week The week number of the most recent purchase
     */
    public void setLastPurchasedWeek(int week) {
        // Update the stored last purchase week
        this.lastPurchasedWeek = week;
    }
    
    /**
     * Compares this item with another object for equality.
     * Items are considered equal if they have the same name.
     * 
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        // Check if comparing to self
        if (this == obj) return true;
        
        // Check if comparing to null or different class
        if (obj == null || getClass() != obj.getClass()) return false;
        
        // Cast to Item for comparison
        Item other = (Item) obj;
        
        // Compare names for equality
        return name.equals(other.name);
    }
    
    /**
     * Generates a hash code for the item.
     * Uses the item's name as the basis for the hash code.
     * 
     * @return The hash code value
     */
    @Override
    public int hashCode() {
        // Generate hash code based on name
        return name.hashCode();
    }
    
    /**
     * Provides a string representation of the item.
     * Returns just the item's name for simplicity.
     * 
     * @return The string representation
     */
    @Override
    public String toString() {
        // Return name as the string representation
        return name;
    }
}