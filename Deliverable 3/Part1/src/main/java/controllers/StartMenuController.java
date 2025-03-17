package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class StartMenuController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

    @FXML
    private StackPane settingsOverlay;

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefWidth(newVal.doubleValue());
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefHeight(newVal.doubleValue());
        });
    }

    public void handleNewSimulation(ActionEvent event) {
        switchRoot("/fxml/new_simulation.fxml", event);
    }

    public void handleLoadSimulation(ActionEvent event) {
        switchRoot("/fxml/load_simulation.fxml", event);
    }

    public void handleExit(ActionEvent event) {
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
