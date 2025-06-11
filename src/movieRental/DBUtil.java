package movieRental;

import java.sql.*;

public class DBUtil {
    private static final String DB_URL = "jdbc:h2:./moviedb";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Movies (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255)," +
                    "genre VARCHAR(100)," +
                    "release_year INT," +
                    "price DOUBLE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "email VARCHAR(100)," +
                    "phone VARCHAR(20))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Rentals (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "movie_id INT," +
                    "customer_id INT," +
                    "rental_date DATE," +
                    "return_date DATE," +
                    "FOREIGN KEY (movie_id) REFERENCES Movies(id)," +
                    "FOREIGN KEY (customer_id) REFERENCES Customers(id))");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
