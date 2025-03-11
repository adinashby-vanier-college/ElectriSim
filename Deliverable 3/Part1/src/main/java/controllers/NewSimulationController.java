package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class NewSimulationController {

    @FXML
    private TextField simulationNameField;

    // Closes the application
    public void handleExit(ActionEvent event) {
        System.exit(0);
    }

    // Debugging for menu buttons (Help)
    public void handleHelp(ActionEvent event) {
        System.out.println("Help clicked!");
    }

    // Debugging for menu buttons (Background)
    public void handleBackground(ActionEvent event) {
        System.out.println("Change background clicked!");
    }

    // Debugging for menu buttons (Keybinds)
    public void handleKeybinds(ActionEvent event) {
        System.out.println("Keybinds clicked!");
    }

    // Start a new simulation
    public void handleNewSimulation(ActionEvent event) {
        String simulationName = simulationNameField.getText().trim();
        if (simulationName.isEmpty()) {
            System.out.println("Simulation name cannot be empty.");
        } else {
            System.out.println("Starting new simulation: " + simulationName);
            // TODO: Add logic to initialize a new simulation
        }
    }

    // Navigate back to the start menu
    public void handleBackToStartMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/start_menu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
