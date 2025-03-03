package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class StartMenuController {

    // new simulation window
    public void handleNewSimulation(ActionEvent event) throws IOException {
        openWindow("/fxml/new_simulation.fxml", event);
    }

    // load simulation window
    public void handleLoadSimulation(ActionEvent event) throws IOException {
        openWindow("/fxml/load_simulation.fxml", event);
    }

    // close application
    public void handleExit(ActionEvent event) {
        System.exit(0);
    }

    // debugging for menu buttons (help)
    public void handleHelp(ActionEvent event) {
        System.out.println("Help clicked!");
    }

    // debugging for menu buttons (background)
    public void handleBackground(ActionEvent event) {
        System.out.println("Change background clicked!");
    }

    // debugging for menu buttons (keybinds)
    public void handleKeybinds(ActionEvent event) {
        System.out.println("Keybinds clicked!");
    }

    // method to load new windows
    private void openWindow(String fxmlFile, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
