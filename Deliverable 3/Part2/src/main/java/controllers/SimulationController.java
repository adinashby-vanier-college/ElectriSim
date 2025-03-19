package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
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
import java.util.ArrayList;
import java.util.List;

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
    private ScrollPane scrollPane;

    @FXML
    private Pane canvasContainer; // New container for Canvas

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefWidth(newVal.doubleValue()));
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefHeight(newVal.doubleValue()));

        // Wrap canvasContainer in a Group to allow proper zooming
        Group zoomGroup = new Group();
        zoomGroup.getChildren().add(canvasContainer);

        scrollPane.setContent(zoomGroup); // Set the ScrollPane content to this Group

        // Initialize the builder and center the scroll bars
        createBuilder();
        centerScrollBars();
    }

    private void createBuilder() {
        if (builder != null) {
            drawGrid();
        } else {
            System.out.println("Canvas is null. Check FXML binding.");
        }

        // Zoom in/out functionality
        addZoomFunctionality();
    }

    private void centerScrollBars() {
        // Set the scroll bars to the middle (center) of their range
        scrollPane.setHvalue(0.5); // Center horizontally
        scrollPane.setVvalue(0.5); // Center vertically
    }

    private double zoomScale = 1.0; // Default zoom scale
    private final double minZoom = 0.5;
    private final double maxZoom = 3.0;
    private final double gridSize = 20; // Base grid size

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

                // Apply scaling transformation to the container
                canvasContainer.setScaleX(zoomScale);
                canvasContainer.setScaleY(zoomScale);

                // Redraw the grid and components
                redrawCanvas();

                event.consume();
            }
        });
    }

    private void drawGrid() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        double width = builder.getWidth();
        double height = builder.getHeight();

        // Clear previous grid
        gc.setFill(javafx.scene.paint.Color.DARKGRAY);
        gc.fillRect(0, 0, width, height);

        // Set grid line color
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
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // Save code
    }

    @FXML
    private void handleSaveAndExit(ActionEvent event) {
        // Save code
        System.exit(0);
    }

    @FXML
    private void handleExportJSON(ActionEvent event) {
        // Export code
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        // Export code
    }

    @FXML
    private void handleExportText(ActionEvent event) {
        // Export code
    }

    @FXML
    private void handleExportImage(ActionEvent event) {
        // Export code
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleUndo(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleRedo(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleCopy(ActionEvent event) {
        // Code
    }

    @FXML
    private void handlePaste(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleSelectAll(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleColor(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleName(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleMaximizeGraph(ActionEvent event) {
        // Code
    }

    @FXML
    private void handleMinimizeGraph(ActionEvent event) {
        // Code
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

    private List<ImageComponent> components = new ArrayList<>();

    private static class ImageComponent {
        Image image;
        double x, y, width, height;

        ImageComponent(Image image, double x, double y, double width, double height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private void redrawCanvas() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        gc.clearRect(0, 0, builder.getWidth(), builder.getHeight());

        // Redraw grid
        drawGrid();

        // Redraw all components
        for (ImageComponent component : components) {
            gc.drawImage(component.image, component.x, component.y, component.width, component.height);
        }
    }

    @FXML
    private void handleAddBattery(ActionEvent event) {
        if (builder != null) {
            Image batteryImage = new Image(getClass().getResource("/images/circuit_diagrams/Battery Cell.GIF").toExternalForm());

            double x = builder.getWidth() / 2 - 25;
            double y = builder.getHeight() / 2 - 25;
            double width = 110;
            double height = 110;

            ImageComponent battery = new ImageComponent(batteryImage, x, y, width, height);
            setDraggable(battery);

            components.add(battery);
            redrawCanvas();
            System.out.println("Battery added to the builder.");
        } else {
            System.out.println("Canvas is null. Check FXML binding.");
        }
    }

    private void setDraggable(ImageComponent component) {
        final double[] offsetX = {0};
        final double[] offsetY = {0};
        final boolean[] dragging = {false};

        builder.setOnMousePressed(e -> {
            if (e.getX() >= component.x && e.getX() <= component.x + component.width &&
                    e.getY() >= component.y && e.getY() <= component.y + component.height) {

                dragging[0] = true;
                offsetX[0] = e.getX() - component.x;
                offsetY[0] = e.getY() - (component.y + component.height / 2); // Centered y-reference
            } else {
                dragging[0] = false;
            }
        });

        builder.setOnMouseDragged(e -> {
            if (dragging[0]) {
                double newX = e.getX() - offsetX[0];
                double newY = e.getY() - offsetY[0] - component.height / 2; // Keep y reference at the center

                // Keep within canvas bounds
                if (newX >= 0 && newX + component.width <= builder.getWidth()) {
                    component.x = newX;
                }
                if (newY >= 0 && newY + component.height <= builder.getHeight()) {
                    component.y = newY;
                }

                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (dragging[0]) {
                // Snap the component's center to the nearest grid intersection
                component.x = Math.round(component.x / gridSize) * gridSize - 4;
                component.y = Math.round((component.y + component.height / 2) / gridSize) * gridSize - component.height / 2 + 2;

                dragging[0] = false;
                redrawCanvas();
            }
        });
    }
}