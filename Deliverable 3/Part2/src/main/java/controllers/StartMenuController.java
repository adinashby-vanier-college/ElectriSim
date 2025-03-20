package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.io.IOException;

public class StartMenuController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

    @FXML
    private StackPane settingsOverlay;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox menuBox;

    private static boolean animationPlayed = false;

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefWidth(newVal.doubleValue());
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefHeight(newVal.doubleValue());
        });

        if (!animationPlayed) {
            animationPlayed = true;
            playStartupSound();
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> startMenuAnimation());
            delay.play();
        } else {
            titleLabel.setOpacity(1);
            titleLabel.setScaleX(1);
            titleLabel.setScaleY(1);
            titleLabel.setTranslateY(0);
            menuBox.setOpacity(1);
        }
    }

    private void playStartupSound() {
        try {
            String soundPath = getClass().getResource("/sounds/startup.mp3").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing startup sound: " + e.getMessage());
        }
    }

    private void startMenuAnimation() {
        titleLabel.setOpacity(0);
        menuBox.setOpacity(0);

        titleLabel.setScaleX(2);
        titleLabel.setScaleY(2);
        titleLabel.setTranslateY(80);

        FadeTransition fadeInTitle = new FadeTransition(Duration.seconds(1.2), titleLabel);
        fadeInTitle.setFromValue(0);
        fadeInTitle.setToValue(1);

        TranslateTransition moveUpTitle = new TranslateTransition(Duration.seconds(1.5), titleLabel);
        moveUpTitle.setFromY(80);
        moveUpTitle.setToY(0);

        ScaleTransition shrinkTitle = new ScaleTransition(Duration.seconds(1.5), titleLabel);
        shrinkTitle.setFromX(2);
        shrinkTitle.setFromY(2);
        shrinkTitle.setToX(1);
        shrinkTitle.setToY(1);

        ParallelTransition moveAndShrink = new ParallelTransition(moveUpTitle, shrinkTitle);

        FadeTransition fadeInButtons = new FadeTransition(Duration.seconds(1.5), menuBox);
        fadeInButtons.setFromValue(0);
        fadeInButtons.setToValue(1);

        SequentialTransition sequence = new SequentialTransition(
                fadeInTitle,
                moveAndShrink,
                fadeInButtons
        );
        sequence.play();
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
