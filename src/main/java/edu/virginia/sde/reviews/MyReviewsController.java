package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyReviewsController {
    @FXML
    private Button backButton;
    @FXML private ListView<String> myReviewsListView;

    private final int userId = SessionManager.getUserId();

    @FXML
    public void initialize() {
        loadMyReviews();
    }



    @FXML
    private void handleBack(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearchScene.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void loadMyReviews() {
        myReviewsListView.getItems().clear();

        String sql = """
            SELECT Reviews.course_id, Courses.subject, Courses.number, Courses.title,
                   Reviews.rating, Reviews.comment
            FROM Reviews
            JOIN Courses ON Reviews.course_id = Courses.id
            WHERE Reviews.user_id = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String subject = rs.getString("subject");
                int number = rs.getInt("number");
                int rating = rs.getInt("rating");

                // Optional: skip title/comment if space is limited
                String display = subject + " " + number + " | ⭐ " + rating;

                // Store full entry with courseId so we can open it later
                String entry = courseId + "|" + display;
                myReviewsListView.getItems().add(entry);
            }

            myReviewsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String[] parts = item.split("\\|", 2);
                        int courseId = Integer.parseInt(parts[0]);
                        String displayText = parts[1];

                        Label label = new Label(displayText);
                        Button viewButton = new Button("View");
                        viewButton.setOnAction(e -> openCourseReviewScene(courseId));

                        HBox hbox = new HBox(10, label, viewButton);
                        setGraphic(hbox);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openCourseReviewScene(int courseId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseReviewsScene.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            CourseReviewsController controller = loader.getController();
            controller.setCourse(courseId);

            Stage stage = (Stage) myReviewsListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
