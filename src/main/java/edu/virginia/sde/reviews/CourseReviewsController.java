package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CourseReviewsController implements Initializable {

    @FXML private Button goBackButton;
    @FXML private Button submitButton; // added for polish
    @FXML private Label reviewMessageLabel;
    @FXML private Label courseTitleLabel;
    @FXML private Label averageRatingLabel;
    @FXML private ListView<String> reviewListView;
    @FXML private TextField ratingField;
    @FXML private TextArea commentField;

    private int courseId;
    private final int userId = SessionManager.getUserId();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ratingField.setPromptText("1–5");
        commentField.setPromptText("Optional comment");
        submitButton.setDisable(true); // Start disabled

        // Enable only if rating is valid
        ratingField.textProperty().addListener((obs, oldVal, newVal) -> {
            submitButton.setDisable(!newVal.matches("[1-5]"));
        });
    }

    public void setCourse(int courseId) {
        this.courseId = courseId;
        loadCourseInfo();
        loadReviews();
        updateAverageRating();
    }

    private void loadCourseInfo() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT subject, number, title FROM Courses WHERE id = ?")) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String subject = rs.getString("subject");
                int number = rs.getInt("number");
                String title = rs.getString("title");

                courseTitleLabel.setText(subject + " " + number + ": " + title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearchScene.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = (Stage) goBackButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmitReview() {
        String ratingText = ratingField.getText();
        String comment = commentField.getText();

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 5) {
                reviewMessageLabel.setText("Rating must be between 1 and 5.");
                return;
            }
        } catch (NumberFormatException e) {
            reviewMessageLabel.setText("Please enter a number between 1 and 5.");
            return;
        }

        String timestamp = new java.sql.Timestamp(System.currentTimeMillis()).toString();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                INSERT INTO Reviews (user_id, course_id, rating, comment, timestamp)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(user_id, course_id)
                DO UPDATE SET rating = excluded.rating,
                              comment = excluded.comment,
                              timestamp = excluded.timestamp;
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, courseId);
                stmt.setInt(3, rating);
                stmt.setString(4, comment);
                stmt.setString(5, timestamp);
                stmt.executeUpdate();
            }

            reviewMessageLabel.setText("Review submitted successfully!");
            loadReviews();
            updateAverageRating();
        } catch (SQLException e) {
            e.printStackTrace();
            reviewMessageLabel.setText("Error submitting review.");
        }
    }

    private void loadReviews() {
        reviewListView.getItems().clear();

        String sql = "SELECT rating, comment, timestamp FROM Reviews WHERE course_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                String timestamp = rs.getString("timestamp");

                String reviewText = "⭐ " + rating + " | " + timestamp;
                if (comment != null && !comment.trim().isEmpty()) {
                    reviewText += " | " + comment;
                }

                reviewListView.getItems().add(reviewText);
            }

        } catch (SQLException e) {
            reviewMessageLabel.setText("Error loading reviews.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteReview() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "DELETE FROM Reviews WHERE user_id = ? AND course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, courseId);

                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    reviewMessageLabel.setText("Review deleted successfully.");
                } else {
                    reviewMessageLabel.setText("You have no review to delete.");
                }
            }

            ratingField.clear();
            commentField.clear();
            submitButton.setDisable(true); // Re-disable after deletion
            loadReviews();
            updateAverageRating();
        } catch (SQLException e) {
            e.printStackTrace();
            reviewMessageLabel.setText("Error deleting review.");
        }
    }

    private void updateAverageRating() {
        String sql = "SELECT AVG(rating) AS avg_rating FROM Reviews WHERE course_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");
                if (!rs.wasNull()) {
                    averageRatingLabel.setText("Average Rating: " + String.format("%.2f", avg) + " ⭐");
                } else {
                    averageRatingLabel.setText("No ratings yet");
                }
            }

        } catch (SQLException e) {
            averageRatingLabel.setText("Error loading rating");
            e.printStackTrace();
        }
    }
}
