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

    // Typography Configuration (Slightly larger fonts)
    private final Font fontTitle = new Font("Arial", Font.BOLD, 20);
    private final Font fontSubtitle = new Font("Arial", Font.ITALIC, 13);
    private final Font fontButton = new Font("Arial", Font.BOLD, 14);
    private final Font fontLabel = new Font("Arial", Font.PLAIN, 15);

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

        setTitle("Junaie's Flower Shop - POS & Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        String role = currentUser.getRole().toUpperCase();
        boolean isOwner = role.equals("OWNER") || role.equals("ADMIN");
        boolean isManager = role.equals("MANAGER");

        // --- LEFT NAVIGATION PANEL ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(260, 750));
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JLabel titleLabel = new JLabel("Junaie's POS");
        titleLabel.setFont(fontTitle);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("<html><center>" + currentUser.getFullName() + "<br><font color='gray'>(" + role + ")</font></center></html>");
        userLabel.setFont(fontSubtitle);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        navPanel.add(titleLabel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        navPanel.add(userLabel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Define Navigation Buttons (No Numbering & Larger Font)
        JButton btnSales = createNavButton("Process Sales");
        JButton btnInventory = createNavButton("Inventory Management");
        JButton btnSuppliers = createNavButton("Manage Suppliers");
        JButton btnReports = createNavButton("Executive Reports");
        JButton btnDisputes = createNavButton("Handle Disputes");
        JButton btnManageUsers = createNavButton("User Accounts");
        JButton btnAddCategory = createNavButton("Add Category");
        JButton btnLogout = createNavButton("Logout & Exit");

        // Dynamic Visibility: Hide restricted buttons completely
        navPanel.add(btnSales);
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        navPanel.add(btnInventory);
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        if (isOwner || isManager) {
            navPanel.add(btnSuppliers);
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        if (isOwner) {
            navPanel.add(btnReports);
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));

            navPanel.add(btnDisputes);
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));

            navPanel.add(btnManageUsers);
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        if (isOwner || isManager) {
            navPanel.add(btnAddCategory);
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        navPanel.add(Box.createVerticalGlue());
        navPanel.add(btnLogout);

        add(navPanel, BorderLayout.WEST);

        // --- RIGHT WORKSPACE PANEL (Single-Page CardLayout Area) ---
        cardLayout = new CardLayout();
        rightWorkspacePanel = new JPanel(cardLayout);

        // Instantiating embedded panels for all accessible features
        rightWorkspacePanel.add(createSalesPanel(), "SALES");
        rightWorkspacePanel.add(createInventoryPanel(), "INVENTORY");

        if (isOwner || isManager) {
            rightWorkspacePanel.add(createSuppliersPanel(), "SUPPLIERS");
        }

        if (isOwner) {
            rightWorkspacePanel.add(new ReportPanel(orderRepository, currentUser), "REPORTS");
            rightWorkspacePanel.add(createDisputesPanel(), "DISPUTES");
            rightWorkspacePanel.add(createUserManagementPanel(), "USERS");
        }

        if (isOwner || isManager) {
            rightWorkspacePanel.add(createCategoryPanel(), "CATEGORY");
        }

        // Set default landing page based on Role
        if (isOwner) {
            cardLayout.show(rightWorkspacePanel, "REPORTS"); // Default landing for Owner
        } else {
            cardLayout.show(rightWorkspacePanel, "SALES");   // Default landing for Staff & Manager
        }

        add(rightWorkspacePanel, BorderLayout.CENTER);

        // --- BUTTON ACTION LISTENERS (Card Switching without Pop-ups) ---
        btnSales.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "SALES"));
        btnInventory.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "INVENTORY"));
        btnSuppliers.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "SUPPLIERS"));
        btnReports.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "REPORTS"));
        btnDisputes.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "DISPUTES"));
        btnManageUsers.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "USERS"));
        btnAddCategory.addActionListener(e -> cardLayout.show(rightWorkspacePanel, "CATEGORY"));
        btnLogout.addActionListener(e -> System.exit(0));
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(fontButton);
        btn.setMaximumSize(new Dimension(230, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        return btn;
    }

    // --- EMBEDDED WORKSPACE PANELS ---

    private JPanel createSalesPanel() {
        SalesFrame salesFrame = new SalesFrame(productRepository, orderRepository, orderItemRepository, currentUser);
        return (JPanel) salesFrame.getContentPane();
    }

    private JPanel createInventoryPanel() {
        InventoryFrame inventoryFrame = new InventoryFrame(productRepository, categoryRepository, currentUser);
        return (JPanel) inventoryFrame.getContentPane();
    }

    private JPanel createSuppliersPanel() {
        SupplierFrame supplierFrame = new SupplierFrame(supplierRepository, currentUser);
        return (JPanel) supplierFrame.getContentPane();
    }

    private JPanel createDisputesPanel() {
        DisputesFrame disputesFrame = new DisputesFrame(orderRepository, currentUser);
        return (JPanel) disputesFrame.getContentPane();
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("User Accounts & Access Control", SwingConstants.LEFT);
        header.setFont(fontTitle);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnCreate = new JButton("Create New System User");
        JButton btnView = new JButton("View All Registered Users");
        btnCreate.setFont(fontButton);
        btnView.setFont(fontButton);

        content.add(btnCreate);
        content.add(btnView);
        panel.add(content, BorderLayout.CENTER);

        btnCreate.addActionListener(e -> {
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
                try {
                    authService.createNewUser(fullNameField.getText().trim(), usernameField.getText().trim(), new String(passwordField.getPassword()).trim(), (String) roleCombo.getSelectedItem());
                    JOptionPane.showMessageDialog(this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to create user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnView.addActionListener(e -> {
            List<User> users = authService.getAllUsers();
            StringBuilder sb = new StringBuilder("--- SYSTEM USERS ---\n\n");
            for (User u : users) {
                sb.append(String.format("ID: %d | Name: %s | Username: %s | Role: %s\n", u.getUserId(), u.getFullName(), u.getUsername(), u.getRole()));
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(fontLabel);
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "System Users", JOptionPane.PLAIN_MESSAGE);
        });

        return panel;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Add Product Category", SwingConstants.CENTER);
        lblTitle.setFont(fontTitle);

        JLabel lblName = new JLabel("Category Name:");
        lblName.setFont(fontLabel);
        JTextField txtName = new JTextField(20);
        txtName.setFont(fontLabel);

        JLabel lblType = new JLabel("Category Type:");
        lblType.setFont(fontLabel);
        JTextField txtType = new JTextField(20);
        txtType.setFont(fontLabel);

        JButton btnSave = new JButton("Save Category");
        btnSave.setFont(fontButton);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblName, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblType, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtType, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            try {
                if (txtName.getText().trim().isEmpty() || txtType.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please complete all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Category category = new Category();
                category.setName(txtName.getText().trim());
                category.setType(txtType.getText().trim());
                categoryRepository.save(category);

                JOptionPane.showMessageDialog(this, "Category saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtName.setText("");
                txtType.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }
}