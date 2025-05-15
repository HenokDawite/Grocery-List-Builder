package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * The graphical user interface for the Smart Grocery List Builder application.
 * Provides a visual way to interact with the GroceryListBuilderImpl functionality.
 * 
 * @author Henok Dawite & Arjun Sisodia
 */

public class GroceryListGUI {
    // The grocery list builder implementation that handles the business logic
    private GroceryListBuilderImpl builder;
    
    // The main application window
    private JFrame frame;
    
    // Text field for entering item names
    private JTextField itemField;
    
    // Text field for entering week numbers
    private JTextField weekField;
    
    // Dropdown for selecting item categories
    private JComboBox<String> categoryDropdown;
    
    // Text area for displaying results and messages
    private JTextArea displayArea;

    /**
     * Creates and initializes the grocery list application UI components.
     */
    public GroceryListGUI() {
        // Initialize the grocery list builder
        builder = new GroceryListBuilderImpl();
        
        // Create and configure the main application window
        frame = new JFrame("Smart Grocery List Builder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);

        // Create a panel for input controls with a grid layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 5, 5));  // 7 rows, 2 columns, 5px gaps

        // Initialize the text fields for user input
        itemField = new JTextField();
        weekField = new JTextField();
        
        // Initialize the category dropdown with predefined categories
        categoryDropdown = new JComboBox<>(new String[]{
            "Fruits",          // Categories for produce items
            "Vegetables", 
            "Dairy", 
            "Snacks", 
            "Meat", 
            "Beverages", 
            "Grains", 
            "Frozen Foods",
            "Canned Goods",
            "Bakery",
            "Deli",
            "Seafood",
            "Condiments"
        });
        
        // Initialize the display area for output
        displayArea = new JTextArea();
        displayArea.setEditable(false);  // Make it read-only

        // Create buttons for various actions
        JButton addButton = new JButton("Add Item");
        JButton viewFrequentButton = new JButton("View Frequent Items");
        JButton suggestButton = new JButton("Generate Smart Suggestions");
        JButton rotateButton = new JButton("Rotate Time-Sensitive Items");
        JButton filterButton = new JButton("Filter by Category");
        JButton loadButton = new JButton("Load CSV");
        JButton markTimeSensitiveButton = new JButton("Mark as Time-Sensitive");

        // Add labels and controls to the panel in order
        panel.add(new JLabel("Item Name:"));
        panel.add(itemField);
        panel.add(new JLabel("Week Number:"));
        panel.add(weekField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryDropdown);
        panel.add(addButton);
        panel.add(viewFrequentButton);
        panel.add(suggestButton);
        panel.add(rotateButton);
        panel.add(filterButton);
        panel.add(loadButton);
        panel.add(markTimeSensitiveButton);
        panel.add(new JLabel("")); // Empty label for spacing

        // Add scrolling capability to the display area
        JScrollPane scrollPane = new JScrollPane(displayArea);

        // Add the panel to the top of the frame and the scroll pane to the center
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Configure the Add Item button's action listener
        addButton.addActionListener(e -> {
            // Get the item name from the text field and remove whitespace
            String item = itemField.getText().trim();
            
            // Get the selected category from the dropdown
            String category = (String) categoryDropdown.getSelectedItem();
            
            try {
                // Validate that an item name was entered
                if (item.isEmpty()) {
                    displayArea.setText("Please enter an item name.");
                    return;
                }
                
                // Parse the week number from the text field
                int week = Integer.parseInt(weekField.getText());
                
                // Validate that the week number is positive
                if (week < 1) {
                    displayArea.setText("Week number must be positive.");
                    return;
                }
                
                // Add the item to the grocery list
                builder.addItem(item, week);
                
                // Assign the selected category to the item
                builder.addCategory(item, category);
                
                // Display confirmation message
                displayArea.setText("Item added: " + item);
                
                // Clear the item field for the next entry
                itemField.setText(""); 
            } catch (NumberFormatException ex) {
                // Handle case where week input is not a valid number
                displayArea.setText("Please enter a valid week number.");
            }
        });

        // Configure the View Frequent Items button's action listener
        viewFrequentButton.addActionListener(e -> {
            // Get the list of most frequently purchased items
            java.util.List<String> items = builder.getFrequentItems();
            
            // Display the list in the text area, one item per line
            displayArea.setText("Frequent Items (by count only):\n" + String.join("\n", items));
        });

        // Configure the Generate Suggestions button's action listener
        suggestButton.addActionListener(e -> {
            // Variable to store the parsed week number
            int currentWeek;
            
            try {
                // Parse the week number from the text field
                currentWeek = Integer.parseInt(weekField.getText());
                
                // Update the current week in the grocery list builder
                builder.setCurrentWeek(currentWeek);
            } catch (NumberFormatException ex) {
                // Handle case where week input is not a valid number
                displayArea.setText("Please enter a valid current week number first.");
                return;
            }

            // Get the raw list of suggestions based on patterns
            java.util.List<String> allSuggestions = builder.generateSuggestedList();
            
            // Create a list to store formatted suggestion explanations
            java.util.List<String> explained = new ArrayList<>();
            
            // Track items we've already processed to avoid duplicates
            Set<String> seen = new HashSet<>();
            
            // Counter to limit the number of suggestions shown
            int count = 0;
            
            // Process each suggestion to add explanations
            for (String item : allSuggestions) {
                // Skip if we've already seen this item
                if (seen.contains(item)) continue;
                
                // Mark this item as seen
                seen.add(item);

                // Calculate the average purchase interval
                double interval = builder.getAveragePurchaseInterval(item);
                
                // Format the interval for display
                String intervalStr;
                if (interval < 0) {
                    // Handle case where we don't have enough data for an interval
                    intervalStr = "n/a (new item)";
                } else {
                    // Format with one decimal place
                    intervalStr = String.format("%.1f weeks", interval);
                }
                
                // Get additional information about the item
                int lastWeek = builder.getLastPurchaseWeek(item);
                boolean timeSensitive = builder.isTimeSensitive(item);

                // Calculate how long it's been since the last purchase
                int weeksSinceLast = currentWeek - lastWeek;
                
                // Skip items that are not yet due based on their interval
                if (interval > 0 && weeksSinceLast < interval - 0.5) continue;

                // Determine the reason for suggesting this item
                String reason;
                if (timeSensitive) {
                    reason = "Time-sensitive item";
                } else if (interval < 0) {
                    reason = "New item";
                } else {
                    reason = String.format("Due based on %s average interval", intervalStr);
                }

                // Create a formatted explanation string for this suggestion
                explained.add(String.format("%s - %s (last bought: Week %d)",
                        item,
                        reason,
                        lastWeek));
                
                // Increment counter and limit to 10 suggestions
                count++;
                if (count >= 10) break;
            }

            // Display appropriate message based on whether we found suggestions
            if (explained.isEmpty()) {
                displayArea.setText("No items are due this week based on smart interval logic.");
            } else {
                displayArea.setText("Suggested List (Smart, Top 10):\n" + String.join("\n", explained));
            }
        });

        // Configure the Rotate Items button's action listener
        rotateButton.addActionListener(e -> {
            try {
                // Parse the week number from the text field
                int currentWeek = Integer.parseInt(weekField.getText());
                
                // Create a list to track which items were rotated
                java.util.List<String> rotated = new ArrayList<>();
                
                // Process perishable categories that commonly need rotation
                for (String category : new String[]{"Fruits", "Vegetables", "Dairy"}) {
                    // Get all items in this category
                    for (String item : builder.getItemsByCategory(category)) {
                        // Check if the item hasn't been purchased in at least 2 weeks
                        if (builder.getLastPurchaseWeek(item) <= currentWeek - 2) {
                            // Add the item to the current week to simulate repurchase
                            builder.addItem(item, currentWeek);
                            
                            // Track that this item was rotated
                            rotated.add(item);
                        }
                    }
                }
                
                // Display appropriate message based on whether items were rotated
                if (rotated.isEmpty()) {
                    displayArea.setText("No time-sensitive items needed rotation for week: " + currentWeek);
                } else {
                    displayArea.setText("Rotated items for week " + currentWeek + ":\n" + String.join("\n", rotated));
                }
            } catch (NumberFormatException ex) {
                // Handle case where week input is not a valid number
                displayArea.setText("Please enter a valid week number.");
            }
        });

        // Configure the Filter by Category button's action listener
        filterButton.addActionListener(e -> {
            // Get the selected category from the dropdown
            String category = (String) categoryDropdown.getSelectedItem();
            
            // Get all items in this category
            java.util.List<String> items = builder.getItemsByCategory(category);
            
            // Display the items in the text area
            displayArea.setText("Items in category '" + category + "':\n" + String.join("\n", items));
        });

        // Configure the Load CSV button's action listener
        loadButton.addActionListener(e -> {
            // Create a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            
            // Show the dialog and get the result
            int returnVal = fileChooser.showOpenDialog(frame);
            
            // Process the selected file if approved
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File file = fileChooser.getSelectedFile();
                
                // Initialize counters for tracking import results
                int count = 0;
                int errorCount = 0;
                
                // Create a buffer to collect error details
                StringBuilder errorDetails = new StringBuilder();
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Variable to store each line from the file
                    String line;
                    
                    // Flag to identify and skip the header row
                    boolean isHeader = true;
                    
                    // Track line numbers for error reporting
                    int lineNumber = 0;
                    
                    // Process the file line by line
                    while ((line = reader.readLine()) != null) {
                        // Increment line counter
                        lineNumber++;
                        
                        // Skip the header row
                        if (isHeader) {
                            isHeader = false;
                            continue;
                        }
                        
                        // Split the CSV line into parts using comma as delimiter
                        String[] parts = line.split(",");
                        
                        // Check if we have enough data (item, week, category)
                        if (parts.length >= 3) {
                            // Extract and clean the data fields
                            String item = parts[0].trim();
                            String weekStr = parts[1].trim();
                            String category = parts[2].trim();
                            
                            // Validate that an item name exists
                            if (item.isEmpty()) {
                                // Count and log the error
                                errorCount++;
                                errorDetails.append("Line ").append(lineNumber).append(": Empty item name\n");
                                continue;
                            }
                            
                            try {
                                // Parse the week string to an integer
                                int week = Integer.parseInt(weekStr);
                                
                                // Validate that the week number is positive
                                if (week < 1) {
                                    // Count and log the error
                                    errorCount++;
                                    errorDetails.append("Line ").append(lineNumber).append(": Invalid week number (").append(weekStr).append(")\n");
                                    continue;
                                }
                                
                                // Add the item to the grocery list
                                builder.addItem(item, week);
                                
                                // Assign the category to the item
                                builder.addCategory(item, category);
                                
                                // Increment successful import counter
                                count++;
                            } catch (NumberFormatException ex) {
                                // Count and log the error for invalid week format
                                errorCount++;
                                errorDetails.append("Line ").append(lineNumber).append(": Invalid week number format (").append(weekStr).append(")\n");
                            }
                        } else {
                            // Count and log the error for insufficient columns
                            errorCount++;
                            errorDetails.append("Line ").append(lineNumber).append(": Invalid format (expected 3 columns)\n");
                        }
                    }
                    
                    // Create the success message
                    String message = "Loaded " + count + " items from CSV.";
                    
                    // Add error details if any occurred
                    if (errorCount > 0) {
                        message += "\nSkipped " + errorCount + " invalid entries:\n" + errorDetails.toString();
                    }
                    
                    // Display the import results
                    displayArea.setText(message);
                } catch (FileNotFoundException ex) {
                    // Handle case where file doesn't exist
                    displayArea.setText("Error: File not found - " + ex.getMessage());
                } catch (IOException ex) {
                    // Handle other file reading errors
                    displayArea.setText("Error reading file: " + ex.getMessage());
                } catch (Exception ex) {
                    // Handle any other unexpected errors
                    displayArea.setText("Unexpected error: " + ex.getMessage());
                }
            }
        });

        // Configure the Mark as Time-Sensitive button's action listener
        markTimeSensitiveButton.addActionListener(e -> {
            // Get the item name from the text field
            String item = itemField.getText().trim();
            
            // Validate that an item name was entered
            if (item.isEmpty()) {
                displayArea.setText("Please enter an item name to mark as time-sensitive.");
                return;
            }
            
            // Check if the item exists in the system
            if (builder.getItemCategory(item) == null) {
                displayArea.setText("Item '" + item + "' not found. Please add the item first.");
                return;
            }
            
            // Mark the item as time-sensitive
            builder.markAsTimeSensitive(item);
            
            // Confirm the action to the user
            displayArea.setText("Item '" + item + "' marked as time-sensitive.");
        });

        // Make the frame visible
        frame.setVisible(true);
    }

    /**
     * Application entry point that launches the GUI on the EDT.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Schedule the GUI creation on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(GroceryListGUI::new);
    }
}
