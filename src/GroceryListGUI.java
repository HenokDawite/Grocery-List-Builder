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
        panel.setLayout(new GridLayout(6, 2));

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

        JScrollPane scrollPane = new JScrollPane(displayArea);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            String item = itemField.getText();
            String category = (String) categoryDropdown.getSelectedItem();
            try {
                int week = Integer.parseInt(weekField.getText());
                builder.addItem(item, week);
                builder.addCategory(item, category);
                displayArea.setText("Item added: " + item);
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
                String intervalStr = interval < 0 ? "n/a" : String.format("%.1f", interval);
                int lastWeek = builder.getLastPurchaseWeek(item);
                boolean timeSensitive = builder.isTimeSensitive(item);

                int weeksSinceLast = currentWeek - lastWeek;
                if (interval > 0 && weeksSinceLast < interval - 0.5) continue;

                explained.add(String.format("%s - %s (last bought: Week %d, interval: %s)",
                        item,
                        timeSensitive ? "Time-sensitive" : "Smart based on history",
                        lastWeek,
                        intervalStr));
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
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (isHeader) {
                            isHeader = false;
                            continue;
                        }
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String item = parts[0].trim();
                            int week = Integer.parseInt(parts[1].trim());
                            String category = parts[2].trim();
                            builder.addItem(item, week);
                            builder.addCategory(item, category);
                            count++;
                        }
                    }
                    displayArea.setText("Loaded " + count + " items from CSV.");
                } catch (Exception ex) {
                    displayArea.setText("Error loading CSV: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GroceryListGUI::new);
    }
}
