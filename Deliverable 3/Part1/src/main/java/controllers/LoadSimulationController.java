package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class LoadSimulationController {

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
    private void handleHelp(ActionEvent event) {
        System.out.println("Help clicked!");
    }

    @FXML
    private void handleKeybinds(ActionEvent event) {
        System.out.println("Keybinds clicked!");
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
