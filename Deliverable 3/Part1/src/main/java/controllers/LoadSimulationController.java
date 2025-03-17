package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class LoadSimulationController {

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

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
    private void handleLoadSimulation(ActionEvent event) {
        switchScene("/fxml/simulation.fxml", event);
    }

    @FXML
    private void handleBackToStartMenu(ActionEvent event) {
        switchScene("/fxml/start_menu.fxml", event);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void handleOpenSettings(ActionEvent event) {
        settingsOverlay.setVisible(true);
    }

    @FXML
    public void handleCloseSettings(ActionEvent event) {
        settingsOverlay.setVisible(false);
    }

    private void switchScene(String fxmlFile, ActionEvent event) {
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
