package controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.Arrays;

/**
 * This class contains nested classes for each electrical component in the FXML file.
 * Each component is represented as a separate class with relevant electrical properties.
 */
public class ComponentsController {
    // Interface for drawable objects

    public interface Drawable {
        void draw(GraphicsContext gc);
    }

    // Distinguishing Types in Json
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ImageComponent.class, name = "component"),
            @JsonSubTypes.Type(value = Wire.class, name = "wire")
    })

    // Base class for all image components
    public static class ImageComponent extends ComponentBase implements Drawable {
        public ImageComponent () {
            this.image = null;
            this.imageURL = "";
            this.x = 0;
            this.y = 0;
            this.width = 0;
            this.height = 0;
            this.componentType = "";
            updateEndPoints();
        }
        public ImageComponent(Image image, double x, double y, double width, double height, String componentType) {
            this.image = image;
            this.imageURL = image.getUrl();
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.componentType = componentType;
            updateEndPoints();
        }

        public void updateEndPoints() {
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
            updateEndPoints();
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

    // Distinguishing Types in Json
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ComponentBase.class, name = "component"),
            @JsonSubTypes.Type(value = Wire.class, name = "wire")
    })
    public static class Wire implements Drawable {
        public String componentType = "Wire";
        public double startX, startY, endX, endY;
        @JsonIgnore
        public Circle endCircle;
        @JsonIgnore
        private boolean selected = false;

        public Wire() {
            this.startX = 0;
            this.startY = 0;
            this.endX = 0;
            this.endY = 0;
        }
        public Wire(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        @Override
        public void draw(GraphicsContext gc) {
            endCircle = new Circle(endX, endY, 6, Color.BLACK);
            if (selected) {
                gc.setStroke(Color.LIGHTBLUE);
                gc.setLineWidth(8);
                gc.strokeLine(startX, startY, endX, endY);
            }
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(4);
            gc.strokeLine(startX, startY, endX, endY);

            gc.setFill(Color.BLACK);
            gc.fillOval(endX - 6, endY - 6, 12, 12);
        }

        @Override
        public String toString() {
            return "Wire{" +
                    "componentType='" + componentType + '\'' +
                    ", startX=" + startX +
                    ", startY=" + startY +
                    ", endX=" + endX +
                    ", endY=" + endY +
                    ", endCircle=" + endCircle +
                    ", selected=" + selected +
                    '}';
        }
    }

    // Component data classes with their control generation methods

    // Switch and Relay Components
    public static class SPSTToggleSwitch extends ImageComponent {
        public boolean isClosed;

        public SPSTToggleSwitch(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "SPSTToggleSwitch");
            this.isClosed = false;
        }

        public static void addControls(VBox container) {
            Label stateLabel = new Label("State:");
            stateLabel.setStyle("-fx-text-fill: #F5F5F5;");
            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Open", "Closed");
            stateComboBox.setValue("Open");
            stateComboBox.setOnAction(e -> {
                SPSTToggleSwitch component = (SPSTToggleSwitch) container.getProperties().get("component");
                if (component != null) {
                    component.isClosed = stateComboBox.getValue().equals("Closed");
                    // Get the SimulationController instance and update circuit analysis
                    SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                    if (simulationController != null) {
                        simulationController.updateCircuitAnalysis();
                    }
                }
            });

            container.getChildren().addAll(stateLabel, stateComboBox);
        }
    }

    public static class PushbuttonSwitchNO extends ImageComponent {
        public boolean isPressed;

        public PushbuttonSwitchNO(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "PushbuttonSwitchNO");
            this.isPressed = false;
        }

        public static void addControls(VBox container) {
            Label stateLabel = new Label("State:");
            stateLabel.setStyle("-fx-text-fill: #F5F5F5;");
            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Released", "Pressed");
            stateComboBox.setValue("Released");
            stateComboBox.setOnAction(e -> {
                PushbuttonSwitchNO component = (PushbuttonSwitchNO) container.getProperties().get("component");
                if (component != null) {
                    component.isPressed = stateComboBox.getValue().equals("Pressed");
                    // Get the SimulationController instance and update circuit analysis
                    SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                    if (simulationController != null) {
                        simulationController.updateCircuitAnalysis();
                    }
                }
            });
            container.getChildren().addAll(stateLabel, stateComboBox);
        }
    }

    // Ground Components
    public static class EarthGround extends ImageComponent {
        public double resistance;

        public EarthGround(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "EarthGround");
            this.resistance = 0.0;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("0.0");
            resistanceField.setOnAction(e -> {
                EarthGround component = (EarthGround) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });
            container.getChildren().addAll(resistanceLabel, resistanceField);
        }
    }

    public static class ChassisGround extends ImageComponent {
        public double resistance;

        public ChassisGround(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "ChassisGround");
            this.resistance = 0.0;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("0.0");
            resistanceField.setOnAction(e -> {
                ChassisGround component = (ChassisGround) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });
            container.getChildren().addAll(resistanceLabel, resistanceField);
        }
    }

    // Resistor Components
    public static class ResistorIEEE extends ImageComponent {
        public double resistance;
        public double powerRating;

        public ResistorIEEE(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "ResistorIEEE");
            this.resistance = 10.0;
            this.powerRating = 0.25;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("100.0");
            resistanceField.setOnAction(e -> {
                ResistorIEEE component = (ResistorIEEE) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });

            Label powerLabel = new Label("Power Rating (W):");
            powerLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField powerField = new TextField("0.25");
            powerField.setOnAction(e -> {
                ResistorIEEE component = (ResistorIEEE) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.powerRating = Double.parseDouble(powerField.getText());
                } catch (NumberFormatException ex) {
                        powerField.setText(String.valueOf(component.powerRating));
                    }
                }
            });

            container.getChildren().addAll(resistanceLabel, resistanceField, powerLabel, powerField);
        }
    }

    public static class ResistorIEC extends ImageComponent {
        public double resistance;
        public double powerRating;

        public ResistorIEC(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "ResistorIEC");
            this.resistance = 100.0;
            this.powerRating = 0.25;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("100.0");
            resistanceField.setOnAction(e -> {
                ResistorIEC component = (ResistorIEC) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });

            Label powerLabel = new Label("Power Rating (W):");
            powerLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField powerField = new TextField("0.25");
            powerField.setOnAction(e -> {
                ResistorIEC component = (ResistorIEC) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.powerRating = Double.parseDouble(powerField.getText());
                } catch (NumberFormatException ex) {
                        powerField.setText(String.valueOf(component.powerRating));
                    }
                }
            });

            container.getChildren().addAll(resistanceLabel, resistanceField, powerLabel, powerField);
        }
    }

    public static class PotentiometerIEEE extends ImageComponent {
        public double resistance;
        public double powerRating;
        public double wiperPosition;

        public PotentiometerIEEE(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "PotentiometerIEEE");
            this.resistance = 1000.0;
            this.powerRating = 0.25;
            this.wiperPosition = 0.5;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Total Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("1000.0");
            resistanceField.setOnAction(e -> {
                PotentiometerIEEE component = (PotentiometerIEEE) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });

            Label powerLabel = new Label("Power Rating (W):");
            powerLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField powerField = new TextField("0.25");
            powerField.setOnAction(e -> {
                PotentiometerIEEE component = (PotentiometerIEEE) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.powerRating = Double.parseDouble(powerField.getText());
                } catch (NumberFormatException ex) {
                        powerField.setText(String.valueOf(component.powerRating));
                    }
                }
            });

            Label wiperLabel = new Label("Wiper Position (0-1):");
            wiperLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField wiperField = new TextField("0.5");
            wiperField.setOnAction(e -> {
                PotentiometerIEEE component = (PotentiometerIEEE) container.getProperties().get("component");
                if (component != null) {
                    try {
                        double position = Double.parseDouble(wiperField.getText());
                        component.wiperPosition = Math.max(0, Math.min(1, position));
                        wiperField.setText(String.valueOf(component.wiperPosition));
                } catch (NumberFormatException ex) {
                        wiperField.setText(String.valueOf(component.wiperPosition));
                    }
                }
            });

            container.getChildren().addAll(resistanceLabel, resistanceField, powerLabel, powerField, wiperLabel, wiperField);
        }
    }

    public static class PotentiometerIEC extends ImageComponent {
        public double resistance;
        public double powerRating;
        public double wiperPosition;

        public PotentiometerIEC(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "PotentiometerIEC");
            this.resistance = 1000.0;
            this.powerRating = 0.25;
            this.wiperPosition = 0.5;
        }

        public static void addControls(VBox container) {
            Label resistanceLabel = new Label("Total Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField resistanceField = new TextField("1000.0");
            resistanceField.setOnAction(e -> {
                PotentiometerIEC component = (PotentiometerIEC) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.resistance = Double.parseDouble(resistanceField.getText());
                } catch (NumberFormatException ex) {
                        resistanceField.setText(String.valueOf(component.resistance));
                    }
                }
            });

            Label powerLabel = new Label("Power Rating (W):");
            powerLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField powerField = new TextField("0.25");
            powerField.setOnAction(e -> {
                PotentiometerIEC component = (PotentiometerIEC) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.powerRating = Double.parseDouble(powerField.getText());
                } catch (NumberFormatException ex) {
                        powerField.setText(String.valueOf(component.powerRating));
                    }
                }
            });

            Label wiperLabel = new Label("Wiper Position (0-1):");
            wiperLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField wiperField = new TextField("0.5");
            wiperField.setOnAction(e -> {
                PotentiometerIEC component = (PotentiometerIEC) container.getProperties().get("component");
                if (component != null) {
                    try {
                        double position = Double.parseDouble(wiperField.getText());
                        component.wiperPosition = Math.max(0, Math.min(1, position));
                        wiperField.setText(String.valueOf(component.wiperPosition));
                } catch (NumberFormatException ex) {
                        wiperField.setText(String.valueOf(component.wiperPosition));
                    }
                }
            });

            container.getChildren().addAll(resistanceLabel, resistanceField, powerLabel, powerField, wiperLabel, wiperField);
        }
    }

    // Capacitor Components
    public static class Capacitor extends ImageComponent {
        public double capacitance;
        public double voltageRating;
        public double voltage;

        public Capacitor(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Capacitor");
            this.capacitance = 1e-6;
            this.voltageRating = 16.0;
            this.voltage = 0.0;
        }

        public static void addControls(VBox container) {
            Label capacitanceLabel = new Label("Capacitance (F):");
            capacitanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField capacitanceField = new TextField("1e-6");
            capacitanceField.setOnAction(e -> {
                Capacitor component = (Capacitor) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.capacitance = Double.parseDouble(capacitanceField.getText());
                } catch (NumberFormatException ex) {
                        capacitanceField.setText(String.valueOf(component.capacitance));
                    }
                }
            });

            Label voltageRatingLabel = new Label("Voltage Rating (V):");
            voltageRatingLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField voltageRatingField = new TextField("16.0");
            voltageRatingField.setOnAction(e -> {
                Capacitor component = (Capacitor) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.voltageRating = Double.parseDouble(voltageRatingField.getText());
                } catch (NumberFormatException ex) {
                        voltageRatingField.setText(String.valueOf(component.voltageRating));
                    }
                }
            });

            Label voltageLabel = new Label("Initial Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField voltageField = new TextField("0.0");
            voltageField.setOnAction(e -> {
                Capacitor component = (Capacitor) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.voltage = Double.parseDouble(voltageField.getText());
                } catch (NumberFormatException ex) {
                        voltageField.setText(String.valueOf(component.voltage));
                    }
                }
            });

            container.getChildren().addAll(capacitanceLabel, capacitanceField, voltageRatingLabel, voltageRatingField, voltageLabel, voltageField);
        }
    }

    // Inductor and Coil Components
    public static class Inductor extends ImageComponent {
        public double inductance;
        public double currentRating;
        public double current;

        public Inductor(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Inductor");
            this.inductance = 1e-3;
            this.currentRating = 1.0;
            this.current = 0.0;
        }

        public static void addControls(VBox container) {
            Label inductanceLabel = new Label("Inductance (H):");
            inductanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField inductanceField = new TextField("1e-3");
            inductanceField.setOnAction(e -> {
                Inductor component = (Inductor) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.inductance = Double.parseDouble(inductanceField.getText());
                } catch (NumberFormatException ex) {
                        inductanceField.setText(String.valueOf(component.inductance));
                    }
                }
            });

            Label currentRatingLabel = new Label("Current Rating (A):");
            currentRatingLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField currentRatingField = new TextField("1.0");
            currentRatingField.setOnAction(e -> {
                Inductor component = (Inductor) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.currentRating = Double.parseDouble(currentRatingField.getText());
                } catch (NumberFormatException ex) {
                        currentRatingField.setText(String.valueOf(component.currentRating));
                    }
                }
            });

            Label currentLabel = new Label("Initial Current (A):");
            currentLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField currentField = new TextField("0.0");
            currentField.setOnAction(e -> {
                Inductor component = (Inductor) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.current = Double.parseDouble(currentField.getText());
                } catch (NumberFormatException ex) {
                        currentField.setText(String.valueOf(component.current));
                    }
                }
            });

            container.getChildren().addAll(inductanceLabel, inductanceField, currentRatingLabel, currentRatingField, currentLabel, currentField);
        }
    }

    // Power Supply Components
    public static class VoltageSource extends ImageComponent {
        public double voltage;
        public double internalResistance;

        public VoltageSource(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "VoltageSource");
            this.voltage = 12.0;
            this.internalResistance = 0.1;
        }

        public static void addControls(VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField voltageField = new TextField("12.0");
            voltageField.setOnAction(e -> {
                VoltageSource component = (VoltageSource) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.voltage = Double.parseDouble(voltageField.getText());
                } catch (NumberFormatException ex) {
                        voltageField.setText(String.valueOf(component.voltage));
                    }
                }
            });

            Label internalResistanceLabel = new Label("Internal Resistance (Ω):");
            internalResistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField internalResistanceField = new TextField("0.1");
            internalResistanceField.setOnAction(e -> {
                VoltageSource component = (VoltageSource) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.internalResistance = Double.parseDouble(internalResistanceField.getText());
                } catch (NumberFormatException ex) {
                        internalResistanceField.setText(String.valueOf(component.internalResistance));
                    }
                }
            });

            container.getChildren().addAll(voltageLabel, voltageField, internalResistanceLabel, internalResistanceField);
        }
    }

    public static class BatteryCell extends ImageComponent {
        public double voltage;
        public double internalResistance;
        public double capacity;

        public BatteryCell(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "BatteryCell");
            this.voltage = 5;
            this.internalResistance = 0.1;
            this.capacity = 2000.0;
        }

        public static void addControls(VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField voltageField = new TextField("1.5");
            voltageField.setOnAction(e -> {
                BatteryCell component = (BatteryCell) container.getProperties().get("component");
                if (component != null) {
                try {
                        component.voltage = Double.parseDouble(voltageField.getText());
                } catch (NumberFormatException ex) {
                        voltageField.setText(String.valueOf(component.voltage));
                    }
                }
            });

            Label internalResistanceLabel = new Label("Internal Resistance (Ω):");
            internalResistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField internalResistanceField = new TextField("0.1");
            internalResistanceField.setOnAction(e -> {
                BatteryCell component = (BatteryCell) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.internalResistance = Double.parseDouble(internalResistanceField.getText());
                } catch (NumberFormatException ex) {
                        internalResistanceField.setText(String.valueOf(component.internalResistance));
                    }
                }
            });

            Label capacityLabel = new Label("Capacity (mAh):");
            capacityLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField capacityField = new TextField("2000.0");
            capacityField.setOnAction(e -> {
                BatteryCell component = (BatteryCell) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.capacity = Double.parseDouble(capacityField.getText());
                } catch (NumberFormatException ex) {
                        capacityField.setText(String.valueOf(component.capacity));
                    }
                }
            });

            container.getChildren().addAll(voltageLabel, voltageField, internalResistanceLabel, internalResistanceField, capacityLabel, capacityField);
        }
    }

    public static class Battery extends ImageComponent {
        public double voltage;
        public double internalResistance;
        public double capacity;

        public Battery(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Battery");
            this.voltage = 10.0;
            this.internalResistance = 0.5;
            this.capacity = 500.0;
        }

        public static void addControls(VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField voltageField = new TextField("9.0");
            voltageField.setOnAction(e -> {
                Battery component = (Battery) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.voltage = Double.parseDouble(voltageField.getText());
                } catch (NumberFormatException ex) {
                        voltageField.setText(String.valueOf(component.voltage));
                    }
                }
            });

            Label internalResistanceLabel = new Label("Internal Resistance (Ω):");
            internalResistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField internalResistanceField = new TextField("0.5");
            internalResistanceField.setOnAction(e -> {
                Battery component = (Battery) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.internalResistance = Double.parseDouble(internalResistanceField.getText());
                } catch (NumberFormatException ex) {
                        internalResistanceField.setText(String.valueOf(component.internalResistance));
                    }
                }
            });

            Label capacityLabel = new Label("Capacity (mAh):");
            capacityLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField capacityField = new TextField("500.0");
            capacityField.setOnAction(e -> {
                Battery component = (Battery) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.capacity = Double.parseDouble(capacityField.getText());
                } catch (NumberFormatException ex) {
                        capacityField.setText(String.valueOf(component.capacity));
                    }
                }
            });

            container.getChildren().addAll(voltageLabel, voltageField, internalResistanceLabel, internalResistanceField, capacityLabel, capacityField);
        }
    }

    // Meter Components
    public static class Voltmeter extends ImageComponent {
        public double range;
        public double internalResistance;
        private CircuitAnalyzer analyzer;
        private Label measurementLabel;

        public Voltmeter(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Voltmeter");
            this.range = 20.0;
            this.internalResistance = 1e6;
            this.measurementLabel = new Label("0.00 V");
            this.measurementLabel.setStyle("-fx-text-fill: #F5F5F5;");
        }

        public void setAnalyzer(CircuitAnalyzer analyzer) {
            this.analyzer = analyzer;
            updateMeasurement();
        }

        private void updateMeasurement() {
            if (analyzer != null) {
                double voltage = analyzer.getVoltageAcross(this);
                measurementLabel.setText(String.format("%.2f V", voltage));
            }
        }

        public static void addControls(VBox container) {
            Label rangeLabel = new Label("Range (V):");
            rangeLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField rangeField = new TextField("20.0");
            rangeField.setOnAction(e -> {
                Voltmeter component = (Voltmeter) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.range = Double.parseDouble(rangeField.getText());
                } catch (NumberFormatException ex) {
                        rangeField.setText(String.valueOf(component.range));
                    }
                }
            });

            Label internalResistanceLabel = new Label("Internal Resistance (Ω):");
            internalResistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField internalResistanceField = new TextField("1e6");
            internalResistanceField.setOnAction(e -> {
                Voltmeter component = (Voltmeter) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.internalResistance = Double.parseDouble(internalResistanceField.getText());
                } catch (NumberFormatException ex) {
                        internalResistanceField.setText(String.valueOf(component.internalResistance));
                    }
                }
            });

            container.getChildren().addAll(rangeLabel, rangeField, internalResistanceLabel, internalResistanceField);
        }
    }

    public static class Ammeter extends ImageComponent {
        public double range;
        public double internalResistance;
        private CircuitAnalyzer analyzer;
        private Label measurementLabel;

        public Ammeter(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Ammeter");
            this.range = 1.0;
            this.internalResistance = 0.1;
            this.measurementLabel = new Label("0.00 A");
            this.measurementLabel.setStyle("-fx-text-fill: #F5F5F5;");
        }

        public void setAnalyzer(CircuitAnalyzer analyzer) {
            this.analyzer = analyzer;
            updateMeasurement();
        }

        private void updateMeasurement() {
            if (analyzer != null) {
                double current = analyzer.getCurrentThrough(this);
                measurementLabel.setText(String.format("%.2f A", current));
            }
        }

        public static void addControls(VBox container) {
            Label rangeLabel = new Label("Range (A):");
            rangeLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField rangeField = new TextField("1.0");
            rangeField.setOnAction(e -> {
                Ammeter component = (Ammeter) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.range = Double.parseDouble(rangeField.getText());
                } catch (NumberFormatException ex) {
                        rangeField.setText(String.valueOf(component.range));
                    }
                }
            });

            Label internalResistanceLabel = new Label("Internal Resistance (Ω):");
            internalResistanceLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField internalResistanceField = new TextField("0.1");
            internalResistanceField.setOnAction(e -> {
                Ammeter component = (Ammeter) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.internalResistance = Double.parseDouble(internalResistanceField.getText());
                } catch (NumberFormatException ex) {
                        internalResistanceField.setText(String.valueOf(component.internalResistance));
                    }
                }
            });

            container.getChildren().addAll(rangeLabel, rangeField, internalResistanceLabel, internalResistanceField);
        }
    }

    public static class Ohmmeter extends ImageComponent {
        public double range;
        private CircuitAnalyzer analyzer;
        private Label measurementLabel;

        public Ohmmeter(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Ohmmeter");
            this.range = 1000.0;
            this.measurementLabel = new Label("0.00 Ω");
            this.measurementLabel.setStyle("-fx-text-fill: #F5F5F5;");
        }

        public void setAnalyzer(CircuitAnalyzer analyzer) {
            this.analyzer = analyzer;
            updateMeasurement();
        }

        private void updateMeasurement() {
            if (analyzer != null) {
                double resistance = analyzer.getResistance(this);
                measurementLabel.setText(String.format("%.2f Ω", resistance));
            }
        }

        public static void addControls(VBox container) {
            Label rangeLabel = new Label("Range (Ω):");
            rangeLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField rangeField = new TextField("1000.0");
            rangeField.setOnAction(e -> {
                Ohmmeter component = (Ohmmeter) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.range = Double.parseDouble(rangeField.getText());
                } catch (NumberFormatException ex) {
                        rangeField.setText(String.valueOf(component.range));
                    }
                }
            });

            container.getChildren().addAll(rangeLabel, rangeField);
        }
    }

    // Diode and LED Components
    public static class Diode extends ImageComponent {
        public double forwardVoltage;
        public double reverseBreakdownVoltage;
        public double forwardCurrent;
        public double reverseCurrent;

        public Diode(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Diode");
            this.forwardVoltage = 0.7;
            this.reverseBreakdownVoltage = 50.0;
            this.forwardCurrent = 1.0;
            this.reverseCurrent = 1e-6;
        }

        public static void addControls(VBox container) {
            Label forwardVoltageLabel = new Label("Forward Voltage (V):");
            forwardVoltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField forwardVoltageField = new TextField("0.7");
            forwardVoltageField.setOnAction(e -> {
                Diode component = (Diode) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.forwardVoltage = Double.parseDouble(forwardVoltageField.getText());
                } catch (NumberFormatException ex) {
                        forwardVoltageField.setText(String.valueOf(component.forwardVoltage));
                    }
                }
            });

            Label reverseBreakdownLabel = new Label("Reverse Breakdown (V):");
            reverseBreakdownLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField reverseBreakdownField = new TextField("50.0");
            reverseBreakdownField.setOnAction(e -> {
                Diode component = (Diode) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.reverseBreakdownVoltage = Double.parseDouble(reverseBreakdownField.getText());
                } catch (NumberFormatException ex) {
                        reverseBreakdownField.setText(String.valueOf(component.reverseBreakdownVoltage));
                    }
                }
            });

            Label forwardCurrentLabel = new Label("Forward Current (A):");
            forwardCurrentLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField forwardCurrentField = new TextField("1.0");
            forwardCurrentField.setOnAction(e -> {
                Diode component = (Diode) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.forwardCurrent = Double.parseDouble(forwardCurrentField.getText());
                } catch (NumberFormatException ex) {
                        forwardCurrentField.setText(String.valueOf(component.forwardCurrent));
                    }
                }
            });

            Label reverseCurrentLabel = new Label("Reverse Current (A):");
            reverseCurrentLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField reverseCurrentField = new TextField("1e-6");
            reverseCurrentField.setOnAction(e -> {
                Diode component = (Diode) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.reverseCurrent = Double.parseDouble(reverseCurrentField.getText());
                } catch (NumberFormatException ex) {
                        reverseCurrentField.setText(String.valueOf(component.reverseCurrent));
                    }
                }
            });

            container.getChildren().addAll(forwardVoltageLabel, forwardVoltageField, reverseBreakdownLabel, reverseBreakdownField, forwardCurrentLabel, forwardCurrentField, reverseCurrentLabel, reverseCurrentField);
        }
    }

    public static class LED extends ImageComponent {
        public double forwardVoltage;
        public double forwardCurrent;
        public String color;

        public LED(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "LED");
            this.forwardVoltage = 2.0;
            this.forwardCurrent = 0.02;
            this.color = "Red";
        }

        public static void addControls(VBox container) {
            Label forwardVoltageLabel = new Label("Forward Voltage (V):");
            forwardVoltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField forwardVoltageField = new TextField("2.0");
            forwardVoltageField.setOnAction(e -> {
                LED component = (LED) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.forwardVoltage = Double.parseDouble(forwardVoltageField.getText());
                } catch (NumberFormatException ex) {
                        forwardVoltageField.setText(String.valueOf(component.forwardVoltage));
                    }
                }
            });

            Label forwardCurrentLabel = new Label("Forward Current (A):");
            forwardCurrentLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField forwardCurrentField = new TextField("0.02");
            forwardCurrentField.setOnAction(e -> {
                LED component = (LED) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.forwardCurrent = Double.parseDouble(forwardCurrentField.getText());
                } catch (NumberFormatException ex) {
                        forwardCurrentField.setText(String.valueOf(component.forwardCurrent));
                    }
                }
            });

            Label colorLabel = new Label("Color:");
            colorLabel.setStyle("-fx-text-fill: #F5F5F5;");
            ComboBox<String> colorComboBox = new ComboBox<>();
            colorComboBox.getItems().addAll("Red", "Green", "Blue", "Yellow", "White");
            colorComboBox.setValue("Red");
            colorComboBox.setOnAction(e -> {
                LED component = (LED) container.getProperties().get("component");
                if (component != null) {
                    component.color = colorComboBox.getValue();
                }
            });

            container.getChildren().addAll(forwardVoltageLabel, forwardVoltageField, forwardCurrentLabel, forwardCurrentField, colorLabel, colorComboBox);
        }
    }

    // Miscellaneous Components
    public static class Transformer extends ImageComponent {
        public double primaryVoltage;
        public double secondaryVoltage;
        public double turnsRatio;

        public Transformer(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Transformer");
            this.primaryVoltage = 120.0;
            this.secondaryVoltage = 12.0;
            this.turnsRatio = 10.0;
        }

        public static void addControls(VBox container) {
            Label primaryVoltageLabel = new Label("Primary Voltage (V):");
            primaryVoltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField primaryVoltageField = new TextField("120.0");
            primaryVoltageField.setOnAction(e -> {
                Transformer component = (Transformer) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.primaryVoltage = Double.parseDouble(primaryVoltageField.getText());
                } catch (NumberFormatException ex) {
                        primaryVoltageField.setText(String.valueOf(component.primaryVoltage));
                    }
                }
            });

            Label secondaryVoltageLabel = new Label("Secondary Voltage (V):");
            secondaryVoltageLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField secondaryVoltageField = new TextField("12.0");
            secondaryVoltageField.setOnAction(e -> {
                Transformer component = (Transformer) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.secondaryVoltage = Double.parseDouble(secondaryVoltageField.getText());
                } catch (NumberFormatException ex) {
                        secondaryVoltageField.setText(String.valueOf(component.secondaryVoltage));
                    }
                }
            });

            Label turnsRatioLabel = new Label("Turns Ratio:");
            turnsRatioLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField turnsRatioField = new TextField("10.0");
            turnsRatioField.setOnAction(e -> {
                Transformer component = (Transformer) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.turnsRatio = Double.parseDouble(turnsRatioField.getText());
                } catch (NumberFormatException ex) {
                        turnsRatioField.setText(String.valueOf(component.turnsRatio));
                    }
                }
            });

            container.getChildren().addAll(primaryVoltageLabel, primaryVoltageField, secondaryVoltageLabel, secondaryVoltageField, turnsRatioLabel, turnsRatioField);
        }
    }

    public static class Fuse extends ImageComponent {
        public double ratedCurrent;
        public double breakingCapacity;

        public Fuse(Image image, double x, double y, double width, double height) {
            super(image, x, y, width, height, "Fuse");
            this.ratedCurrent = 1.0;
            this.breakingCapacity = 100.0;
        }

        public static void addControls(VBox container) {
            Label ratedCurrentLabel = new Label("Rated Current (A):");
            ratedCurrentLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField ratedCurrentField = new TextField("1.0");
            ratedCurrentField.setOnAction(e -> {
                Fuse component = (Fuse) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.ratedCurrent = Double.parseDouble(ratedCurrentField.getText());
                } catch (NumberFormatException ex) {
                        ratedCurrentField.setText(String.valueOf(component.ratedCurrent));
                    }
                }
            });

            Label breakingCapacityLabel = new Label("Breaking Capacity (A):");
            breakingCapacityLabel.setStyle("-fx-text-fill: #F5F5F5;");
            TextField breakingCapacityField = new TextField("100.0");
            breakingCapacityField.setOnAction(e -> {
                Fuse component = (Fuse) container.getProperties().get("component");
                if (component != null) {
                    try {
                        component.breakingCapacity = Double.parseDouble(breakingCapacityField.getText());
                } catch (NumberFormatException ex) {
                        breakingCapacityField.setText(String.valueOf(component.breakingCapacity));
                    }
                }
            });

            container.getChildren().addAll(ratedCurrentLabel, ratedCurrentField, breakingCapacityLabel, breakingCapacityField);
        }
    }

    // Main method to generate parameter controls
    public static void generateParameterControls(ImageComponent component, VBox parametersPane) {
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: #2A2A2A; -fx-padding: 10; -fx-background-radius: 5;");
        container.getProperties().put("component", component);

        // Add component type label
        Label typeLabel = new Label(component.componentType);
        typeLabel.setStyle("-fx-text-fill: #F5F5F5; -fx-font-weight: bold;");
        container.getChildren().add(typeLabel);

        // Add controls based on component type
        if (component instanceof SPSTToggleSwitch) {
            SPSTToggleSwitch.addControls(container);
        } else if (component instanceof PushbuttonSwitchNO) {
            PushbuttonSwitchNO.addControls(container);
        } else if (component instanceof ResistorIEEE) {
            ResistorIEEE.addControls(container);
        } else if (component instanceof ResistorIEC) {
            ResistorIEC.addControls(container);
        } else if (component instanceof PotentiometerIEEE) {
            PotentiometerIEEE.addControls(container);
        } else if (component instanceof PotentiometerIEC) {
            PotentiometerIEC.addControls(container);
        } else if (component instanceof Capacitor) {
            Capacitor.addControls(container);
        } else if (component instanceof Inductor) {
            Inductor.addControls(container);
        } else if (component instanceof VoltageSource) {
            VoltageSource.addControls(container);
        } else if (component instanceof BatteryCell) {
            BatteryCell.addControls(container);
        } else if (component instanceof Battery) {
            Battery.addControls(container);
        } else if (component instanceof Voltmeter) {
            Voltmeter.addControls(container);
        } else if (component instanceof Ammeter) {
            Ammeter.addControls(container);
        } else if (component instanceof Ohmmeter) {
            Ohmmeter.addControls(container);
        }

        // Get the SimulationController instance from the parametersPane
        SimulationController simulationController = (SimulationController) parametersPane.getProperties().get("simulationController");
        if (simulationController != null) {
            container.getProperties().put("simulationController", simulationController);
        }

        parametersPane.getChildren().add(container);
        component.parameterControls = container;
    }
}