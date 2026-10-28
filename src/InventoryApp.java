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

class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return product.getPrice() * quantity; }
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
    private List<CartItem> cart;
    
    // Inventory Table Components
    private DefaultTableModel inventoryTableModel;
    private JTable inventoryTable;
    private JTextField idField, nameField, categoryField, priceField, qtyField;
    private JComboBox<String> categoryComboBox;

    // Billing / Cart Components
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JTextField cartIdField, cartQtyField;
    private JLabel grandTotalLabel;

    public InventoryApp() {
        inventory = new Inventory();
        cart = new ArrayList<>();
        
        // Realistic electronics sample data
        inventory.addProduct(new Product("E001", "Wireless Mouse", "Accessories", 750.0, 15));
        inventory.addProduct(new Product("E002", "Mechanical Keyboard", "Accessories", 2999.0, 4));
        inventory.addProduct(new Product("E003", "USB-C Hub", "Connectivity", 1250.0, 8));
        inventory.addProduct(new Product("E004", "Gaming Headset", "Audio", 3499.0, 2));
        inventory.addProduct(new Product("E005", "Fast Wall Charger 20W", "Power", 899.0, 25));
        inventory.addProduct(new Product("E006", "Type-C Braided Cable", "Accessories", 399.0, 40));
        inventory.addProduct(new Product("E007", "Bluetooth Earbuds", "Audio", 1999.0, 12));
        inventory.addProduct(new Product("E008", "Portable Power Bank 10000mAh", "Power", 1599.0, 6));
        inventory.addProduct(new Product("E009", "Smart Fitness Band", "Wearables", 2499.0, 3));
        inventory.addProduct(new Product("E010", "External SSD 512GB", "Storage", 4500.0, 5));

        setTitle("Electronics Retail Inventory & Multi-Item Billing System");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- TABBED INTERFACE ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Inventory Management & Filters", createInventoryPanel());
        tabbedPane.addTab("POS & Multi-Item Billing", createBillingPanel());

        add(tabbedPane);
    }

    // ==========================================
    // TAB 1: INVENTORY MANAGEMENT PANEL
    // ==========================================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        String[] columnNames = {"ID", "Name", "Category", "Price (₹)", "Quantity"};
        inventoryTableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(inventoryTableModel);
        refreshInventoryTable(inventory.getAllProducts());
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Controls & Forms
        JPanel southContainer = new JPanel(new GridLayout(2, 1, 5, 5));

        // Add/Delete Form
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Operations"));

        idField = new JTextField();
        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        qtyField = new JTextField();

        formPanel.add(new JLabel("ID:")); formPanel.add(idField);
        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Category:")); formPanel.add(categoryField);
        formPanel.add(new JLabel("Price (₹):")); formPanel.add(priceField);
        formPanel.add(new JLabel("Quantity:")); formPanel.add(qtyField);

        JButton addButton = new JButton("Add Product");
        JButton deleteButton = new JButton("Delete Selected");
        formPanel.add(addButton);
        formPanel.add(deleteButton);

        // Functional Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Options"));

        String[] categories = {"Accessories", "Audio", "Power", "Wearables", "Storage", "Connectivity"};
        categoryComboBox = new JComboBox<>(categories);
        JButton filterCategoryBtn = new JButton("Filter Category");
        JButton lowStockBtn = new JButton("Low Stock Alert (<5)");
        JButton resetBtn = new JButton("Show All Products");

        filterPanel.add(categoryComboBox);
        filterPanel.add(filterCategoryBtn);
        filterPanel.add(lowStockBtn);
        filterPanel.add(resetBtn);

        southContainer.add(formPanel);
        southContainer.add(filterPanel);
        panel.add(southContainer, BorderLayout.SOUTH);

        // --- Event Listeners for Tab 1 ---
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
                refreshInventoryTable(inventory.getAllProducts());
                clearFields();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid numbers for Price and Quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow != -1) {
                String prodId = (String) inventoryTableModel.getValueAt(selectedRow, 0);
                inventory.removeProduct(prodId);
                refreshInventoryTable(inventory.getAllProducts());
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        filterCategoryBtn.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            List<Product> filtered = inventory.getAllProducts().stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(selectedCategory))
                    .collect(Collectors.toList());
            refreshInventoryTable(filtered);
        });

        lowStockBtn.addActionListener(e -> {
            List<Product> lowStock = inventory.getAllProducts().stream()
                    .filter(p -> p.getQuantity() < 5)
                    .collect(Collectors.toList());
            refreshInventoryTable(lowStock);
        });

        resetBtn.addActionListener(e -> refreshInventoryTable(inventory.getAllProducts()));

        return panel;
    }

    // ==========================================
    // TAB 2: POS & MULTI-ITEM BILLING PANEL
    // ==========================================
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Cart Table Display
        String[] cartColumns = {"Product ID", "Item Name", "Price (₹)", "Quantity", "Total (₹)"};
        cartTableModel = new DefaultTableModel(cartColumns, 0);
        cartTable = new JTable(cartTableModel);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // North: Add to Cart Form
        JPanel addCartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addCartPanel.setBorder(BorderFactory.createTitledBorder("Add Item to Current Cart / Basket"));

        cartIdField = new JTextField(6);
        cartQtyField = new JTextField(3);
        JButton addToCartButton = new JButton("Add to Cart");
        JButton removeFromCartButton = new JButton("Remove Selected Item");

        addCartPanel.add(new JLabel("Product ID:"));
        addCartPanel.add(cartIdField);
        addCartPanel.add(new JLabel("Quantity:"));
        addCartPanel.add(cartQtyField);
        addCartPanel.add(addToCartButton);
        addCartPanel.add(removeFromCartButton);

        panel.add(addCartPanel, BorderLayout.NORTH);

        // South: Checkout & Grand Total
        JPanel checkoutPanel = new JPanel(new BorderLayout());
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        grandTotalLabel = new JLabel("Grand Total: ₹0.0", JLabel.RIGHT);
        grandTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton checkoutButton = new JButton("Complete Checkout & Generate Multi-Item Bill");
        checkoutButton.setBackground(new Color(46, 139, 87));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 14));

        checkoutPanel.add(grandTotalLabel, BorderLayout.NORTH);
        checkoutPanel.add(checkoutButton, BorderLayout.SOUTH);

        panel.add(checkoutPanel, BorderLayout.SOUTH);

        // --- Event Listeners for Tab 2 ---
        addToCartButton.addActionListener(e -> {
            try {
                String id = cartIdField.getText().trim();
                int qty = Integer.parseInt(cartQtyField.getText().trim());

                Product product = inventory.findProductById(id);
                if (product == null) {
                    JOptionPane.showMessageDialog(this, "Product ID not found in inventory!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (product.getQuantity() < qty) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + product.getQuantity(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add to cart list
                cart.add(new CartItem(product, qty));
                refreshCartTable();
                cartIdField.setText("");
                cartQtyField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Product ID and Quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        removeFromCartButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                cart.remove(selectedRow);
                refreshCartTable();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        checkoutButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty! Add items before checkout.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StringBuilder invoice = new StringBuilder("========== TAX INVOICE (MULTI-ITEM) ==========\n");
            double grandTotal = 0;

            // Deduct stock and build invoice
            for (CartItem item : cart) {
                Product p = item.getProduct();
                int buyQty = item.getQuantity();

                // Deduct from inventory stock
                p.setQuantity(p.getQuantity() - buyQty);

                double itemTotal = item.getTotalPrice();
                grandTotal += itemTotal;

                invoice.append(String.format("- %s (x%d) : ₹%.2f\n", p.getName(), buyQty, itemTotal));
            }

            invoice.append("----------------------------------------------\n");
            invoice.append(String.format("GRAND TOTAL: ₹%.2f\n", grandTotal));
            invoice.append("==============================================\n");
            invoice.append("Checkout Successful! Stock updated.");

            JOptionPane.showMessageDialog(this, invoice.toString(), "Multi-Item Bill Receipt", JOptionPane.INFORMATION_MESSAGE);

            // Clear cart & refresh both tables
            cart.clear();
            refreshCartTable();
            refreshInventoryTable(inventory.getAllProducts());
        });

        return panel;
    }

    // Helper to refresh Inventory table
    private void refreshInventoryTable(List<Product> products) {
        inventoryTableModel.setRowCount(0);
        for (Product p : products) {
            Object[] row = {p.getProductId(), p.getName(), p.getCategory(), "₹" + p.getPrice(), p.getQuantity()};
            inventoryTableModel.addRow(row);
        }
    }

    // Helper to refresh Cart table & calculate grand total
    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        double grandTotal = 0;
        for (CartItem item : cart) {
            Product p = item.getProduct();
            double total = item.getTotalPrice();
            grandTotal += total;
            Object[] row = {p.getProductId(), p.getName(), "₹" + p.getPrice(), item.getQuantity(), "₹" + total};
            cartTableModel.addRow(row);
        }
        grandTotalLabel.setText("Grand Total: ₹" + grandTotal);
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