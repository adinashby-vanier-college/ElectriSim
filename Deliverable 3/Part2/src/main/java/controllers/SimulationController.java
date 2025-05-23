package controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import app.saveLoadExtender;
import java.io.IOException;
import java.util.*;

public class SimulationController {
    // FXML Components
    @FXML private StackPane settingsOverlay;
    @FXML private HBox searchHbox;
    @FXML private TextField searchBar;
    @FXML private AnchorPane rootPane;
    @FXML private BorderPane mainPane;
    @FXML private Canvas builder;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane canvasContainer;
    @FXML private VBox parametersPane;
    @FXML private VBox circuitFeedbackPane;
    @FXML private ScrollPane feedbackScrollPane;
    @FXML private TextArea feedbackText;

    // Simulation State
    private static final double EPSILON = 1e-6; //For more accurate double comparison
    private final List<ComponentsController.Drawable> drawables = new ArrayList<>();
    private final double gridSize = 20; // Grid size for snapping
    private double zoomScale = 1.0; // Current zoom scale
    private final double minZoom = 0.5; // Minimum zoom level
    private final double maxZoom = 3.0; // Maximum zoom level
    // Undo/Redo functionality
    private final Stack<UndoableAction> undoStack = new Stack<>();
    private final Stack<UndoableAction> redoStack = new Stack<>();
    private boolean isUndoRedoOperation = false;
    // Component Placement
    private ImageView floatingComponentImage; // Floating image for component placement
    private Image currentlySelectedImage; // Currently selected component image
    private double selectedImageWidth = 80; // Default width for components
    private double selectedImageHeight = 80; // Default height for components
    private ComponentsController.ImageComponent draggedExistingComponent = null; // Component being dragged
    private double currentRotation = 0; // Current rotation of the component

    // Wire Drawing
    private boolean isDrawingWire = false; // Whether a wire is being drawn
    private double wireStartX, wireStartY; // Start coordinates of the wire
    @JsonIgnore
    private Circle wireStartCircle; // Start circle for wire drawing
    private ComponentsController.Wire selectedWire = null; // Currently selected wire

    // Importing Load/Save
    private saveLoadExtender sl = new saveLoadExtender();

    private CircuitAnalyzer circuitAnalyzer;

    private CircuitAnalyzerTest al = new CircuitAnalyzerTest();

    private CircuitAnalyzerTest.CircuitGraph CG = new CircuitAnalyzerTest.CircuitGraph();

    //Checking simulation status
    boolean hasVoltmeter = false;
    boolean hasAmmeter = false;
    boolean hasOhmmeter = false;
    boolean positiveConnected = false;
    boolean negativeConnected = false;

    String output = "";

    // Initialization
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

        // Initialize circuit analyzer
        circuitAnalyzer = new CircuitAnalyzer(drawables);

