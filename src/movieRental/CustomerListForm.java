package movieRental;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerListForm extends JFrame {
    private JTable customerTable;
    private JButton deleteButton, editButton;
    private JTextField searchField;
    private JButton searchButton;
    private DefaultTableModel tableModel;

    public CustomerListForm() {
        setTitle("Customer List");
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Top panel with search
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());
        topPanel.add(new JLabel("Search by name:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Name", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(tableModel);
        loadCustomerData();
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel();
        deleteButton = new JButton("Delete Selected Customer");
        deleteButton.addActionListener(e -> deleteCustomer());
        bottomPanel.add(deleteButton);

        editButton = new JButton("Edit Selected Customer");
        editButton.addActionListener(e -> editCustomer());
        bottomPanel.add(editButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadCustomerData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Customers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Customers WHERE name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow != -1) {
            int customerId = (int) tableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DBUtil.getConnection()) {
                // Check if this customer has any rentals
                String checkSql = "SELECT COUNT(*) FROM Rentals WHERE customer_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, customerId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int rentalCount = rs.getInt(1);

                if (rentalCount > 0) {
                    JOptionPane.showMessageDialog(this, "Cannot delete: Customer has existing rentals.");
                    return;
                }

                // Ask for confirmation
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String sql = "DELETE FROM Customers WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, customerId);
                    stmt.executeUpdate();

                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Customer deleted!");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
        }
    }


    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) tableModel.getValueAt(selectedRow, 2);
        String currentPhone = (String) tableModel.getValueAt(selectedRow, 3);

        JTextField nameField = new JTextField(currentName);
        JTextField emailField = new JTextField(currentEmail);
        JTextField phoneField = new JTextField(currentPhone);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Customer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE Customers SET name = ?, email = ?, phone = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, emailField.getText().trim());
                stmt.setString(3, phoneField.getText().trim());
                stmt.setInt(4, id);
                stmt.executeUpdate();

                // Update the table
                tableModel.setValueAt(nameField.getText(), selectedRow, 1);
                tableModel.setValueAt(emailField.getText(), selectedRow, 2);
                tableModel.setValueAt(phoneField.getText(), selectedRow, 3);

                JOptionPane.showMessageDialog(this, "Customer updated!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating customer.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomerListForm::new);
    }
}
