# CourseReviewApplication
JavaFX-based CRUD application allowing users to log in, search for courses, and submit, edit, or delete course reviews.

## To Run

Prior to running, ensure that you include --module-path [Path to JavaFX folder] --add-modules javafx.controls,javafx.fxml in as VM arguments. These are pathways to wherever the JavaFX files are stored on your local machine. Further, ensure that you are running with JDK version 17 and Java version 17. To run the program, run the main function from CourseReviewsApplication.java which extends Application. This then brings up the GUI which allows you to interact with our CRUD application.

# Contributions

### Ben Tnaib

* Absolute GOAT
* Implemented majority of functionality methods/functions in CourseReviewsController
* Implemented majority of functionality methods/functions in CourseSearchController
* Implemented DatabaseManager as well as Login Controller classes
* Worked to embed MyReviews functionality
* The LeBron James of SDE and life

### Joseph Dokken

* Updated Course Search Parameters to be slightly more general
* Added aesthetic features to fxml files and updated spacing to improve UI for each scene
* Helped in debugging and planning out SQL elements of database management and updates when new courses/reviews added

### Ameer Moutaouakil

* Added colors to improve the UI
* Added average rating feature
* Added course sort to be by the average rating of course
* Added text placeholders for further UI polishing
* Disabled form until valid inputs are in
