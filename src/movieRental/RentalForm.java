package movieRental;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class RentalForm extends JFrame {
    private JComboBox<String> customerComboBox, movieComboBox;
    private JButton rentButton;

    public RentalForm() {
        setTitle("Rent Movie");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Customer:"));
        customerComboBox = new JComboBox<>();
        add(customerComboBox);

        add(new JLabel("Movie:"));
        movieComboBox = new JComboBox<>();
        add(movieComboBox);

        rentButton = new JButton("Rent");
        add(rentButton);
        add(new JLabel()); // filler

        rentButton.addActionListener(e -> rentMovie());

        // Load customers and movies
        loadCustomers();
        loadMovies();

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadCustomers() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT name FROM Customers";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customerComboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers.");
        }
    }

    private void loadMovies() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT title FROM Movies";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movieComboBox.addItem(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading movies.");
        }
    }

    private void rentMovie() {
        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        String selectedMovie = (String) movieComboBox.getSelectedItem();

        try (Connection conn = DBUtil.getConnection()) {
            String customerSql = "SELECT id FROM Customers WHERE name = ?";
            PreparedStatement customerStmt = conn.prepareStatement(customerSql);
            customerStmt.setString(1, selectedCustomer);
            ResultSet customerRs = customerStmt.executeQuery();
            customerRs.next();
            int customerId = customerRs.getInt("id");

            String movieSql = "SELECT id FROM Movies WHERE title = ?";
            PreparedStatement movieStmt = conn.prepareStatement(movieSql);
            movieStmt.setString(1, selectedMovie);
            ResultSet movieRs = movieStmt.executeQuery();
            movieRs.next();
            int movieId = movieRs.getInt("id");

            String rentalSql = "INSERT INTO Rentals (customer_id, movie_id, rental_date) VALUES (?, ?, ?)";
            PreparedStatement rentalStmt = conn.prepareStatement(rentalSql);
            rentalStmt.setInt(1, customerId);
            rentalStmt.setInt(2, movieId);
            rentalStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            rentalStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Movie rented!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error renting movie.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RentalForm::new);
    }
}
