module com.example.part1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.opencsv;
    requires com.fasterxml.jackson.databind;
    requires java.sql; //added this line for media use
    opens components to com.fasterxml.jackson.databind;
    opens controllers to javafx.fxml, com.fasterxml.jackson.databind;
    exports app;
}