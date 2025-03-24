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
    private final List<Drawable> drawables = new ArrayList<>(); // All drawable objects (components and wires)
    private final double gridSize = 20; // Grid size for snapping
    private double zoomScale = 1.0; // Current zoom scale
    private final double minZoom = 0.5; // Minimum zoom level
    private final double maxZoom = 3.0; // Maximum zoom level

    // Component Placement
    private ImageView floatingComponentImage; // Floating image for component placement
    private Image currentlySelectedImage; // Currently selected component image
    private double selectedImageWidth = 80; // Default width for components
    private double selectedImageHeight = 80; // Default height for components
    private ImageComponent draggedExistingComponent = null; // Component being dragged
    private double currentRotation = 0; // Current rotation of the component

    // Wire Drawing
    private boolean isDrawingWire = false; // Whether a wire is being drawn
    private double wireStartX, wireStartY; // Start coordinates of the wire
    private Circle wireStartCircle; // Start circle for wire drawing
    private Wire selectedWire = null; // Currently selected wire

    // Initialization
    @FXML
    public void initialize() {
        System.out.println("parametersPane is null: " + (parametersPane == null)); // Debug log
        // Adjust main pane size when root pane resizes
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefWidth(newVal.doubleValue()));
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefHeight(newVal.doubleValue()));

        // Set up zoom group and scroll pane
        Group zoomGroup = new Group();
        zoomGroup.getChildren().add(canvasContainer);
        scrollPane.setContent(zoomGroup);

        // Initialize simulation features
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
                if (!floatingComponentImage.isVisible()) return;

                if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.E) {
                    floatingComponentImage.setVisible(false);
                    currentlySelectedImage = null;
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

        // Check for collisions with existing components
        for (Drawable drawable : drawables) {
            if (drawable instanceof ImageComponent) {
                ImageComponent component = (ImageComponent) drawable;
                if (snappedX < component.x + component.width && snappedX + selectedImageWidth > component.x &&
                        snappedY < component.y + component.height && snappedY + selectedImageHeight > component.y) {
                    return; // Collision detected, do not place
                }
            }
        }

        // Determine the component type based on the currentlySelectedImage URL
        String componentType = determineComponentType(currentlySelectedImage.getUrl());

        // Place the new component
        ImageComponent newComponent = new ImageComponent(currentlySelectedImage, snappedX, snappedY, selectedImageWidth, selectedImageHeight, componentType);
        newComponent.rotation = currentRotation;
        newComponent.updateEndPoints();
        drawables.add(newComponent);
        redrawCanvas();

        // Generate parameter controls for the new component
        generateParameterControls(newComponent);

        // Reset placement state
        floatingComponentImage.setVisible(false);
        currentlySelectedImage = null;
        floatingComponentImage.setRotate(0);
        currentRotation = 0;
    }

    // Helper method to determine the component type based on the image URL
    private String determineComponentType(String imageUrl) {
        System.out.println("Image URL: " + imageUrl); // Debug log

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
        // Check if an existing component is clicked
        for (Iterator<Drawable> iterator = drawables.iterator(); iterator.hasNext(); ) {
            Drawable drawable = iterator.next();
            if (drawable instanceof ImageComponent) {
                ImageComponent component = (ImageComponent) drawable;
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
                    iterator.remove();
                    redrawCanvas();
                    return;
                }
            }
        }

        // Check if a wire is clicked
        selectedWire = null;
        for (Drawable drawable : drawables) {
            if (drawable instanceof Wire) {
                Wire wire = (Wire) drawable;
                if (isPointNearLine(x, y, wire.startX, wire.startY, wire.endX, wire.endY)) {
                    selectedWire = wire;
                    break;
                }
            }
        }

        // If no component or wire is clicked, create a standalone circle
        double snappedX = Math.round(x / gridSize) * gridSize;
        double snappedY = Math.round(y / gridSize) * gridSize;
        StandaloneCircle newCircle = new StandaloneCircle(snappedX, snappedY);
        drawables.add(newCircle);
        redrawCanvas();
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
            for (Drawable drawable : drawables) {
                if (drawable instanceof ImageComponent) {
                    ImageComponent component = (ImageComponent) drawable;
                    if (component.startCircle.contains(e.getX(), e.getY())) {
                        startWireDrawing(component.startX, component.startY, component.startCircle);
                        break;
                    } else if (component.endCircle.contains(e.getX(), e.getY())) {
                        startWireDrawing(component.endX, component.endY, component.endCircle);
                        break;
                    }
                } else if (drawable instanceof StandaloneCircle) {
                    StandaloneCircle circle = (StandaloneCircle) drawable;
                    if (circle.circle.contains(e.getX(), e.getY())) {
                        startWireDrawing(circle.x, circle.y, circle.circle);
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
                Wire newWire = new Wire(wireStartX, wireStartY, snappedX, snappedY);
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
        for (Drawable drawable : drawables) {
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

    // ==================== Drawable Interface and Classes ====================
    private interface Drawable {
        void draw(GraphicsContext gc);
    }

    private static class ImageComponent implements Drawable {
        Image image;
        double x, y, width, height;
        double rotation = 0;
        double startX, startY, endX, endY;
        Circle startCircle, endCircle;
        String componentType; // Add this field

        ImageComponent(Image image, double x, double y, double width, double height, String componentType) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.componentType = componentType; // Initialize component type
            updateEndPoints();
        }

        private void updateEndPoints() {
            if (rotation % 180 == 0) {
                this.startX = x;
                this.startY = y + height / 2;
                this.endX = x + width;
                this.endY = y + height / 2;
            } else {
                this.startX = x + width / 2;
                this.startY = y;
                this.endX = x + width / 2;
                this.endY = y + height;
            }

            if (startCircle == null) {
                this.startCircle = new Circle(startX, startY, 6, Color.BLACK);
                this.endCircle = new Circle(endX, endY, 6, Color.BLACK);
            } else {
                this.startCircle.setCenterX(startX);
                this.startCircle.setCenterY(startY);
                this.endCircle.setCenterX(endX);
                this.endCircle.setCenterY(endY);
            }
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.translate(x + width / 2, y + height / 2);
            gc.rotate(rotation);
            gc.drawImage(image, -width / 2, -height / 2, width, height);
            gc.restore();

            gc.setFill(Color.BLACK);
            gc.fillOval(startX - 6, startY - 6, 12, 12);
            gc.fillOval(endX - 6, endY - 6, 12, 12);
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
            gc.setLineWidth(4);
            gc.strokeLine(startX, startY, endX, endY);
        }
    }

    private static class StandaloneCircle implements Drawable {
        double x, y;
        Circle circle;

        StandaloneCircle(double x, double y) {
            this.x = x;
            this.y = y;
            this.circle = new Circle(x, y, 6, Color.BLACK);
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.setFill(Color.BLACK);
            gc.fillOval(x - 6, y - 6, 12, 12);
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

    private boolean isPowerSupply(ImageComponent component) {
        String SearchedComponent = component.image.getUrl();
        if (SearchedComponent.contains("Voltage%20Source.GIF")||SearchedComponent.contains("Current%20Source.GIF")||SearchedComponent.contains("Generator.GIF")
        ||SearchedComponent.contains("Battery%20Cell.GIF")||SearchedComponent.contains("Battery.GIF")||SearchedComponent.contains("Controlled%20Voltage%20Source.GIF")||SearchedComponent.contains("Controlled%20Current%20Source.GIF")) {
            return true;}
        else return false;
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

    private void generateParameterControls(ImageComponent component) {
        // Create a VBox to hold all controls for this component
        VBox componentBox = new VBox(10);
        componentBox.setPadding(new Insets(10));
        componentBox.setUserData(component); // Set the component as user data

        // Add a label for the component type
        Label componentLabel = new Label("Component: " + component.componentType);
        componentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // Add a remove button for this component
        Button removeButton = new Button("Remove");
        removeButton.setMinWidth(80); // Ensure the button is long enough
        removeButton.setOnAction(event -> {
            // Remove the component from the drawables list
            drawables.remove(component);
            // Remove the component's parameters from the parametersPane
            parametersPane.getChildren().remove(componentBox);
            // Redraw the canvas to reflect the removal
            redrawCanvas();
        });

        // Add the label and remove button to the componentBox
        HBox headerBox = new HBox(10, componentLabel, removeButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        componentBox.getChildren().add(headerBox);

        // Generate the specific controls for the component
        switch (component.componentType) {
            case "SPSTToggleSwitch":
                addToggleSwitchControls(component, componentBox);
                break;
            case "SPDTToggleSwitch":
                addToggleSwitchControls(component, componentBox);
                break;
            case "PushbuttonSwitchNO":
                addPushbuttonSwitchControls(component);
                break;
            case "PushbuttonSwitchNC":
                addPushbuttonSwitchControls(component);
                break;
            case "DIPSwitch":
                addDIPSwitchControls(component);
                break;
            case "SPSTRelay":
                addRelayControls(component);
                break;
            case "SPDTRelay":
                addRelayControls(component);
                break;
            case "Jumper":
                addJumperControls(component);
                break;
            case "SolderBridge":
                addSolderBridgeControls(component);
                break;
            case "EarthGround":
            case "ChassisGround":
            case "DigitalGround":
                addGroundControls(component);
                break;
            case "ResistorIEEE":
            case "ResistorIEC":
                addResistorControls(component);
                break;
            case "PotentiometerIEEE":
            case "PotentiometerIEC":
                addPotentiometerControls(component);
                break;
            case "RheostatIEEE":
            case "RheostatIEC":
                addRheostatControls(component);
                break;
            case "Thermistor":
                addThermistorControls(component);
                break;
            case "Photoresistor":
                addPhotoresistorControls(component);
                break;
            case "Capacitor":
                addCapacitorControls(component);
                break;
            case "PolarizedCapacitor":
                addPolarizedCapacitorControls(component);
                break;
            case "VariableCapacitor":
                addVariableCapacitorControls(component);
                break;
            case "Inductor":
                addInductorControls(component);
                break;
            case "IronCoreInductor":
                addIronCoreInductorControls(component);
                break;
            case "VariableInductor":
                addVariableInductorControls(component);
                break;
            case "VoltageSource":
                addVoltageSourceControls(component);
                break;
            case "CurrentSource":
                addCurrentSourceControls(component);
                break;
            case "Generator":
                addGeneratorControls(component);
                break;
            case "BatteryCell":
            case "Battery":
                addBatteryControls(component);
                break;
            case "ControlledVoltageSource":
                addControlledVoltageSourceControls(component);
                break;
            case "ControlledCurrentSource":
                addControlledCurrentSourceControls(component);
                break;
            case "Voltmeter":
                addVoltmeterControls(component);
                break;
            case "Ammeter":
                addAmmeterControls(component);
                break;
            case "Ohmmeter":
                addOhmmeterControls(component);
                break;
            case "Wattmeter":
                addWattmeterControls(component);
                break;
            case "Diode":
                addDiodeControls(component);
                break;
            case "ZenerDiode":
                addZenerDiodeControls(component);
                break;
            case "SchottkyDiode":
                addSchottkyDiodeControls(component);
                break;
            case "Varactor":
                addVaractorControls(component);
                break;
            case "TunnelDiode":
                addTunnelDiodeControls(component);
                break;
            case "LightEmittingDiode":
                addLightEmittingDiodeControls(component);
                break;
            case "Photodiode":
                addPhotodiodeControls(component);
                break;
            case "NPNBipolarTransistor":
            case "PNPBipolarTransistor":
            case "DarlingtonTransistor":
                addBipolarTransistorControls(component);
                break;
            case "JFETNTransistor":
            case "JFETPTransistor":
                addJFETTransistorControls(component);
                break;
            case "NMOSTransistor":
            case "PMOSTransistor":
                addMOSTransistorControls(component);
                break;
            case "NOTGate":
            case "ANDGate":
            case "NANDGate":
            case "ORGate":
            case "NORGate":
            case "XORGate":
                addLogicGateControls(component);
                break;
            case "DFlipFlop":
                addDFlipFlopControls(component);
                break;
            case "Multiplexer2to1":
            case "Multiplexer4to1":
                addMultiplexerControls(component);
                break;
            case "Demultiplexer1to4":
                addDemultiplexerControls(component);
                break;
            case "Antenna":
            case "DipoleAntenna":
                addAntennaControls(component);
                break;
            case "Motor":
                addMotorControls(component);
                break;
            case "Transformer":
                addTransformerControls(component);
                break;
            case "Fuse":
                addFuseControls(component);
                break;
            case "Optocoupler":
                addOptocouplerControls(component);
                break;
            case "Loudspeaker":
                addLoudspeakerControls(component);
                break;
            case "Microphone":
                addMicrophoneControls(component);
                break;
            case "OperationalAmplifier":
                addOperationalAmplifierControls(component);
                break;
            case "SchmittTrigger":
                addSchmittTriggerControls(component);
                break;
            case "AnalogToDigitalConverter":
                addAnalogToDigitalConverterControls(component);
                break;
            case "DigitalToAnalogConverter":
                addDigitalToAnalogConverterControls(component);
                break;
            case "CrystalOscillator":
                addCrystalOscillatorControls(component);
                break;
            default:
                System.out.println("Unknown component type: " + component.componentType);
                break;
        }

        // Add the componentBox to the parametersPane
        parametersPane.getChildren().add(componentBox);

        // Force UI update
        parametersPane.requestLayout();
    }

    private void addToggleSwitchControls(ImageComponent component, VBox container) {
        // Example: Add controls for a toggle switch
        Label stateLabel = new Label("State:");
        ComboBox<String> stateComboBox = new ComboBox<>();
        stateComboBox.getItems().addAll("Open", "Closed");
        stateComboBox.setValue("Open"); // Default value
        container.getChildren().addAll(stateLabel, stateComboBox);
    }

    private void addPushbuttonSwitchControls(ImageComponent component) {
        Label label = new Label("Pushbutton Switch Parameters");
        parametersPane.getChildren().add(label);

        CheckBox isPressedCheckBox = new CheckBox("Is Pressed");
        isPressedCheckBox.setSelected(false);
        isPressedCheckBox.setOnAction(e -> {
            // Update component state
        });
        parametersPane.getChildren().add(isPressedCheckBox);

        TextField maxVoltageField = new TextField();
        maxVoltageField.setPromptText("Max Voltage");
        maxVoltageField.setText("0.0");
        maxVoltageField.setOnAction(e -> {
            try {
                double maxVoltage = Double.parseDouble(maxVoltageField.getText());
                // Update component maxVoltage
            } catch (NumberFormatException ex) {
                maxVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxVoltageField);

        TextField maxCurrentField = new TextField();
        maxCurrentField.setPromptText("Max Current");
        maxCurrentField.setText("0.0");
        maxCurrentField.setOnAction(e -> {
            try {
                double maxCurrent = Double.parseDouble(maxCurrentField.getText());
                // Update component maxCurrent
            } catch (NumberFormatException ex) {
                maxCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxCurrentField);
    }

    private void addDIPSwitchControls(ImageComponent component) {
        Label label = new Label("DIP Switch Parameters");
        parametersPane.getChildren().add(label);

        for (int i = 0; i < 8; i++) { // Assuming 8 switches in the DIP
            CheckBox switchCheckBox = new CheckBox("Switch " + (i + 1));
            switchCheckBox.setSelected(false);
            switchCheckBox.setOnAction(e -> {
                // Update component state
            });
            parametersPane.getChildren().add(switchCheckBox);
        }

        TextField maxVoltageField = new TextField();
        maxVoltageField.setPromptText("Max Voltage");
        maxVoltageField.setText("0.0");
        maxVoltageField.setOnAction(e -> {
            try {
                double maxVoltage = Double.parseDouble(maxVoltageField.getText());
                // Update component maxVoltage
            } catch (NumberFormatException ex) {
                maxVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxVoltageField);

        TextField maxCurrentField = new TextField();
        maxCurrentField.setPromptText("Max Current");
        maxCurrentField.setText("0.0");
        maxCurrentField.setOnAction(e -> {
            try {
                double maxCurrent = Double.parseDouble(maxCurrentField.getText());
                // Update component maxCurrent
            } catch (NumberFormatException ex) {
                maxCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxCurrentField);
    }

    private void addRelayControls(ImageComponent component) {
        Label label = new Label("Relay Parameters");
        parametersPane.getChildren().add(label);

        CheckBox isEnergizedCheckBox = new CheckBox("Is Energized");
        isEnergizedCheckBox.setSelected(false);
        isEnergizedCheckBox.setOnAction(e -> {
            // Update component state
        });
        parametersPane.getChildren().add(isEnergizedCheckBox);

        TextField coilVoltageField = new TextField();
        coilVoltageField.setPromptText("Coil Voltage");
        coilVoltageField.setText("0.0");
        coilVoltageField.setOnAction(e -> {
            try {
                double coilVoltage = Double.parseDouble(coilVoltageField.getText());
                // Update component coilVoltage
            } catch (NumberFormatException ex) {
                coilVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(coilVoltageField);

        TextField maxVoltageField = new TextField();
        maxVoltageField.setPromptText("Max Voltage");
        maxVoltageField.setText("0.0");
        maxVoltageField.setOnAction(e -> {
            try {
                double maxVoltage = Double.parseDouble(maxVoltageField.getText());
                // Update component maxVoltage
            } catch (NumberFormatException ex) {
                maxVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxVoltageField);

        TextField maxCurrentField = new TextField();
        maxCurrentField.setPromptText("Max Current");
        maxCurrentField.setText("0.0");
        maxCurrentField.setOnAction(e -> {
            try {
                double maxCurrent = Double.parseDouble(maxCurrentField.getText());
                // Update component maxCurrent
            } catch (NumberFormatException ex) {
                maxCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxCurrentField);
    }

    private void addJumperControls(ImageComponent component) {
        Label label = new Label("Jumper Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);
    }

    private void addSolderBridgeControls(ImageComponent component) {
        Label label = new Label("Solder Bridge Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);
    }

    private void addGroundControls(ImageComponent component) {
        Label label = new Label("Ground Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);
    }

    private void addResistorControls(ImageComponent component) {
        Label label = new Label("Resistor Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);

        TextField powerRatingField = new TextField();
        powerRatingField.setPromptText("Power Rating (Watts)");
        powerRatingField.setText("0.0");
        powerRatingField.setOnAction(e -> {
            try {
                double powerRating = Double.parseDouble(powerRatingField.getText());
                // Update component powerRating
            } catch (NumberFormatException ex) {
                powerRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(powerRatingField);
    }

    private void addPotentiometerControls(ImageComponent component) {
        Label label = new Label("Potentiometer Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);

        TextField powerRatingField = new TextField();
        powerRatingField.setPromptText("Power Rating (Watts)");
        powerRatingField.setText("0.0");
        powerRatingField.setOnAction(e -> {
            try {
                double powerRating = Double.parseDouble(powerRatingField.getText());
                // Update component powerRating
            } catch (NumberFormatException ex) {
                powerRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(powerRatingField);

        Slider wiperSlider = new Slider(0, 1, 0.5);
        wiperSlider.setShowTickLabels(true);
        wiperSlider.setShowTickMarks(true);
        wiperSlider.setMajorTickUnit(0.1);
        wiperSlider.setMinorTickCount(1);
        wiperSlider.setBlockIncrement(0.1);
        wiperSlider.setOnMouseReleased(e -> {
            double wiperPosition = wiperSlider.getValue();
            // Update component wiperPosition
        });
        parametersPane.getChildren().add(new Label("Wiper Position"));
        parametersPane.getChildren().add(wiperSlider);
    }

    private void addRheostatControls(ImageComponent component) {
        addPotentiometerControls(component); // Same as potentiometer
    }

    private void addThermistorControls(ImageComponent component) {
        Label label = new Label("Thermistor Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);

        TextField temperatureCoefficientField = new TextField();
        temperatureCoefficientField.setPromptText("Temperature Coefficient");
        temperatureCoefficientField.setText("0.0");
        temperatureCoefficientField.setOnAction(e -> {
            try {
                double temperatureCoefficient = Double.parseDouble(temperatureCoefficientField.getText());
                // Update component temperatureCoefficient
            } catch (NumberFormatException ex) {
                temperatureCoefficientField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(temperatureCoefficientField);
    }

    private void addPhotoresistorControls(ImageComponent component) {
        Label label = new Label("Photoresistor Parameters");
        parametersPane.getChildren().add(label);

        TextField resistanceField = new TextField();
        resistanceField.setPromptText("Resistance (Ohms)");
        resistanceField.setText("0.0");
        resistanceField.setOnAction(e -> {
            try {
                double resistance = Double.parseDouble(resistanceField.getText());
                // Update component resistance
            } catch (NumberFormatException ex) {
                resistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resistanceField);

        TextField lightIntensityField = new TextField();
        lightIntensityField.setPromptText("Light Intensity");
        lightIntensityField.setText("0.0");
        lightIntensityField.setOnAction(e -> {
            try {
                double lightIntensity = Double.parseDouble(lightIntensityField.getText());
                // Update component lightIntensity
            } catch (NumberFormatException ex) {
                lightIntensityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(lightIntensityField);
    }

    private void addCapacitorControls(ImageComponent component) {
        Label label = new Label("Capacitor Parameters");
        parametersPane.getChildren().add(label);

        TextField capacitanceField = new TextField();
        capacitanceField.setPromptText("Capacitance (Farads)");
        capacitanceField.setText("0.0");
        capacitanceField.setOnAction(e -> {
            try {
                double capacitance = Double.parseDouble(capacitanceField.getText());
                // Update component capacitance
            } catch (NumberFormatException ex) {
                capacitanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(capacitanceField);

        TextField voltageRatingField = new TextField();
        voltageRatingField.setPromptText("Voltage Rating (Volts)");
        voltageRatingField.setText("0.0");
        voltageRatingField.setOnAction(e -> {
            try {
                double voltageRating = Double.parseDouble(voltageRatingField.getText());
                // Update component voltageRating
            } catch (NumberFormatException ex) {
                voltageRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageRatingField);
    }

    private void addPolarizedCapacitorControls(ImageComponent component) {
        addCapacitorControls(component); // Same as capacitor

        CheckBox polarityCheckBox = new CheckBox("Polarity Respected");
        polarityCheckBox.setSelected(false);
        polarityCheckBox.setOnAction(e -> {
            // Update component polarity
        });
        parametersPane.getChildren().add(polarityCheckBox);
    }

    private void addVariableCapacitorControls(ImageComponent component) {
        addCapacitorControls(component); // Same as capacitor

        Slider rotationSlider = new Slider(0, 360, 180);
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setShowTickMarks(true);
        rotationSlider.setMajorTickUnit(90);
        rotationSlider.setMinorTickCount(1);
        rotationSlider.setBlockIncrement(10);
        rotationSlider.setOnMouseReleased(e -> {
            double rotationAngle = rotationSlider.getValue();
            // Update component rotationAngle
        });
        parametersPane.getChildren().add(new Label("Rotation Angle"));
        parametersPane.getChildren().add(rotationSlider);
    }

    private void addInductorControls(ImageComponent component) {
        Label label = new Label("Inductor Parameters");
        parametersPane.getChildren().add(label);

        TextField inductanceField = new TextField();
        inductanceField.setPromptText("Inductance (Henries)");
        inductanceField.setText("0.0");
        inductanceField.setOnAction(e -> {
            try {
                double inductance = Double.parseDouble(inductanceField.getText());
                // Update component inductance
            } catch (NumberFormatException ex) {
                inductanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(inductanceField);

        TextField currentRatingField = new TextField();
        currentRatingField.setPromptText("Current Rating (Amps)");
        currentRatingField.setText("0.0");
        currentRatingField.setOnAction(e -> {
            try {
                double currentRating = Double.parseDouble(currentRatingField.getText());
                // Update component currentRating
            } catch (NumberFormatException ex) {
                currentRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentRatingField);
    }

    private void addIronCoreInductorControls(ImageComponent component) {
        addInductorControls(component); // Same as inductor

        TextField corePermeabilityField = new TextField();
        corePermeabilityField.setPromptText("Core Permeability");
        corePermeabilityField.setText("0.0");
        corePermeabilityField.setOnAction(e -> {
            try {
                double corePermeability = Double.parseDouble(corePermeabilityField.getText());
                // Update component corePermeability
            } catch (NumberFormatException ex) {
                corePermeabilityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(corePermeabilityField);
    }

    private void addVariableInductorControls(ImageComponent component) {
        addInductorControls(component); // Same as inductor

        Slider rotationSlider = new Slider(0, 360, 180);
        rotationSlider.setShowTickLabels(true);
        rotationSlider.setShowTickMarks(true);
        rotationSlider.setMajorTickUnit(90);
        rotationSlider.setMinorTickCount(1);
        rotationSlider.setBlockIncrement(10);
        rotationSlider.setOnMouseReleased(e -> {
            double rotationAngle = rotationSlider.getValue();
            // Update component rotationAngle
        });
        parametersPane.getChildren().add(new Label("Rotation Angle"));
        parametersPane.getChildren().add(rotationSlider);
    }

    private void addVoltageSourceControls(ImageComponent component) {
        Label label = new Label("Voltage Source Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageField = new TextField();
        voltageField.setPromptText("Voltage (Volts)");
        voltageField.setText("0.0");
        voltageField.setOnAction(e -> {
            try {
                double voltage = Double.parseDouble(voltageField.getText());
                // Update component voltage
            } catch (NumberFormatException ex) {
                voltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);
    }

    private void addCurrentSourceControls(ImageComponent component) {
        Label label = new Label("Current Source Parameters");
        parametersPane.getChildren().add(label);

        TextField currentField = new TextField();
        currentField.setPromptText("Current (Amps)");
        currentField.setText("0.0");
        currentField.setOnAction(e -> {
            try {
                double current = Double.parseDouble(currentField.getText());
                // Update component current
            } catch (NumberFormatException ex) {
                currentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);
    }

    private void addGeneratorControls(ImageComponent component) {
        Label label = new Label("Generator Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageField = new TextField();
        voltageField.setPromptText("Voltage (Volts)");
        voltageField.setText("0.0");
        voltageField.setOnAction(e -> {
            try {
                double voltage = Double.parseDouble(voltageField.getText());
                // Update component voltage
            } catch (NumberFormatException ex) {
                voltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageField);

        TextField frequencyField = new TextField();
        frequencyField.setPromptText("Frequency (Hz)");
        frequencyField.setText("0.0");
        frequencyField.setOnAction(e -> {
            try {
                double frequency = Double.parseDouble(frequencyField.getText());
                // Update component frequency
            } catch (NumberFormatException ex) {
                frequencyField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(frequencyField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);
    }

    private void addBatteryControls(ImageComponent component) {
        Label label = new Label("Battery Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageField = new TextField();
        voltageField.setPromptText("Voltage (Volts)");
        voltageField.setText("0.0");
        voltageField.setOnAction(e -> {
            try {
                double voltage = Double.parseDouble(voltageField.getText());
                // Update component voltage
            } catch (NumberFormatException ex) {
                voltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity (Ah)");
        capacityField.setText("0.0");
        capacityField.setOnAction(e -> {
            try {
                double capacity = Double.parseDouble(capacityField.getText());
                // Update component capacity
            } catch (NumberFormatException ex) {
                capacityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(capacityField);
    }

    private void addControlledVoltageSourceControls(ImageComponent component) {
        Label label = new Label("Controlled Voltage Source Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageField = new TextField();
        voltageField.setPromptText("Voltage (Volts)");
        voltageField.setText("0.0");
        voltageField.setOnAction(e -> {
            try {
                double voltage = Double.parseDouble(voltageField.getText());
                // Update component voltage
            } catch (NumberFormatException ex) {
                voltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageField);

        TextField controlSignalField = new TextField();
        controlSignalField.setPromptText("Control Signal");
        controlSignalField.setText("0.0");
        controlSignalField.setOnAction(e -> {
            try {
                double controlSignal = Double.parseDouble(controlSignalField.getText());
                // Update component controlSignal
            } catch (NumberFormatException ex) {
                controlSignalField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(controlSignalField);
    }

    private void addControlledCurrentSourceControls(ImageComponent component) {
        Label label = new Label("Controlled Current Source Parameters");
        parametersPane.getChildren().add(label);

        TextField currentField = new TextField();
        currentField.setPromptText("Current (Amps)");
        currentField.setText("0.0");
        currentField.setOnAction(e -> {
            try {
                double current = Double.parseDouble(currentField.getText());
                // Update component current
            } catch (NumberFormatException ex) {
                currentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentField);

        TextField controlSignalField = new TextField();
        controlSignalField.setPromptText("Control Signal");
        controlSignalField.setText("0.0");
        controlSignalField.setOnAction(e -> {
            try {
                double controlSignal = Double.parseDouble(controlSignalField.getText());
                // Update component controlSignal
            } catch (NumberFormatException ex) {
                controlSignalField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(controlSignalField);
    }

    private void addVoltmeterControls(ImageComponent component) {
        Label label = new Label("Voltmeter Parameters");
        parametersPane.getChildren().add(label);

        TextField rangeField = new TextField();
        rangeField.setPromptText("Range (Volts)");
        rangeField.setText("0.0");
        rangeField.setOnAction(e -> {
            try {
                double range = Double.parseDouble(rangeField.getText());
                // Update component range
            } catch (NumberFormatException ex) {
                rangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(rangeField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);
    }

    private void addAmmeterControls(ImageComponent component) {
        Label label = new Label("Ammeter Parameters");
        parametersPane.getChildren().add(label);

        TextField rangeField = new TextField();
        rangeField.setPromptText("Range (Amps)");
        rangeField.setText("0.0");
        rangeField.setOnAction(e -> {
            try {
                double range = Double.parseDouble(rangeField.getText());
                // Update component range
            } catch (NumberFormatException ex) {
                rangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(rangeField);

        TextField internalResistanceField = new TextField();
        internalResistanceField.setPromptText("Internal Resistance (Ohms)");
        internalResistanceField.setText("0.0");
        internalResistanceField.setOnAction(e -> {
            try {
                double internalResistance = Double.parseDouble(internalResistanceField.getText());
                // Update component internalResistance
            } catch (NumberFormatException ex) {
                internalResistanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(internalResistanceField);
    }

    private void addOhmmeterControls(ImageComponent component) {
        Label label = new Label("Ohmmeter Parameters");
        parametersPane.getChildren().add(label);

        TextField rangeField = new TextField();
        rangeField.setPromptText("Range (Ohms)");
        rangeField.setText("0.0");
        rangeField.setOnAction(e -> {
            try {
                double range = Double.parseDouble(rangeField.getText());
                // Update component range
            } catch (NumberFormatException ex) {
                rangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(rangeField);
    }

    private void addWattmeterControls(ImageComponent component) {
        Label label = new Label("Wattmeter Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageRangeField = new TextField();
        voltageRangeField.setPromptText("Voltage Range (Volts)");
        voltageRangeField.setText("0.0");
        voltageRangeField.setOnAction(e -> {
            try {
                double voltageRange = Double.parseDouble(voltageRangeField.getText());
                // Update component voltageRange
            } catch (NumberFormatException ex) {
                voltageRangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageRangeField);

        TextField currentRangeField = new TextField();
        currentRangeField.setPromptText("Current Range (Amps)");
        currentRangeField.setText("0.0");
        currentRangeField.setOnAction(e -> {
            try {
                double currentRange = Double.parseDouble(currentRangeField.getText());
                // Update component currentRange
            } catch (NumberFormatException ex) {
                currentRangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentRangeField);
    }

    private void addDiodeControls(ImageComponent component) {
        Label label = new Label("Diode Parameters");
        parametersPane.getChildren().add(label);

        TextField forwardVoltageField = new TextField();
        forwardVoltageField.setPromptText("Forward Voltage (Volts)");
        forwardVoltageField.setText("0.0");
        forwardVoltageField.setOnAction(e -> {
            try {
                double forwardVoltage = Double.parseDouble(forwardVoltageField.getText());
                // Update component forwardVoltage
            } catch (NumberFormatException ex) {
                forwardVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(forwardVoltageField);

        TextField reverseBreakdownVoltageField = new TextField();
        reverseBreakdownVoltageField.setPromptText("Reverse Breakdown Voltage (Volts)");
        reverseBreakdownVoltageField.setText("0.0");
        reverseBreakdownVoltageField.setOnAction(e -> {
            try {
                double reverseBreakdownVoltage = Double.parseDouble(reverseBreakdownVoltageField.getText());
                // Update component reverseBreakdownVoltage
            } catch (NumberFormatException ex) {
                reverseBreakdownVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(reverseBreakdownVoltageField);
    }

    private void addZenerDiodeControls(ImageComponent component) {
        addDiodeControls(component); // Same as diode

        TextField zenerVoltageField = new TextField();
        zenerVoltageField.setPromptText("Zener Voltage (Volts)");
        zenerVoltageField.setText("0.0");
        zenerVoltageField.setOnAction(e -> {
            try {
                double zenerVoltage = Double.parseDouble(zenerVoltageField.getText());
                // Update component zenerVoltage
            } catch (NumberFormatException ex) {
                zenerVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(zenerVoltageField);
    }

    private void addSchottkyDiodeControls(ImageComponent component) {
        addDiodeControls(component); // Same as diode
    }

    private void addVaractorControls(ImageComponent component) {
        Label label = new Label("Varactor Parameters");
        parametersPane.getChildren().add(label);

        TextField capacitanceField = new TextField();
        capacitanceField.setPromptText("Capacitance (Farads)");
        capacitanceField.setText("0.0");
        capacitanceField.setOnAction(e -> {
            try {
                double capacitance = Double.parseDouble(capacitanceField.getText());
                // Update component capacitance
            } catch (NumberFormatException ex) {
                capacitanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(capacitanceField);

        TextField reverseVoltageField = new TextField();
        reverseVoltageField.setPromptText("Reverse Voltage (Volts)");
        reverseVoltageField.setText("0.0");
        reverseVoltageField.setOnAction(e -> {
            try {
                double reverseVoltage = Double.parseDouble(reverseVoltageField.getText());
                // Update component reverseVoltage
            } catch (NumberFormatException ex) {
                reverseVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(reverseVoltageField);
    }

    private void addTunnelDiodeControls(ImageComponent component) {
        Label label = new Label("Tunnel Diode Parameters");
        parametersPane.getChildren().add(label);

        TextField peakVoltageField = new TextField();
        peakVoltageField.setPromptText("Peak Voltage (Volts)");
        peakVoltageField.setText("0.0");
        peakVoltageField.setOnAction(e -> {
            try {
                double peakVoltage = Double.parseDouble(peakVoltageField.getText());
                // Update component peakVoltage
            } catch (NumberFormatException ex) {
                peakVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(peakVoltageField);

        TextField valleyVoltageField = new TextField();
        valleyVoltageField.setPromptText("Valley Voltage (Volts)");
        valleyVoltageField.setText("0.0");
        valleyVoltageField.setOnAction(e -> {
            try {
                double valleyVoltage = Double.parseDouble(valleyVoltageField.getText());
                // Update component valleyVoltage
            } catch (NumberFormatException ex) {
                valleyVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(valleyVoltageField);
    }

    private void addLightEmittingDiodeControls(ImageComponent component) {
        Label label = new Label("Light Emitting Diode Parameters");
        parametersPane.getChildren().add(label);

        TextField forwardVoltageField = new TextField();
        forwardVoltageField.setPromptText("Forward Voltage (Volts)");
        forwardVoltageField.setText("0.0");
        forwardVoltageField.setOnAction(e -> {
            try {
                double forwardVoltage = Double.parseDouble(forwardVoltageField.getText());
                // Update component forwardVoltage
            } catch (NumberFormatException ex) {
                forwardVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(forwardVoltageField);

        TextField wavelengthField = new TextField();
        wavelengthField.setPromptText("Wavelength (nm)");
        wavelengthField.setText("0.0");
        wavelengthField.setOnAction(e -> {
            try {
                double wavelength = Double.parseDouble(wavelengthField.getText());
                // Update component wavelength
            } catch (NumberFormatException ex) {
                wavelengthField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(wavelengthField);
    }

    private void addPhotodiodeControls(ImageComponent component) {
        Label label = new Label("Photodiode Parameters");
        parametersPane.getChildren().add(label);

        TextField darkCurrentField = new TextField();
        darkCurrentField.setPromptText("Dark Current (Amps)");
        darkCurrentField.setText("0.0");
        darkCurrentField.setOnAction(e -> {
            try {
                double darkCurrent = Double.parseDouble(darkCurrentField.getText());
                // Update component darkCurrent
            } catch (NumberFormatException ex) {
                darkCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(darkCurrentField);

        TextField lightCurrentField = new TextField();
        lightCurrentField.setPromptText("Light Current (Amps)");
        lightCurrentField.setText("0.0");
        lightCurrentField.setOnAction(e -> {
            try {
                double lightCurrent = Double.parseDouble(lightCurrentField.getText());
                // Update component lightCurrent
            } catch (NumberFormatException ex) {
                lightCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(lightCurrentField);
    }

    private void addBipolarTransistorControls(ImageComponent component) {
        Label label = new Label("Bipolar Transistor Parameters");
        parametersPane.getChildren().add(label);

        TextField currentGainField = new TextField();
        currentGainField.setPromptText("Current Gain (Beta)");
        currentGainField.setText("0.0");
        currentGainField.setOnAction(e -> {
            try {
                double currentGain = Double.parseDouble(currentGainField.getText());
                // Update component currentGain
            } catch (NumberFormatException ex) {
                currentGainField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentGainField);

        TextField maxCollectorCurrentField = new TextField();
        maxCollectorCurrentField.setPromptText("Max Collector Current (Amps)");
        maxCollectorCurrentField.setText("0.0");
        maxCollectorCurrentField.setOnAction(e -> {
            try {
                double maxCollectorCurrent = Double.parseDouble(maxCollectorCurrentField.getText());
                // Update component maxCollectorCurrent
            } catch (NumberFormatException ex) {
                maxCollectorCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxCollectorCurrentField);

        TextField maxCollectorEmitterVoltageField = new TextField();
        maxCollectorEmitterVoltageField.setPromptText("Max Collector-Emitter Voltage (Volts)");
        maxCollectorEmitterVoltageField.setText("0.0");
        maxCollectorEmitterVoltageField.setOnAction(e -> {
            try {
                double maxCollectorEmitterVoltage = Double.parseDouble(maxCollectorEmitterVoltageField.getText());
                // Update component maxCollectorEmitterVoltage
            } catch (NumberFormatException ex) {
                maxCollectorEmitterVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxCollectorEmitterVoltageField);
    }

    private void addJFETTransistorControls(ImageComponent component) {
        Label label = new Label("JFET Transistor Parameters");
        parametersPane.getChildren().add(label);

        TextField pinchOffVoltageField = new TextField();
        pinchOffVoltageField.setPromptText("Pinch-Off Voltage (Volts)");
        pinchOffVoltageField.setText("0.0");
        pinchOffVoltageField.setOnAction(e -> {
            try {
                double pinchOffVoltage = Double.parseDouble(pinchOffVoltageField.getText());
                // Update component pinchOffVoltage
            } catch (NumberFormatException ex) {
                pinchOffVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(pinchOffVoltageField);

        TextField maxDrainSourceVoltageField = new TextField();
        maxDrainSourceVoltageField.setPromptText("Max Drain-Source Voltage (Volts)");
        maxDrainSourceVoltageField.setText("0.0");
        maxDrainSourceVoltageField.setOnAction(e -> {
            try {
                double maxDrainSourceVoltage = Double.parseDouble(maxDrainSourceVoltageField.getText());
                // Update component maxDrainSourceVoltage
            } catch (NumberFormatException ex) {
                maxDrainSourceVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxDrainSourceVoltageField);
    }

    private void addMOSTransistorControls(ImageComponent component) {
        Label label = new Label("MOS Transistor Parameters");
        parametersPane.getChildren().add(label);

        TextField thresholdVoltageField = new TextField();
        thresholdVoltageField.setPromptText("Threshold Voltage (Volts)");
        thresholdVoltageField.setText("0.0");
        thresholdVoltageField.setOnAction(e -> {
            try {
                double thresholdVoltage = Double.parseDouble(thresholdVoltageField.getText());
                // Update component thresholdVoltage
            } catch (NumberFormatException ex) {
                thresholdVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(thresholdVoltageField);

        TextField maxDrainSourceVoltageField = new TextField();
        maxDrainSourceVoltageField.setPromptText("Max Drain-Source Voltage (Volts)");
        maxDrainSourceVoltageField.setText("0.0");
        maxDrainSourceVoltageField.setOnAction(e -> {
            try {
                double maxDrainSourceVoltage = Double.parseDouble(maxDrainSourceVoltageField.getText());
                // Update component maxDrainSourceVoltage
            } catch (NumberFormatException ex) {
                maxDrainSourceVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(maxDrainSourceVoltageField);
    }

    private void addLogicGateControls(ImageComponent component) {
        Label label = new Label("Logic Gate Parameters");
        parametersPane.getChildren().add(label);

        TextField propagationDelayField = new TextField();
        propagationDelayField.setPromptText("Propagation Delay (ns)");
        propagationDelayField.setText("0.0");
        propagationDelayField.setOnAction(e -> {
            try {
                double propagationDelay = Double.parseDouble(propagationDelayField.getText());
                // Update component propagationDelay
            } catch (NumberFormatException ex) {
                propagationDelayField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(propagationDelayField);
    }

    private void addDFlipFlopControls(ImageComponent component) {
        Label label = new Label("D Flip-Flop Parameters");
        parametersPane.getChildren().add(label);

        TextField setupTimeField = new TextField();
        setupTimeField.setPromptText("Setup Time (ns)");
        setupTimeField.setText("0.0");
        setupTimeField.setOnAction(e -> {
            try {
                double setupTime = Double.parseDouble(setupTimeField.getText());
                // Update component setupTime
            } catch (NumberFormatException ex) {
                setupTimeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(setupTimeField);

        TextField holdTimeField = new TextField();
        holdTimeField.setPromptText("Hold Time (ns)");
        holdTimeField.setText("0.0");
        holdTimeField.setOnAction(e -> {
            try {
                double holdTime = Double.parseDouble(holdTimeField.getText());
                // Update component holdTime
            } catch (NumberFormatException ex) {
                holdTimeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(holdTimeField);
    }

    private void addMultiplexerControls(ImageComponent component) {
        Label label = new Label("Multiplexer Parameters");
        parametersPane.getChildren().add(label);

        TextField propagationDelayField = new TextField();
        propagationDelayField.setPromptText("Propagation Delay (ns)");
        propagationDelayField.setText("0.0");
        propagationDelayField.setOnAction(e -> {
            try {
                double propagationDelay = Double.parseDouble(propagationDelayField.getText());
                // Update component propagationDelay
            } catch (NumberFormatException ex) {
                propagationDelayField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(propagationDelayField);
    }

    private void addDemultiplexerControls(ImageComponent component) {
        Label label = new Label("Demultiplexer Parameters");
        parametersPane.getChildren().add(label);

        TextField propagationDelayField = new TextField();
        propagationDelayField.setPromptText("Propagation Delay (ns)");
        propagationDelayField.setText("0.0");
        propagationDelayField.setOnAction(e -> {
            try {
                double propagationDelay = Double.parseDouble(propagationDelayField.getText());
                // Update component propagationDelay
            } catch (NumberFormatException ex) {
                propagationDelayField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(propagationDelayField);
    }

    private void addAntennaControls(ImageComponent component) {
        Label label = new Label("Antenna Parameters");
        parametersPane.getChildren().add(label);

        TextField frequencyRangeField = new TextField();
        frequencyRangeField.setPromptText("Frequency Range (Hz)");
        frequencyRangeField.setText("0.0");
        frequencyRangeField.setOnAction(e -> {
            try {
                double frequencyRange = Double.parseDouble(frequencyRangeField.getText());
                // Update component frequencyRange
            } catch (NumberFormatException ex) {
                frequencyRangeField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(frequencyRangeField);

        TextField gainField = new TextField();
        gainField.setPromptText("Gain (dBi)");
        gainField.setText("0.0");
        gainField.setOnAction(e -> {
            try {
                double gain = Double.parseDouble(gainField.getText());
                // Update component gain
            } catch (NumberFormatException ex) {
                gainField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(gainField);
    }

    private void addMotorControls(ImageComponent component) {
        Label label = new Label("Motor Parameters");
        parametersPane.getChildren().add(label);

        TextField voltageRatingField = new TextField();
        voltageRatingField.setPromptText("Voltage Rating (Volts)");
        voltageRatingField.setText("0.0");
        voltageRatingField.setOnAction(e -> {
            try {
                double voltageRating = Double.parseDouble(voltageRatingField.getText());
                // Update component voltageRating
            } catch (NumberFormatException ex) {
                voltageRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(voltageRatingField);

        TextField speedField = new TextField();
        speedField.setPromptText("Speed (RPM)");
        speedField.setText("0.0");
        speedField.setOnAction(e -> {
            try {
                double speed = Double.parseDouble(speedField.getText());
                // Update component speed
            } catch (NumberFormatException ex) {
                speedField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(speedField);

        TextField torqueField = new TextField();
        torqueField.setPromptText("Torque (Nm)");
        torqueField.setText("0.0");
        torqueField.setOnAction(e -> {
            try {
                double torque = Double.parseDouble(torqueField.getText());
                // Update component torque
            } catch (NumberFormatException ex) {
                torqueField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(torqueField);
    }

    private void addTransformerControls(ImageComponent component) {
        Label label = new Label("Transformer Parameters");
        parametersPane.getChildren().add(label);

        TextField primaryVoltageField = new TextField();
        primaryVoltageField.setPromptText("Primary Voltage (Volts)");
        primaryVoltageField.setText("0.0");
        primaryVoltageField.setOnAction(e -> {
            try {
                double primaryVoltage = Double.parseDouble(primaryVoltageField.getText());
                // Update component primaryVoltage
            } catch (NumberFormatException ex) {
                primaryVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(primaryVoltageField);

        TextField secondaryVoltageField = new TextField();
        secondaryVoltageField.setPromptText("Secondary Voltage (Volts)");
        secondaryVoltageField.setText("0.0");
        secondaryVoltageField.setOnAction(e -> {
            try {
                double secondaryVoltage = Double.parseDouble(secondaryVoltageField.getText());
                // Update component secondaryVoltage
            } catch (NumberFormatException ex) {
                secondaryVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(secondaryVoltageField);

        TextField powerRatingField = new TextField();
        powerRatingField.setPromptText("Power Rating (Watts)");
        powerRatingField.setText("0.0");
        powerRatingField.setOnAction(e -> {
            try {
                double powerRating = Double.parseDouble(powerRatingField.getText());
                // Update component powerRating
            } catch (NumberFormatException ex) {
                powerRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(powerRatingField);
    }

    private void addFuseControls(ImageComponent component) {
        Label label = new Label("Fuse Parameters");
        parametersPane.getChildren().add(label);

        TextField currentRatingField = new TextField();
        currentRatingField.setPromptText("Current Rating (Amps)");
        currentRatingField.setText("0.0");
        currentRatingField.setOnAction(e -> {
            try {
                double currentRating = Double.parseDouble(currentRatingField.getText());
                // Update component currentRating
            } catch (NumberFormatException ex) {
                currentRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(currentRatingField);

        TextField breakingCapacityField = new TextField();
        breakingCapacityField.setPromptText("Breaking Capacity (Amps)");
        breakingCapacityField.setText("0.0");
        breakingCapacityField.setOnAction(e -> {
            try {
                double breakingCapacity = Double.parseDouble(breakingCapacityField.getText());
                // Update component breakingCapacity
            } catch (NumberFormatException ex) {
                breakingCapacityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(breakingCapacityField);
    }

    private void addOptocouplerControls(ImageComponent component) {
        Label label = new Label("Optocoupler Parameters");
        parametersPane.getChildren().add(label);

        TextField forwardCurrentField = new TextField();
        forwardCurrentField.setPromptText("Forward Current (mA)");
        forwardCurrentField.setText("0.0");
        forwardCurrentField.setOnAction(e -> {
            try {
                double forwardCurrent = Double.parseDouble(forwardCurrentField.getText());
                // Update component forwardCurrent
            } catch (NumberFormatException ex) {
                forwardCurrentField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(forwardCurrentField);

        TextField isolationVoltageField = new TextField();
        isolationVoltageField.setPromptText("Isolation Voltage (Volts)");
        isolationVoltageField.setText("0.0");
        isolationVoltageField.setOnAction(e -> {
            try {
                double isolationVoltage = Double.parseDouble(isolationVoltageField.getText());
                // Update component isolationVoltage
            } catch (NumberFormatException ex) {
                isolationVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(isolationVoltageField);
    }

    private void addLoudspeakerControls(ImageComponent component) {
        Label label = new Label("Loudspeaker Parameters");
        parametersPane.getChildren().add(label);

        TextField impedanceField = new TextField();
        impedanceField.setPromptText("Impedance (Ohms)");
        impedanceField.setText("0.0");
        impedanceField.setOnAction(e -> {
            try {
                double impedance = Double.parseDouble(impedanceField.getText());
                // Update component impedance
            } catch (NumberFormatException ex) {
                impedanceField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(impedanceField);

        TextField powerRatingField = new TextField();
        powerRatingField.setPromptText("Power Rating (Watts)");
        powerRatingField.setText("0.0");
        powerRatingField.setOnAction(e -> {
            try {
                double powerRating = Double.parseDouble(powerRatingField.getText());
                // Update component powerRating
            } catch (NumberFormatException ex) {
                powerRatingField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(powerRatingField);
    }

    private void addMicrophoneControls(ImageComponent component) {
        Label label = new Label("Microphone Parameters");
        parametersPane.getChildren().add(label);

        TextField sensitivityField = new TextField();
        sensitivityField.setPromptText("Sensitivity (dB)");
        sensitivityField.setText("0.0");
        sensitivityField.setOnAction(e -> {
            try {
                double sensitivity = Double.parseDouble(sensitivityField.getText());
                // Update component sensitivity
            } catch (NumberFormatException ex) {
                sensitivityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(sensitivityField);

        TextField frequencyResponseField = new TextField();
        frequencyResponseField.setPromptText("Frequency Response (Hz)");
        frequencyResponseField.setText("0.0");
        frequencyResponseField.setOnAction(e -> {
            try {
                double frequencyResponse = Double.parseDouble(frequencyResponseField.getText());
                // Update component frequencyResponse
            } catch (NumberFormatException ex) {
                frequencyResponseField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(frequencyResponseField);
    }

    private void addOperationalAmplifierControls(ImageComponent component) {
        Label label = new Label("Operational Amplifier Parameters");
        parametersPane.getChildren().add(label);

        TextField gainBandwidthProductField = new TextField();
        gainBandwidthProductField.setPromptText("Gain Bandwidth Product (Hz)");
        gainBandwidthProductField.setText("0.0");
        gainBandwidthProductField.setOnAction(e -> {
            try {
                double gainBandwidthProduct = Double.parseDouble(gainBandwidthProductField.getText());
                // Update component gainBandwidthProduct
            } catch (NumberFormatException ex) {
                gainBandwidthProductField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(gainBandwidthProductField);

        TextField slewRateField = new TextField();
        slewRateField.setPromptText("Slew Rate (V/µs)");
        slewRateField.setText("0.0");
        slewRateField.setOnAction(e -> {
            try {
                double slewRate = Double.parseDouble(slewRateField.getText());
                // Update component slewRate
            } catch (NumberFormatException ex) {
                slewRateField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(slewRateField);
    }

    private void addSchmittTriggerControls(ImageComponent component) {
        Label label = new Label("Schmitt Trigger Parameters");
        parametersPane.getChildren().add(label);

        TextField upperThresholdField = new TextField();
        upperThresholdField.setPromptText("Upper Threshold (Volts)");
        upperThresholdField.setText("0.0");
        upperThresholdField.setOnAction(e -> {
            try {
                double upperThreshold = Double.parseDouble(upperThresholdField.getText());
                // Update component upperThreshold
            } catch (NumberFormatException ex) {
                upperThresholdField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(upperThresholdField);

        TextField lowerThresholdField = new TextField();
        lowerThresholdField.setPromptText("Lower Threshold (Volts)");
        lowerThresholdField.setText("0.0");
        lowerThresholdField.setOnAction(e -> {
            try {
                double lowerThreshold = Double.parseDouble(lowerThresholdField.getText());
                // Update component lowerThreshold
            } catch (NumberFormatException ex) {
                lowerThresholdField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(lowerThresholdField);
    }

    private void addAnalogToDigitalConverterControls(ImageComponent component) {
        Label label = new Label("Analog-to-Digital Converter Parameters");
        parametersPane.getChildren().add(label);

        TextField resolutionField = new TextField();
        resolutionField.setPromptText("Resolution (bits)");
        resolutionField.setText("0");
        resolutionField.setOnAction(e -> {
            try {
                int resolution = Integer.parseInt(resolutionField.getText());
                // Update component resolution
            } catch (NumberFormatException ex) {
                resolutionField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resolutionField);

        TextField samplingRateField = new TextField();
        samplingRateField.setPromptText("Sampling Rate (Hz)");
        samplingRateField.setText("0.0");
        samplingRateField.setOnAction(e -> {
            try {
                double samplingRate = Double.parseDouble(samplingRateField.getText());
                // Update component samplingRate
            } catch (NumberFormatException ex) {
                samplingRateField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(samplingRateField);
    }

    private void addDigitalToAnalogConverterControls(ImageComponent component) {
        Label label = new Label("Digital-to-Analog Converter Parameters");
        parametersPane.getChildren().add(label);

        TextField resolutionField = new TextField();
        resolutionField.setPromptText("Resolution (bits)");
        resolutionField.setText("0");
        resolutionField.setOnAction(e -> {
            try {
                int resolution = Integer.parseInt(resolutionField.getText());
                // Update component resolution
            } catch (NumberFormatException ex) {
                resolutionField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(resolutionField);

        TextField outputVoltageField = new TextField();
        outputVoltageField.setPromptText("Output Voltage (Volts)");
        outputVoltageField.setText("0.0");
        outputVoltageField.setOnAction(e -> {
            try {
                double outputVoltage = Double.parseDouble(outputVoltageField.getText());
                // Update component outputVoltage
            } catch (NumberFormatException ex) {
                outputVoltageField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(outputVoltageField);
    }

    private void addCrystalOscillatorControls(ImageComponent component) {
        Label label = new Label("Crystal Oscillator Parameters");
        parametersPane.getChildren().add(label);

        TextField frequencyField = new TextField();
        frequencyField.setPromptText("Frequency (Hz)");
        frequencyField.setText("0.0");
        frequencyField.setOnAction(e -> {
            try {
                double frequency = Double.parseDouble(frequencyField.getText());
                // Update component frequency
            } catch (NumberFormatException ex) {
                frequencyField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(frequencyField);

        TextField stabilityField = new TextField();
        stabilityField.setPromptText("Stability (ppm)");
        stabilityField.setText("0.0");
        stabilityField.setOnAction(e -> {
            try {
                double stability = Double.parseDouble(stabilityField.getText());
                // Update component stability
            } catch (NumberFormatException ex) {
                stabilityField.setText("Invalid input");
            }
        });
        parametersPane.getChildren().add(stabilityField);
    }

    private void generateDropdownAndParameterControls(ImageComponent component) {
        System.out.println("Generating dropdown and controls for component: " + component.componentType); // Debug log
        parametersPane.getChildren().clear(); // Clear existing controls

        // Create a dropdown list (ComboBox) for components
        ComboBox<String> componentDropdown = new ComboBox<>();
        componentDropdown.setPromptText("Select Component");

        // Add the name of the component to the dropdown
        componentDropdown.getItems().add(component.image.getUrl());

        // Add an event handler to the dropdown
        componentDropdown.setOnAction(e -> {
            String selectedComponent = componentDropdown.getSelectionModel().getSelectedItem();
            if (selectedComponent != null) {
                // Generate parameter controls for the selected component
                generateParameterControls(component);
            }
        });

        // Add the dropdown to the parametersPane
        parametersPane.getChildren().add(componentDropdown);
        System.out.println("Added dropdown to parametersPane"); // Debug log

        // Generate parameter controls for the newly added component
        generateParameterControls(component);
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