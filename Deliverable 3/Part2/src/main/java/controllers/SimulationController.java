package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class SimulationController {
    @FXML private StackPane settingsOverlay;
    @FXML private HBox searchHbox;
    @FXML private TextField searchBar;
    @FXML private AnchorPane rootPane;
    @FXML private BorderPane mainPane;
    @FXML private Canvas builder;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane canvasContainer;

    private List<Drawable> drawables = new ArrayList<>(); // Store all drawable objects (components and wires)
    private final double gridSize = 20;
    private double zoomScale = 1.0;
    private final double minZoom = 0.5;
    private final double maxZoom = 3.0;

    private ImageView floatingComponentImage;
    private Image currentlySelectedImage;
    private double selectedImageWidth = 80;
    private double selectedImageHeight = 80;
    private ImageComponent draggedExistingComponent = null;
    private double currentRotation = 0;

    // Wire creation variables
    private boolean isDrawingWire = false;
    private double wireStartX, wireStartY;
    private Circle wireStartCircle;
    private Wire selectedWire = null; // Track the selected wire

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefWidth(newVal.doubleValue()));
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefHeight(newVal.doubleValue()));

        Group zoomGroup = new Group();
        zoomGroup.getChildren().add(canvasContainer);
        scrollPane.setContent(zoomGroup);

        createBuilder();
        centerScrollBars();
        setupDragging();
        setupFloatingImage();
        setupCanvasClickPlacement();
        setupWireDrawing();
    }

    private void setupFloatingImage() {
        floatingComponentImage = new ImageView();
        floatingComponentImage.setMouseTransparent(true);
        floatingComponentImage.setVisible(false);
        rootPane.getChildren().add(floatingComponentImage);

        rootPane.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (floatingComponentImage.isVisible()) {
                floatingComponentImage.setLayoutX(e.getSceneX() - selectedImageWidth / 2);
                floatingComponentImage.setLayoutY(e.getSceneY() - selectedImageHeight / 2);
            }
        });

        Platform.runLater(() -> {
            rootPane.getScene().setOnKeyPressed(e -> {
                if (!floatingComponentImage.isVisible()) return;

                if (e.getCode() == KeyCode.ESCAPE) {
                    floatingComponentImage.setVisible(false);
                    currentlySelectedImage = null;
                } else if (e.getCode() == KeyCode.E) {
                    floatingComponentImage.setVisible(false);
                    currentlySelectedImage = null;
                } else if (e.getCode() == KeyCode.R) {
                    currentRotation = (currentRotation + 90) % 360;
                    floatingComponentImage.setRotate(currentRotation);
                }
            });
        });
    }

    private void setupCanvasClickPlacement() {
        builder.setOnMouseClicked(e -> {
            // Check if a component is being placed
            if (floatingComponentImage.isVisible() && currentlySelectedImage != null) {
                double snappedX = Math.round((e.getX() - selectedImageWidth / 2) / gridSize) * gridSize;
                double snappedY = Math.round((e.getY() - selectedImageHeight / 2) / gridSize) * gridSize;

                for (Drawable drawable : drawables) {
                    if (drawable instanceof ImageComponent) {
                        ImageComponent component = (ImageComponent) drawable;
                        if (snappedX < component.x + component.width && snappedX + selectedImageWidth > component.x &&
                                snappedY < component.y + component.height && snappedY + selectedImageHeight > component.y) {
                            return;
                        }
                    }
                }

                ImageComponent newComponent = new ImageComponent(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                newComponent.rotation = currentRotation;
                drawables.add(newComponent);
                redrawCanvas();

                floatingComponentImage.setVisible(false);
                currentlySelectedImage = null;
                floatingComponentImage.setRotate(0);
                currentRotation = 0;
                return;
            }

            // Check if an existing component is clicked to be moved
            for (Iterator<Drawable> iterator = drawables.iterator(); iterator.hasNext(); ) {
                Drawable drawable = iterator.next();
                if (drawable instanceof ImageComponent) {
                    ImageComponent component = (ImageComponent) drawable;
                    if (e.getX() >= component.x && e.getX() <= component.x + component.width &&
                            e.getY() >= component.y && e.getY() <= component.y + component.height) {
                        currentlySelectedImage = component.image;
                        floatingComponentImage.setImage(currentlySelectedImage);
                        floatingComponentImage.setFitWidth(component.width);
                        floatingComponentImage.setFitHeight(component.height);
                        floatingComponentImage.setRotate(component.rotation);
                        currentRotation = component.rotation;
                        selectedImageWidth = component.width;
                        selectedImageHeight = component.height;
                        floatingComponentImage.setVisible(true);
                        iterator.remove();
                        redrawCanvas();
                        return;
                    }
                }
            }

            // Check if a wire is clicked
            selectedWire = null; // Reset selected wire
            for (Drawable drawable : drawables) {
                if (drawable instanceof Wire) {
                    Wire wire = (Wire) drawable;
                    if (isPointNearLine(e.getX(), e.getY(), wire.startX, wire.startY, wire.endX, wire.endY)) {
                        selectedWire = wire; // Select the wire
                        break;
                    }
                }
            }

            // Redraw the canvas to highlight the selected wire
            redrawCanvas();
        });
    }

    private boolean isPointNearLine(double px, double py, double x1, double y1, double x2, double y2) {
        double lineLength = Math.hypot(x2 - x1, y2 - y1);
        double distance = Math.abs((x2 - x1) * (y1 - py) - (x1 - px) * (y2 - y1)) / lineLength;
        return distance < 5; // Tolerance for wire selection
    }

    private double wireEndX, wireEndY; // Add these variables to store the current mouse coordinates

    private void setupWireDrawing() {
        builder.setOnMousePressed(e -> {
            for (Drawable drawable : drawables) {
                if (drawable instanceof ImageComponent) {
                    ImageComponent component = (ImageComponent) drawable;
                    if (component.startCircle.contains(e.getX(), e.getY())) {
                        isDrawingWire = true;
                        wireStartX = component.startX;
                        wireStartY = component.startY;
                        wireStartCircle = component.startCircle;
                        break;
                    } else if (component.endCircle.contains(e.getX(), e.getY())) {
                        isDrawingWire = true;
                        wireStartX = component.endX;
                        wireStartY = component.endY;
                        wireStartCircle = component.endCircle;
                        break;
                    }
                }
            }
        });

        builder.setOnMouseDragged(e -> {
            if (isDrawingWire) {
                // Store the current mouse coordinates
                wireEndX = e.getX();
                wireEndY = e.getY();
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (isDrawingWire) {
                double snappedX = Math.round(e.getX() / gridSize) * gridSize;
                double snappedY = Math.round(e.getY() / gridSize) * gridSize;

                // Add the wire to the list of drawables
                Wire newWire = new Wire(wireStartX, wireStartY, snappedX, snappedY);
                drawables.add(newWire);
                isDrawingWire = false;
                wireStartCircle = null;
                redrawCanvas();
            }
        });
    }

    @FXML
    private void handleComponentButtonClick(ActionEvent event) {
        if (floatingComponentImage.isVisible()) return;

        Button sourceButton = (Button) event.getSource();
        ImageView imageView = (ImageView) ((HBox) sourceButton.getGraphic()).getChildren().get(0);
        Image image = imageView.getImage();

        if (image != null) {
            currentlySelectedImage = image;
            floatingComponentImage.setImage(image);
            floatingComponentImage.setFitWidth(selectedImageWidth);
            floatingComponentImage.setFitHeight(selectedImageHeight);
            floatingComponentImage.setRotate(0);
            floatingComponentImage.setVisible(true);
            currentRotation = 0;
        }
    }

    private void createBuilder() {
        if (builder != null) {
            drawGrid();
        }
        addZoomFunctionality();
    }

    private void centerScrollBars() {
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    private void addZoomFunctionality() {
        builder.setOnScroll(event -> {
            if (event.isControlDown()) {
                double delta = event.getDeltaY();
                double zoomFactor = delta > 0 ? 1.1 : 0.9;
                double newScale = zoomScale * zoomFactor;

                if (newScale < minZoom || newScale > maxZoom) return;
                zoomScale = newScale;

                canvasContainer.setScaleX(zoomScale);
                canvasContainer.setScaleY(zoomScale);
                redrawCanvas();

                event.consume();
            }
        });
    }

    private void drawGrid() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        double width = builder.getWidth();
        double height = builder.getHeight();

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, width, height);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (double x = 0; x <= width; x += gridSize) gc.strokeLine(x, 0, x, height);
        for (double y = 0; y <= height; y += gridSize) gc.strokeLine(0, y, width, y);
    }

    private void redrawCanvas() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        gc.clearRect(0, 0, builder.getWidth(), builder.getHeight());
        drawGrid();

        // Create a light blue shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTBLUE);
        shadow.setRadius(10);
        shadow.setSpread(0.6);

        // Draw all drawable objects
        for (Drawable drawable : drawables) {
            if (drawable == selectedWire) {
                // Apply the shadow effect to the selected wire
                gc.save();
                gc.setEffect(shadow);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(4); // Set wire thickness to 4 pixels
                ((Wire) drawable).draw(gc);
                gc.restore();
            } else {
                drawable.draw(gc);
            }
        }

        // Draw the temporary red wire if drawing
        if (isDrawingWire) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(4); // Set temporary wire thickness to 4 pixels
            gc.strokeLine(wireStartX, wireStartY, wireEndX, wireEndY);
        }
    }

    private void setupDragging() {
        final double[] offsetX = {0};
        final double[] offsetY = {0};

        builder.setOnMousePressed(e -> {
            for (Drawable drawable : drawables) {
                if (drawable instanceof ImageComponent) {
                    ImageComponent component = (ImageComponent) drawable;
                    if (e.getX() >= component.x && e.getX() <= component.x + component.width &&
                            e.getY() >= component.y && e.getY() <= component.y + component.height) {
                        draggedExistingComponent = component;
                        offsetX[0] = e.getX() - component.x;
                        offsetY[0] = e.getY() - component.y;
                        break;
                    }
                }
            }
        });

        builder.setOnMouseDragged(e -> {
            if (draggedExistingComponent != null) {
                double newX = e.getX() - offsetX[0];
                double newY = e.getY() - offsetY[0];

                draggedExistingComponent.x = newX;
                draggedExistingComponent.y = newY;

                // Update start and end points
                draggedExistingComponent.startX = newX;
                draggedExistingComponent.startY = newY + draggedExistingComponent.height / 2;
                draggedExistingComponent.endX = newX + draggedExistingComponent.width;
                draggedExistingComponent.endY = newY + draggedExistingComponent.height / 2;

                // Update wires connected to this component
                updateWiresForComponent(draggedExistingComponent);

                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (draggedExistingComponent != null) {
                draggedExistingComponent.x = Math.round(draggedExistingComponent.x / gridSize) * gridSize;
                draggedExistingComponent.y = Math.round(draggedExistingComponent.y / gridSize) * gridSize;

                // Update the start and end points after snapping to grid
                draggedExistingComponent.startX = draggedExistingComponent.x;
                draggedExistingComponent.startY = draggedExistingComponent.y + draggedExistingComponent.height / 2;
                draggedExistingComponent.endX = draggedExistingComponent.x + draggedExistingComponent.width;
                draggedExistingComponent.endY = draggedExistingComponent.y + draggedExistingComponent.height / 2;

                // Update wires connected to this component
                updateWiresForComponent(draggedExistingComponent);

                draggedExistingComponent = null;
                redrawCanvas();
            }
        });
    }

    private void updateWiresForComponent(ImageComponent component) {
        for (Drawable drawable : drawables) {
            if (drawable instanceof Wire) {
                Wire wire = (Wire) drawable;
                if (wire.startX == component.startX && wire.startY == component.startY) {
                    wire.startX = component.startX;
                    wire.startY = component.startY;
                }
                if (wire.endX == component.endX && wire.endY == component.endY) {
                    wire.endX = component.endX;
                    wire.endY = component.endY;
                }
            }
        }
    }

    // Base interface for all drawable objects
    private interface Drawable {
        void draw(GraphicsContext gc);
    }

    private static class ImageComponent implements Drawable {
        Image image;
        double x, y, width, height;
        double rotation = 0;
        double startX, startY, endX, endY;
        Circle startCircle, endCircle;

        ImageComponent(Image image, double x, double y, double width, double height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y + height / 2;
            this.endX = x + width;
            this.endY = y + height / 2;

            // Create circles at start and end points
            this.startCircle = new Circle(startX, startY, 8, Color.BLACK); // Change circle color to black
            this.endCircle = new Circle(endX, endY, 8, Color.BLACK); // Change circle color to black
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x + width / 2, y + height / 2);
            gc.rotate(rotation);
            gc.drawImage(image, -width / 2, -height / 2, width, height);
            gc.restore();

            // Draw start and end circles
            gc.setFill(Color.BLACK); // Change circle color to black
            gc.fillOval(startX - 8, startY - 8, 16, 16); // Increase circle size to 16x16 pixels
            gc.fillOval(endX - 8, endY - 8, 16, 16); // Increase circle size to 16x16 pixels
        }
    }

    private static class Wire implements Drawable {
        double startX, startY, endX, endY;

        Wire(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(4); // Set wire thickness to 4 pixels
            gc.strokeLine(startX, startY, endX, endY);
        }
    }

    private boolean isPowerSupply(ImageComponent component) {
        String SearchedComponent = component.image.getUrl();
        if (SearchedComponent.contains("Voltage%20Source.GIF")||SearchedComponent.contains("Current%20Source.GIF")||SearchedComponent.contains("Generator.GIF")
        ||SearchedComponent.contains("Battery%20Cell.GIF")||SearchedComponent.contains("Battery.GIF")||SearchedComponent.contains("Controlled%20Voltage%20Source.GIF")||SearchedComponent.contains("Controlled%20Current%20Source.GIF")) {
            return true;}
        else return false;
    }

    private void verifyCircuit() {
        // Find a power supply component
        ImageComponent powerSupply = null;
        for (Drawable drawable : drawables) {
            if (drawable instanceof ImageComponent) {
                ImageComponent component = (ImageComponent) drawable;
                if (isPowerSupply(component)) {
                    powerSupply = component;
                    break;
                }
            }
        }

        if (powerSupply == null) {
            System.out.println("Circuit open: No power supply found.");
            return;
        }

        // Start traversal from one end of the power supply
        double startX = powerSupply.startX;
        double startY = powerSupply.startY;

        // Use a set to keep track of visited wires and components
        Set<Drawable> visited = new HashSet<>();

        // Perform traversal
        if (traverseCircuit(startX, startY, startX, startY, visited)) {
            System.out.println("Circuit closed.");
        } else {
            System.out.println("Circuit open.");
        }
    }

    private boolean traverseCircuit(double startX, double startY, double initialX, double initialY, Set<Drawable> visited) {
        // Check if we have returned to the starting point
        if (startX == initialX && startY == initialY && !visited.isEmpty()) {
            return true; // Circuit is closed
        }

        // Look for connected wires or components
        for (Drawable drawable : drawables) {
            if (visited.contains(drawable)) {
                continue; // Skip already visited elements
            }

            if (drawable instanceof Wire) {
                Wire wire = (Wire) drawable;
                if ((wire.startX == startX && wire.startY == startY) || (wire.endX == startX && wire.endY == startY)) {
                    visited.add(wire); // Mark wire as visited
                    double nextX = (wire.startX == startX && wire.startY == startY) ? wire.endX : wire.startX;
                    double nextY = (wire.startY == startY && wire.startX == startX) ? wire.endY : wire.startY;
                    if (traverseCircuit(nextX, nextY, initialX, initialY, visited)) {
                        return true; // Continue traversal
                    }
                }
            } else if (drawable instanceof ImageComponent) {
                ImageComponent component = (ImageComponent) drawable;
                if ((component.startX == startX && component.startY == startY) || (component.endX == startX && component.endY == startY)) {
                    visited.add(component); // Mark component as visited
                    double nextX = (component.startX == startX && component.startY == startY) ? component.endX : component.startX;
                    double nextY = (component.startY == startY && component.startX == startX) ? component.endY : component.startY;
                    if (traverseCircuit(nextX, nextY, initialX, initialY, visited)) {
                        return true; // Continue traversal
                    }
                }
            }
        }

        return false; // No closed circuit found
    }

    @FXML
    private void handleVerifyCircuit(ActionEvent event) {
        verifyCircuit();
    }

    @FXML
    private void handleReset(ActionEvent event) {
        resetBuilder();
    }

    private void resetBuilder() {
        // Clear all components and wires
        drawables.clear();

        // Reset the floating component image
        floatingComponentImage.setVisible(false);
        currentlySelectedImage = null;
        floatingComponentImage.setRotate(0);
        currentRotation = 0;

        // Reset the wire drawing state
        isDrawingWire = false;
        wireStartCircle = null;

        // Redraw the canvas to reflect the reset state
        redrawCanvas();
    }

    @FXML private void handleSave(ActionEvent event) {}
    @FXML private void handleSaveAndExit(ActionEvent event) { System.exit(0); }
    @FXML private void handleExportJSON(ActionEvent event) {}
    @FXML private void handleExportCSV(ActionEvent event) {}
    @FXML private void handleExportText(ActionEvent event) {}
    @FXML private void handleExportImage(ActionEvent event) {}
    @FXML private void handleExit(ActionEvent event) { System.exit(0); }
    @FXML private void handleUndo(ActionEvent event) {}
    @FXML private void handleRedo(ActionEvent event) {}
    @FXML private void handleCopy(ActionEvent event) {}
    @FXML private void handlePaste(ActionEvent event) {}
    @FXML private void handleDelete(ActionEvent event) {
        if (selectedWire != null) {
            drawables.remove(selectedWire); // Remove the selected wire
            selectedWire = null; // Clear the selection
            redrawCanvas(); // Redraw the canvas
        }
    }
    @FXML private void handleSelectAll(ActionEvent event) {}
    @FXML private void handleColor(ActionEvent event) {}
    @FXML private void handleName(ActionEvent event) {}
    @FXML private void handleMaximizeGraph(ActionEvent event) {}
    @FXML private void handleMinimizeGraph(ActionEvent event) {}
    @FXML public void handleOpenSettings(ActionEvent event) { settingsOverlay.setVisible(true); }
    @FXML public void handleCloseSettings(ActionEvent event) { settingsOverlay.setVisible(false); }

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