package movieRental;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReturnMovieForm extends JFrame {
    private JTable rentalTable;
    private DefaultTableModel model;
    private JButton returnButton, deleteButton, searchButton, editButton;
    private JTextField searchField;

    public ReturnMovieForm() {
        setTitle("Return Rented Movie");
        setSize(700, 500);
        setLayout(new BorderLayout());

     // Top search panel with separate fields
        JPanel topPanel = new JPanel(new FlowLayout());

        JTextField customerSearchField = new JTextField(15);
        JTextField movieSearchField = new JTextField(15);

        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchRentals(customerSearchField.getText(), movieSearchField.getText()));

        topPanel.add(new JLabel("Customer:"));
        topPanel.add(customerSearchField);
        topPanel.add(new JLabel("Movie:"));
        topPanel.add(movieSearchField);
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);


        model = new DefaultTableModel(new String[]{"Rental ID", "Customer", "Movie", "Rent Date", "Return Date"}, 0);
        rentalTable = new JTable(model);
        loadRentedMovies();
        add(new JScrollPane(rentalTable), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel();

        returnButton = new JButton("Return Movie");
        returnButton.addActionListener(e -> returnSelectedMovie());
        bottomPanel.add(returnButton);

        deleteButton = new JButton("Delete Rental");
        deleteButton.addActionListener(e -> deleteRental());
        bottomPanel.add(deleteButton);

        editButton = new JButton("Edit Return Date");
        editButton.addActionListener(e -> editReturnDate());
        bottomPanel.add(editButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadRentedMovies() {
        model.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = """
                SELECT r.id, c.name, m.title, r.rental_date, r.return_date
                FROM Rentals r
                JOIN Customers c ON r.customer_id = c.id
                JOIN Movies m ON r.movie_id = m.id
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getDate("rental_date"),
                    rs.getDate("return_date")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchRentals(String customerKeyword, String movieKeyword) {
        model.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = """
                SELECT r.id, c.name, m.title, r.rental_date, r.return_date
                FROM Rentals r
                JOIN Customers c ON r.customer_id = c.id
                JOIN Movies m ON r.movie_id = m.id
                WHERE c.name LIKE ? AND m.title LIKE ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + customerKeyword.trim() + "%");
            stmt.setString(2, "%" + movieKeyword.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getDate("rental_date"),
                    rs.getDate("return_date")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void returnSelectedMovie() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a rental to return.");
            return;
        }

        int rentalId = (int) model.getValueAt(selectedRow, 0);

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE Rentals SET return_date = CURRENT_DATE WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rentalId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Movie returned successfully.");
            loadRentedMovies();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error returning movie.");
        }
    }

    private void deleteRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a rental to delete.");
            return;
        }

        int rentalId = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this rental?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "DELETE FROM Rentals WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, rentalId);
                stmt.executeUpdate();

                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Rental deleted!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting rental.");
            }
        }
    }

    private void editReturnDate() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a rental to edit.");
            return;
        }

        int rentalId = (int) model.getValueAt(selectedRow, 0);
        String currentDate = String.valueOf(model.getValueAt(selectedRow, 4));

        String newDate = JOptionPane.showInputDialog(this, "Enter new return date (YYYY-MM-DD):", currentDate);
        if (newDate == null || newDate.trim().isEmpty()) return;

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE Rentals SET return_date = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(newDate.trim()));
            stmt.setInt(2, rentalId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Return date updated!");
            loadRentedMovies();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid date format or update failed.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReturnMovieForm::new);
    }
}
