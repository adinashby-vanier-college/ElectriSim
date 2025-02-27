package com.example.part1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    public HelloApplication() {
        // No-arg constructor required by JavaFX
    }
    private Pane centralPane;
    private String selectedComponent = "Resistor"; // Default selection

    @Override
    public void start(Stage primaryStage) {
        // Left panel: List of electrical components
        ListView<String> componentList = new ListView<>();
        componentList.getItems().addAll("Resistor", "Capacitor", "Inductor", "Battery", "Switch");
        componentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedComponent = newVal;
            }
        });

        // Central pane where components will appear
        centralPane = new Pane();
        centralPane.setStyle("-fx-border-color: black; -fx-background-color: lightgray;");
        centralPane.setOnMouseClicked(this::placeComponent);

        // Layout setup
        BorderPane root = new BorderPane();
        root.setLeft(componentList);
        root.setCenter(centralPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Electrical Circuit Builder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void placeComponent(MouseEvent event) {
        // Create a rectangle to represent the component
        Rectangle component = new Rectangle(50, 30);
        component.setStyle("-fx-fill: skyblue; -fx-stroke: black;");
        component.setX(event.getX());
        component.setY(event.getY());

        // Enable dragging functionality
        component.setOnMousePressed(e -> {
            component.setUserData(new double[]{e.getX(), e.getY()});
        });

        component.setOnMouseDragged(e -> {
            double[] offset = (double[]) component.getUserData();
            component.setX(e.getX() - offset[0]);
            component.setY(e.getY() - offset[1]);
        });

        centralPane.getChildren().add(component);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
