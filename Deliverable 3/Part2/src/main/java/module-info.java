module com.example.part1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media; //added this line for media use

    opens controllers to javafx.fxml;
    exports app;
}