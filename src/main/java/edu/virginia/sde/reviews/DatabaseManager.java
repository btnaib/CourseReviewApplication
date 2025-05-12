package edu.virginia.sde.reviews;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:course_reviews.db";
    private static DatabaseManager instance = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void setup() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Creates the Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL" +
                    ");");
            // Creates the Courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS Courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"  +
                    "subject TEXT,"+
                    "number INTEGER," +
                    "title TEXT" +
                    // NEED TO ADD AVERAGE? will handle that later
                    ");");

            // Creates the Reviews table
            stmt.execute("CREATE TABLE IF NOT EXISTS Reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "course_id INTEGER NOT NULL," +
                    "rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5)," +
                    "comment TEXT," +
                    "timestamp TEXT NOT NULL," +
                    "UNIQUE(user_id, course_id)," +
                    "FOREIGN KEY(user_id) REFERENCES Users(id)," +
                    "FOREIGN KEY(course_id) REFERENCES Courses(id)" +
                    ");");

            //stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}