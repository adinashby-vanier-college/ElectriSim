module com.example.part1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.part1 to javafx.fxml;
    exports com.example.part1;
}