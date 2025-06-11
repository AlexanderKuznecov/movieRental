package movieRental;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MovieListForm extends JFrame {
    private JTable movieTable;
    private JButton deleteButton, editButton, searchButton;
    private JTextField searchField;
    private DefaultTableModel tableModel;

    public MovieListForm() {
        setTitle("Movie List");
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Top search panel
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchMovies());
        topPanel.add(new JLabel("Search by title:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Title", "Genre", "Release Year", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        movieTable = new JTable(tableModel);
        loadMovieData();
        add(new JScrollPane(movieTable), BorderLayout.CENTER);

        // Bottom button panel
        JPanel bottomPanel = new JPanel();
        deleteButton = new JButton("Delete Selected Movie");
        deleteButton.addActionListener(e -> deleteMovie());
        bottomPanel.add(deleteButton);

        editButton = new JButton("Edit Selected Movie");
        editButton.addActionListener(e -> editMovie());
        bottomPanel.add(editButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void loadMovieData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Movies";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("release_year"),
                    rs.getDouble("price")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchMovies() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Movies WHERE title LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("release_year"),
                    rs.getDouble("price")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow != -1) {
            int movieId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this movie?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBUtil.getConnection()) {
                    String deleteRentalsSQL = "DELETE FROM Rentals WHERE movie_id = ?";
                    PreparedStatement rentalStmt = conn.prepareStatement(deleteRentalsSQL);
                    rentalStmt.setInt(1, movieId);
                    rentalStmt.executeUpdate();

                    String sql = "DELETE FROM Movies WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, movieId);
                    stmt.executeUpdate();

                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Movie and related rentals deleted!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting movie.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a movie to delete.");
        }
    }

    private void editMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a movie to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentTitle = (String) tableModel.getValueAt(selectedRow, 1);
        String currentGenre = (String) tableModel.getValueAt(selectedRow, 2);
        int currentYear = (int) tableModel.getValueAt(selectedRow, 3);
        double currentPrice = (double) tableModel.getValueAt(selectedRow, 4);

        JTextField titleField = new JTextField(currentTitle);
        JTextField genreField = new JTextField(currentGenre);
        JTextField yearField = new JTextField(String.valueOf(currentYear));
        JTextField priceField = new JTextField(String.valueOf(currentPrice));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Genre:"));
        panel.add(genreField);
        panel.add(new JLabel("Release Year:"));
        panel.add(yearField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Movie", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE Movies SET title = ?, genre = ?, release_year = ?, price = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, titleField.getText().trim());
                stmt.setString(2, genreField.getText().trim());
                stmt.setInt(3, Integer.parseInt(yearField.getText().trim()));
                stmt.setDouble(4, Double.parseDouble(priceField.getText().trim()));
                stmt.setInt(5, id);
                stmt.executeUpdate();

                // Update the table
                tableModel.setValueAt(titleField.getText(), selectedRow, 1);
                tableModel.setValueAt(genreField.getText(), selectedRow, 2);
                tableModel.setValueAt(Integer.parseInt(yearField.getText()), selectedRow, 3);
                tableModel.setValueAt(Double.parseDouble(priceField.getText()), selectedRow, 4);

                JOptionPane.showMessageDialog(this, "Movie updated!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating movie.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MovieListForm::new);
    }
}
