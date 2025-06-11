package movieRental;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;

public class CustomerForm extends JFrame {
    private JTextField nameField, emailField;
    private JFormattedTextField phoneField;
    private JButton saveButton;

    public CustomerForm() {
        setTitle("Add Customer");
        setSize(300, 200);
        setLayout(new GridLayout(4, 2));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Phone:"));
        try {
            MaskFormatter phoneFormatter = new MaskFormatter("+### ### ### ###");
            phoneFormatter.setPlaceholderCharacter('_');
            phoneField = new JFormattedTextField(phoneFormatter);
        } catch (ParseException e) {
            e.printStackTrace();
            phoneField = new JFormattedTextField();
        }
        add(phoneField);

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCustomer());
        add(saveButton);
        add(new JLabel()); // filler

        setVisible(true);
    }

    private void saveCustomer() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO Customers (name, email, phone) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer saved!");

            // Clear fields after successful save
            nameField.setText("");
            emailField.setText("");
            phoneField.setValue(null);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving customer.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomerForm::new);
    }
}