        // Store this instance in the parametersPane properties
        parametersPane.getProperties().put("simulationController", this);
    }

    // ==================== Component Placement ====================
    private void setupFloatingImage() {
        floatingComponentImage = new ImageView();
        floatingComponentImage.setMouseTransparent(true);
        floatingComponentImage.setVisible(false);
        rootPane.getChildren().add(floatingComponentImage);

        // Move floating image with the mouse
        rootPane.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (floatingComponentImage.isVisible()) {
                floatingComponentImage.setLayoutX(e.getSceneX() - selectedImageWidth / 2);
                floatingComponentImage.setLayoutY(e.getSceneY() - selectedImageHeight / 2);
            }
        });

        // Handle key presses for component placement
        Platform.runLater(() -> {
            rootPane.getScene().setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.E) {
                    if (draggedExistingComponent != null) {
                        // Remove the component from the drawables list
                        drawables.remove(draggedExistingComponent);
                        // Remove the component's parameter controls from the parametersPane
                        parametersPane.getChildren().remove(draggedExistingComponent.parameterControls);
                        // Reset the dragged component
                        draggedExistingComponent = null;
                        // Redraw the canvas
                        redrawCanvas();
                    } else if (floatingComponentImage.isVisible()) {
                        floatingComponentImage.setVisible(false);
                        currentlySelectedImage = null;
                    }
                } else if (e.getCode() == KeyCode.R) {
                    currentRotation = (currentRotation + 90) % 360;
                    floatingComponentImage.setRotate(currentRotation);
                }
            });
        });
    }

    // ==================== Canvas Interaction ====================
    private void setupCanvasClickPlacement() {
        builder.setOnMouseClicked(e -> {
            if (floatingComponentImage.isVisible() && currentlySelectedImage != null) {
                placeComponent(e.getX(), e.getY());
            } else {
                selectOrCreateComponent(e.getX(), e.getY());
            }
        });
    }

    private void placeComponent(double x, double y) {
        double snappedX = Math.round((x - selectedImageWidth / 2) / gridSize) * gridSize;
        double snappedY = Math.round((y - selectedImageHeight / 2) / gridSize) * gridSize;

        // Check for collisions
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (snappedX < component.x + component.width &&
                        snappedX + selectedImageWidth > component.x &&
                        snappedY < component.y + component.height &&
                        snappedY + selectedImageHeight > component.y) {
                    return; // Collision detected, don't place
                }
            }
        }

        // Create new component based on type
        String componentType = determineComponentType(currentlySelectedImage.getUrl());
        ComponentsController.ImageComponent newComponent;

        // Create the specific component type
        switch (componentType) {
            case "SPSTToggleSwitch":
                newComponent = new ComponentsController.SPSTToggleSwitch();
                break;
            case "EarthGround":
                newComponent = new ComponentsController.EarthGround(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "ResistorIEEE":
                newComponent = new ComponentsController.ResistorIEEE();
                break;
            case "PotentiometerIEEE":
                newComponent = new ComponentsController.PotentiometerIEEE(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "Capacitor":
                newComponent = new ComponentsController.Capacitor();
                break;
            case "Inductor":
                newComponent = new ComponentsController.Inductor();
                break;
            case "VoltageSource":
                newComponent = new ComponentsController.VoltageSource();
                break;
            case "BatteryCell":
                newComponent = new ComponentsController.CurrentSource();
                break;
            case "Battery":
                newComponent = new ComponentsController.Battery();
                break;
            case "Voltmeter":
                newComponent = new ComponentsController.Voltmeter(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "Ammeter":
                newComponent = new ComponentsController.Ammeter(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "Ohmmeter":
                newComponent = new ComponentsController.Ohmmeter(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "Wattmeter":
                newComponent = new ComponentsController.Wattmeter(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight);
                break;
            case "Diode":
                newComponent = new ComponentsController.Diode();
                break;
            case "Transformer":
                newComponent = new ComponentsController.Transformer();
                break;
            case "Fuse":
                newComponent = new ComponentsController.Fuse();
                break;
            case "NOTGate":
                newComponent = new ComponentsController.NOTGate();
                break;
            case "ANDGate":
                newComponent = new ComponentsController.ANDGate();
                break;
            case "NANDGate":
                newComponent = new ComponentsController.NANDGate();
                break;
            case "ORGate":
                newComponent = new ComponentsController.ORGate();
                break;
            case "NORGate":
                newComponent = new ComponentsController.NORGate();
                break;
            case "XORGate":
                newComponent = new ComponentsController.XORGate();
                break;
            default:
                newComponent = new ComponentsController.ImageComponent(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight, componentType);
                break;
        }

        // Set common properties for all components
        newComponent.x = snappedX;
        newComponent.y = snappedY;
        newComponent.width = selectedImageWidth;
        newComponent.height = selectedImageHeight;
        newComponent.image = currentlySelectedImage;
        newComponent.imageURL = currentlySelectedImage.getUrl();
        newComponent.rotation = currentRotation;
        newComponent.updateEndPoints();

        // Add to drawables list
        drawables.add(newComponent);

        // Remove any existing parameter controls before adding new ones
        if (newComponent.parameterControls != null) {
            parametersPane.getChildren().remove(newComponent.parameterControls);
        }

        // Generate new parameter controls
        ComponentsController.generateParameterControls(newComponent, parametersPane);

        // Add to undo stack
        if (!isUndoRedoOperation) {
            undoStack.push(new AddComponentAction(drawables, parametersPane, this, newComponent));
            redoStack.clear();
        }

        // Reset placement state
        floatingComponentImage.setVisible(false);
        currentlySelectedImage = null;
        floatingComponentImage.setRotate(0);
        currentRotation = 0;

        // Redraw canvas
        redrawCanvas();
    }

    // Helper method to determine the component type based on the image URL
    private String determineComponentType(String imageUrl) {
        if (imageUrl.contains("SPST%20Toggle%20Switch")) {
            return "SPSTToggleSwitch";
        } else if (imageUrl.contains("Earth%20Ground")) {
            return "EarthGround";
        } else if (imageUrl.contains("Resistor%20(IEEE)")) {
            return "ResistorIEEE";
        } else if (imageUrl.contains("Potentiometer%20(IEEE)")) {
            return "PotentiometerIEEE";
        } else if (imageUrl.contains("Capacitor")) {
            return "Capacitor";
        } else if (imageUrl.contains("Inductor")) {
            return "Inductor";
        } else if (imageUrl.contains("Voltage%20Source")) {
            return "VoltageSource";
        } else if (imageUrl.contains("Current%Source")) {
            return "CurrentSource";
        } else if (imageUrl.contains("Battery")) {
            return "Battery";
        } else if (imageUrl.contains("Voltmeter")) {
            return "Voltmeter";
        } else if (imageUrl.contains("Ammeter")) {
            return "Ammeter";
        } else if (imageUrl.contains("Ohmmeter")) {
            return "Ohmmeter";
        } else if (imageUrl.contains("Wattmeter")) {
            return "Wattmeter";
        } else if (imageUrl.contains("Diode")) {
            return "Diode";
        } else if (imageUrl.contains("Transformer")) {
            return "Transformer";
        } else if (imageUrl.contains("Fuse")) {
            return "Fuse";
        } else if (imageUrl.contains("NOT%20Gate")) {
            return "NOTGate";
        } else if (imageUrl.contains("AND%20Gate")) {
            return "ANDGate";
        } else if (imageUrl.contains("NAND%20Gate")) {
            return "NANDGate";
        } else if (imageUrl.contains("OR%20Gate")) {
            return "ORGate";
        } else if (imageUrl.contains("NOR%20Gate")) {
            return "NORGate";
        } else if (imageUrl.contains("XOR%20Gate")) {
            return "XORGate";
        }
        return "Unknown";
    }

    private void selectOrCreateComponent(double x, double y) {
        // First, deselect any previously selected wire
        if (selectedWire != null) {
            selectedWire.setSelected(false);
        }
        selectedWire = null;

        // Check if an existing component is clicked
        for (Iterator<ComponentsController.Drawable> iterator = drawables.iterator(); iterator.hasNext(); ) {
            ComponentsController.Drawable drawable = iterator.next();
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (x >= component.x && x <= component.x + component.width &&
                        y >= component.y && y <= component.y + component.height) {
                    currentlySelectedImage = component.image;
                    floatingComponentImage.setImage(currentlySelectedImage);
                    floatingComponentImage.setFitWidth(component.width);
                    floatingComponentImage.setFitHeight(component.height);
                    floatingComponentImage.setRotate(component.rotation);
                    currentRotation = component.rotation;
                    selectedImageWidth = component.width;
                    selectedImageHeight = component.height;
                    floatingComponentImage.setVisible(true);

                    // Remove the component from the drawables list
                    iterator.remove();

                    // Remove the component's parameter controls
                    ComponentsController.removeParameterControls(component, parametersPane);

                    // Remove any connected wires
                    removeConnectedWires(component);

                    redrawCanvas();
                    //updateCircuitAnalysis();
                    return;
                }
            }
        }

        // Check if a wire is clicked
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                if (isPointNearLine(x, y, wire.startX, wire.startY, wire.endX, wire.endY) ||
                        Math.hypot(x - wire.endX, y - wire.endY) <= 6) {
                    wire.setSelected(true);
                    selectedWire = wire;
                    break;
                }
            }
        }

        redrawCanvas(); // Add this to ensure the selection is visually updated
    }

    private boolean isPointNearLine(double px, double py, double x1, double y1, double x2, double y2) {
        double lineLength = Math.hypot(x2 - x1, y2 - y1);
        double distance = Math.abs((x2 - x1) * (y1 - py) - (x1 - px) * (y2 - y1)) / lineLength;
        return distance < 5; // Tolerance for wire selection
    }

    // ==================== Wire Drawing ====================
    private double wireEndX, wireEndY; // Add these variables to store the current mouse coordinates

    private void setupWireDrawing() {
        builder.setOnMousePressed(e -> {
            for (ComponentsController.Drawable drawable : drawables) {
                if (drawable instanceof ComponentsController.ImageComponent) {
                    ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                    if (component.startCircle.contains(e.getX(), e.getY())) {
                        startWireDrawing(component.startX, component.startY, component.startCircle);
                        break;
                    } else if (component.endCircle.contains(e.getX(), e.getY())) {
                        startWireDrawing(component.endX, component.endY, component.endCircle);
                        break;
                    }
                } else if (drawable instanceof ComponentsController.Wire) {
                    ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                    // Check if clicking near the wire's end circle
                    if (Math.hypot(e.getX() - wire.endX, e.getY() - wire.endY) <= 6) {
                        startWireDrawing(wire.endX, wire.endY, wire.endCircle);
                        break;
                    }
                }
            }
        });

        builder.setOnMouseDragged(e -> {
            if (isDrawingWire) {
                wireEndX = e.getX();
                wireEndY = e.getY();
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (isDrawingWire) {
                double snappedX = Math.round(e.getX() / gridSize) * gridSize;
                double snappedY = Math.round(e.getY() / gridSize) * gridSize;
                ComponentsController.Wire newWire = new ComponentsController.Wire(wireStartX, wireStartY, snappedX, snappedY);
                drawables.add(newWire);
                isDrawingWire = false;
                wireStartCircle = null;
                redrawCanvas();
                //updateCircuitAnalysis();
            }
        });
    }

    private void startWireDrawing(double startX, double startY, Circle startCircle) {
        isDrawingWire = true;
        wireStartX = startX;
        wireStartY = startY;
        wireStartCircle = startCircle;
    }

    // ==================== Canvas Drawing ====================
    private void createBuilder() {
        if (builder != null) {
            drawGrid();
        }
        addZoomFunctionality();
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

    public void redrawCanvas() {
        GraphicsContext gc = builder.getGraphicsContext2D();
        gc.clearRect(0, 0, builder.getWidth(), builder.getHeight());
        drawGrid();

        // Draw all drawable objects
        for (ComponentsController.Drawable drawable : drawables) {
            drawable.draw(gc);
        }

        // Draw temporary wire if drawing
        if (isDrawingWire) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(4);
            gc.strokeLine(wireStartX, wireStartY, wireEndX, wireEndY);
        }
    }

    // ==================== Dragging Components ====================
    private void setupDragging() {
        final double[] offsetX = {0};
        final double[] offsetY = {0};
        final double[] oldX = {0};
        final double[] oldY = {0};
        builder.setOnMousePressed(e -> {
            for (ComponentsController.Drawable drawable : drawables) {
                if (drawable instanceof ComponentsController.ImageComponent) {
                    ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                    if (e.getX() >= component.x && e.getX() <= component.x + component.width &&
                            e.getY() >= component.y && e.getY() <= component.y + component.height) {
                        draggedExistingComponent = component;
                        offsetX[0] = e.getX() - component.x;
                        offsetY[0] = e.getY() - component.y;
                        oldX[0] = component.x;
                        oldY[0] = component.y;
                        break;
                    }
                }
            }
        });

        builder.setOnMouseDragged(e -> {
            if (draggedExistingComponent != null) {
                draggedExistingComponent.x = e.getX() - offsetX[0];
                draggedExistingComponent.y = e.getY() - offsetY[0];
                updateWiresForComponent(draggedExistingComponent);
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (draggedExistingComponent != null) {
                // Add move action to undo stack
                if (!isUndoRedoOperation) {
                    undoStack.push(new MoveComponentAction(drawables, parametersPane, this, draggedExistingComponent,
                            oldX[0], oldY[0], draggedExistingComponent.x, draggedExistingComponent.y));
                    redoStack.clear();
                    System.out.println("Component moved. Undo stack size: " + undoStack.size());
                }
                draggedExistingComponent = null;

                //updateCircuitAnalysis();
            }
        });
    }

    public void updateWiresForComponent(ComponentsController.ImageComponent component) {
        // Update wire positions when component moves
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                if (wire.startX == component.x + component.width / 2 &&
                        wire.startY == component.y + component.height / 2) {
                    wire.startX = component.x + component.width / 2;
                    wire.startY = component.y + component.height / 2;
                }
                if (wire.endX == component.x + component.width / 2 &&
                        wire.endY == component.y + component.height / 2) {
                    wire.endX = component.x + component.width / 2;
                    wire.endY = component.y + component.height / 2;
                }
            }
        }
    }

    // ==================== Event Handlers ====================
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

    @FXML
    private void handleVerifyCircuit(ActionEvent event) {
        verifyCircuit();
        //updateCircuitAnalysis();
        temporaryAnalysis();
    }

    @FXML
    private void handleReset(ActionEvent event) {
        resetBuilder();
    }

    private void resetBuilder() {
        drawables.clear();
        floatingComponentImage.setVisible(false);
        currentlySelectedImage = null;
        floatingComponentImage.setRotate(0);
        currentRotation = 0;
        isDrawingWire = false;
        wireStartCircle = null;
        // Clear all parameter controls
        parametersPane.getChildren().clear();
        redrawCanvas();
        //updateCircuitAnalysis();
    }

    int numberOfComp = 0;
    int previousComp = 0;
    double battStartX = 0;
    double battStartY = 0;
    double battEndX = 0;
    double battEndY = 0;
    TreeMap<Double, ComponentsController.Drawable> priority = new TreeMap<>();
    // ==================== Helper Methods ====================
    private boolean verifyCircuit() {
        // Find a power supply component to start from
        ComponentsController.ImageComponent powerSupply = null;
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (isPowerSupply(component)) {
                    CircuitAnalyzerTest.CircuitGraph.nodes.clear();
                    CircuitAnalyzerTest.CircuitGraph.globalEdges.clear();
                    powerSupply = component;
                    battStartX = component.getStartX();
                    battStartY = component.getStartY();
                    battEndX = component.getEndX();
                    battEndY = component.getEndY();
                    numberOfComp = 0;
                    negativeConnected = false;
                    positiveConnected = false;
                    hasVoltmeter = false;
                    //This is the node for negative end
                    CG.addNode(numberOfComp+"",battStartX,battStartY,battEndX,battEndY);
                    System.out.println("Main Branch:");
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created (- end)");
                    break;
                }
            }
        }

        if (powerSupply == null) {
            addFeedbackMessage("No power supply found in the circuit.", "error");
            return false;
        }

        // Start traversal from the power supply

        addFeedbackMessage("Starting circuit verification from power supply...", "info");
        Set<ComponentsController.Drawable> visited = new HashSet<>();
        boolean isClosed = traverseCircuit(powerSupply.startX, powerSupply.startY,
                powerSupply.startX, powerSupply.startY, powerSupply,visited);

        if (isClosed) {
            addFeedbackMessage("Circuit is closed! Found a complete path.", "success");
            return true;
        } else {
            addFeedbackMessage("Circuit is open! No complete path found.", "error");
            return false;
        }

    }

    private Set<ComponentsController.Drawable> totalVisited = new HashSet<>();

    private void addUpAddTheComponents () {
        for (ComponentsController.Drawable currentComponent: drawables) {
            double startX, startY, endX, endY;
            if (!totalVisited.contains(currentComponent)) {
            }
        }
    }



    private boolean traverseCircuit(double startX, double startY, double initialX, double initialY, ComponentsController.Drawable currentDraw, Set<ComponentsController.Drawable> visited) {
        // If we've reached the initial point and visited at least one component, we've found a closed circuit
        totalVisited = visited;
        if (positiveConnected && negativeConnected) {
            addFeedbackMessage("Found closed circuit! Reached initial point.", "success");
            return true;
        }
        /*

        if ((Math.abs(currentDraw.getXStart() - initialX) < EPSILON &&
                Math.abs(currentDraw.getYStart()- initialY) < EPSILON &&
                !visited.isEmpty()) || (Math.abs(currentDraw.getXEnd() - initialX) < EPSILON &&
                Math.abs(currentDraw.getYEnd()- initialY) < EPSILON &&
                !visited.isEmpty())) {
            addFeedbackMessage("Found closed circuit! Reached initial point.", "success");
            return true;
        }
         */

        // Check all drawables for connections

        if (currentDraw instanceof ComponentsController.Wire) {
            ComponentsController.Wire wire = (ComponentsController.Wire) currentDraw;
            // Check if this wire connects to our current position
            if ((wire.startX == startX && wire.startY == startY) || (wire.endX == startX && wire.endY == startY)) {
                previousComp = numberOfComp;
                System.out.println("Wire");
                System.out.println("PreviousComp: " +previousComp);
                numberOfComp++;
                System.out.println("Coordinates of wire: " +wire.startX +", " + wire.startY + ", " + wire.endX + ", " + wire.endY);
                System.out.println("Coordinates of batt: " +battStartX+", " +battStartY + ", " + battEndX + ", " + battEndY);
                if ((wire.startX == battStartX && wire.startY == battStartY && previousComp != 0) || (wire.endX == battStartX && wire.endY == battStartY && previousComp != 0)) {
                    CG.addEdge(previousComp+"", "0", wire);
                    System.out.println("- connected (from wire to bat)");
                    negativeConnected = true;
                }
                else if ((wire.startX == battEndX && wire.startY == battEndY && previousComp != 0) || (wire.endX == battEndX && wire.endY == battEndY && previousComp != 0)) {
                    CG.addNode(numberOfComp+"",wire.startX,wire.startY,wire.endX,wire.endY);
                    CG.addEdge("1", numberOfComp+"", wire);
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created");
                    System.out.println("+ connected (from bat to wire)");
                    positiveConnected = true;
                }
                else {
                    CG.addNode(numberOfComp+"",wire.startX,wire.startY,wire.endX,wire.endY);
                    CG.addEdge(previousComp+"", numberOfComp+"", wire);
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created");
                }
                System.out.println("Current edges: ");
                int i = 0;
                for (CircuitAnalyzerTest.CircuitGraph.Edge edge : CG.getEdges()) {
                    System.out.println("Edge number: " + i);
                    System.out.println("From: " + edge.from.id);
                    System.out.println("to: " + edge.to.id);
                    i++;
                }
                //System.out.println(CG.getEdges().get(numberOfComp - 1).component.toString());
                addFeedbackMessage("Wire: start(" + wire.startX + "," + wire.startY + "), end(" + wire.endX + "," + wire.endY + ")", "info");
                visited.add(wire); // Mark wire as visited
                // Move to the other end of the wire
                boolean match = wire.startX == startX && wire.startY == startY;

                double nextX = match ? wire.endX : wire.startX;
                double nextY = match ? wire.endY : wire.startY;
                priority.clear();
                for (ComponentsController.Drawable draw: drawables) {
                    if (visited.contains(draw)) {
                        continue;
                    }
                    if (Math.abs(draw.getXStart() - nextX) < EPSILON &&
                            Math.abs(draw.getYStart() - nextY) < EPSILON) {
                        double distance = distanceBetween(draw.getXEnd(), draw.getYEnd(), battStartX, battStartY) - distanceBetween(draw.getXEnd(), draw.getYEnd(), battEndX, battEndY);
                        priority.put(distance, draw);
                    }
                    else if (Math.abs(draw.getXEnd() - nextX) < EPSILON &&
                            Math.abs(draw.getYEnd() - nextY) < EPSILON) {
                        double distance = distanceBetween(draw.getXStart(), draw.getYStart(), battStartX, battStartY) - distanceBetween(draw.getXStart(), draw.getYStart(), battEndX, battEndY);
                        priority.put(distance, draw);
                    }
                }
                ComponentsController.Drawable nextDraw;
                if (!priority.isEmpty()) {
                    double smallest = priority.firstKey();
                    nextDraw = priority.get(smallest);
                    System.out.println("Main is going to: " + nextDraw.toString());
                    System.out.println();
                    if (traverseCircuit(nextX, nextY, initialX, initialY, nextDraw,visited)) {
                        return true;
                    }
                }
                else {
                    System.out.println("Fail to load next main line or the circuit ends here,");
                    System.out.println();
                }
            }
        } else if (currentDraw instanceof ComponentsController.ImageComponent) {
            ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) currentDraw;
            // Check if this component connects to our current position
            if ((component.startX == startX && component.startY == startY) ||
                    (component.endX == startX && component.endY == startY)) {
                // Skip meters as they don't affect circuit closure
                if (component instanceof ComponentsController.Voltmeter ||
                        component instanceof ComponentsController.Ammeter ||
                        component instanceof ComponentsController.Ohmmeter) {
                    addFeedbackMessage("Meter: start(" + component.startX + "," + component.startY + "), end(" + component.endX + "," + component.endY + ")", "info");
                    visited.add(component);
                    boolean match = component.startX == startX && component.startY == startY;
                    double nextX = match ? component.endX : component.startX;
                    double nextY = match ? component.endY : component.startY;
                    if (component instanceof ComponentsController.Voltmeter) {
                        hasVoltmeter = true;
                    }
                    else if (component instanceof ComponentsController.Ammeter) {
                        hasAmmeter = true;
                    }
                    else {
                        hasOhmmeter = true;
                    }
                    priority.clear();
                    for (ComponentsController.Drawable draw: drawables) {
                        if (visited.contains(draw)) {
                            continue;
                        }
                        if (Math.abs(draw.getXStart() - nextX) < EPSILON &&
                                Math.abs(draw.getYStart() - nextY) < EPSILON) {
                            double distance = distanceBetween(draw.getXEnd(), draw.getYEnd(), battStartX, battStartY)  - distanceBetween(draw.getXEnd(), draw.getYEnd(), battEndX, battEndY);
                            priority.put(distance, draw);
                            System.out.println(draw.toString());
                        }
                        else if (Math.abs(draw.getXEnd() - nextX) < EPSILON &&
                                Math.abs(draw.getYEnd() - nextY) < EPSILON) {
                            double distance = distanceBetween(draw.getXStart(), draw.getYStart(), battStartX, battStartY) - distanceBetween(draw.getXStart(), draw.getYStart(), battEndX, battEndY);
                            priority.put(distance, draw);
                            System.out.println(draw.toString());
                        }
                    }
                    ComponentsController.Drawable nextDraw;
                    if (!priority.isEmpty()) {
                        double smallest = priority.firstKey();
                        nextDraw = priority.get(smallest);
                        if (traverseCircuit(nextX, nextY, initialX, initialY, nextDraw ,visited)) {
                            return true;
                        }
                    }
                    else {
                        System.out.println("Fail!");
                        System.out.println();
                    }
                }
                // Check switch state
                boolean isSwitchClosed = true;
                if (component instanceof ComponentsController.SPSTToggleSwitch) {
                    ComponentsController.SPSTToggleSwitch spstSwitch = (ComponentsController.SPSTToggleSwitch) component;
                    isSwitchClosed = spstSwitch.isClosed;
                    addFeedbackMessage("SPST Toggle Switch: start(" + component.startX + "," + component.startY + "), end(" + component.endX + "," + component.endY + "), state(" + (isSwitchClosed ? "closed" : "open") + ")", "info");
                } else if (component instanceof ComponentsController.ResistorIEEE) {
                    addFeedbackMessage("Resistor: start(" + component.startX + "," + component.startY + "), end(" + component.endX + "," + component.endY + "), resistance(" + component.resistance + "Ω)", "info");
                } else if (component instanceof ComponentsController.VoltageSource) {
                    addFeedbackMessage("Voltage Source: start(" + component.startX + "," + component.startY + "), end(" + component.endX + "," + component.endY + "), voltage(" + component.voltage + "V)", "info");
                } else if (component instanceof ComponentsController.Battery) {
                    addFeedbackMessage("Battery: start(" + component.startX + "," + component.startY + "), end(" + component.endX + "," + component.endY + "), voltage(" + component.voltage + "V)", "info");
                }

                // If it's a switch and it's open, the circuit is open
                if (!isSwitchClosed) {
                    addFeedbackMessage("Found open switch, circuit is open!", "error");
                    return false;
                }

                // If we get here, either it's not a switch or the switch is closed
                previousComp = numberOfComp;
                System.out.println("Component");
                System.out.println("PreviousComp: " +previousComp);
                numberOfComp++;
                System.out.println("Component: Type: " + component.imageURL);
                System.out.println("Coordinates of comp: " +component.startX +", " + component.startY + ", " + component.endX + ", " + component.endY);
                System.out.println("Coordinates of batt: " +battStartX+", " +battStartY + ", " + battEndX + ", " + battEndY);
                if (component.endX == battStartX && component.endY == battStartY && previousComp != 0 || (component.startX == battStartX && component.startY == battStartY && previousComp != 0)) {
                    CG.addEdge(previousComp+"", "0", component);
                    System.out.println("- connected (from comp to bat");
                    negativeConnected = true;
                }
                else if ((component.startX == battEndX && component.startY == battEndY && previousComp != 0) || (component.endX == battEndX && component.endY == battEndY && previousComp != 0)) {
                    CG.addNode(numberOfComp+"",component.startX,component.startY,component.endX,component.endY);
                    CG.addEdge("1", numberOfComp+"", component);
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created");
                    System.out.println("+ connected (from bat to comp)");
                    positiveConnected = true;
                }
                else {
                    CG.addNode(numberOfComp+"",component.startX,component.startY,component.endX,component.endY);
                    CG.addEdge(previousComp+"", numberOfComp+"", component);
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created");
                }
                System.out.println("Current edges: ");
                int i = 0;
                for (CircuitAnalyzerTest.CircuitGraph.Edge edge : CG.getEdges()) {
                    System.out.println("Edge number: " + i);
                    System.out.println("From: " + edge.from.id);
                    System.out.println("to: " + edge.to.id);
                    i++;
                }
                //System.out.println(CG.getEdges().get(numberOfComp - 1).component.toString());
                visited.add(component);
                boolean match = component.startX == startX && component.startY == startY;

                double nextX = match ? component.endX : component.startX;
                double nextY = match ? component.endY : component.startY;

                priority.clear();
                for (ComponentsController.Drawable draw: drawables) {
                    if (visited.contains(draw)) {
                        continue;
                    }
                    if (Math.abs(draw.getXStart() - nextX) < EPSILON &&
                            Math.abs(draw.getYStart() - nextY) < EPSILON) {
                        double distance = distanceBetween(draw.getXEnd(), draw.getYEnd(), battStartX, battStartY) - distanceBetween(draw.getXEnd(), draw.getYEnd(), battEndX, battEndY);
                        priority.put(distance, draw);
                        System.out.println(draw.toString());
                    }
                    else if (Math.abs(draw.getXEnd() - nextX) < EPSILON &&
                            Math.abs(draw.getYEnd() - nextY) < EPSILON) {
                        double distance = distanceBetween(draw.getXStart(), draw.getYStart(), battStartX, battStartY) - distanceBetween(draw.getXStart(), draw.getYStart(), battEndX, battEndY);
                        priority.put(distance, draw);
                        System.out.println(draw.toString());
                    }
                }
                ComponentsController.Drawable nextDraw;
                if (!priority.isEmpty() && (!(positiveConnected) || !(negativeConnected))) {
                    double smallest = priority.firstKey();
                    nextDraw = priority.get(smallest);
                    System.out.println("Main is going to: " + nextDraw.toString());
                    System.out.println();
                    if (traverseCircuit(nextX, nextY, initialX, initialY, nextDraw,visited)) {
                        return true;
                    }
                }
                else {
                    System.out.println("Failed to get next main line or the circuit ends here");
                    System.out.println();
                }
            }
        }
        if (positiveConnected && negativeConnected) {
            addFeedbackMessage("Found closed circuit! Reached initial point.", "success");
            return true;
        }
        addFeedbackMessage("No closed circuit found from current position", "error");
        return false; // No closed circuit found
    }

    private double distanceBetween (double x1, double y1, double x2, double y2) {
        double xf = Math.abs(x1 - x2);
        double yf = Math.abs(y1-y2);
        return Math.sqrt(xf*xf + yf*yf);
    }



    @FXML private void handleSave(ActionEvent event) {}
    @FXML private void handleSaveAndExit(ActionEvent event) { System.exit(0); }
    @FXML private void handleExportJSON(ActionEvent event) {
        String filename = "src/main/resources/json/save1.json";
        sl.jsonWriter(filename, drawables);
    }
    @FXML private void handleExportCSV(ActionEvent event) {
        GraphicsContext gc = builder.getGraphicsContext2D();
        String filename = "src/main/resources/json/save1.json";
        resetBuilder();
        for (ComponentsController.Drawable draw: sl.jsonReader(filename)) {
            if (draw instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent image = (ComponentsController.ImageComponent) draw;
                ComponentsController.generateParameterControls(image , parametersPane);
                String imagePath = image.getImageURL();
                System.out.println(imagePath);
                image.setImage(new Image(imagePath));
                drawables.add(image);
                image.draw(gc);
            }
            else {
                drawables.add(draw);
                draw.draw(gc);
            }

        }
        System.out.println(drawables.toString());
    }
    @FXML private void handleExportText(ActionEvent event) {}
    @FXML private void handleExportImage(ActionEvent event) {}
    @FXML private void handleExit(ActionEvent event) { System.exit(0); }
    @FXML private void handleUndo(ActionEvent event) {
        if (!undoStack.isEmpty()) {
            isUndoRedoOperation = true;
            UndoableAction action = undoStack.pop();
            action.undo();
            redoStack.push(action);
            isUndoRedoOperation = false;
            System.out.println("Undo performed. Undo stack size: " + undoStack.size() +
                    ", Redo stack size: " + redoStack.size());
        }
    }
    @FXML private void handleRedo(ActionEvent event) {
        if (!redoStack.isEmpty()) {
            isUndoRedoOperation = true;
            UndoableAction action = redoStack.pop();
            action.redo();
            undoStack.push(action);
            isUndoRedoOperation = false;
            System.out.println("Redo performed. Undo stack size: " + undoStack.size() +
                    ", Redo stack size: " + redoStack.size());
        }
    }
    @FXML private void handleCopy(ActionEvent event) {}
    @FXML private void handlePaste(ActionEvent event) {}
    @FXML private void handleDelete(ActionEvent event) {
        if (selectedWire != null) {
            // Store wire for undo
            ComponentsController.Wire wireToDelete = selectedWire;

            // Remove the wire
            drawables.remove(selectedWire);
            selectedWire = null;
            // Add to undo stack
            if (!isUndoRedoOperation) {
                undoStack.push(new DeleteComponentAction(drawables, parametersPane, this, wireToDelete));
                redoStack.clear();
                System.out.println("Wire deleted. Undo stack size: " + undoStack.size());
            }

            redrawCanvas();
        } else if (draggedExistingComponent != null) {
            // Store component for undo
            ComponentsController.ImageComponent componentToDelete = draggedExistingComponent;

            // Remove the component
            drawables.remove(draggedExistingComponent);

            parametersPane.getChildren().remove(draggedExistingComponent.parameterControls);
            removeConnectedWires(draggedExistingComponent);


            draggedExistingComponent = null;
            // Add to undo stack
            if (!isUndoRedoOperation) {
                undoStack.push(new DeleteComponentAction(drawables, parametersPane, this, componentToDelete));
                redoStack.clear();
                System.out.println("Component deleted. Undo stack size: " + undoStack.size());
            }

            redrawCanvas();
            //updateCircuitAnalysis();
        }
    }
    @FXML private void handleSelectAll(ActionEvent event) {}
    @FXML private void handleColor(ActionEvent event) {}
    @FXML private void handleName(ActionEvent event) {}
    @FXML private void handleMaximizeGraph(ActionEvent event) {}
    @FXML private void handleMinimizeGraph(ActionEvent event) {}
    @FXML public void handleOpenSettings(ActionEvent event) {
        settingsOverlay.setVisible(true);
    }
    @FXML public void handleCloseSettings(ActionEvent event) {
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

    public void updateCircuitAnalysis() {
        circuitAnalyzer = new CircuitAnalyzer(drawables);
        circuitAnalyzer.analyzeCircuit();

        // Print debug information about the circuit analysis
        circuitAnalyzer.debugPrintState();

        // Update all meter measurements and display them in feedback
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component instanceof ComponentsController.Voltmeter) {
                    ComponentsController.Voltmeter voltmeter = (ComponentsController.Voltmeter) component;
                    voltmeter.setAnalyzer(circuitAnalyzer);
                    // Find the component being measured
                    ComponentsController.ImageComponent measuredComponent = circuitAnalyzer.findMeasuredComponent(voltmeter);
                    if (measuredComponent != null) {
                        addFeedbackMessage("Voltmeter measuring " + measuredComponent.componentType + ": " +
                                String.format("%.2f V", voltmeter.getVoltage()), "info");
                    } else {
                        addFeedbackMessage("Voltmeter: " + String.format("%.2f V", voltmeter.getVoltage()), "info");
                    }
                } else if (component instanceof ComponentsController.Ammeter) {
                    ComponentsController.Ammeter ammeter = (ComponentsController.Ammeter) component;
                    ammeter.setAnalyzer(circuitAnalyzer);
                    addFeedbackMessage("Ammeter: " + String.format("%.2f A", ammeter.getCurrent()), "info");
                } else if (component instanceof ComponentsController.Ohmmeter) {
                    ComponentsController.Ohmmeter ohmmeter = (ComponentsController.Ohmmeter) component;
                    ohmmeter.setAnalyzer(circuitAnalyzer);
                    addFeedbackMessage("Ohmmeter: " + String.format("%.2f Ω", ohmmeter.getResistance()), "info");
                }
            }
        }
    }

    private void displayMeterMeasurements() {
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component instanceof ComponentsController.Voltmeter) {
                    ComponentsController.Voltmeter voltmeter = (ComponentsController.Voltmeter) component;
                    addFeedbackMessage("Voltmeter: " + String.format("%.2f V", voltmeter.getVoltage()), "info");
                } else if (component instanceof ComponentsController.Ammeter) {
                    ComponentsController.Ammeter ammeter = (ComponentsController.Ammeter) component;
                    addFeedbackMessage("Ammeter: " + String.format("%.2f A", ammeter.getCurrent()), "info");
                } else if (component instanceof ComponentsController.Ohmmeter) {
                    ComponentsController.Ohmmeter ohmmeter = (ComponentsController.Ohmmeter) component;
                    addFeedbackMessage("Ohmmeter: " + String.format("%.2f Ω", ohmmeter.getResistance()), "info");
                }
            }
        }
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

    private void centerScrollBars() {
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    private boolean isPowerSupply(ComponentsController.ImageComponent component) {
        String searchedComponent = component.image.getUrl();
        return searchedComponent.contains("Voltage%20Source.GIF") ||
                searchedComponent.contains("Current%20Source.GIF") ||
                searchedComponent.contains("Generator.GIF") ||
                searchedComponent.contains("Battery%20Cell.GIF") ||
                searchedComponent.contains("Battery.GIF") ||
                searchedComponent.contains("Controlled%20Voltage%20Source.GIF") ||
                searchedComponent.contains("Controlled%20Current%20Source.GIF");
    }

    private void addFeedbackMessage(String message, String type) {
        // Create a container for the message
        HBox messageContainer = new HBox(10);
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setMaxWidth(Double.MAX_VALUE);
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        // Create timestamp
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(timestamp);
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        // Apply styling based on message type
        switch (type) {
            case "error":
                messageLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                break;
            case "success":
                messageLabel.setStyle("-fx-text-fill: #44ff44; -fx-font-weight: bold;");
                break;
            case "info":
                messageLabel.setStyle("-fx-text-fill: #ffffff;");
                break;
        }

        // Add components to container
        messageContainer.getChildren().addAll(timeLabel, messageLabel);

        // Add the message container to the feedback pane
        circuitFeedbackPane.getChildren().add(messageContainer);

        // Ensure the feedback pane is laid out before scrolling
        circuitFeedbackPane.layout();

        // Use Platform.runLater to ensure scrolling happens after the UI is updated
        Platform.runLater(() -> {
            feedbackScrollPane.setVvalue(1.0);
            // Force a second scroll after a short delay to ensure it works
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> feedbackScrollPane.setVvalue(1.0));
                }
            }, 100);
        });
    }

    private void removeConnectedWires(ComponentsController.ImageComponent component) {
        // Remove any wires connected to this component
        Iterator<ComponentsController.Drawable> iterator = drawables.iterator();
        while (iterator.hasNext()) {
            ComponentsController.Drawable drawable = iterator.next();
            if (drawable instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                if ((wire.startX == component.startX && wire.startY == component.startY) ||
                        (wire.startX == component.endX && wire.startY == component.endY) ||
                        (wire.endX == component.startX && wire.endY == component.startY) ||
                        (wire.endX == component.endX && wire.endY == component.endY)) {
                    iterator.remove();
                }
            }
        }
    }

    private void temporaryAnalysis() {
        if (positiveConnected && negativeConnected) {
            double totalResistanceSeries = 0;
            double sourceVolt = 0;
            double currentSeries = 0;
            for (CircuitAnalyzerTest.CircuitGraph.Edge ed : CG.getEdges()) {
                totalResistanceSeries += ed.component.getResistance();
                if (ed.component instanceof ComponentsController.Battery) {
                    sourceVolt += ed.component.getVoltage();
                }
            }
            if (totalResistanceSeries != 0) {
                currentSeries = sourceVolt/totalResistanceSeries;
                for (CircuitAnalyzerTest.CircuitGraph.Edge ed : CG.getEdges()) {
                    if (ed.component instanceof ComponentsController.ImageComponent) {
                        ((ComponentsController.ImageComponent) ed.component).setCurrent(currentSeries);
                        if(ed.component instanceof ComponentsController.ResistorIEEE) {
                            ((ComponentsController.ResistorIEEE) ed.component).setVoltage(ed.component.getResistance()*ed.component.getCurrent());
                        }
                        String message = "The info of the following component: " + ((ComponentsController.ImageComponent) ed.component).componentType + " from node " + ed.from.id +" to node " +ed.to.id
                                + " has resistance of " + ed.component.getResistance() + ", " + " current of " + ed.component.getCurrent() + " and voltage of " + ed.component.getVoltage();
                        addFeedbackMessage(message,"info");
                        output = output + "\n" + message;

                    }
                }
            }
            String message = "The Req of the main line is: " + totalResistanceSeries + " and current flowing on it is: " + currentSeries;
            output = output + "\n" + message;
            System.out.println(output);
            addFeedbackMessage(message, "info");

        }

    }
}