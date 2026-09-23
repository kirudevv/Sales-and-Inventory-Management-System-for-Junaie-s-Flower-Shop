package com.gplsadgroup.ui;

import com.gplsadgroup.model.Order;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.OrderRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {

    private final OrderRepository orderRepository;
    private final User currentUser;

    private JLabel lblTotalRevenue;
    private JLabel lblTotalOrders;
    private JLabel lblAverageOrder;
    private JTable salesHistoryTable;
    private DefaultTableModel tableModel;
    private JTabbedPane timeFilterTabs;

    public ReportPanel(OrderRepository orderRepository, User currentUser) {
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        initUI();
        generateSalesReport("DAILY");
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel lblHeader = new JLabel("Executive Sales & KPI Report", SwingConstants.LEFT);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Report Viewer: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")", SwingConstants.LEFT);
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(lblHeader);
        headerPanel.add(lblUser);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel with Time-based KPI Tabs
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        timeFilterTabs = new JTabbedPane();
        timeFilterTabs.addTab("Daily KPIs", null);
        timeFilterTabs.addTab("Weekly KPIs", null);
        timeFilterTabs.addTab("Monthly KPIs", null);
        timeFilterTabs.addTab("All-Time", null);

        timeFilterTabs.addChangeListener(e -> {
            int sel = timeFilterTabs.getSelectedIndex();
            switch (sel) {
                case 0 -> generateSalesReport("DAILY");
                case 1 -> generateSalesReport("WEEKLY");
                case 2 -> generateSalesReport("MONTHLY");
                default -> generateSalesReport("ALL");
            }
        });

        centerPanel.add(timeFilterTabs, BorderLayout.NORTH);

        // Metrics Summary Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Key Performance Indicators (KPIs)"));

        lblTotalRevenue = createMetricLabel("Total Revenue", "₱0.00", new Color(0, 128, 0));
        lblTotalOrders = createMetricLabel("Total Transactions", "0", Color.BLUE);
        lblAverageOrder = createMetricLabel("Avg. Order Value", "₱0.00", Color.DARK_GRAY);

        metricsPanel.add(lblTotalRevenue);
        metricsPanel.add(lblTotalOrders);
        metricsPanel.add(lblAverageOrder);

        JPanel contentBox = new JPanel(new BorderLayout(10, 10));
        contentBox.add(metricsPanel, BorderLayout.NORTH);

        // Sales History Table
        String[] columns = {"Order ID", "Cashier / Staff", "Date & Time", "Type", "Status", "Total Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesHistoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(salesHistoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Filtered Transaction Log"));
        contentBox.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(contentBox, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnRefresh = new JButton("Refresh Report");
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> generateSalesReport("DAILY"));
    }

    private JLabel createMetricLabel(String title, String value, Color color) {
        JLabel label = new JLabel("<html><center><b>" + title + "</b><br><font size='5' color='" + 
                toHexString(color) + "'>" + value + "</font></center></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEtchedBorder());
        return label;
    }

    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void generateSalesReport(String filterPeriod) {
        try {
            tableModel.setRowCount(0);
            List<Order> allOrders = orderRepository.findAll();
            LocalDateTime now = LocalDateTime.now();

            List<Order> filteredOrders = allOrders.stream().filter(o -> {
                if (o.getOrderDate() == null) return false;
                LocalDateTime oDate = o.getOrderDate();
                return switch (filterPeriod) {
                    case "DAILY" -> oDate.toLocalDate().isEqual(LocalDate.now());
                    case "WEEKLY" -> oDate.isAfter(now.minusDays(7));
                    case "MONTHLY" -> oDate.getMonth() == now.getMonth() && oDate.getYear() == now.getYear();
                    default -> true;
                };
            }).collect(Collectors.toList());

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalOrders = filteredOrders.size();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Order o : filteredOrders) {
                totalRevenue = totalRevenue.add(o.getTotalAmount());
                String cashierName = (o.getUser() != null) ? o.getUser().getFullName() : "Unknown";
                String dateStr = o.getOrderDate().format(formatter);

                tableModel.addRow(new Object[]{
                        "#" + o.getOrderId(),
                        cashierName,
                        dateStr,
                        o.getOrderType(),
                        o.getOrderStatus(),
                        "₱" + o.getTotalAmount()
                });
            }

            BigDecimal avgOrder = totalOrders > 0 
                    ? totalRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) 
                    : BigDecimal.ZERO;

            lblTotalRevenue.setText("<html><center><b>Total Revenue (" + filterPeriod + ")</b><br><font size='5' color='#008000'>₱" + String.format("%.2f", totalRevenue) + "</font></center></html>");
            lblTotalOrders.setText("<html><center><b>Total Transactions</b><br><font size='5' color='#0000FF'>" + totalOrders + "</font></center></html>");
            lblAverageOrder.setText("<html><center><b>Avg. Order Value</b><br><font size='5' color='#404040'>₱" + String.format("%.2f", avgOrder) + "</font></center></html>");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}