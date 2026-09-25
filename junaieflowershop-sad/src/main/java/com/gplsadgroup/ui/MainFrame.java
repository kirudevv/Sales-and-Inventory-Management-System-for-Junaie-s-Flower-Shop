package com.gplsadgroup.ui;

import com.gplsadgroup.model.Category;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.CategoryRepository;
import com.gplsadgroup.repository.OrderItemRepository;
import com.gplsadgroup.repository.OrderRepository;
import com.gplsadgroup.repository.ProductRepository;
import com.gplsadgroup.repository.SupplierRepository;
import com.gplsadgroup.service.AuthService;
import javax.swing.table.DefaultTableModel;
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

        // --- DEDICATED HEADER PANEL (Guarantees true center) ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(230, 65));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Junaie's POS", SwingConstants.CENTER);
        titleLabel.setFont(fontTitle);

        JLabel userLabel = new JLabel("<html><center>" + currentUser.getFullName() + "<br><font color='gray'>(" + role + ")</font></center></html>", SwingConstants.CENTER);
        userLabel.setFont(fontSubtitle);

        headerPanel.add(titleLabel);
        headerPanel.add(userLabel);

        navPanel.add(headerPanel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Define Navigation Buttons (No Numbering & Larger Font)
        JButton btnSales = createNavButton("Process Sales");
        JButton btnInventory = createNavButton("Inventory Management");
        JButton btnSuppliers = createNavButton("Manage Suppliers");
        JButton btnReports = createNavButton("Executive Reports");
        JButton btnDisputes = createNavButton("Handle Disputes");
        JButton btnManageUsers = createNavButton("User Accounts");
        JButton btnAddCategory = createNavButton("Categories");
        JButton btnLogout = createNavButton("Logout");

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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ==========================================
        // 1. TOP BAR: Title (Left) + Actions (Right)
        // ==========================================
        JPanel topBar = new JPanel(new BorderLayout(10, 10));

        JLabel header = new JLabel("User Accounts & Access Control", SwingConstants.LEFT);
        header.setFont(fontTitle);
        topBar.add(header, BorderLayout.WEST);

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnEditUser = new JButton("Edit Selected User");
        btnEditUser.setFont(fontButton);

        JButton btnCreate = new JButton("Create New System User");
        btnCreate.setFont(fontButton);

        topButtonPanel.add(btnEditUser);
        topButtonPanel.add(btnCreate);
        topBar.add(topButtonPanel, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER: Table showing Users & Roles
        // ==========================================
        String[] columns = {"ID", "Full Name", "Username", "Role"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only cells
            }
        };

        JTable userTable = new JTable(tableModel);
        userTable.setFont(fontLabel);
        userTable.setRowHeight(26);
        userTable.getTableHeader().setFont(fontLabel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Helper to fetch and populate users into the table
        Runnable loadUsers = () -> {
            tableModel.setRowCount(0);
            try {
                List<User> users = authService.getAllUsers();
                for (User u : users) {
                    tableModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getFullName(),
                        u.getUsername(),
                        u.getRole()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        // Initial table load
        loadUsers.run();

        // ==========================================
        // 3. CREATE USER ACTION
        // ==========================================
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
                    authService.createNewUser(
                        fullNameField.getText().trim(),
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()).trim(),
                        (String) roleCombo.getSelectedItem()
                    );
                    JOptionPane.showMessageDialog(this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh table to display newly registered user
                    loadUsers.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to create user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ==========================================
        // 4. EDIT / MODIFY USER ACTION
        // ==========================================
        btnEditUser.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a user from the table to modify.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long userId = Long.valueOf(tableModel.getValueAt(selectedRow, 0).toString());
            String currentFullName = tableModel.getValueAt(selectedRow, 1).toString();
            String currentUsername = tableModel.getValueAt(selectedRow, 2).toString();
            String currentRole = tableModel.getValueAt(selectedRow, 3).toString();

            JTextField fullNameField = new JTextField(currentFullName);
            JTextField usernameField = new JTextField(currentUsername);
            JPasswordField passwordField = new JPasswordField(); // Leave blank to keep existing
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "MANAGER", "OWNER"});
            roleCombo.setSelectedItem(currentRole.toUpperCase());

            Object[] fields = {
                "Full Name:", fullNameField,
                "Username:", usernameField,
                "New Password (leave empty to keep current):", passwordField,
                "Role:", roleCombo
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Modify User Account (ID: " + userId + ")", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String newPass = new String(passwordField.getPassword()).trim();
                    authService.updateUser(
                        userId,
                        fullNameField.getText().trim(),
                        usernameField.getText().trim(),
                        newPass.isEmpty() ? null : newPass,
                        (String) roleCombo.getSelectedItem()
                    );
                    JOptionPane.showMessageDialog(this, "User account updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to update user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ==========================================================
        // 1. TOP: Category Creation Form (GridBagLayout pinned to left)
        // ==========================================================
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        JLabel lblTitle = new JLabel("Add Product Category");
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

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblTitle, gbc);

        // Row 1: Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblName, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtName, gbc);

        // Row 2: Type
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblType, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtType, gbc);

        // Row 3: Button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnSave, gbc);

        // Horizontal spacer: prevents form fields from stretching across the entire width
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(Box.createHorizontalGlue(), gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // ==========================================================
        // 2. CENTER: Categories Table (Below the Form)
        // ==========================================================
        String[] columns = {"ID", "Category Name", "Category Type"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };

        JTable tableCategories = new JTable(tableModel);
        tableCategories.setFont(fontLabel);
        tableCategories.setRowHeight(26);
        tableCategories.getTableHeader().setFont(fontLabel);
        tableCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID Column visually from UI but keep it accessible for JPA queries
        tableCategories.getColumnModel().getColumn(0).setMinWidth(0);
        tableCategories.getColumnModel().getColumn(0).setMaxWidth(0);
        tableCategories.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(tableCategories);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Helper to fetch and populate category data
        Runnable loadCategories = () -> {
            tableModel.setRowCount(0);
            try {
                List<Category> list = categoryRepository.findAll();
                for (Category c : list) {
                    tableModel.addRow(new Object[]{
                        c.getCategoryId(),
                        c.getName(),
                        c.getType()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        // Initial load
        loadCategories.run();

        // ==========================================================
        // 3. BOTTOM: Actions (Edit Category Button)
        // ==========================================================
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEditCategory = new JButton("Edit Selected Category");
        btnEditCategory.setFont(fontButton);
        bottomActionPanel.add(btnEditCategory);
        panel.add(bottomActionPanel, BorderLayout.SOUTH);

        // ==========================================================
        // 4. ACTION LISTENERS
        // ==========================================================
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

                // Refresh table records immediately
                loadCategories.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEditCategory.addActionListener(e -> {
            int selectedRow = tableCategories.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a category from the table to modify.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long categoryId = Long.valueOf(tableModel.getValueAt(selectedRow, 0).toString());
            String currentName = tableModel.getValueAt(selectedRow, 1).toString();
            String currentType = tableModel.getValueAt(selectedRow, 2).toString();

            JTextField editNameField = new JTextField(currentName);
            JTextField editTypeField = new JTextField(currentType);

            Object[] fields = {
                "Category Name:", editNameField,
                "Category Type:", editTypeField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Edit Category (ID: " + categoryId + ")", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String updatedName = editNameField.getText().trim();
                    String updatedType = editTypeField.getText().trim();

                    if (updatedName.isEmpty() || updatedType.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Category cat = categoryRepository.findById(categoryId).orElseThrow(
                        () -> new RuntimeException("Category not found with ID: " + categoryId)
                    );

                    cat.setName(updatedName);
                    cat.setType(updatedType);
                    categoryRepository.save(cat);

                    JOptionPane.showMessageDialog(this, "Category updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategories.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to update category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }
}