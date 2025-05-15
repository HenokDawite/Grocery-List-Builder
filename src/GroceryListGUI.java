package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class GroceryListGUI {
    private GroceryListBuilderImpl builder;
    private JFrame frame;
    private JTextField itemField;
    private JTextField weekField;
    private JComboBox<String> categoryDropdown;
    private JTextArea displayArea;

    public GroceryListGUI() {
        builder = new GroceryListBuilderImpl();
        frame = new JFrame("Smart Grocery List Builder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 5, 5));  // Added gaps between components

        itemField = new JTextField();
        weekField = new JTextField();
        categoryDropdown = new JComboBox<>(new String[]{"Fruits", "Vegetables", "Dairy", "Snacks", "Meat", "Beverages", "Grains", "Other"});
        displayArea = new JTextArea();
        displayArea.setEditable(false);

        JButton addButton = new JButton("Add Item");
        JButton viewFrequentButton = new JButton("View Frequent Items");
        JButton suggestButton = new JButton("Generate Smart Suggestions");
        JButton rotateButton = new JButton("Rotate Time-Sensitive Items");
        JButton filterButton = new JButton("Filter by Category");
        JButton loadButton = new JButton("Load CSV");
        JButton markTimeSensitiveButton = new JButton("Mark as Time-Sensitive");

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

        JScrollPane scrollPane = new JScrollPane(displayArea);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            String item = itemField.getText().trim();
            String category = (String) categoryDropdown.getSelectedItem();
            try {
                if (item.isEmpty()) {
                    displayArea.setText("Please enter an item name.");
                    return;
                }
                int week = Integer.parseInt(weekField.getText());
                if (week < 1) {
                    displayArea.setText("Week number must be positive.");
                    return;
                }
                builder.addItem(item, week);
                builder.addCategory(item, category);
                displayArea.setText("Item added: " + item);
                itemField.setText(""); // Clear the field after successful addition
            } catch (NumberFormatException ex) {
                displayArea.setText("Please enter a valid week number.");
            }
        });

        viewFrequentButton.addActionListener(e -> {
            java.util.List<String> items = builder.getFrequentItems();
            displayArea.setText("Frequent Items (by count only):\n" + String.join("\n", items));
        });

        suggestButton.addActionListener(e -> {
            int currentWeek;
            try {
                currentWeek = Integer.parseInt(weekField.getText());
                builder.setCurrentWeek(currentWeek);
            } catch (NumberFormatException ex) {
                displayArea.setText("Please enter a valid current week number first.");
                return;
            }

            java.util.List<String> allSuggestions = builder.generateSuggestedList();
            java.util.List<String> explained = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            int count = 0;
            for (String item : allSuggestions) {
                if (seen.contains(item)) continue;
                seen.add(item);

                double interval = builder.getAveragePurchaseInterval(item);
                String intervalStr;
                if (interval < 0) {
                    intervalStr = "n/a (new item)";
                } else {
                    intervalStr = String.format("%.1f weeks", interval);
                }
                
                int lastWeek = builder.getLastPurchaseWeek(item);
                boolean timeSensitive = builder.isTimeSensitive(item);

                int weeksSinceLast = currentWeek - lastWeek;
                if (interval > 0 && weeksSinceLast < interval - 0.5) continue;

                String reason;
                if (timeSensitive) {
                    reason = "Time-sensitive item";
                } else if (interval < 0) {
                    reason = "New item";
                } else {
                    reason = String.format("Due based on %s average interval", intervalStr);
                }

                explained.add(String.format("%s - %s (last bought: Week %d)",
                        item,
                        reason,
                        lastWeek));
                count++;
                if (count >= 10) break;
            }

            if (explained.isEmpty()) {
                displayArea.setText("No items are due this week based on smart interval logic.");
            } else {
                displayArea.setText("Suggested List (Smart, Top 10):\n" + String.join("\n", explained));
            }
        });

        rotateButton.addActionListener(e -> {
            try {
                int currentWeek = Integer.parseInt(weekField.getText());
                java.util.List<String> rotated = new ArrayList<>();
                for (String category : new String[]{"Fruits", "Vegetables", "Dairy"}) {
                    for (String item : builder.getItemsByCategory(category)) {
                        if (builder.getLastPurchaseWeek(item) <= currentWeek - 2) {
                            builder.addItem(item, currentWeek);
                            rotated.add(item);
                        }
                    }
                }
                if (rotated.isEmpty()) {
                    displayArea.setText("No time-sensitive items needed rotation for week: " + currentWeek);
                } else {
                    displayArea.setText("Rotated items for week " + currentWeek + ":\n" + String.join("\n", rotated));
                }
            } catch (NumberFormatException ex) {
                displayArea.setText("Please enter a valid week number.");
            }
        });

        filterButton.addActionListener(e -> {
            String category = (String) categoryDropdown.getSelectedItem();
            java.util.List<String> items = builder.getItemsByCategory(category);
            displayArea.setText("Items in category '" + category + "':\n" + String.join("\n", items));
        });

        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                int count = 0;
                int errorCount = 0;
                StringBuilder errorDetails = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isHeader = true;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        if (isHeader) {
                            isHeader = false;
                            continue;
                        }
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String item = parts[0].trim();
                            String weekStr = parts[1].trim();
                            String category = parts[2].trim();
                            
                            if (item.isEmpty()) {
                                errorCount++;
                                errorDetails.append("Line ").append(lineNumber).append(": Empty item name\n");
                                continue;
                            }
                            
                            try {
                                int week = Integer.parseInt(weekStr);
                                if (week < 1) {
                                    errorCount++;
                                    errorDetails.append("Line ").append(lineNumber).append(": Invalid week number (").append(weekStr).append(")\n");
                                    continue;
                                }
                                builder.addItem(item, week);
                                builder.addCategory(item, category);
                                count++;
                            } catch (NumberFormatException ex) {
                                errorCount++;
                                errorDetails.append("Line ").append(lineNumber).append(": Invalid week number format (").append(weekStr).append(")\n");
                            }
                        } else {
                            errorCount++;
                            errorDetails.append("Line ").append(lineNumber).append(": Invalid format (expected 3 columns)\n");
                        }
                    }
                    String message = "Loaded " + count + " items from CSV.";
                    if (errorCount > 0) {
                        message += "\nSkipped " + errorCount + " invalid entries:\n" + errorDetails.toString();
                    }
                    displayArea.setText(message);
                } catch (FileNotFoundException ex) {
                    displayArea.setText("Error: File not found - " + ex.getMessage());
                } catch (IOException ex) {
                    displayArea.setText("Error reading file: " + ex.getMessage());
                } catch (Exception ex) {
                    displayArea.setText("Unexpected error: " + ex.getMessage());
                }
            }
        });

        markTimeSensitiveButton.addActionListener(e -> {
            String item = itemField.getText().trim();
            if (item.isEmpty()) {
                displayArea.setText("Please enter an item name to mark as time-sensitive.");
                return;
            }
            
            // Check if item exists in the system
            if (builder.getItemCategory(item) == null) {
                displayArea.setText("Item '" + item + "' not found. Please add the item first.");
                return;
            }
            
            builder.markAsTimeSensitive(item);
            displayArea.setText("Item '" + item + "' marked as time-sensitive.");
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GroceryListGUI::new);
    }
}
