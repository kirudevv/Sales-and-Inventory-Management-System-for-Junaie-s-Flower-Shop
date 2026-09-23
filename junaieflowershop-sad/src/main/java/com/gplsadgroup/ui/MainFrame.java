package com.gplsadgroup.ui;

import com.gplsadgroup.model.Category;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.CategoryRepository;
import com.gplsadgroup.repository.OrderItemRepository;
import com.gplsadgroup.repository.OrderRepository;
import com.gplsadgroup.repository.ProductRepository;
import com.gplsadgroup.repository.SupplierRepository;
import com.gplsadgroup.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SupplierRepository supplierRepository;
    private final AuthService authService;
    private final User currentUser;

    private JPanel rightWorkspacePanel;
    private CardLayout cardLayout;

    public MainFrame(CategoryRepository categoryRepository,
                     ProductRepository productRepository,
                     OrderRepository orderRepository,
                     OrderItemRepository orderItemRepository,
                     SupplierRepository supplierRepository,
                     AuthService authService,
                     User currentUser) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.supplierRepository = supplierRepository;
        this.authService = authService;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Sales & Inventory System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- LEFT NAVIGATION PANEL ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(10, 1, 5, 5));
        navPanel.setPreferredSize(new Dimension(240, 700));
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));

        JLabel titleLabel = new JLabel("Junaie's POS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel userLabel = new JLabel("<html><center>" + currentUser.getFullName() + "<br><font color='gray'>(" + currentUser.getRole() + ")</font></center></html>", SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        navPanel.add(titleLabel);
        navPanel.add(userLabel);

        // Buttons (Without Numbering)
        JButton btnProcessSales = new JButton("Process Sales");
        JButton btnInventory = new JButton("Inventory Management");
        JButton btnSuppliers = new JButton("Manage Suppliers");
        JButton btnReports = new JButton("Executive Reports (KPI)");
        JButton btnDisputes = new JButton("Handle Disputes");
        JButton btnManageUsers = new JButton("User Accounts");
        JButton btnAddCategory = new JButton("Add Category");
        JButton btnExit = new JButton("Logout & Exit");

        String role = currentUser.getRole().toUpperCase();
        boolean isOwner = role.equals("OWNER") || role.equals("ADMIN");
        boolean isManager = role.equals("MANAGER");

        btnManageUsers.setEnabled(isOwner);
        btnReports.setEnabled(isOwner);
        btnDisputes.setEnabled(isOwner);
        btnSuppliers.setEnabled(isOwner || isManager);

        navPanel.add(btnProcessSales);
        navPanel.add(btnInventory);
        navPanel.add(btnSuppliers);
        navPanel.add(btnReports);
        navPanel.add(btnDisputes);
        navPanel.add(btnManageUsers);
        navPanel.add(btnAddCategory);
        navPanel.add(btnExit);

        add(navPanel, BorderLayout.WEST);

        // --- RIGHT WORKSPACE PANEL (CardLayout) ---
        cardLayout = new CardLayout();
        rightWorkspacePanel = new JPanel(cardLayout);

        // Load Views into Workspace Cards
        ReportPanel reportPanel = new ReportPanel(orderRepository, currentUser);
        rightWorkspacePanel.add(reportPanel, "REPORTS");

        // Set default display based on role
        if (isOwner) {
            cardLayout.show(rightWorkspacePanel, "REPORTS"); // Default to KPI for Owner
        } else {
            openSalesWindow(); // Launch POS Sales module for Staff / Manager
        }

        add(rightWorkspacePanel, BorderLayout.CENTER);

        // --- BUTTON ACTION LISTENERS ---
        btnProcessSales.addActionListener(e -> openSalesWindow());

        btnInventory.addActionListener(e -> {
            InventoryFrame inventoryFrame = new InventoryFrame(productRepository, categoryRepository, currentUser);
            inventoryFrame.setVisible(true);
        });

        btnSuppliers.addActionListener(e -> {
            SupplierFrame supplierFrame = new SupplierFrame(supplierRepository, currentUser);
            supplierFrame.setVisible(true);
        });

        btnReports.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "REPORTS"));

        btnDisputes.addActionListener(e -> {
            DisputesFrame disputesFrame = new DisputesFrame(orderRepository, currentUser);
            disputesFrame.setVisible(true);
        });

        btnManageUsers.addActionListener(e -> manageUsersUI());
        btnAddCategory.addActionListener(e -> addCategoryUI());
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void openSalesWindow() {
        SalesFrame salesFrame = new SalesFrame(productRepository, orderRepository, orderItemRepository, currentUser);
        salesFrame.setVisible(true);
    }

    private void manageUsersUI() {
        String[] options = {"Create New User", "View All Users", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select a User Management Action:",
                "User Management",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            JTextField fullNameField = new JTextField();
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "MANAGER", "OWNER"});

            Object[] fields = {
                "Full Name:", fullNameField,
                "Username:", usernameField,
                "Password:", passwordField,
                "Role:", roleCombo
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Create New User Account", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String fullName = fullNameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String role = (String) roleCombo.getSelectedItem();

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    authService.createNewUser(fullName, username, password, role);
                    JOptionPane.showMessageDialog(this, "User " + username + " (" + role + ") created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to create user: Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == 1) {
            List<User> users = authService.getAllUsers();
            StringBuilder sb = new StringBuilder("--- SYSTEM USERS ---\n\n");
            for (User u : users) {
                sb.append(String.format("ID: %d | Name: %s | Username: %s | Role: %s\n",
                        u.getUserId(), u.getFullName(), u.getUsername(), u.getRole()));
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 250));

            JOptionPane.showMessageDialog(this, scrollPane, "All Registered Users", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void addCategoryUI() {
        try {
            String name = JOptionPane.showInputDialog(this, "Enter Category Name (e.g., Fresh Flowers):");
            if (name == null || name.trim().isEmpty()) return;

            String type = JOptionPane.showInputDialog(this, "Enter Category Type (e.g., Raw Material):");
            if (type == null || type.trim().isEmpty()) return;

            Category category = new Category();
            category.setName(name);
            category.setType(type);

            categoryRepository.save(category);
            JOptionPane.showMessageDialog(this, "SUCCESS: Category saved to MySQL!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}