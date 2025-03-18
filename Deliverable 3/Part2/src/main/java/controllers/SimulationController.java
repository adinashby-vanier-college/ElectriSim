package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    private AnchorPane rootPane;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Canvas builder;

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefWidth(newVal.doubleValue());
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            mainPane.setPrefHeight(newVal.doubleValue());
        });

        createBuilder();
    }

    private void createBuilder(){
        if (builder != null) {
            GraphicsContext gc = builder.getGraphicsContext2D();
            double width = builder.getWidth();
            double height = builder.getHeight();
            double gridSize = 20; // Adjust grid size as needed

            // Set background to dark gray
            gc.setFill(javafx.scene.paint.Color.DARKGRAY);
            gc.fillRect(0, 0, width, height);

            // Set line color to black
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.setLineWidth(1);

            // Draw vertical lines
            for (double x = 0; x <= width; x += gridSize) {
                gc.strokeLine(x, 0, x, height);
            }

            // Draw horizontal lines
            for (double y = 0; y <= height; y += gridSize) {
                gc.strokeLine(0, y, width, y);
            }
        } else {
            System.out.println("Canvas is null. Check FXML binding.");
        }

        //zoom in/out functionality
        addZoomFunctionality();
    }

    private double zoomScale = 1.0; // Default zoom scale
    private final double minZoom = 0.5;
    private final double maxZoom = 3.0;
    private final double gridSize = 15; // Base grid size

    private void addZoomFunctionality() {
        builder.setOnScroll(event -> {
            if (event.isControlDown()) { // Check if CTRL is held
                double delta = event.getDeltaY();
                double zoomFactor = delta > 0 ? 1.1 : 0.9; // Zoom in or out

                // Calculate new zoom scale within limits
                double newScale = zoomScale * zoomFactor;
                if (newScale < minZoom || newScale > maxZoom) {
                    return; // Prevent excessive zooming
                }
                zoomScale = newScale;

                // Resize the canvas
                builder.setWidth(3000 * zoomScale);
                builder.setHeight(1500 * zoomScale);

                // Redraw the grid
                drawGrid();

                event.consume();
            }
        });
    }

    private void drawGrid() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        double width = builder.getWidth();
        double height = builder.getHeight();
        double scaledGridSize = gridSize * zoomScale; // Adjust grid size based on zoom

        // Extra margin to ensure full coverage
        double extraMargin = scaledGridSize;

        // Clear previous grid
        gc.setFill(javafx.scene.paint.Color.DARKGRAY);
        gc.fillRect(0, 0, width, height);

        // Set grid line color
        gc.setStroke(javafx.scene.paint.Color.BLACK);
        gc.setLineWidth(1);

        // Draw vertical lines (extend beyond width)
        for (double x = 0; x <= width + extraMargin; x += scaledGridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Draw horizontal lines (extend beyond height)
        for (double y = 0; y <= height + extraMargin; y += scaledGridSize) {
            gc.strokeLine(0, y, width, y);
        }
    }


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

    @FXML
    private void handleAddBattery(ActionEvent event) {
        if (builder != null) {
            GraphicsContext gc = builder.getGraphicsContext2D();

            Image batteryImage = new Image(getClass().getResource("/images/circuit_diagrams/Battery Cell.GIF").toExternalForm());

            double x = builder.getWidth() / 2 - 25;
            double y = builder.getHeight() / 2 - 25;

            gc.drawImage(batteryImage, x, y, 50, 50);

            System.out.println("Battery added to the builder.");
        } else {
            System.out.println("Canvas is null. Check FXML binding.");
        }
    }

}
