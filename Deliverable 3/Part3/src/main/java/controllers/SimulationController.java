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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

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
    @FXML private VBox keybindsVBox;
    @FXML private VBox helpVBox;
    @FXML private HBox graphContainer;


    // Simulation State
    private String currentFile;
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

    // Store active keybinds: key (Character) -> action (String)
    private final Map<Character, String> customKeybinds = new HashMap<>();

    // Copy buffer for component
    private ComponentsController.ImageComponent copiedComponent = null;


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

    private String outPut;
    //Checking simulation status
    boolean hasVoltmeter = false;
    boolean hasAmmeter = false;
    boolean hasOhmmeter = false;
    boolean positiveConnected = false;
    boolean negativeConnected = false;

    // Add these new fields at the top of the class
    private static final int MAX_DATA_POINTS = 50; // Maximum number of data points to show
    private static final long UPDATE_INTERVAL = 100; // Update interval in milliseconds
    private Timer graphUpdateTimer;
    private double currentTime = 0;
    private Map<ComponentsController.ImageComponent, List<XYChart.Data<Number, Number>>> voltageHistory = new HashMap<>();
    private Map<ComponentsController.ImageComponent, List<XYChart.Data<Number, Number>>> currentHistory = new HashMap<>();

    // Initialization
    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefWidth(newVal.doubleValue()));
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> mainPane.setPrefHeight(newVal.doubleValue()));
        Group zoomGroup = new Group();
        zoomGroup.getChildren().add(canvasContainer);
        scrollPane.setContent(zoomGroup);
        setupKeybindsSection();
        setupHelpSection();
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

    public void setUpWithFile() {
        importingSave();
    }

    private void setupKeybindsSection() {
        if (keybindsVBox != null) {
            keybindsVBox.setAlignment(Pos.TOP_LEFT); // Align children to top-left

            Label deleteLabel = new Label("- Press E while dragging a component to delete it.");
            Label rotateLabel = new Label("- Press R while dragging a component to rotate it by 90 degrees.");

            deleteLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            rotateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

            deleteLabel.setMaxWidth(Double.MAX_VALUE);
            deleteLabel.setAlignment(Pos.TOP_LEFT);

            rotateLabel.setMaxWidth(Double.MAX_VALUE);
            rotateLabel.setAlignment(Pos.TOP_LEFT);

            // Create the "Add Keybind" button
            Button addKeybindButton = new Button("Add Keybind");
            addKeybindButton.setStyle("-fx-font-size: 16px;");

            // Track how many custom keybinds have been added
            final int maxKeybinds = 5;

            addKeybindButton.setOnAction(event -> {
                int keybindCount = (int) keybindsVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox)
                        .count();

                if (keybindCount < maxKeybinds) {
                    HBox keybindHBox = new HBox(5); // spacing of 5 between elements
                    keybindHBox.setAlignment(Pos.TOP_LEFT);
                    keybindHBox.setMaxWidth(Double.MAX_VALUE);
                    keybindHBox.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

                    Label pressLabel = new Label("- Press ");
                    pressLabel.setStyle("-fx-text-fill: black;");

                    TextField keyField = new TextField();
                    keyField.setPromptText("Key");
                    keyField.setPrefWidth(50);

                    keyField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.length() > 1) {
                            keyField.setText(newValue.substring(0, 1));
                        }
                    });

                    Label toLabel = new Label(" to ");
                    toLabel.setStyle("-fx-text-fill: black;");

                    ComboBox<String> actionComboBox = new ComboBox<>();
                    actionComboBox.getItems().addAll("Copy", "Paste", "Cut", "Undo", "Redo");
                    actionComboBox.setPromptText("Select Action");

                    Button saveRemoveButton = new Button("Save");
                    saveRemoveButton.setStyle("-fx-font-size: 14px;");

                    saveRemoveButton.setOnAction(e -> {
                        if (saveRemoveButton.getText().equals("Save")) {
                            if (keyField.getText().isEmpty() || actionComboBox.getValue() == null) {
                                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a key and select an action.");
                                alert.show();
                            } else {
                                keyField.setEditable(false);
                                actionComboBox.setDisable(true);
                                saveRemoveButton.setText("Remove");

                                // === NEW: Save to custom keybinds Map ===
                                customKeybinds.put(keyField.getText().toUpperCase().charAt(0), actionComboBox.getValue());
                            }
                        } else {
                            // Remove logic
                            keybindsVBox.getChildren().remove(keybindHBox);

                            // === NEW: Remove from custom keybinds Map ===
                            customKeybinds.remove(keyField.getText().toUpperCase().charAt(0));

                            if (keybindsVBox.getChildren().stream().noneMatch(node -> node instanceof Button && ((Button) node).getText().equals("Add Keybind"))) {
                                keybindsVBox.getChildren().add(addKeybindButton);
                            }
                        }
                    });

                    keybindHBox.getChildren().addAll(pressLabel, keyField, toLabel, actionComboBox, saveRemoveButton);

                    int buttonIndex = keybindsVBox.getChildren().indexOf(addKeybindButton);
                    keybindsVBox.getChildren().add(buttonIndex, keybindHBox);

                    if (keybindCount + 1 >= maxKeybinds) {
                        keybindsVBox.getChildren().remove(addKeybindButton);
                    }
                }
            });

            keybindsVBox.getChildren().addAll(deleteLabel, rotateLabel, addKeybindButton);
        } else {
            System.out.println("Keybinds VBox is null. Make sure fx:id is set correctly in FXML.");
        }
    }

    private void setupHelpSection() {
        if (keybindsVBox != null) {

            Label helpDescription = new Label(
                    "- Welcome to the Application!\n\n" +
                            "- This application is a builder of electric circuits that allows you to spawn and interact with components on the builder and customize keybinds.\n\n" +
                            "Main Features:\n" +
                            "• Creating Components:\n" +
                            "    → You can click on components on the components list to make them appear on your screen.\n" +
                            "• Dragging Components:\n" +
                            "    → You can drag components around the interface freely.\n" +
                            "    → While dragging:\n" +
                            "        - Press 'E' to delete a component instantly.\n" +
                            "        - Press 'R' to rotate the component by 90 degrees.\n\n" +
                            "• Zoom In / Zoom Out:\n" +
                            "    → Use ctrl + the mouse scroll wheel to zoom in or out.\n" +
                            "    → Zooming helps you focus on fine details or get a broader view of your simulation.\n\n" +
                            "• Save and Load:\n" +
                            "    → You can save your current simulation to a file.\n" +
                            "    → Supported formats include JSON, CSV, and text files.\n" +
                            "    → Use the 'Save' or 'Save and Quit' options under the File menu.\n" +
                            "    → To load a saved simulation, use the 'Load' option and select your file type.\n\n" +
                            "• Custom Keybinds:\n" +
                            "    → Click the 'Add Keybind' button to define your own shortcuts.\n" +
                            "    → Each keybind lets you assign a key (one character only) to an action (Copy, Paste, Cut, Undo, Redo).\n" +
                            "    → You can add up to 5 custom keybinds.\n\n" +
                            "• Interface Behavior:\n" +
                            "    → All labels, fields, and controls are styled for readability.\n"
            );
            helpDescription.setWrapText(true);
            helpDescription.setMaxWidth(Double.MAX_VALUE);
            helpDescription.setAlignment(Pos.TOP_LEFT);
            helpDescription.setStyle("-fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: white;");

            VBox vbox = new VBox();
            vbox.getChildren().add(helpDescription);
            vbox.setStyle("-fx-background-color: white;");

            ScrollPane helpScrollPane = new ScrollPane(vbox);
            helpScrollPane.setFitToWidth(true);
            helpScrollPane.setStyle(
                    "-fx-background: white;" +
                            "-fx-background-color: white;" +
                            "-fx-control-inner-background: white;"
            );

            helpVBox.getChildren().add(helpScrollPane);
        } else {
            System.out.println("Help VBox is null. Make sure fx:id is set correctly in FXML.");
        }
    }

    private ComponentsController.ImageComponent cloneComponent(ComponentsController.ImageComponent original) {
        ComponentsController.ImageComponent clone = new ComponentsController.ImageComponent(
                original.getImage(),
                original.x,
                original.y,
                original.width,
                original.height,
                original.componentType
        );
        clone.rotation = original.rotation;
        clone.updateEndPoints();
        return clone;
    }

    private void updateCustomKeybinds() {
        customKeybinds.clear();
        for (Node node : keybindsVBox.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                TextField keyField = null;
                ComboBox<String> actionBox = null;

                for (Node child : hbox.getChildren()) {
                    if (child instanceof TextField) {
                        keyField = (TextField) child;
                    } else if (child instanceof ComboBox) {
                        actionBox = (ComboBox<String>) child;
                    }
                }

                if (keyField != null && actionBox != null && !keyField.isEditable() && !actionBox.isDisable()) {
                    // Only add saved (non-editable) keybinds
                    String keyText = keyField.getText().toUpperCase();
                    String actionText = actionBox.getValue();
                    if (!keyText.isEmpty() && actionText != null) {
                        customKeybinds.put(keyText.charAt(0), actionText);
                    }
                }
            }
        }
    }



    // ==================== Component Placement ====================
    private ComponentsController.ImageComponent selectedComponent = null; // <-- ADD this field to your controller!

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
                        removeGraphButton(draggedExistingComponent);
                        drawables.remove(draggedExistingComponent);
                        parametersPane.getChildren().remove(draggedExistingComponent.parameterControls);
                        draggedExistingComponent = null;
                        redrawCanvas();
                    } else if (floatingComponentImage.isVisible()) {
                        floatingComponentImage.setVisible(false);
                        currentlySelectedImage = null;
                    }
                } else if (e.getCode() == KeyCode.R) {
                    currentRotation = (currentRotation + 90) % 360;
                    floatingComponentImage.setRotate(currentRotation);
                } else {
                    // Handle custom keybinds safely
                    String text = e.getText();
                    if (text != null && !text.isEmpty()) {
                        char keyChar = Character.toUpperCase(text.charAt(0));
                        String action = customKeybinds.getOrDefault(keyChar, null);

                        if (action != null) {
                            switch (action) {
                                case "Copy":
                                    if (draggedExistingComponent != null) {
                                        copiedComponent = cloneComponent(draggedExistingComponent);
                                        addFeedbackMessage(copiedComponent.componentType+" copied!", "info");
                                    } else if (selectedComponent != null) { // <-- NEW!
                                        copiedComponent = cloneComponent(selectedComponent);
                                        addFeedbackMessage(copiedComponent.componentType+" copied!", "info");
                                    }
                                    break;

                                case "Paste":
                                    if (copiedComponent != null && !floatingComponentImage.isVisible()) {
                                        currentlySelectedImage = copiedComponent.getImage();
                                        floatingComponentImage.setImage(currentlySelectedImage);
                                        floatingComponentImage.setFitWidth(copiedComponent.width);
                                        floatingComponentImage.setFitHeight(copiedComponent.height);
                                        floatingComponentImage.setRotate(copiedComponent.rotation);
                                        floatingComponentImage.setVisible(true);
                                        currentRotation = copiedComponent.rotation;
                                        selectedImageWidth = copiedComponent.width;
                                        selectedImageHeight = copiedComponent.height;
                                        addFeedbackMessage("Pasted "+copiedComponent.componentType+"!", "info");
                                    }
                                    break;

                                case "Cut":
                                    if (draggedExistingComponent != null) {
                                        removeGraphButton(draggedExistingComponent);
                                        copiedComponent = cloneComponent(draggedExistingComponent);
                                        drawables.remove(draggedExistingComponent);
                                        parametersPane.getChildren().remove(draggedExistingComponent.parameterControls);
                                        removeConnectedWires(draggedExistingComponent); // <-- add this!
                                        draggedExistingComponent = null;
                                        redrawCanvas();
                                        addFeedbackMessage(copiedComponent.componentType + " cut!", "success");
                                    } else if (selectedComponent != null) {
                                        removeGraphButton(selectedComponent);
                                        copiedComponent = cloneComponent(selectedComponent);
                                        drawables.remove(selectedComponent);
                                        parametersPane.getChildren().remove(selectedComponent.parameterControls);
                                        removeConnectedWires(selectedComponent); // <-- add this!
                                        selectedComponent = null;
                                        redrawCanvas();
                                        if (!undoStack.isEmpty()) {
                                            isUndoRedoOperation = true;
                                            UndoableAction actionUndo = undoStack.pop();
                                            actionUndo.undo();
                                            isUndoRedoOperation = false;
                                        }
                                        addFeedbackMessage(copiedComponent.componentType + " cut!", "info");
                                    }
                                    break;

                                case "Undo":
                                    if (!undoStack.isEmpty()) {
                                        isUndoRedoOperation = true;
                                        UndoableAction actionUndo = undoStack.pop();
                                        actionUndo.undo();
                                        redoStack.push(actionUndo);
                                        isUndoRedoOperation = false;
                                        addFeedbackMessage("Undo action performed.", "info");
                                    }
                                    break;

                                case "Redo":
                                    if (!redoStack.isEmpty()) {
                                        isUndoRedoOperation = true;
                                        UndoableAction actionRedo = redoStack.pop();
                                        actionRedo.redo();
                                        undoStack.push(actionRedo);
                                        isUndoRedoOperation = false;
                                        addFeedbackMessage("Redo action performed.", "info");
                                    }
                                    break;
                            }
                        }
                    }
                }
            });
        });
    }


    // ==================== Canvas Interaction ====================
    private void setupCanvasClickPlacement() {
        builder.setOnMouseClicked(e -> {
            if (isDrawingWire) return;
            if (floatingComponentImage.isVisible() && currentlySelectedImage != null) {
                placeComponent(e.getX(), e.getY());
            } else {
                selectOrCreateComponent(e.getX(), e.getY());
            }
        });
    }


    private void placeComponent(double x, double y) {
        if (isDrawingWire) return;

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

        // Create graph button for the new component
        if (newComponent instanceof ComponentsController.ImageComponent) {
            createGraphButton((ComponentsController.ImageComponent) newComponent);
        }

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
        if (isDrawingWire) return;

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
                    selectedComponent = component;
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
                    removeGraphButton(component);

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
            if (isDrawingWire) {
                e.consume();
                return;
            }
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
            if (isDrawingWire) {
                e.consume();
                return;
            }
            if (draggedExistingComponent != null) {
                draggedExistingComponent.x = e.getX() - offsetX[0];
                draggedExistingComponent.y = e.getY() - offsetY[0];
                updateWiresForComponent(draggedExistingComponent);
                redrawCanvas();
            }
        });

        builder.setOnMouseReleased(e -> {
            if (isDrawingWire) {
                e.consume();
                return;
            }
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
        temporaryAnalysis();
        startGraphUpdates(); // Start the graph updates when circuit is verified
    }

    @FXML
    private void handleReset(ActionEvent event) {
        stopGraphUpdates(); // Stop the graph updates when circuit is reset
        currentTime = 0;
        voltageHistory.clear();
        currentHistory.clear();
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
                    outPut = outPut + "\nMain Branch:";
                    System.out.println("Node "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created (- end)");
                    outPut = outPut + "\nNode "+ CircuitAnalyzerTest.CircuitGraph.nodes.get(numberOfComp+"").id +" is created (- end)";
                    break;
                }
            }
        }

        if (powerSupply == null) {
            addFeedbackMessage("No power supply found in the circuit.", "error");
            outPut = outPut + "\nNo power supply found in the circuit.";
            return false;
        }

        // Start traversal from the power supply

        addFeedbackMessage("Starting circuit verification from power supply...", "info");
        outPut = outPut + "\nStarting circuit verification from power supply...";
        Set<ComponentsController.Drawable> visited = new HashSet<>();
        boolean isClosed = traverseCircuit(powerSupply.startX, powerSupply.startY,
                powerSupply.startX, powerSupply.startY, powerSupply,visited);

        if (isClosed) {
            addFeedbackMessage("Circuit is closed! Found a complete path.", "success");
            outPut = outPut + "\nCircuit is closed! Found a complete path.";
            return true;
        } else {
            addFeedbackMessage("Circuit is open! No complete path found.", "error");
            outPut = outPut + "\nCircuit is open! No complete path found.";
            return false;
        }

    }

    private boolean traverseCircuit(double startX, double startY, double initialX, double initialY, ComponentsController.Drawable currentDraw, Set<ComponentsController.Drawable> visited) {
        // If we've reached the initial point and visited at least one component, we've found a closed circuit
        if (positiveConnected && negativeConnected) {
            addFeedbackMessage("Found closed circuit! Reached initial point.", "success");
            outPut = outPut + "\nFound closed circuit! Reached initial point.";
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


    @FXML private void handleExportJSON(ActionEvent event) {
        String filename = currentFile;
        sl.jsonWriter(filename, drawables);
    }
    @FXML private void handleExportCSV(ActionEvent event) {
       importingSave();
    }

    private void importingSave () {
        GraphicsContext gc = builder.getGraphicsContext2D();
        String filename = currentFile;
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
    @FXML private void handleExportText(ActionEvent event) {
        try {
            String data = outPut;
            Path outputPath = Path.of("src/main/resources/txt/debuggingLog.txt");
            FileWriter writer = new FileWriter(outputPath.toFile());
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
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

            // Remove the component's graph button
            removeGraphButton(draggedExistingComponent);

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

    String output;

    private void temporaryAnalysis() {
        if (positiveConnected && negativeConnected) {
            List<CircuitAnalyzerTest.CircuitGraph.Edge> path =
                    CG.findConductivePath(CG.getBatteryPlusTerminal(), CG.getBatteryMinusTerminal());

            double totalResistanceSeries = path.stream()
                    .mapToDouble(e -> e.component.getResistance())
                    .sum();

            double sourceVolt = path.stream()
                    .filter(e -> e.component instanceof ComponentsController.Battery)
                    .mapToDouble(e -> e.component.getVoltage())
                    .sum();

            if (totalResistanceSeries != 0) {
                double currentSeries = sourceVolt / totalResistanceSeries;

                // First pass: Update all components with their new current values
                for (CircuitAnalyzerTest.CircuitGraph.Edge ed : path) {
                    if (ed.component instanceof ComponentsController.ImageComponent) {
                        ComponentsController.ImageComponent ic = (ComponentsController.ImageComponent) ed.component;
                        
                        // Set current for all components in the path
                        ic.setCurrent(currentSeries);

                        // For resistors, calculate voltage drop
                        if (ic instanceof ComponentsController.ResistorIEEE) {
                            ic.setVoltage(ic.getResistance() * currentSeries);
                        }

                        // Debug: Print current value for battery
                        if (ic instanceof ComponentsController.Battery) {
                            System.out.println("Battery current after setting: " + ic.getCurrent());
                        }

                        addFeedbackMessage(
                                String.format("Component: %s from %s to %s — R=%.2f, I=%.2f, V=%.2f",
                                        ic.componentType, ed.from.id, ed.to.id,
                                        ic.getResistance(), ic.getCurrent(), ic.getVoltage()),
                                "info"
                        );
                    }
                }

                // Second pass: Update all graph data after all components have their new values
                for (CircuitAnalyzerTest.CircuitGraph.Edge ed : path) {
                    if (ed.component instanceof ComponentsController.ImageComponent) {
                        ComponentsController.ImageComponent ic = (ComponentsController.ImageComponent) ed.component;
                        // Debug: Print current value for battery before graph update
                        if (ic instanceof ComponentsController.Battery) {
                            System.out.println("Battery current before graph update: " + ic.getCurrent());
                        }
                        // Update graph data immediately after setting the values
                        updateGraphData(ic);
                    }
                }

                addFeedbackMessage(
                        String.format("Req: %.2f Ω, I: %.2f A", totalResistanceSeries, currentSeries),
                        "info"
                );
            }
        }
    }

    private void updateGraphData(ComponentsController.ImageComponent component) {
        // Find the graph button for this component
        for (Node node : graphContainer.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                VBox vbox = (VBox) button.getGraphic();
                Label label = (Label) vbox.getChildren().get(0);
                if (label.getText().startsWith(component.componentType)) {
                    // Get the current value before updating
                    double previousCurrent = component.getCurrent();
                    
                    // Update voltage graph
                    LineChart<Number, Number> voltageChart = (LineChart<Number, Number>) vbox.getChildren().get(1);
                    XYChart.Series<Number, Number> voltageSeries = voltageChart.getData().get(0);
                    
                    // Get or create history for this component
                    List<XYChart.Data<Number, Number>> voltageData = voltageHistory.computeIfAbsent(component, k -> new ArrayList<>());
                    
                    // Add new data point
                    voltageData.add(new XYChart.Data<>(currentTime, component.getVoltage()));
                    
                    // Update x-axis bounds to show the last 10 seconds
                    NumberAxis voltageXAxis = (NumberAxis) voltageChart.getXAxis();
                    double windowSize = 10.0; // 10 second window
                    double lowerBound = Math.max(0, currentTime - windowSize);
                    double upperBound = currentTime;
                    
                    // Remove data points that are outside the visible range
                    voltageData.removeIf(data -> data.getXValue().doubleValue() < lowerBound);
                    
                    // Update the series with only visible data points
                    voltageSeries.getData().setAll(voltageData);
                    
                    // Update axis bounds
                    voltageXAxis.setLowerBound(lowerBound);
                    voltageXAxis.setUpperBound(upperBound);

                    // Update current graph if it exists
                    if (vbox.getChildren().size() > 2) {
                        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) vbox.getChildren().get(3);
                        XYChart.Series<Number, Number> currentSeries = currentChart.getData().get(0);
                        
                        // Get or create history for this component
                        List<XYChart.Data<Number, Number>> currentData = currentHistory.computeIfAbsent(component, k -> new ArrayList<>());
                        
                        // Add new data point with the actual current value
                        double currentValue = component.getCurrent();
                        
                        // Only print debug message if the current value has changed
                        if (component instanceof ComponentsController.Battery && currentValue != previousCurrent) {
                            System.out.println("Battery current changed from " + previousCurrent + " to " + currentValue);
                        }
                        
                        currentData.add(new XYChart.Data<>(currentTime, currentValue));
                        
                        // Remove data points that are outside the visible range
                        currentData.removeIf(data -> data.getXValue().doubleValue() < lowerBound);
                        
                        // Update the series with only visible data points
                        currentSeries.getData().setAll(currentData);
                        
                        // Update axis bounds
                        NumberAxis currentXAxis = (NumberAxis) currentChart.getXAxis();
                        currentXAxis.setLowerBound(lowerBound);
                        currentXAxis.setUpperBound(upperBound);
                    }
                    break;
                }
            }
        }
    }

    private void createGraphButton(ComponentsController.ImageComponent component) {
        // Skip graphs for switches and ground elements
        if (component instanceof ComponentsController.SPSTToggleSwitch ||
            component instanceof ComponentsController.EarthGround  ||
            component instanceof ComponentsController.Voltmeter ||
            component instanceof ComponentsController.Ammeter ||
            component instanceof ComponentsController.Ohmmeter   ||
            component instanceof ComponentsController.Wattmeter) {
            return;
        }

        // Check if this component should have dual graphs
        boolean hasDualGraphs = component instanceof ComponentsController.ResistorIEEE ||
                              component instanceof ComponentsController.PotentiometerIEEE ||
                              component instanceof ComponentsController.Capacitor ||
                              component instanceof ComponentsController.Inductor ||
                              component instanceof ComponentsController.VoltageSource ||
                              component instanceof ComponentsController.Battery;

        Button graphButton = new Button();
        graphButton.setPrefHeight(hasDualGraphs ? 500.0 : 264.0);
        graphButton.setPrefWidth(437.0);
        graphButton.getStyleClass().add("graph-button");
        
        VBox graphVBox = new VBox();
        graphVBox.setPrefHeight(hasDualGraphs ? 490.0 : 270.0);
        graphVBox.setPrefWidth(419.0);
        graphVBox.getStyleClass().add("graph-vbox");
        
        // Create voltage graph
        Label voltageLabel = new Label(component.componentType + " - Voltage vs Time");
        voltageLabel.setAlignment(Pos.CENTER);
        voltageLabel.setPrefHeight(17.0);
        voltageLabel.setPrefWidth(510.0);
        voltageLabel.getStyleClass().add("graph-label");
        
        NumberAxis voltageXAxis = new NumberAxis();
        NumberAxis voltageYAxis = new NumberAxis();
        voltageXAxis.setLabel("Time (s)");
        voltageYAxis.setLabel("Voltage (V)");
        voltageXAxis.setAutoRanging(false);
        voltageXAxis.setLowerBound(0);
        voltageXAxis.setUpperBound(10); // Initial window size of 10 seconds
        
        LineChart<Number, Number> voltageChart = new LineChart<>(voltageXAxis, voltageYAxis);
        voltageChart.setPrefHeight(hasDualGraphs ? 200.0 : 229.0);
        voltageChart.setPrefWidth(462.0);
        voltageChart.getStyleClass().add("graph-chart");
        voltageChart.setAnimated(false);
        
        // Create voltage series
        XYChart.Series<Number, Number> voltageSeries = new XYChart.Series<>();
        voltageSeries.setName("Voltage");
        
        // Add initial data point
        voltageSeries.getData().add(new XYChart.Data<>(0, 0));
        
        voltageChart.getData().add(voltageSeries);
        
        graphVBox.getChildren().addAll(voltageLabel, voltageChart);

        // Add current graph for components that need it
        if (hasDualGraphs) {
            Label currentLabel = new Label(component.componentType + " - Current vs Time");
            currentLabel.setAlignment(Pos.CENTER);
            currentLabel.setPrefHeight(17.0);
            currentLabel.setPrefWidth(510.0);
            currentLabel.getStyleClass().add("graph-label");
            
            NumberAxis currentXAxis = new NumberAxis();
            NumberAxis currentYAxis = new NumberAxis();
            currentXAxis.setLabel("Time (s)");
            currentYAxis.setLabel("Current (A)");
            currentXAxis.setAutoRanging(false);
            currentXAxis.setLowerBound(0);
            currentXAxis.setUpperBound(10); // Initial window size of 10 seconds
            
            LineChart<Number, Number> currentChart = new LineChart<>(currentXAxis, currentYAxis);
            currentChart.setPrefHeight(200.0);
            currentChart.setPrefWidth(462.0);
            currentChart.getStyleClass().add("graph-chart");
            currentChart.setAnimated(false);
            
            // Create current series
            XYChart.Series<Number, Number> currentSeries = new XYChart.Series<>();
            currentSeries.setName("Current");
            
            // Add initial data point
            currentSeries.getData().add(new XYChart.Data<>(0, 0));
            
            currentChart.getData().add(currentSeries);
            
            graphVBox.getChildren().addAll(currentLabel, currentChart);
        }
        
        graphButton.setGraphic(graphVBox);
        graphContainer.getChildren().add(graphButton);
    }

    private void removeGraphButton(ComponentsController.ImageComponent component) {
        // Find and remove the graph button for this component
        for (Node node : graphContainer.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                VBox vbox = (VBox) button.getGraphic();
                Label label = (Label) vbox.getChildren().get(0);
                if (label.getText().startsWith(component.componentType)) {
                    graphContainer.getChildren().remove(button);
                    break;
                }
            }
        }
    }

    private void startGraphUpdates() {
        if (graphUpdateTimer != null) {
            graphUpdateTimer.cancel();
        }
        graphUpdateTimer = new Timer();
        graphUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    currentTime += UPDATE_INTERVAL / 1000.0; // Convert to seconds
                    // Only update graphs if the circuit is verified
                    if (positiveConnected && negativeConnected) {
                        updateAllGraphs();
                    }
                });
            }
        }, 0, UPDATE_INTERVAL);
    }

    private void stopGraphUpdates() {
        if (graphUpdateTimer != null) {
            graphUpdateTimer.cancel();
            graphUpdateTimer = null;
        }
    }

    private void updateAllGraphs() {
        for (Node node : graphContainer.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                VBox vbox = (VBox) button.getGraphic();
                Label label = (Label) vbox.getChildren().get(0);
                
                // Find the component for this graph
                for (ComponentsController.Drawable drawable : drawables) {
                    if (drawable instanceof ComponentsController.ImageComponent) {
                        ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                        if (label.getText().startsWith(component.componentType)) {
                            updateGraphData(component);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setFile(String filePath) {
        currentFile = filePath;
    }
}