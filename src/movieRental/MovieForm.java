package movieRental;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MovieForm extends JFrame {
    private JTextField titleField, genreField, releaseYearField, priceField;
    private JButton saveButton;

    public MovieForm() {
        setTitle("Add Movie");
        setSize(300, 250);
        setLayout(new GridLayout(5, 2));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel("Title:"));
        titleField = new JTextField();
        add(titleField);

        add(new JLabel("Genre:"));
        genreField = new JTextField();
        add(genreField);

        add(new JLabel("Release Year:"));
        releaseYearField = new JTextField();
        add(releaseYearField);

        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveMovie());
        add(saveButton);
        add(new JLabel()); // filler

        setVisible(true);
    }

    private void saveMovie() {
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String releaseYearText = releaseYearField.getText().trim();
        String priceText = priceField.getText().trim();

        if (title.isEmpty() || genre.isEmpty() || releaseYearText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int releaseYear;
        double price;

        try {
            releaseYear = Integer.parseInt(releaseYearText);
            if (releaseYear < 1800 || releaseYear > 2100) {
                JOptionPane.showMessageDialog(this, "Enter a valid release year.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Release year must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            price = Double.parseDouble(priceText);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO Movies (title, genre, release_year, price) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, genre);
            stmt.setInt(3, releaseYear);
            stmt.setDouble(4, price);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Movie saved!");

            // Clear the fields
            titleField.setText("");
            genreField.setText("");
            releaseYearField.setText("");
            priceField.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving movie.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MovieForm::new);
    }
}
