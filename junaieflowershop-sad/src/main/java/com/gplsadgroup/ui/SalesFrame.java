package com.gplsadgroup.ui;

import com.gplsadgroup.model.Order;
import com.gplsadgroup.model.OrderItem;
import com.gplsadgroup.model.Product;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.OrderItemRepository;
import com.gplsadgroup.repository.OrderRepository;
import com.gplsadgroup.repository.ProductRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

    // Catalog Table Components
    private JTable catalogTable;
    private DefaultTableModel catalogModel;
    private JSpinner spinQuantity;
    private List<Product> catalogProductList;

    // Cart Components
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotal;
    private JLabel lblChange;
    private JComboBox<String> comboPayment;
    private JTextField txtCash;

    private final List<OrderItem> cartItems = new ArrayList<>();
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // Fonts
    private final Font fontTitle = new Font("Arial", Font.BOLD, 16);
    private final Font fontBoldLarge = new Font("Arial", Font.BOLD, 18);
    private final Font fontRegular = new Font("Arial", Font.PLAIN, 14);

    public SalesFrame(ProductRepository productRepository,
                      OrderRepository orderRepository,
                      OrderItemRepository orderItemRepository,
                      User currentUser) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Point of Sale");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadCatalogData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Main Container Grid (1 Row, 2 Columns)
        JPanel workspacePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        workspacePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ==========================================
        // LEFT PANEL: FLOWER SHOP PRODUCT CATALOG
        // ==========================================
        JPanel leftCatalogPanel = new JPanel(new BorderLayout(10, 10));
        leftCatalogPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Product Catalog & Stock", 0, 0, fontTitle, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] catalogCols = {"ID", "Item Name", "Available Stock", "Price (₱)"};
        catalogModel = new DefaultTableModel(catalogCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        catalogTable = new JTable(catalogModel);
        catalogTable.setFont(fontRegular);
        catalogTable.setRowHeight(25);

        // Highlight low stock items in red
        catalogTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object stockObj = table.getValueAt(row, 2);
                if (stockObj != null) {
                    int stock = Integer.parseInt(stockObj.toString());
                    if (stock <= 5) {
                        c.setForeground(Color.RED);
                        c.setFont(fontRegular.deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        c.setFont(fontRegular);
                    }
                }
                return c;
            }
        });

        leftCatalogPanel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        // Catalog Controls (Bottom)
        JPanel catalogControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinQuantity.setFont(fontRegular);

        JButton btnAddToCart = new JButton("Add Selected to Cart");
        btnAddToCart.setFont(fontRegular);

        catalogControlPanel.add(new JLabel("Quantity:"));
        catalogControlPanel.add(spinQuantity);
        catalogControlPanel.add(btnAddToCart);

        leftCatalogPanel.add(catalogControlPanel, BorderLayout.SOUTH);

        // ==========================================
        // RIGHT PANEL: ACTIVE ORDER CART
        // ==========================================
        JPanel rightCartPanel = new JPanel(new BorderLayout(10, 10));
        rightCartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Active Order Cart", 0, 0, fontTitle, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] cartCols = {"Product", "Qty", "Unit Price", "Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(fontRegular);
        cartTable.setRowHeight(25);

        rightCartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Checkout & Payment Panel (Bottom)
        JPanel checkoutContainer = new JPanel();
        checkoutContainer.setLayout(new BoxLayout(checkoutContainer, BoxLayout.Y_AXIS));

        JButton btnRemoveItem = new JButton("Remove Selected Item");
        btnRemoveItem.setFont(fontRegular);

        lblTotal = new JLabel("Grand Total: ₱0.00");
        lblTotal.setFont(fontBoldLarge);
        lblTotal.setForeground(new Color(0, 128, 0));

        lblChange = new JLabel("Change: ₱0.00");
        lblChange.setFont(fontRegular);

        JPanel paymentDetails = new JPanel(new GridLayout(3, 2, 5, 5));
        paymentDetails.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        comboPayment = new JComboBox<>(new String[]{"Cash", "GCash", "Credit/Debit Card"});
        comboPayment.setFont(fontRegular);

        txtCash = new JTextField();
        txtCash.setFont(fontRegular);

        paymentDetails.add(new JLabel("Payment Type:"));
        paymentDetails.add(comboPayment);
        paymentDetails.add(new JLabel("Cash Tendered (₱):"));
        paymentDetails.add(txtCash);
        paymentDetails.add(lblChange);

        JButton btnCheckout = new JButton("Complete Checkout & Print");
        btnCheckout.setFont(fontBoldLarge);
        btnCheckout.setPreferredSize(new Dimension(200, 40));

        checkoutContainer.add(btnRemoveItem);
        checkoutContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        checkoutContainer.add(lblTotal);
        checkoutContainer.add(paymentDetails);
        checkoutContainer.add(btnCheckout);

        rightCartPanel.add(checkoutContainer, BorderLayout.SOUTH);

        // Add Both Panels
        workspacePanel.add(leftCatalogPanel);
        workspacePanel.add(rightCartPanel);

        add(workspacePanel, BorderLayout.CENTER);

        // --- LISTENERS ---
        btnAddToCart.addActionListener(e -> addToCart());
        btnRemoveItem.addActionListener(e -> removeFromCart());
        btnCheckout.addActionListener(e -> processCheckout());
    }

    private void loadCatalogData() {
        try {
            catalogModel.setRowCount(0);
            catalogProductList = productRepository.findAll();

            for (Product p : catalogProductList) {
                if ("AVAILABLE".equalsIgnoreCase(p.getStatus())) {
                    catalogModel.addRow(new Object[]{
                            p.getProductCode(),
                            p.getProductName(),
                            p.getQuantityInStock() != null ? p.getQuantityInStock() : 0,
                            "₱" + p.getSellingPrice()
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading product catalog: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the Catalog!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = catalogProductList.get(selectedRow);
        int qty = (int) spinQuantity.getValue();
        int availableStock = selectedProduct.getQuantityInStock() != null ? selectedProduct.getQuantityInStock() : 0;

        int cartQty = 0;
        for (OrderItem item : cartItems) {
            if (item.getProduct().getProductId().equals(selectedProduct.getProductId())) {
                cartQty += item.getQuantity();
            }
        }

        if ((cartQty + qty) > availableStock) {
            JOptionPane.showMessageDialog(this, "Insufficient stock available! Remaining: " + availableStock, "Stock Exceeded", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal unitPrice = selectedProduct.getSellingPrice();
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(qty));

        OrderItem item = new OrderItem();
        item.setProduct(selectedProduct);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(subtotal);

        cartItems.add(item);

        cartModel.addRow(new Object[]{
                selectedProduct.getProductName(),
                qty,
                "₱" + unitPrice,
                "₱" + subtotal
        });

        grandTotal = grandTotal.add(subtotal);
        lblTotal.setText(String.format("Grand Total: ₱%.2f", grandTotal));
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the Cart to remove!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrderItem itemToRemove = cartItems.get(selectedRow);
        grandTotal = grandTotal.subtract(itemToRemove.getSubtotal());

        cartItems.remove(selectedRow);
        cartModel.removeRow(selectedRow);

        lblTotal.setText(String.format("Grand Total: ₱%.2f", grandTotal));
        lblChange.setText("Change: ₱0.00");
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The active cart is empty!", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cashStr = txtCash.getText().trim();
        if (cashStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Cash Tendered amount!", "Payment Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal cashTendered = new BigDecimal(cashStr);
            if (cashTendered.compareTo(grandTotal) < 0) {
                JOptionPane.showMessageDialog(this, "Insufficient payment tendered!", "Payment Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal change = cashTendered.subtract(grandTotal);
            lblChange.setText(String.format("Change: ₱%.2f", change));

            // Save Order Header
            Order order = new Order();
            order.setUser(currentUser);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(grandTotal);
            order.setOrderStatus("COMPLETED");
            order.setOrderType("POS_" + comboPayment.getSelectedItem().toString().toUpperCase());

            Order savedOrder = orderRepository.save(order);

            // Deduct Stock and Save Items
            List<String> lowStockAlerts = new ArrayList<>();
            for (OrderItem item : cartItems) {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);

                Product p = item.getProduct();
                int newStock = p.getQuantityInStock() - item.getQuantity();
                p.setQuantityInStock(newStock);
                productRepository.save(p);

                if (p.isLowStock(5)) {
                    lowStockAlerts.add(p.getProductName() + " (Remaining stock: " + newStock + ")");
                }
            }

            // Receipt Summary
            JOptionPane.showMessageDialog(this,
                    String.format("Transaction Completed!\n\nOrder ID: #%d\nTotal Amount: ₱%.2f\nCash Paid: ₱%.2f\nChange: ₱%.2f",
                            savedOrder.getOrderId(), grandTotal, cashTendered, change),
                    "Receipt", JOptionPane.INFORMATION_MESSAGE);

            // Low Stock Alert
            if (!lowStockAlerts.isEmpty()) {
                StringBuilder alertMsg = new StringBuilder("⚠️ LOW STOCK ALERT WARNING:\n\n");
                for (String msg : lowStockAlerts) {
                    alertMsg.append("• ").append(msg).append("\n");
                }
                alertMsg.append("\nPlease reorder these items from the supplier!");
                JOptionPane.showMessageDialog(this, alertMsg.toString(), "Low Stock Warning", JOptionPane.WARNING_MESSAGE);
            }

            // Reset UI
            cartItems.clear();
            cartModel.setRowCount(0);
            grandTotal = BigDecimal.ZERO;
            lblTotal.setText("Grand Total: ₱0.00");
            lblChange.setText("Change: ₱0.00");
            txtCash.setText("");
            loadCatalogData();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for Cash Tendered.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing checkout: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}