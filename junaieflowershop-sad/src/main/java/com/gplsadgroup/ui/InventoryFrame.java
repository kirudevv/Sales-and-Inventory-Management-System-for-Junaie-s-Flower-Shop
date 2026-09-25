package com.gplsadgroup.ui;

import com.gplsadgroup.model.Category;
import com.gplsadgroup.model.Product;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.CategoryRepository;
import com.gplsadgroup.repository.ProductRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class InventoryFrame extends JFrame {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final User currentUser;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private List<Product> productList;

    public InventoryFrame(ProductRepository productRepository, CategoryRepository categoryRepository, User currentUser) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Inventory Management");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadInventoryData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel lblHeader = new JLabel("Stock & Inventory Management", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Logged in: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")", SwingConstants.CENTER);
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(lblHeader);
        headerPanel.add(lblUser);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "Code", "Product Name", "Selling Price", "Stock Quantity", "Status", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Highlight the entire row in bold red when stock hits reorder level (<= 5 or Out of Stock)
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                Object stockVal = table.getValueAt(row, 4);
                String stockStr = (stockVal != null) ? stockVal.toString() : "";

                boolean isReorderLevel = stockStr.contains("LOW STOCK") || stockStr.contains("OUT OF STOCK");

                if (isReorderLevel) {
                    c.setForeground(new Color(205, 32, 38)); // Deep warning red
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnAddProduct = new JButton("Add New Product");
        JButton btnEditProduct = new JButton("Edit Selected Product");
        JButton btnRestock = new JButton("Restock Product");
        JButton btnRefresh = new JButton("Refresh Stock");
        JButton btnUpdateStatus = new JButton("Toggle Product Status");

        String role = currentUser.getRole().toUpperCase();
        boolean canManage = role.equals("OWNER") || role.equals("ADMIN") || role.equals("MANAGER");

        // Management-only operations
        btnAddProduct.setEnabled(canManage);
        btnEditProduct.setEnabled(canManage);
        btnUpdateStatus.setEnabled(canManage);

        // All users (including STAFF) can restock items
        btnRestock.setEnabled(true);

        if (!canManage) {
            btnAddProduct.setToolTipText("Restricted to Manager and Owner");
            btnEditProduct.setToolTipText("Restricted to Manager and Owner");
            btnUpdateStatus.setToolTipText("Restricted to Manager and Owner");
        }

        bottomPanel.add(btnAddProduct);
        bottomPanel.add(btnEditProduct);
        bottomPanel.add(btnRestock);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnUpdateStatus);

        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadInventoryData());
        btnUpdateStatus.addActionListener(e -> toggleProductStatus());
        btnAddProduct.addActionListener(e -> addProductUI());
        btnEditProduct.addActionListener(e -> editProductUI());
        btnRestock.addActionListener(e -> restockProductUI());
    }

    private void loadInventoryData() {
        try {
            tableModel.setRowCount(0);
            productList = productRepository.findAll();

            for (Product p : productList) {
                String catName = (p.getCategory() != null) ? p.getCategory().getName() : "Unassigned";
                int stock = p.getQuantityInStock() != null ? p.getQuantityInStock() : 0;
                
                String stockDisplay;
                if (stock <= 0) {
                    stockDisplay = stock + " ❌ (OUT OF STOCK)";
                } else if (p.isLowStock(5)) { // Reorder threshold is 5
                    stockDisplay = stock + " ⚠️ (LOW STOCK)";
                } else {
                    stockDisplay = String.valueOf(stock);
                }

                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getProductCode(),
                        p.getProductName(),
                        "₱" + p.getSellingPrice(),
                        stockDisplay,
                        p.getStatus(),
                        catName
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductUI() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one Category first (Option 7 in Main Menu)!", "No Categories Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField("50");
        JComboBox<Category> categoryCombo = new JComboBox<>(categories.toArray(new Category[0]));
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });

        Object[] fields = {
            "Product Code (e.g., FLW-002):", codeField,
            "Product Name:", nameField,
            "Selling Price (₱):", priceField,
            "Initial Stock Quantity:", stockField,
            "Category:", categoryCombo
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Register New Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            Category selectedCategory = (Category) categoryCombo.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceStr);
                int initialStock = Integer.parseInt(stockStr);

                Product product = new Product();
                product.setProductCode(code);
                product.setProductName(name);
                product.setSellingPrice(price);
                product.setQuantityInStock(initialStock);
                product.setCategory(selectedCategory);

                productRepository.save(product);
                JOptionPane.showMessageDialog(this, "Product registered successfully in MySQL!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price or stock quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProductUI() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to modify.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = productList.get(selectedRow);

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No categories available.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField codeField = new JTextField(selectedProduct.getProductCode());
        JTextField nameField = new JTextField(selectedProduct.getProductName());
        JTextField priceField = new JTextField(selectedProduct.getSellingPrice() != null ? selectedProduct.getSellingPrice().toString() : "");
        
        JComboBox<Category> categoryCombo = new JComboBox<>(categories.toArray(new Category[0]));
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });

        // Set matching current category in combo box
        if (selectedProduct.getCategory() != null) {
            for (Category c : categories) {
                if (c.getCategoryId().equals(selectedProduct.getCategory().getCategoryId())) {
                    categoryCombo.setSelectedItem(c);
                    break;
                }
            }
        }

        Object[] fields = {
            "Product Code:", codeField,
            "Product Name:", nameField,
            "Selling Price (₱):", priceField,
            "Category:", categoryCombo
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Modify Product Details (ID: " + selectedProduct.getProductId() + ")", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            Category chosenCategory = (Category) categoryCombo.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Code, Name, and Selling Price are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                BigDecimal updatedPrice = new BigDecimal(priceStr);

                selectedProduct.setProductCode(code);
                selectedProduct.setProductName(name);
                selectedProduct.setSellingPrice(updatedPrice);
                selectedProduct.setCategory(chosenCategory);

                productRepository.save(selectedProduct);
                JOptionPane.showMessageDialog(this, "Product details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid selling price format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restockProductUI() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to restock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = productList.get(selectedRow);
        String qtyStr = JOptionPane.showInputDialog(this, 
                "Enter additional quantity received for " + selectedProduct.getProductName() + ":", 
                "Restock Product", 
                JOptionPane.PLAIN_MESSAGE);

        if (qtyStr != null && !qtyStr.trim().isEmpty()) {
            try {
                int addQty = Integer.parseInt(qtyStr.trim());
                if (addQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int newTotal = (selectedProduct.getQuantityInStock() != null ? selectedProduct.getQuantityInStock() : 0) + addQty;
                selectedProduct.setQuantityInStock(newTotal);

                productRepository.save(selectedProduct);
                JOptionPane.showMessageDialog(this, "Restocked successfully! New quantity: " + newTotal, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving restock: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleProductStatus() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = productList.get(selectedRow);
        String currentStatus = selectedProduct.getStatus();
        String newStatus = "AVAILABLE".equalsIgnoreCase(currentStatus) ? "OUT_OF_STOCK" : "AVAILABLE";

        selectedProduct.setStatus(newStatus);
        productRepository.save(selectedProduct);

        JOptionPane.showMessageDialog(this,
                "Updated status for " + selectedProduct.getProductName() + " to " + newStatus,
                "Status Updated",
                JOptionPane.INFORMATION_MESSAGE);

        loadInventoryData();
    }
}