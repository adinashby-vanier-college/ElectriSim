module com.example.part1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens controllers to javafx.fxml;
    exports app;
}