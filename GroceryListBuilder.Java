import java.util.*;

/**
 * GroceryListBuilder Interface
 * This interface defines the core methods for the Grocery List Builder final project.
 *
 * @author Henok Dawite & Arjun Sisodia
 */
public interface GroceryListBuilder {

    /**
     * Adds an item to the grocery list for a given week.
     * @param item - The item to be added.
     * @param week - The week number for the item.
     */
    void addItem(String item, int week);

    /**
     * Returns the most frequently bought items.
     * @return List of frequent items.
     */
    List<String> getFrequentItems();

    /**
     * Generates a suggested list for the next shopping trip.
     * @return List of suggested items.
     */
    List<String> generateSuggestedList();

    /**
     * Rotates time-sensitive items.
     * @param currentWeek - The current week number.
     */
    void rotateItems(int currentWeek);

}
