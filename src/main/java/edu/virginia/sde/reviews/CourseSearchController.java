package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CourseSearchController implements Initializable {

    @FXML private TextField addSubjectField;
    @FXML private TextField addNumberField;
    @FXML private TextField addTitleField;
    @FXML private Label addCourseMessageLabel;
    @FXML private TextField searchField;
    @FXML private ListView<String> courseListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchField.setText("");
        searchField.setPromptText("Search by subject, number, or title");
        addSubjectField.setPromptText("e.g., CS");
        addNumberField.setPromptText("e.g., 3140");
        addTitleField.setPromptText("e.g., Software Development Essentials");
        handleSearch();
    }

    @FXML
    private void handleAddCourse() {
        String subject = addSubjectField.getText();
        String coursenumber = addNumberField.getText();
        String title = addTitleField.getText();

        if (!(subject.length() >= 2 && subject.length() <= 4)) {
            addCourseMessageLabel.setText("Subject Mnemonic must be 2-4 letters.");
        } else if (!isAllLetters(subject)) {
            addCourseMessageLabel.setText("Subject Mnemonic must contain only letters.");
        } else if (title.length() > 50 || title.length() < 1) {
            addCourseMessageLabel.setText("Course title must be 1–50 characters.");
        } else if (!isAllDigits(coursenumber) || coursenumber.length() != 4) {
            addCourseMessageLabel.setText("Course number must be a 4-digit number.");
        } else {
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO Courses (subject, number, title) VALUES (?, ?, ?)")) {

                stmt.setString(1, subject.toUpperCase());
                stmt.setInt(2, Integer.parseInt(coursenumber));
                stmt.setString(3, title);

                stmt.executeUpdate();
                addCourseMessageLabel.setText("Course created!");
                addSubjectField.clear();
                addNumberField.clear();
                addTitleField.clear();
                handleSearch();
            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE")) {
                    addCourseMessageLabel.setText("Course already exists.");
                } else {
                    e.printStackTrace();
                    addCourseMessageLabel.setText("Error creating course.");
                }
            }
        }
    }

    private boolean isAllLetters(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllDigits(String s) {
        return s != null && s.matches("\\d+");
    }

    @FXML
    private void handleLogOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/LoginScene.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = (Stage) addSubjectField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchInput = searchField.getText().trim();
        ObservableList<String> courseResults = FXCollections.observableArrayList();

        String[] keywords = searchInput.split("\\s+");
        if (keywords.length == 0) {
            courseListView.setItems(courseResults);
            return;
        }

        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0) whereClause.append(" OR ");
            whereClause.append("(")
                    .append("LOWER(Courses.subject) LIKE ? OR ")
                    .append("LOWER(CAST(Courses.number AS TEXT)) LIKE ? OR ")
                    .append("LOWER(Courses.title) LIKE ?")
                    .append(")");
        }

        String sql = """
        SELECT Courses.id, Courses.subject, Courses.number, Courses.title,
               AVG(Reviews.rating) AS avg_rating
        FROM Courses
        LEFT JOIN Reviews ON Courses.id = Reviews.course_id
        WHERE """ + whereClause + """
        GROUP BY Courses.id
        ORDER BY avg_rating DESC NULLS LAST, subject ASC, number ASC
    """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            for (String keyword : keywords) {
                String term = "%" + keyword.toLowerCase() + "%";
                stmt.setString(paramIndex++, term); // subject
                stmt.setString(paramIndex++, term); // number
                stmt.setString(paramIndex++, term); // title
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("id");
                String subject = rs.getString("subject");
                int number = rs.getInt("number");
                String title = rs.getString("title");

                double avgRating = rs.getDouble("avg_rating");
                String ratingStr = rs.wasNull() ? "No ratings" : String.format("Avg: %.2f ⭐", avgRating);

                String displayLine = subject + " " + number + " - " + title + " | " + ratingStr;
                String combinedLine = courseId + "|" + displayLine;

                courseResults.add(combinedLine);
            }

            courseListView.setItems(courseResults);

            // ⬇️ Cell Factory logic — unchanged and preserved
            courseListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String courseLine, boolean empty) {
                    super.updateItem(courseLine, empty);
                    if (empty || courseLine == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String[] parts = courseLine.split("\\|", 2);
                        int courseId = Integer.parseInt(parts[0]);
                        String displayText = parts[1];

                        Label courseLabel = new Label(displayText);
                        Button reviewButton = new Button("Reviews");
                        reviewButton.setOnAction(event -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseReviewsScene.fxml"));
                                Scene scene = new Scene(loader.load());
                                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

                                CourseReviewsController controller = loader.getController();
                                controller.setCourse(courseId);

                                Stage stage = (Stage) addSubjectField.getScene().getWindow();
                                stage.setScene(scene);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        HBox hbox = new HBox(10, courseLabel, reviewButton);
                        setGraphic(hbox);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleGoMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/MyReviewsScene.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = (Stage) addSubjectField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
