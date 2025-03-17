package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SimulationController {
    @FXML
    private StackPane settingsOverlay;

    @FXML
    private HBox searchHbox;

    @FXML
    private TextField searchBar;

    @FXML
    private void handleSave(ActionEvent event) {
        //save code
    }

    @FXML
    private void handleSaveAndExit(ActionEvent event) {
        //save code
        System.exit(0);
    }

    @FXML
    private void handleExportJSON(ActionEvent event) {
        //export code
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        //export code
    }

    @FXML
    private void handleExportText(ActionEvent event) {
        //export code
    }

    @FXML
    private void handleExportImage(ActionEvent event) {
        //export code
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleUndo(ActionEvent event) {
        //code
    }

    @FXML
    private void handleRedo(ActionEvent event) {
        //code
    }

    @FXML
    private void handleCopy(ActionEvent event) {
        //code
    }

    @FXML
    private void handlePaste(ActionEvent event) {
        //code
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        //code
    }

    @FXML
    private void handleSelectAll(ActionEvent event) {
        //code
    }

    @FXML
    private void handleColor(ActionEvent event) {
        //code
    }

    @FXML
    private void handleName(ActionEvent event) {
        //code
    }

    @FXML
    private void handleMaximizeGraph(ActionEvent event) {
        //code
    }

    @FXML
    private void handleMinimizeGraph(ActionEvent event) {
        //code
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
