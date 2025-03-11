package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class StartMenuController {

    public void handleNewSimulation(ActionEvent event) {
        switchRoot("/fxml/new_simulation.fxml", event);
    }

    public void handleLoadSimulation(ActionEvent event) {
        switchRoot("/fxml/load_simulation.fxml", event);
    }

    public void handleExit(ActionEvent event) {
        System.exit(0);
    }

    public void handleHelp(ActionEvent event) {
        System.out.println("Help clicked!");
    }

    public void handleBackground(ActionEvent event) {
        System.out.println("Change background clicked!");
    }

    public void handleKeybinds(ActionEvent event) {
        System.out.println("Keybinds clicked!");
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
