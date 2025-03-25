package controllers;

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

    // Simulation State
    private final List<ComponentsController.Drawable> drawables = new ArrayList<>();
    private final double gridSize = 20; // Grid size for snapping
    private double zoomScale = 1.0; // Current zoom scale
    private final double minZoom = 0.5; // Minimum zoom level
    private final double maxZoom = 3.0; // Maximum zoom level

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
    private Circle wireStartCircle; // Start circle for wire drawing
    private ComponentsController.Wire selectedWire = null; // Currently selected wire

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

        // Create new component
        String componentType = determineComponentType(currentlySelectedImage.getUrl());
        ComponentsController.ImageComponent newComponent = new ComponentsController.ImageComponent(
                currentlySelectedImage, snappedX, snappedY,
                selectedImageWidth, selectedImageHeight, componentType);

        newComponent.rotation = currentRotation;
        newComponent.updateEndPoints();

        // Add to drawables list - this is the corrected line
        drawables.add(newComponent);
        redrawCanvas();

        // Update parameter controls
        if (newComponent.parameterControls != null) {
            parametersPane.getChildren().remove(newComponent.parameterControls);
        }

        ComponentsController.generateParameterControls(newComponent, parametersPane);

        // Reset placement state
        floatingComponentImage.setVisible(false);
        currentlySelectedImage = null;
        floatingComponentImage.setRotate(0);
        currentRotation = 0;
    }

    // Helper method to determine the component type based on the image URL
    private String determineComponentType(String imageUrl) {
        if (imageUrl.contains("SPST%20Toggle%20Switch")) {
            return "SPSTToggleSwitch";
        } else if (imageUrl.contains("SPDT%20Toggle%20Switch")) {
            return "SPDTToggleSwitch";
        } else if (imageUrl.contains("Pushbutton%20Switch%20NO")) {
            return "PushbuttonSwitchNO";
        } else if (imageUrl.contains("Pushbutton%20Switch%20NC")) {
            return "PushbuttonSwitchNC";
        } else if (imageUrl.contains("DIP%20Switch")) {
            return "DIPSwitch";
        } else if (imageUrl.contains("SPST%20Relay")) {
            return "SPSTRelay";
        } else if (imageUrl.contains("SPDT%20Relay")) {
            return "SPDTRelay";
        } else if (imageUrl.contains("Jumper")) {
            return "Jumper";
        } else if (imageUrl.contains("Solder%20Bridge")) {
            return "SolderBridge";
        } else if (imageUrl.contains("Earth%20Ground")) {
            return "EarthGround";
        } else if (imageUrl.contains("Chassis%20Ground")) {
            return "ChassisGround";
        } else if (imageUrl.contains("Digital%20Ground")) {
            return "DigitalGround";
        } else if (imageUrl.contains("Resistor%20(IEEE)")) {
            return "ResistorIEEE";
        } else if (imageUrl.contains("Resistor%20(IEC)")) {
            return "ResistorIEC";
        } else if (imageUrl.contains("Potentiometer%20(IEEE)")) {
            return "PotentiometerIEEE";
        } else if (imageUrl.contains("Potentiometer%20(IEC)")) {
            return "PotentiometerIEC";
        } else if (imageUrl.contains("Rheostat%20(IEEE)")) {
            return "RheostatIEEE";
        } else if (imageUrl.contains("Rheostat%20(IEC)")) {
            return "RheostatIEC";
        } else if (imageUrl.contains("Thermistor")) {
            return "Thermistor";
        } else if (imageUrl.contains("Photoresistor")) {
            return "Photoresistor";
        } else if (imageUrl.contains("Capacitor")) {
            return "Capacitor";
        } else if (imageUrl.contains("Polarized%20Capacitor")) {
            return "PolarizedCapacitor";
        } else if (imageUrl.contains("Variable%20Capacitor")) {
            return "VariableCapacitor";
        } else if (imageUrl.contains("Inductor")) {
            return "Inductor";
        } else if (imageUrl.contains("Iron%20Core%20Inductor")) {
            return "IronCoreInductor";
        } else if (imageUrl.contains("Variable%20Inductor")) {
            return "VariableInductor";
        } else if (imageUrl.contains("Voltage%20Source")) {
            return "VoltageSource";
        } else if (imageUrl.contains("Current%20Source")) {
            return "CurrentSource";
        } else if (imageUrl.contains("Generator")) {
            return "Generator";
        } else if (imageUrl.contains("Battery%20Cell")) {
            return "BatteryCell";
        } else if (imageUrl.contains("Battery")) {
            return "Battery";
        } else if (imageUrl.contains("Controlled%20Voltage%20Source")) {
            return "ControlledVoltageSource";
        } else if (imageUrl.contains("Controlled%20Current%20Source")) {
            return "ControlledCurrentSource";
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
        } else if (imageUrl.contains("Zener%20Diode")) {
            return "ZenerDiode";
        } else if (imageUrl.contains("Schottky%20Diode")) {
            return "SchottkyDiode";
        } else if (imageUrl.contains("Varactor")) {
            return "Varactor";
        } else if (imageUrl.contains("Tunnel%20Diode")) {
            return "TunnelDiode";
        } else if (imageUrl.contains("Light%20Emitting%20Diode")) {
            return "LightEmittingDiode";
        } else if (imageUrl.contains("Photodiode")) {
            return "Photodiode";
        } else if (imageUrl.contains("NPN%20Bipolar%20Transistor")) {
            return "NPNBipolarTransistor";
        } else if (imageUrl.contains("PNP%20Bipolar%20Transistor")) {
            return "PNPBipolarTransistor";
        } else if (imageUrl.contains("Darlington%20Transistor")) {
            return "DarlingtonTransistor";
        } else if (imageUrl.contains("JFET%20N%20Transistor")) {
            return "JFETNTransistor";
        } else if (imageUrl.contains("JFET%20P%20Transistor")) {
            return "JFETPTransistor";
        } else if (imageUrl.contains("NMOS%20Transistor")) {
            return "NMOSTransistor";
        } else if (imageUrl.contains("PMOS%20Transistor")) {
            return "PMOSTransistor";
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
        } else if (imageUrl.contains("D%20Flip%20Flop")) {
            return "DFlipFlop";
        } else if (imageUrl.contains("Multiplexer%202to1")) {
            return "Multiplexer2to1";
        } else if (imageUrl.contains("Multiplexer%204to1")) {
            return "Multiplexer4to1";
        } else if (imageUrl.contains("Demultiplexer%201to4")) {
            return "Demultiplexer1to4";
        } else if (imageUrl.contains("Antenna")) {
            return "Antenna";
        } else if (imageUrl.contains("Dipole%20Antenna")) {
            return "DipoleAntenna";
        } else if (imageUrl.contains("Motor")) {
            return "Motor";
        } else if (imageUrl.contains("Transformer")) {
            return "Transformer";
        } else if (imageUrl.contains("Fuse")) {
            return "Fuse";
        } else if (imageUrl.contains("Optocoupler")) {
            return "Optocoupler";
        } else if (imageUrl.contains("Loudspeaker")) {
            return "Loudspeaker";
        } else if (imageUrl.contains("Microphone")) {
            return "Microphone";
        } else if (imageUrl.contains("Operational%20Amplifier")) {
            return "OperationalAmplifier";
        } else if (imageUrl.contains("Schmitt%20Trigger")) {
            return "SchmittTrigger";
        } else if (imageUrl.contains("Analog%20To%20Digital%20Converter")) {
            return "AnalogToDigitalConverter";
        } else if (imageUrl.contains("Digital%20To%20Analog%20Converter")) {
            return "DigitalToAnalogConverter";
        } else if (imageUrl.contains("Crystal%20Oscillator")) {
            return "CrystalOscillator";
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
                    // Remove the component's parameter controls from the parametersPane
                    if (component.parameterControls != null) {
                        parametersPane.getChildren().remove(component.parameterControls);
                    }

                    redrawCanvas();
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

    private void redrawCanvas() {
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

        builder.setOnMousePressed(e -> {
            for (ComponentsController.Drawable drawable : drawables) {
                if (drawable instanceof ComponentsController.ImageComponent) {
                    ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
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
                draggedExistingComponent.x = e.getX() - offsetX[0];
                draggedExistingComponent.y = e.getY() - offsetY[0];
                updateWiresForComponent(draggedExistingComponent);
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (draggedExistingComponent != null) {
                draggedExistingComponent.x = Math.round(draggedExistingComponent.x / gridSize) * gridSize;
                draggedExistingComponent.y = Math.round(draggedExistingComponent.y / gridSize) * gridSize;
                updateWiresForComponent(draggedExistingComponent);
                draggedExistingComponent = null;
                redrawCanvas();
            }
        });
    }

    private void updateWiresForComponent(ComponentsController.ImageComponent component) {
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                if (wire.startX == component.startX && wire.startY == component.startY) {
                    wire.startX = component.startX;
                    wire.startY = component.startY;
                    wire.endCircle.setCenterX(wire.endX);
                    wire.endCircle.setCenterY(wire.endY);
                }
                if (wire.endX == component.endX && wire.endY == component.endY) {
                    wire.endX = component.endX;
                    wire.endY = component.endY;
                    wire.endCircle.setCenterX(wire.endX);
                    wire.endCircle.setCenterY(wire.endY);
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
        redrawCanvas();
    }

    // ==================== Helper Methods ====================
    private void verifyCircuit() {
        // Find a power supply component
        ComponentsController.ImageComponent powerSupply = null;
        for (ComponentsController.Drawable drawable : drawables) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
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
        Set<ComponentsController.Drawable> visited = new HashSet<>();

        // Perform traversal
        if (traverseCircuit(startX, startY, startX, startY, visited)) {
            System.out.println("Circuit closed.");
        } else {
            System.out.println("Circuit open.");
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
        String SearchedComponent = component.image.getUrl();
        if (SearchedComponent.contains("Voltage%20Source.GIF")||SearchedComponent.contains("Current%20Source.GIF")||SearchedComponent.contains("Generator.GIF")
                ||SearchedComponent.contains("Battery%20Cell.GIF")||SearchedComponent.contains("Battery.GIF")||SearchedComponent.contains("Controlled%20Voltage%20Source.GIF")||SearchedComponent.contains("Controlled%20Current%20Source.GIF")) {
            return true;}
        else return false;
    }

    private boolean traverseCircuit(double startX, double startY, double initialX, double initialY, Set<ComponentsController.Drawable> visited) {
        // Check if we have returned to the starting point
        if (startX == initialX && startY == initialY && !visited.isEmpty()) {
            return true; // Circuit is closed
        }

        // Look for connected wires or components
        for (ComponentsController.Drawable drawable : drawables) {
            if (visited.contains(drawable)) {
                continue; // Skip already visited elements
            }

            if (drawable instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) drawable;
                if ((wire.startX == startX && wire.startY == startY) || (wire.endX == startX && wire.endY == startY)) {
                    visited.add(wire); // Mark wire as visited
                    double nextX = (wire.startX == startX && wire.startY == startY) ? wire.endX : wire.startX;
                    double nextY = (wire.startY == startY && wire.startX == startX) ? wire.endY : wire.startY;
                    if (traverseCircuit(nextX, nextY, initialX, initialY, visited)) {
                        return true; // Continue traversal
                    }
                }
            } else if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
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
            drawables.remove(selectedWire);
            selectedWire = null;
            redrawCanvas();
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
}