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
        public Image image;
        public String imageURL;
        public double x;
        public double y;
        public double width;
        public double height;
        public String componentType;
        public VBox parameterControls;

        public ImageComponent() {
            this.image = null;
            this.imageURL = "";
            this.x = 0;
            this.y = 0;
            this.width = 0;
            this.height = 0;
            this.componentType = "";
            updateEndPoints();
        }

        public ImageComponent(String componentType, String imagePath) {
            this.componentType = componentType;
            try {
                this.image = new Image(ComponentsController.class.getResourceAsStream(imagePath));
                this.imageURL = image.getUrl();
                this.width = 40;
                this.height = 40;
                this.x = 0;
                this.y = 0;
                updateEndPoints();
            } catch (Exception e) {
                System.err.println("Error loading image: " + imagePath);
                e.printStackTrace();
            }
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
        public boolean isClosed = false;

        public SPSTToggleSwitch() {
            super("SPSTToggleSwitch", "/images/components/SPSTToggleSwitch.png");
        }

        public static void addControls(SPSTToggleSwitch switch_, VBox container) {
            Label stateLabel = new Label("State:");
            stateLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(stateLabel);

            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Open", "Closed");
            stateComboBox.setValue(switch_.isClosed ? "Closed" : "Open");
            stateComboBox.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            stateComboBox.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #3a3a3a;");
                    }
                }
            });
            container.getChildren().add(stateComboBox);

            stateComboBox.setOnAction(e -> {
                switch_.isClosed = stateComboBox.getValue().equals("Closed");
                // Get the SimulationController instance and update circuit analysis
                SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                if (simulationController != null) {
                    simulationController.updateCircuitAnalysis();
                }
            });
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
            resistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField resistanceField = new TextField("0.0");
            resistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            resistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField resistanceField = new TextField("0.0");
            resistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
        public ResistorIEEE() {
            super("ResistorIEEE", "/images/components/ResistorIEEE.png");
            setResistance(100); // Set default resistance to 100Ω
        }

        public static void addControls(ResistorIEEE resistor, VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(resistanceLabel);

            TextField resistanceField = new TextField(String.valueOf(resistor.getResistance())); // Use getter
            resistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(resistanceField);

            resistanceField.setOnAction(e -> {
                try {
                    double newResistance = Double.parseDouble(resistanceField.getText());
                    if (newResistance > 0) {
                        resistor.setResistance(newResistance); // Use setter
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    resistanceField.setText(String.valueOf(resistor.getResistance())); // Use getter
                }
            });
        }
    }

    public static class ResistorIEC extends ImageComponent {
        public ResistorIEC() {
            super("ResistorIEC", "/images/components/ResistorIEC.png");
            setResistance(100); // Set default resistance to 100Ω
        }

        public static void addControls(ResistorIEC resistor, VBox container) {
            Label resistanceLabel = new Label("Resistance (Ω):");
            resistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(resistanceLabel);

            TextField resistanceField = new TextField(String.valueOf(resistor.getResistance())); // Use getter
            resistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(resistanceField);

            resistanceField.setOnAction(e -> {
                try {
                    double newResistance = Double.parseDouble(resistanceField.getText());
                    if (newResistance > 0) {
                        resistor.setResistance(newResistance); // Use setter
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    resistanceField.setText(String.valueOf(resistor.getResistance())); // Use getter
                }
            });
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
            resistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField resistanceField = new TextField("1000.0");
            resistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            powerLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField powerField = new TextField("0.25");
            powerField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            wiperLabel.setStyle("-fx-text-fill: #FFFFFF;");
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
            powerLabel.setStyle("-fx-text-fill: #FFFFFF;");
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
            wiperLabel.setStyle("-fx-text-fill: #FFFFFF;");
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
        public double capacitance = 100e-6; // Default 100µF

        public Capacitor() {
            super("Capacitor", "/images/components/Capacitor.png");
        }

        public static void addControls(Capacitor capacitor, VBox container) {
            Label capacitanceLabel = new Label("Capacitance (F):");
            capacitanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(capacitanceLabel);

            TextField capacitanceField = new TextField(String.valueOf(capacitor.capacitance));
            capacitanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(capacitanceField);

            capacitanceField.setOnAction(e -> {
                try {
                    double newCapacitance = Double.parseDouble(capacitanceField.getText());
                    if (newCapacitance > 0) {
                        capacitor.capacitance = newCapacitance;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    capacitanceField.setText(String.valueOf(capacitor.capacitance));
                }
            });
        }
    }

    // Inductor and Coil Components
    public static class Inductor extends ImageComponent {
        public double inductance = 1e-3; // Default 1mH

        public Inductor() {
            super("Inductor", "/images/components/Inductor.png");
        }

        public static void addControls(Inductor inductor, VBox container) {
            Label inductanceLabel = new Label("Inductance (H):");
            inductanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(inductanceLabel);

            TextField inductanceField = new TextField(String.valueOf(inductor.inductance));
            inductanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(inductanceField);

            inductanceField.setOnAction(e -> {
                try {
                    double newInductance = Double.parseDouble(inductanceField.getText());
                    if (newInductance > 0) {
                        inductor.inductance = newInductance;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    inductanceField.setText(String.valueOf(inductor.inductance));
                }
            });
        }
    }

    // Power Supply Components
    public static class VoltageSource extends ImageComponent {
        public double voltage = 12; // Default 12V

        public VoltageSource() {
            super("VoltageSource", "/images/components/VoltageSource.png");
        }

        public static void addControls(VoltageSource source, VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageLabel);

            TextField voltageField = new TextField(String.valueOf(source.voltage));
            voltageField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageField);

            voltageField.setOnAction(e -> {
                try {
                    double newVoltage = Double.parseDouble(voltageField.getText());
                    source.voltage = newVoltage;
                    // Get the SimulationController instance and update circuit analysis
                    SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                    if (simulationController != null) {
                        simulationController.updateCircuitAnalysis();
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    voltageField.setText(String.valueOf(source.voltage));
                }
            });
        }
    }

    public static class BatteryCell extends ImageComponent {
        public double voltage = 1.5; // Default 1.5V

        public BatteryCell() {
            super("BatteryCell", "/images/components/BatteryCell.png");
        }

        public static void addControls(BatteryCell cell, VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageLabel);

            TextField voltageField = new TextField(String.valueOf(cell.voltage));
            voltageField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageField);

            voltageField.setOnAction(e -> {
                try {
                    double newVoltage = Double.parseDouble(voltageField.getText());
                    if (newVoltage > 0) {
                        cell.voltage = newVoltage;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    voltageField.setText(String.valueOf(cell.voltage));
                }
            });
        }
    }

    public static class Battery extends ImageComponent {
        public double voltage = 9; // Default 9V

        public Battery() {
            super("Battery", "/images/components/Battery.png");
            setVoltage(9); // Set the voltage in the base class
        }

        public static void addControls(Battery battery, VBox container) {
            Label voltageLabel = new Label("Voltage (V):");
            voltageLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageLabel);

            TextField voltageField = new TextField(String.valueOf(battery.getVoltage()));
            voltageField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(voltageField);

            voltageField.setOnAction(e -> {
                try {
                    double newVoltage = Double.parseDouble(voltageField.getText());
                    if (newVoltage > 0) {
                        battery.voltage = newVoltage;
                        battery.setVoltage(newVoltage);
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    voltageField.setText(String.valueOf(battery.getVoltage()));
                }
            });
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
            rangeLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField rangeField = new TextField("20.0");
            rangeField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            internalResistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField internalResistanceField = new TextField("1e6");
            internalResistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            rangeLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField rangeField = new TextField("1.0");
            rangeField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            internalResistanceLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField internalResistanceField = new TextField("0.1");
            internalResistanceField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            rangeLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField rangeField = new TextField("1000.0");
            rangeField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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

    // Logic Gate Components
    public static class NOTGate extends ImageComponent {
        public double propagationDelay; // Propagation delay in nanoseconds

        public NOTGate() {
            super("NOTGate", "/images/components/NOTGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(NOTGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    public static class ANDGate extends ImageComponent {
        public double propagationDelay;

        public ANDGate() {
            super("ANDGate", "/images/components/ANDGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(ANDGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    public static class NANDGate extends ImageComponent {
        public double propagationDelay;

        public NANDGate() {
            super("NANDGate", "/images/components/NANDGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(NANDGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    public static class ORGate extends ImageComponent {
        public double propagationDelay;

        public ORGate() {
            super("ORGate", "/images/components/ORGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(ORGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    public static class NORGate extends ImageComponent {
        public double propagationDelay;

        public NORGate() {
            super("NORGate", "/images/components/NORGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(NORGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    public static class XORGate extends ImageComponent {
        public double propagationDelay;

        public XORGate() {
            super("XORGate", "/images/components/XORGate.png");
            this.propagationDelay = 10.0; // Default 10ns
        }

        public static void addControls(XORGate gate, VBox container) {
            Label propagationDelayLabel = new Label("Propagation Delay (ns):");
            propagationDelayLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayLabel);

            TextField propagationDelayField = new TextField(String.valueOf(gate.propagationDelay));
            propagationDelayField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(propagationDelayField);

            propagationDelayField.setOnAction(e -> {
                try {
                    double newDelay = Double.parseDouble(propagationDelayField.getText());
                    if (newDelay >= 0) {
                        gate.propagationDelay = newDelay;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    propagationDelayField.setText(String.valueOf(gate.propagationDelay));
                }
            });
        }
    }

    // Diode and LED Components
    public static class Diode extends ImageComponent {
        public double forwardVoltage = 0.7; // Default 0.7V
        public double maxCurrent = 1.0; // Default 1A

        public Diode() {
            super("Diode", "/images/components/Diode.png");
        }

        public static void addControls(Diode diode, VBox container) {
            Label forwardVoltageLabel = new Label("Forward Voltage (V):");
            forwardVoltageLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(forwardVoltageLabel);

            TextField forwardVoltageField = new TextField(String.valueOf(diode.forwardVoltage));
            forwardVoltageField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(forwardVoltageField);

            forwardVoltageField.setOnAction(e -> {
                try {
                    double newForwardVoltage = Double.parseDouble(forwardVoltageField.getText());
                    if (newForwardVoltage > 0) {
                        diode.forwardVoltage = newForwardVoltage;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    forwardVoltageField.setText(String.valueOf(diode.forwardVoltage));
                }
            });

            Label maxCurrentLabel = new Label("Max Current (A):");
            maxCurrentLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(maxCurrentLabel);

            TextField maxCurrentField = new TextField(String.valueOf(diode.maxCurrent));
            maxCurrentField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(maxCurrentField);

            maxCurrentField.setOnAction(e -> {
                try {
                    double newMaxCurrent = Double.parseDouble(maxCurrentField.getText());
                    if (newMaxCurrent > 0) {
                        diode.maxCurrent = newMaxCurrent;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    maxCurrentField.setText(String.valueOf(diode.maxCurrent));
                }
            });
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
            forwardVoltageLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField forwardVoltageField = new TextField("2.0");
            forwardVoltageField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            forwardCurrentLabel.setStyle("-fx-text-fill: #FFFFFF;");
            TextField forwardCurrentField = new TextField("0.02");
            forwardCurrentField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
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
            colorLabel.setStyle("-fx-text-fill: #FFFFFF;");
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
        public double primaryTurns = 100; // Default 100 turns
        public double secondaryTurns = 100; // Default 100 turns
        public double coupling = 0.99; // Default coupling coefficient

        public Transformer() {
            super("Transformer", "/images/components/Transformer.png");
        }

        public static void addControls(Transformer transformer, VBox container) {
            Label primaryTurnsLabel = new Label("Primary Turns:");
            primaryTurnsLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(primaryTurnsLabel);

            TextField primaryTurnsField = new TextField(String.valueOf(transformer.primaryTurns));
            primaryTurnsField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(primaryTurnsField);

            primaryTurnsField.setOnAction(e -> {
                try {
                    double newPrimaryTurns = Double.parseDouble(primaryTurnsField.getText());
                    if (newPrimaryTurns > 0) {
                        transformer.primaryTurns = newPrimaryTurns;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    primaryTurnsField.setText(String.valueOf(transformer.primaryTurns));
                }
            });

            Label secondaryTurnsLabel = new Label("Secondary Turns:");
            secondaryTurnsLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(secondaryTurnsLabel);

            TextField secondaryTurnsField = new TextField(String.valueOf(transformer.secondaryTurns));
            secondaryTurnsField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(secondaryTurnsField);

            secondaryTurnsField.setOnAction(e -> {
                try {
                    double newSecondaryTurns = Double.parseDouble(secondaryTurnsField.getText());
                    if (newSecondaryTurns > 0) {
                        transformer.secondaryTurns = newSecondaryTurns;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    secondaryTurnsField.setText(String.valueOf(transformer.secondaryTurns));
                }
            });

            Label couplingLabel = new Label("Coupling Coefficient:");
            couplingLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(couplingLabel);

            TextField couplingField = new TextField(String.valueOf(transformer.coupling));
            couplingField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(couplingField);

            couplingField.setOnAction(e -> {
                try {
                    double newCoupling = Double.parseDouble(couplingField.getText());
                    if (newCoupling > 0 && newCoupling <= 1) {
                        transformer.coupling = newCoupling;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    couplingField.setText(String.valueOf(transformer.coupling));
                }
            });
        }
    }

    public static class Fuse extends ImageComponent {
        public double currentRating = 1.0; // Default 1A
        public boolean isBlown = false;

        public Fuse() {
            super("Fuse", "/images/components/Fuse.png");
        }

        public static void addControls(Fuse fuse, VBox container) {
            Label currentRatingLabel = new Label("Current Rating (A):");
            currentRatingLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(currentRatingLabel);

            TextField currentRatingField = new TextField(String.valueOf(fuse.currentRating));
            currentRatingField.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            container.getChildren().add(currentRatingField);

            currentRatingField.setOnAction(e -> {
                try {
                    double newCurrentRating = Double.parseDouble(currentRatingField.getText());
                    if (newCurrentRating > 0) {
                        fuse.currentRating = newCurrentRating;
                        // Get the SimulationController instance and update circuit analysis
                        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                        if (simulationController != null) {
                            simulationController.updateCircuitAnalysis();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Invalid input, revert to previous value
                    currentRatingField.setText(String.valueOf(fuse.currentRating));
                }
            });

            Label stateLabel = new Label("State:");
            stateLabel.setStyle("-fx-text-fill: #FFFFFF;");
            container.getChildren().add(stateLabel);

            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Good", "Blown");
            stateComboBox.setValue(fuse.isBlown ? "Blown" : "Good");
            stateComboBox.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: #FFFFFF;");
            stateComboBox.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #3a3a3a;");
                    }
                }
            });
            container.getChildren().add(stateComboBox);

            stateComboBox.setOnAction(e -> {
                fuse.isBlown = stateComboBox.getValue().equals("Blown");
                // Get the SimulationController instance and update circuit analysis
                SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
                if (simulationController != null) {
                    simulationController.updateCircuitAnalysis();
                }
            });
        }
    }

    // Main method to generate parameter controls
    public static void generateParameterControls(ImageComponent component, VBox container) {
        // First, remove any existing parameter controls for this component
        if (component.parameterControls != null) {
            container.getChildren().remove(component.parameterControls);
        }

        VBox componentParams = new VBox(5);
        componentParams.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 5;");
        componentParams.getProperties().put("component", component);
        
        // Get the SimulationController instance from the main parametersPane
        SimulationController simulationController = (SimulationController) container.getProperties().get("simulationController");
        if (simulationController != null) {
            componentParams.getProperties().put("simulationController", simulationController);
        }
        
        component.parameterControls = componentParams; // Store reference to the parameter controls

        // Add component name header
        Label componentHeader = new Label(component.componentType + " Parameters");
        componentHeader.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10px 0;");
        componentParams.getChildren().add(componentHeader);

        if (component.componentType.equals("SPSTToggleSwitch")) {
            SPSTToggleSwitch.addControls((SPSTToggleSwitch) component, componentParams);
        } else if (component.componentType.equals("EarthGround")) {
            EarthGround.addControls(componentParams);
        } else if (component.componentType.equals("ChassisGround")) {
            ChassisGround.addControls(componentParams);
        } else if (component.componentType.equals("ResistorIEEE")) {
            ResistorIEEE.addControls((ResistorIEEE) component, componentParams);
        } else if (component.componentType.equals("ResistorIEC")) {
            ResistorIEC.addControls((ResistorIEC) component, componentParams);
        } else if (component.componentType.equals("PotentiometerIEEE")) {
            PotentiometerIEEE.addControls(componentParams);
        } else if (component.componentType.equals("PotentiometerIEC")) {
            PotentiometerIEC.addControls(componentParams);
        } else if (component.componentType.equals("Capacitor")) {
            Capacitor.addControls((Capacitor) component, componentParams);
        } else if (component.componentType.equals("Inductor")) {
            Inductor.addControls((Inductor) component, componentParams);
        } else if (component.componentType.equals("VoltageSource")) {
            VoltageSource.addControls((VoltageSource) component, componentParams);
        } else if (component.componentType.equals("BatteryCell")) {
            BatteryCell.addControls((BatteryCell) component, componentParams);
        } else if (component.componentType.equals("Battery")) {
            Battery.addControls((Battery) component, componentParams);
        } else if (component.componentType.equals("Voltmeter")) {
            Voltmeter.addControls(componentParams);
        } else if (component.componentType.equals("Ammeter")) {
            Ammeter.addControls(componentParams);
        } else if (component.componentType.equals("Ohmmeter")) {
            Ohmmeter.addControls(componentParams);
        } else if (component instanceof NOTGate) {
            NOTGate.addControls((NOTGate) component, componentParams);
        } else if (component instanceof ANDGate) {
            ANDGate.addControls((ANDGate) component, componentParams);
        } else if (component instanceof NANDGate) {
            NANDGate.addControls((NANDGate) component, componentParams);
        } else if (component instanceof ORGate) {
            ORGate.addControls((ORGate) component, componentParams);
        } else if (component instanceof NORGate) {
            NORGate.addControls((NORGate) component, componentParams);
        } else if (component instanceof XORGate) {
            XORGate.addControls((XORGate) component, componentParams);
        } else if (component.componentType.equals("Diode")) {
            Diode.addControls((Diode) component, componentParams);
        } else if (component.componentType.equals("LED")) {
            LED.addControls(componentParams);
        } else if (component.componentType.equals("Transformer")) {
            Transformer.addControls((Transformer) component, componentParams);
        } else if (component.componentType.equals("Fuse")) {
            Fuse.addControls((Fuse) component, componentParams);
        }

        container.getChildren().add(componentParams);
    }

    public static void removeParameterControls(ImageComponent component, VBox container) {
        if (component.parameterControls != null) {
            container.getChildren().remove(component.parameterControls);
            component.parameterControls = null;
        }
    }
}