package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class NewSimulationController {

    @FXML
    private TextField simulationNameField;

    @FXML
    private Label errorLabel;

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

    public void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefWidth(newVal.doubleValue());
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefHeight(newVal.doubleValue());
        });
    }

    @FXML
    public void handleOpenSettings(ActionEvent event) {
        settingsOverlay.setVisible(true);
    }

    @FXML
    public void handleCloseSettings(ActionEvent event) {
        settingsOverlay.setVisible(false);
    }

    public void handleNewSimulation(ActionEvent event) {
        String simulationName = simulationNameField.getText().trim();

        if (simulationName.isEmpty()) {
            errorLabel.setText("Error: Simulation name is required.");
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            errorLabel.setText("");
            System.out.println("Starting new simulation: " + simulationName);
            switchRoot("/fxml/simulation.fxml", event);
        }
    }

    public void handleBackToStartMenu(ActionEvent event) {
        switchRoot("/fxml/start_menu.fxml", event);
    }

    private void switchRoot(String fxmlFile, ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            scene.setRoot(root);

            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
