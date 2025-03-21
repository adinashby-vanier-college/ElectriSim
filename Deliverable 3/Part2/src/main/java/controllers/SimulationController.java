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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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

    private List<ImageComponent> components = new ArrayList<>();
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
            if (floatingComponentImage.isVisible() && currentlySelectedImage != null) {
                double snappedX = Math.round((e.getX() - selectedImageWidth / 2) / gridSize) * gridSize;
                double snappedY = Math.round((e.getY() - selectedImageHeight / 2) / gridSize) * gridSize;

                for (ImageComponent existing : components) {
                    if (snappedX < existing.x + existing.width && snappedX + selectedImageWidth > existing.x &&
                            snappedY < existing.y + existing.height && snappedY + selectedImageHeight > existing.y) {
                        return;
                    }
                }

                ImageComponent newComponent = new ImageComponent(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                newComponent.rotation = currentRotation;
                components.add(newComponent);
                redrawCanvas();

                floatingComponentImage.setVisible(false);
                currentlySelectedImage = null;
                floatingComponentImage.setRotate(0);
                currentRotation = 0;
                return;
            }

            for (Iterator<ImageComponent> iterator = components.iterator(); iterator.hasNext(); ) {
                ImageComponent component = iterator.next();
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

        gc.setFill(javafx.scene.paint.Color.DARKGRAY);
        gc.fillRect(0, 0, width, height);

        gc.setStroke(javafx.scene.paint.Color.BLACK);
        gc.setLineWidth(1);

        for (double x = 0; x <= width; x += gridSize) gc.strokeLine(x, 0, x, height);
        for (double y = 0; y <= height; y += gridSize) gc.strokeLine(0, y, width, y);
    }

    private void redrawCanvas() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        gc.clearRect(0, 0, builder.getWidth(), builder.getHeight());
        drawGrid();
        for (ImageComponent component : components) {
            gc.save();
            gc.translate(component.x + component.width / 2, component.y + component.height / 2);
            gc.rotate(component.rotation);
            gc.drawImage(component.image, -component.width / 2, -component.height / 2, component.width, component.height);
            gc.restore();
        }
    }

    private void setupDragging() {
        final double[] offsetX = {0};
        final double[] offsetY = {0};

        builder.setOnMousePressed(e -> {
            for (ImageComponent component : components) {
                if (e.getX() >= component.x && e.getX() <= component.x + component.width &&
                        e.getY() >= component.y && e.getY() <= component.y + component.height) {
                    draggedExistingComponent = component;
                    offsetX[0] = e.getX() - component.x;
                    offsetY[0] = e.getY() - component.y;
                    break;
                }
            }
        });

        builder.setOnMouseDragged(e -> {
            if (draggedExistingComponent != null) {
                double newX = e.getX() - offsetX[0];
                double newY = e.getY() - offsetY[0];

                draggedExistingComponent.x = newX;
                draggedExistingComponent.y = newY;
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (draggedExistingComponent != null) {
                draggedExistingComponent.x = Math.round(draggedExistingComponent.x / gridSize) * gridSize;
                draggedExistingComponent.y = Math.round(draggedExistingComponent.y / gridSize) * gridSize;
                draggedExistingComponent = null;
                redrawCanvas();
            }
        });
    }

    private static class ImageComponent {
        Image image;
        double x, y, width, height;
        double rotation = 0;

        ImageComponent(Image image, double x, double y, double width, double height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
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
    @FXML private void handleDelete(ActionEvent event) {}
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

class CircuitVerifier {
    private Map<String, List<String>> graph = new HashMap<>();
    private Map<String, Double> voltages = new HashMap<>();
    private Map<String, Double> resistances = new HashMap<>();
    private Map<String, Double> currents = new HashMap<>();

    public void addComponent(String id, double resistance) {
        graph.putIfAbsent(id, new ArrayList<>());
        resistances.put(id, resistance);
    }

    public void addBattery(String id, double voltage) {
        graph.putIfAbsent(id, new ArrayList<>());
        voltages.put(id, voltage);
    }

    public void connect(String a, String b) {
        graph.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        graph.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    public boolean isCircuitClosed() {
        if (voltages.isEmpty()) return false;
        String start = voltages.keySet().iterator().next();
        Set<String> visited = new HashSet<>();
        return dfs(start, visited, null);
    }

    private boolean dfs(String node, Set<String> visited, String parent) {
        if (visited.contains(node)) return true;
        visited.add(node);
        for (String neighbor : graph.getOrDefault(node, new ArrayList<>())) {
            if (!neighbor.equals(parent) && dfs(neighbor, visited, node)) {
                return true;
            }
        }
        return false;
    }

    public void calculateCurrents() {
        if (!isCircuitClosed()) {
            System.out.println("Open circuit! No current flows.");
            return;
        }

        for (String node : voltages.keySet()) {
            double voltage = voltages.get(node);
            for (String neighbor : graph.get(node)) {
                if (resistances.containsKey(neighbor)) {
                    double resistance = resistances.get(neighbor);
                    double current = voltage / resistance;
                    currents.put(neighbor, current);
                    System.out.println("Current through " + neighbor + " = " + current + "A");
                }
            }
        }
    }
}