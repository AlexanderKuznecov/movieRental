package movieRental;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    public MainMenu() {
        setTitle("Movie Rental Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        JButton addCustomerBtn = new JButton("Add Customer");
        JButton viewCustomersBtn = new JButton("View Customers");
        JButton addMovieBtn = new JButton("Add Movie");
        JButton viewMoviesBtn = new JButton("View Movies");
        JButton addRentalBtn = new JButton("Add Rental");
        
        JButton returnMovieButton = new JButton("Return Movie");

        add(addCustomerBtn);
        add(viewCustomersBtn);
        add(addMovieBtn);
        add(viewMoviesBtn);
        add(addRentalBtn);
        
        add(returnMovieButton);

        // Action listeners to open forms
        addCustomerBtn.addActionListener(e -> new CustomerForm());
        viewCustomersBtn.addActionListener(e -> new CustomerListForm());
        addMovieBtn.addActionListener(e -> new MovieForm());
        viewMoviesBtn.addActionListener(e -> new MovieListForm());
        addRentalBtn.addActionListener(e -> new RentalForm());
        
        returnMovieButton.addActionListener(e -> new ReturnMovieForm());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
