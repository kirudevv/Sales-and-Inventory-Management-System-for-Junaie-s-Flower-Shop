package com.gplsadgroup.ui;

import com.gplsadgroup.model.Supplier;
import com.gplsadgroup.model.User;
import com.gplsadgroup.repository.SupplierRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierFrame extends JFrame {

    private final SupplierRepository supplierRepository;
    private final User currentUser;

    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private List<Supplier> supplierList;

    public SupplierFrame(SupplierRepository supplierRepository, User currentUser) {
        this.supplierRepository = supplierRepository;
        this.currentUser = currentUser;

        setTitle("Junaie's Flower Shop - Supplier Management");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadSupplierData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel lblHeader = new JLabel("Supplier Directory & Management", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblUser = new JLabel("Logged in: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")", SwingConstants.CENTER);
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        headerPanel.add(lblHeader);
        headerPanel.add(lblUser);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "Supplier Name", "Contact Person", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnAddSupplier = new JButton("Add New Supplier");
        JButton btnEditSupplier = new JButton("Edit Selected Supplier");
        JButton btnRefresh = new JButton("Refresh List");

        bottomPanel.add(btnAddSupplier);
        bottomPanel.add(btnEditSupplier);
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadSupplierData());
        btnAddSupplier.addActionListener(e -> addSupplierUI());
        btnEditSupplier.addActionListener(e -> editSupplierUI());
    }

    private void loadSupplierData() {
        try {
            tableModel.setRowCount(0);
            supplierList = supplierRepository.findAll();

            for (Supplier s : supplierList) {
                tableModel.addRow(new Object[]{
                        s.getSupplierId(),
                        s.getSupplierName(),
                        s.getContactPerson(),
                        s.getPhone(),
                        s.getEmail(),
                        s.getAddress()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSupplierUI() {
        JTextField nameField = new JTextField();
        JTextField contactPersonField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        Object[] fields = {
            "Supplier Name:", nameField,
            "Contact Person:", contactPersonField,
            "Phone Number:", phoneField,
            "Email Address:", emailField,
            "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Register New Supplier", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Supplier Name and Phone are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Supplier supplier = new Supplier();
                supplier.setSupplierName(name);
                supplier.setContactPerson(contactPerson);
                supplier.setPhone(phone);
                supplier.setEmail(email);
                supplier.setAddress(address);

                supplierRepository.save(supplier);
                JOptionPane.showMessageDialog(this, "Supplier saved successfully to MySQL!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSupplierData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving supplier: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- EDIT / MODIFY FUNCTIONALITY ---
    private void editSupplierUI() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier from the table to modify.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long supplierId = Long.valueOf(tableModel.getValueAt(selectedRow, 0).toString());
        String currentName = tableModel.getValueAt(selectedRow, 1) != null ? tableModel.getValueAt(selectedRow, 1).toString() : "";
        String currentContact = tableModel.getValueAt(selectedRow, 2) != null ? tableModel.getValueAt(selectedRow, 2).toString() : "";
        String currentPhone = tableModel.getValueAt(selectedRow, 3) != null ? tableModel.getValueAt(selectedRow, 3).toString() : "";
        String currentEmail = tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString() : "";
        String currentAddress = tableModel.getValueAt(selectedRow, 5) != null ? tableModel.getValueAt(selectedRow, 5).toString() : "";

        JTextField nameField = new JTextField(currentName);
        JTextField contactPersonField = new JTextField(currentContact);
        JTextField phoneField = new JTextField(currentPhone);
        JTextField emailField = new JTextField(currentEmail);
        JTextField addressField = new JTextField(currentAddress);

        Object[] fields = {
            "Supplier Name:", nameField,
            "Contact Person:", contactPersonField,
            "Phone Number:", phoneField,
            "Email Address:", emailField,
            "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Modify Supplier Details (ID: " + supplierId + ")", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Supplier Name and Phone are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(
                    () -> new RuntimeException("Supplier not found with ID: " + supplierId)
                );

                supplier.setSupplierName(name);
                supplier.setContactPerson(contactPerson);
                supplier.setPhone(phone);
                supplier.setEmail(email);
                supplier.setAddress(address);

                supplierRepository.save(supplier);
                JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSupplierData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating supplier: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}