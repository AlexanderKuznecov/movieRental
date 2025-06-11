package movieRental;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RentalListForm extends JFrame {
    private JTable rentalTable;
    private JButton deleteButton;
    private DefaultTableModel tableModel;

    public RentalListForm() {
        setTitle("Rental List");
        setSize(500, 400);
        setLayout(new BorderLayout());

        // Create the table model
        String[] columnNames = {"ID", "Customer Name", "Movie Title", "Rent Date", "Return Date"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Create the JTable
        rentalTable = new JTable(tableModel);
        loadRentalData(); // load data into the table

        JScrollPane scrollPane = new JScrollPane(rentalTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add delete button
        deleteButton = new JButton("Delete Selected Rental");
        deleteButton.addActionListener(e -> deleteRental());
        add(deleteButton, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadRentalData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT r.id, c.name AS customer_name, m.title AS movie_title, r.rental_date, r.return_date "
                       + "FROM Rentals r "
                       + "JOIN Customers c ON r.customer_id = c.id "
                       + "JOIN Movies m ON r.movie_id = m.id "
                       + "WHERE r.return_date IS NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rentalId = rs.getInt("id");
                String customerName = rs.getString("customer_name");  // This should match the alias in the query
                String movieTitle = rs.getString("movie_title");      // This should match the alias in the query
                Date rentDate = rs.getDate("rental_date");
                Date returnDate = rs.getDate("return_date");

                // Add the data to your table model or any other UI component you're using
                tableModel.addRow(new Object[]{rentalId, customerName, movieTitle, rentDate, returnDate});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rental data.");
        }
    }


    private void deleteRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow != -1) {
            int rentalId = (int) tableModel.getValueAt(selectedRow, 0);

            // Ask for confirmation before deletion
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this rental?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBUtil.getConnection()) {
                    String sql = "DELETE FROM Rentals WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, rentalId);
                    stmt.executeUpdate();

                    // Remove the row from the table
                    tableModel.removeRow(selectedRow);

                    JOptionPane.showMessageDialog(this, "Rental deleted!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting rental.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a rental to delete.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RentalListForm::new);
    }
}
