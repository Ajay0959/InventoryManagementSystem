import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// ==========================================
// 1. DATA MODELS (OOP)
// ==========================================
class Product {
    private String productId;
    private String name;
    private String category;
    private double price;
    private int quantity;

    public Product(String productId, String name, String category, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}

class Inventory {
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) { products.add(product); }
    public void removeProduct(String productId) {
        products.removeIf(p -> p.getProductId().equalsIgnoreCase(productId));
    }
    public List<Product> getAllProducts() { return products; }
    
    public Product findProductById(String productId) {
        return products.stream()
                .filter(p -> p.getProductId().equalsIgnoreCase(productId))
                .findFirst()
                .orElse(null);
    }
}

// ==========================================
// 2. MAIN GUI APPLICATION (Event-Driven & FP)
// ==========================================
public class InventoryApp extends JFrame {
    private Inventory inventory;
    private DefaultTableModel tableModel;
    private JTable table;
    
    // Form Input Fields
    private JTextField idField, nameField, categoryField, priceField, qtyField;
    // Checkout Fields
    private JTextField sellIdField, sellQtyField;

    public InventoryApp() {
        inventory = new Inventory();
        
        // Added realistic real-world electronics items
        inventory.addProduct(new Product("E001", "Wireless Mouse", "Accessories", 750.0, 15));
        inventory.addProduct(new Product("E002", "Mechanical Keyboard", "Accessories", 2999.0, 4)); // Low stock
        inventory.addProduct(new Product("E003", "USB-C Hub", "Connectivity", 1250.0, 8));
        inventory.addProduct(new Product("E004", "Gaming Headset", "Audio", 3499.0, 2)); // Low stock
        inventory.addProduct(new Product("E005", "Fast Wall Charger 20W", "Power", 899.0, 25));
        inventory.addProduct(new Product("E006", "Type-C Braided Cable", "Accessories", 399.0, 40));
        inventory.addProduct(new Product("E007", "Bluetooth Earbuds", "Audio", 1999.0, 12));
        inventory.addProduct(new Product("E008", "Portable Power Bank 10000mAh", "Power", 1599.0, 6));
        inventory.addProduct(new Product("E009", "Smart Fitness Band", "Wearables", 2499.0, 3)); // Low stock
        inventory.addProduct(new Product("E010", "External SSD 512GB", "Storage", 4500.0, 5));

        setTitle("Electronics Retail Inventory & Billing System - Assignment CSD207B");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- TOP: TABLE DISPLAY ---
        String[] columnNames = {"ID", "Name", "Category", "Price (₹)", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        refreshTable(inventory.getAllProducts());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- CENTER-SOUTH: FORMS & CONTROLS ---
        JPanel bottomContainer = new JPanel(new GridLayout(2, 1, 5, 5));

        // Form 1: Add Product & Delete Product
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Management"));

        idField = new JTextField();
        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        qtyField = new JTextField();

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Price (₹):"));
        formPanel.add(priceField);

        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(qtyField);

        JButton addButton = new JButton("Add Product");
        JButton deleteButton = new JButton("Delete Selected");
        formPanel.add(addButton);
        formPanel.add(deleteButton);

        // Form 2: Billing & Checkout + Functional Filters
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Billing & Functional Filters"));

        sellIdField = new JTextField(5);
        sellQtyField = new JTextField(3);
        JButton checkoutButton = new JButton("Process Bill / Checkout");
        
        JButton filterAudioBtn = new JButton("Filter: Audio");
        JButton lowStockBtn = new JButton("Low Stock Alert (<5)");
        JButton resetBtn = new JButton("Show All");

        actionPanel.add(new JLabel("Sell ID:"));
        actionPanel.add(sellIdField);
        actionPanel.add(new JLabel("Qty:"));
        actionPanel.add(sellQtyField);
        actionPanel.add(checkoutButton);
        actionPanel.add(filterAudioBtn);
        actionPanel.add(lowStockBtn);
        actionPanel.add(resetBtn);

        bottomContainer.add(formPanel);
        bottomContainer.add(actionPanel);
        add(bottomContainer, BorderLayout.SOUTH);

        // ==========================================
        // EVENT LISTENERS (Event-Driven Paradigm)
        // ==========================================
        
        // 1. Add Product Event
        addButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String cat = categoryField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());

                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                inventory.addProduct(new Product(id, name, cat, price, qty));
                refreshTable(inventory.getAllProducts());
                clearFields();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid numbers for Price and Quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 2. Delete Product Event
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String prodId = (String) tableModel.getValueAt(selectedRow, 0);
                inventory.removeProduct(prodId);
                refreshTable(inventory.getAllProducts());
                JOptionPane.showMessageDialog(this, "Product " + prodId + " deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row in the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 3. Billing & Checkout Event
        checkoutButton.addActionListener(e -> {
            try {
                String id = sellIdField.getText().trim();
                int buyQty = Integer.parseInt(sellQtyField.getText().trim());

                Product product = inventory.findProductById(id);
                if (product == null) {
                    JOptionPane.showMessageDialog(this, "Product ID not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (product.getQuantity() < buyQty) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + product.getQuantity(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double totalBill = product.getPrice() * buyQty;
                product.setQuantity(product.getQuantity() - buyQty);
                
                refreshTable(inventory.getAllProducts());
                sellIdField.setText("");
                sellQtyField.setText("");

                JOptionPane.showMessageDialog(this, 
                    "--- TAX INVOICE ---\n" +
                    "Item: " + product.getName() + "\n" +
                    "Quantity Sold: " + buyQty + "\n" +
                    "Total Amount: ₹" + totalBill + "\n" +
                    "----------------------\n" +
                    "Checkout Successful!", "Billing Receipt", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Product ID and Quantity to sell.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 4. Functional Filter Event: Audio
        filterAudioBtn.addActionListener(e -> {
            List<Product> filtered = inventory.getAllProducts().stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase("Audio"))
                    .collect(Collectors.toList());
            refreshTable(filtered);
        });

        // 5. Functional Filter Event: Low Stock (<5)
        lowStockBtn.addActionListener(e -> {
            List<Product> lowStock = inventory.getAllProducts().stream()
                    .filter(p -> p.getQuantity() < 5)
                    .collect(Collectors.toList());
            refreshTable(lowStock);
        });

        // 6. Reset Table Event
        resetBtn.addActionListener(e -> refreshTable(inventory.getAllProducts()));
    }

    // Helper method to update JTable rows
    private void refreshTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            Object[] row = {p.getProductId(), p.getName(), p.getCategory(), "₹" + p.getPrice(), p.getQuantity()};
            tableModel.addRow(row);
        }
    }

    // Helper to clear form inputs
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        qtyField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InventoryApp().setVisible(true);
        });
    }
}