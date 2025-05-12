package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.stage.Stage;


public class CourseReviewsApplication extends Application {



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage){

        DatabaseManager.getInstance().setup();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/LoginScene.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setWidth(600);
            stage.setHeight(600);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // print the error to console
        }


    }
}
