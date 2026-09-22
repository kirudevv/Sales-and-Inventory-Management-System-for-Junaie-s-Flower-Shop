package com.gplsadgroup.ui;

import com.gplsadgroup.model.Order;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.OrderRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DisputesFrame extends JFrame {

    private final OrderRepository orderRepository;
    private final User currentUser;

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private List<Order> orderList;

    public DisputesFrame(OrderRepository orderRepository, User currentUser) {
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Dispute Handling & Returns");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadOrdersData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel lblHeader = new JLabel("Customer Disputes & Order Returns Management", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Authorized Officer: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")", SwingConstants.CENTER);
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(lblHeader);
        headerPanel.add(lblUser);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Order ID", "Processed By", "Order Date", "Type", "Status", "Total Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Select an Order to Process Return or Flag Dispute"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnProcessReturn = new JButton("Process Full Refund / Return");
        JButton btnMarkDisputed = new JButton("Flag Under Dispute");
        JButton btnRefresh = new JButton("Refresh List");

        btnProcessReturn.setBackground(new Color(220, 53, 69));
        btnProcessReturn.setForeground(Color.WHITE);
        btnProcessReturn.setOpaque(true);

        bottomPanel.add(btnProcessReturn);
        bottomPanel.add(btnMarkDisputed);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadOrdersData());
        btnProcessReturn.addActionListener(e -> updateOrderStatus("REFUNDED_RETURN"));
        btnMarkDisputed.addActionListener(e -> updateOrderStatus("UNDER_DISPUTE"));
    }

    private void loadOrdersData() {
        try {
            tableModel.setRowCount(0);
            orderList = orderRepository.findAll();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Order o : orderList) {
                String cashierName = (o.getUser() != null) ? o.getUser().getFullName() : "Unknown";
                String dateStr = (o.getOrderDate() != null) ? o.getOrderDate().format(formatter) : "N/A";

                tableModel.addRow(new Object[]{
                        "#" + o.getOrderId(),
                        cashierName,
                        dateStr,
                        o.getOrderType(),
                        o.getOrderStatus(),
                        "₱" + o.getTotalAmount()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders for disputes: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderStatus(String newStatus) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the list first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Order selectedOrder = orderList.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to update Order #" + selectedOrder.getOrderId() + " status to '" + newStatus + "'?",
                "Confirm Dispute Action",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                selectedOrder.setOrderStatus(newStatus);
                orderRepository.save(selectedOrder);

                JOptionPane.showMessageDialog(this,
                        "Order #" + selectedOrder.getOrderId() + " status updated to " + newStatus + " in MySQL!",
                        "Status Updated",
                        JOptionPane.INFORMATION_MESSAGE);

                loadOrdersData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}