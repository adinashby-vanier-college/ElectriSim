package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.awt.*;
import java.net.URL;
import java.io.File;
import java.io.IOException;

public class  LoadSimulationController {

    String fileName;

    @FXML
    private Button loadButton;

    @FXML
    private TextField loadTextField;

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label loadLabel;

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
    private void handleLoadSubmit(ActionEvent event) {
        String input = loadTextField.getText();
        checkFilename(input + ".json", event);
    }

   // @FXML
    //private void handleLoadSimulation(ActionEvent event) {
        //switchScene("/fxml/simulation.fxml", event);
    //}

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
            if (fileName != null) {
                SimulationController controller = loader.getController();
                controller.setFile(fileName);
                controller.setUpWithFile();
            }
            scene.setRoot(root);
            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkFilename(String inputName, ActionEvent event) {
        URL resourceFolder = getClass().getResource("/json/" + inputName);
        if (resourceFolder != null) {
           System.out.println("The file is found");
           fileName = "src/main/resources/json/" + inputName;
           switchScene("/fxml/simulation.fxml", event);
        }
        else {
            System.out.println("File does not exist");
            loadLabel.setText("No such save data exist, try again");
            loadLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
