package com.gplsadgroup.ui;

import com.gplsadgroup.model.Order;
import com.gplsadgroup.model.OrderItem;
import com.gplsadgroup.model.Product;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.OrderItemRepository;
import com.gplsadgroup.repository.OrderRepository;
import com.gplsadgroup.repository.ProductRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesFrame extends JFrame {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final User currentUser;

    private JComboBox<Product> comboProducts;
    private JSpinner spinQuantity;
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalAmount;
    private JLabel lblChange;
    private JTextField txtCash;

    private final List<OrderItem> cartItems = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public SalesFrame(ProductRepository productRepository,
                      OrderRepository orderRepository,
                      OrderItemRepository orderItemRepository,
                      User currentUser) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Process Sales (POS Checkout)");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel lblHeader = new JLabel("POS Register / New Sale", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Cashier: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")", SwingConstants.CENTER);
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(lblHeader);
        headerPanel.add(lblUser);
        add(headerPanel, BorderLayout.NORTH);

        // Selection & Cart Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        comboProducts = new JComboBox<>();
        comboProducts.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product) {
                    Product p = (Product) value;
                    setText(p.getProductCode() + " - " + p.getProductName() + " (₱" + p.getSellingPrice() + ") | Stock: " + p.getQuantityInStock());
                }
                return this;
            }
        });

        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnAddToCart = new JButton("Add to Cart");

        addPanel.add(new JLabel("Select Product:"));
        addPanel.add(comboProducts);
        addPanel.add(new JLabel("Qty:"));
        addPanel.add(spinQuantity);
        addPanel.add(btnAddToCart);

        centerPanel.add(addPanel, BorderLayout.NORTH);

        // Cart Table
        String[] columns = {"Product Code", "Product Name", "Unit Price", "Qty", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(tableModel);
        centerPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Payment & Actions Panel
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel paymentPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        lblTotalAmount = new JLabel("Total: ₱0.00");
        lblTotalAmount.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalAmount.setForeground(new Color(0, 128, 0));

        txtCash = new JTextField();
        lblChange = new JLabel("Change: ₱0.00");
        lblChange.setFont(new Font("Arial", Font.BOLD, 14));

        paymentPanel.add(lblTotalAmount);
        paymentPanel.add(new JLabel(""));
        paymentPanel.add(new JLabel("Cash Tendered (₱):"));
        paymentPanel.add(txtCash);
        paymentPanel.add(lblChange);
        paymentPanel.add(new JLabel(""));

        JButton btnCheckout = new JButton("Complete Sale");
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 14));

        southPanel.add(paymentPanel, BorderLayout.CENTER);
        southPanel.add(btnCheckout, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        btnAddToCart.addActionListener(e -> addToCart());
        btnCheckout.addActionListener(e -> completeSale());
    }

    private void loadProducts() {
        try {
            comboProducts.removeAllItems();
            List<Product> products = productRepository.findAll();
            for (Product p : products) {
                if ("AVAILABLE".equalsIgnoreCase(p.getStatus())) {
                    comboProducts.addItem(p);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        Product selectedProduct = (Product) comboProducts.getSelectedItem();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid product.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) spinQuantity.getValue();
        int availableStock = selectedProduct.getQuantityInStock() != null ? selectedProduct.getQuantityInStock() : 0;

        // Check stock availability
        int currentCartQty = 0;
        for (OrderItem item : cartItems) {
            if (item.getProduct().getProductId().equals(selectedProduct.getProductId())) {
                currentCartQty += item.getQuantity();
            }
        }

        if ((currentCartQty + qty) > availableStock) {
            JOptionPane.showMessageDialog(this,
                    "Insufficient stock available for " + selectedProduct.getProductName() + "!\nAvailable: " + availableStock + " | Currently in Cart: " + currentCartQty,
                    "Stock Exceeded",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal unitPrice = selectedProduct.getSellingPrice();
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(qty));

        OrderItem item = new OrderItem();
        item.setProduct(selectedProduct);
        item.setQuantity(qty);
        item.setSubtotal(subtotal);
        item.setUnitPrice(unitPrice);

        cartItems.add(item);

        tableModel.addRow(new Object[]{
                selectedProduct.getProductCode(),
                selectedProduct.getProductName(),
                "₱" + unitPrice,
                qty,
                "₱" + subtotal
        });

        totalAmount = totalAmount.add(subtotal);
        lblTotalAmount.setText(String.format("Total: ₱%.2f", totalAmount));
    }

    private void completeSale() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "The cart is empty! Add products before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cashStr = txtCash.getText().trim();
        if (cashStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the cash tendered amount.", "Payment Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal cashTendered = new BigDecimal(cashStr);
            if (cashTendered.compareTo(totalAmount) < 0) {
                JOptionPane.showMessageDialog(this, "Insufficient cash tendered!", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verify stock limits before persistence
            for (OrderItem item : cartItems) {
                Product product = item.getProduct();
                int requestedQty = item.getQuantity();
                int currentStock = product.getQuantityInStock() != null ? product.getQuantityInStock() : 0;

                if (currentStock < requestedQty) {
                    JOptionPane.showMessageDialog(this,
                            "Insufficient stock for " + product.getProductName() + "!\nAvailable: " + currentStock + ", Requested: " + requestedQty,
                            "Stock Limit Reached",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            BigDecimal change = cashTendered.subtract(totalAmount);

            // Save Order Header
            Order order = new Order();
            order.setUser(currentUser);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(totalAmount);
            order.setOrderStatus("COMPLETED");
            order.setOrderType("POS_SALE");

            Order savedOrder = orderRepository.save(order);

            // Save Line Items & Deduct Inventory Stock
            List<String> lowStockAlerts = new ArrayList<>();

            for (OrderItem item : cartItems) {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);

                // Smart Inventory Update
                Product product = item.getProduct();
                int updatedStock = product.getQuantityInStock() - item.getQuantity();
                product.setQuantityInStock(updatedStock);
                productRepository.save(product);

                // Low Stock Detection (Threshold: <= 5 units)
                if (product.isLowStock(5)) {
                    lowStockAlerts.add(product.getProductName() + " (Remaining Stock: " + updatedStock + ")");
                }
            }

            lblChange.setText(String.format("Change: ₱%.2f", change));

            // Receipt Confirmation
            JOptionPane.showMessageDialog(this,
                    String.format("Transaction Saved & Inventory Deducted!\n\nOrder ID: #%d\nTotal Paid: ₱%.2f\nCash Received: ₱%.2f\nChange: ₱%.2f",
                            savedOrder.getOrderId(), totalAmount, cashTendered, change),
                    "Receipt",
                    JOptionPane.INFORMATION_MESSAGE);

            // Low Stock Notification
            if (!lowStockAlerts.isEmpty()) {
                StringBuilder alertMsg = new StringBuilder("⚠️ LOW STOCK ALERT WARNING:\n\n");
                for (String alert : lowStockAlerts) {
                    alertMsg.append("• ").append(alert).append("\n");
                }
                alertMsg.append("\nPlease reorder these items with suppliers soon!");

                JOptionPane.showMessageDialog(this, alertMsg.toString(), "Low Stock Notification", JOptionPane.WARNING_MESSAGE);
            }

            // Reset UI State
            tableModel.setRowCount(0);
            cartItems.clear();
            totalAmount = BigDecimal.ZERO;
            lblTotalAmount.setText("Total: ₱0.00");
            lblChange.setText("Change: ₱0.00");
            txtCash.setText("");
            loadProducts();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid cash amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error saving sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}