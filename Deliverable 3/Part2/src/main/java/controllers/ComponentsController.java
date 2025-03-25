package controllers;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * This class contains nested classes for each electrical component in the FXML file.
 * Each component is represented as a separate class with relevant electrical properties.
 */
public class ComponentsController {
    // Interface for drawable objects
    public interface Drawable {
        void draw(GraphicsContext gc);
    }

    // Base class for all image components
    public static class ImageComponent implements Drawable {
        public Image image;
        public double x, y, width, height;
        public double rotation = 0;
        public double startX, startY, endX, endY;
        public Circle startCircle, endCircle;
        public String componentType;
        public VBox parameterControls;

        public ImageComponent(Image image, double x, double y, double width, double height, String componentType) {
            this.image = image;
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

    public static class Wire implements Drawable {
        public double startX, startY, endX, endY;
        public Circle endCircle;
        private boolean selected = false;

        public Wire(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.endCircle = new Circle(endX, endY, 6, Color.BLACK);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        @Override
        public void draw(GraphicsContext gc) {
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
    }

    // Component data classes with their control generation methods

    // Switch and Relay Components
    public static class SPSTToggleSwitch {
        public boolean isClosed;
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("SPSTT Toggle Switch Parameters");
            container.getChildren().add(label);

            Label stateLabel = new Label("State:");
            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Open", "Closed");
            stateComboBox.setValue("Open");

            TextField maxVoltageField = new TextField();
            maxVoltageField.setPromptText("Max Voltage");
            maxVoltageField.setText("0.0");

            TextField maxCurrentField = new TextField();
            maxCurrentField.setPromptText("Max Current");
            maxCurrentField.setText("0.0");

            container.getChildren().addAll(stateLabel, stateComboBox, maxVoltageField, maxCurrentField);
        }
    }

    public static class SPDTToggleSwitch {
        public boolean isClosed;
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("SPDTT Toggle Switch Parameters");
            container.getChildren().add(label);

            Label stateLabel = new Label("State:");
            ComboBox<String> stateComboBox = new ComboBox<>();
            stateComboBox.getItems().addAll("Open", "Closed");
            stateComboBox.setValue("Open");

            TextField maxVoltageField = new TextField();
            maxVoltageField.setPromptText("Max Voltage");
            maxVoltageField.setText("0.0");

            TextField maxCurrentField = new TextField();
            maxCurrentField.setPromptText("Max Current");
            maxCurrentField.setText("0.0");

            container.getChildren().addAll(stateLabel, stateComboBox, maxVoltageField, maxCurrentField);
        }
    }

    public static class PushbuttonSwitchNO {
        public boolean isPressed;
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("Pushbutton Switch NO Parameters");
            container.getChildren().add(label);

            CheckBox isPressedCheckBox = new CheckBox("Is Pressed");
            isPressedCheckBox.setSelected(false);

            TextField maxVoltageField = new TextField();
            maxVoltageField.setPromptText("Max Voltage");
            maxVoltageField.setText("0.0");

            TextField maxCurrentField = new TextField();
            maxCurrentField.setPromptText("Max Current");
            maxCurrentField.setText("0.0");

            container.getChildren().addAll(isPressedCheckBox, maxVoltageField, maxCurrentField);
        }
    }

    public static class PushbuttonSwitchNC {
        public boolean isPressed; // State of the pushbutton (pressed/released)
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("Pushbutton Switch NC Parameters");
            container.getChildren().add(label);

            CheckBox isPressedCheckBox = new CheckBox("Is Pressed");
            isPressedCheckBox.setSelected(false);

            TextField maxVoltageField = new TextField();
            maxVoltageField.setPromptText("Max Voltage");
            maxVoltageField.setText("0.0");

            TextField maxCurrentField = new TextField();
            maxCurrentField.setPromptText("Max Current");
            maxCurrentField.setText("0.0");

            container.getChildren().addAll(isPressedCheckBox, maxVoltageField, maxCurrentField);
        }
    }

    public static class DIPSwitch {
        public boolean[] switchStates; // Array of states for each switch in the DIP
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("DIP Switch Parameters");
            container.getChildren().add(label);

            for (int i = 0; i < 8; i++) { // Assuming 8 switches in the DIP
                final int switchIndex = i; // Capture the current value of i
                CheckBox switchCheckBox = new CheckBox("Switch " + (switchIndex + 1));
                switchCheckBox.setSelected(false); // Default state is open
                switchCheckBox.setOnAction(e -> {
                    boolean isClosed = switchCheckBox.isSelected();
                    if (isClosed) {
                        System.out.println("Switch " + (switchIndex + 1) + " closed");
                    } else {
                        System.out.println("Switch " + (switchIndex + 1) + " open");
                    }
                });
                container.getChildren().add(switchCheckBox);
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
            container.getChildren().add(maxVoltageField);

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
            container.getChildren().add(maxCurrentField);
        }
    }

    public static class SPSTRelay {
        public boolean isEnergized; // State of the relay coil (energized/de-energized)
        public double coilVoltage; // Voltage required to energize the coil
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("DIP Switch Parameters");
            container.getChildren().add(label);

            for (int i = 0; i < 8; i++) { // Assuming 8 switches in the DIP
                final int switchIndex = i; // Capture the current value of i
                CheckBox switchCheckBox = new CheckBox("Switch " + (switchIndex + 1));
                switchCheckBox.setSelected(false); // Default state is open
                switchCheckBox.setOnAction(e -> {
                    boolean isClosed = switchCheckBox.isSelected();
                    if (isClosed) {
                        System.out.println("Switch " + (switchIndex + 1) + " closed");
                    } else {
                        System.out.println("Switch " + (switchIndex + 1) + " open");
                    }
                });
                container.getChildren().add(switchCheckBox);
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
            container.getChildren().add(maxVoltageField);

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
            container.getChildren().add(maxCurrentField);
        }
    }

    public static class SPDTRelay {
        public boolean isEnergized; // State of the relay coil (energized/de-energized)
        public double coilVoltage; // Voltage required to energize the coil
        public double maxVoltage;
        public double maxCurrent;

        public static void addControls(VBox container) {
            Label label = new Label("DIP Switch Parameters");
            container.getChildren().add(label);

            for (int i = 0; i < 8; i++) { // Assuming 8 switches in the DIP
                final int switchIndex = i; // Capture the current value of i
                CheckBox switchCheckBox = new CheckBox("Switch " + (switchIndex + 1));
                switchCheckBox.setSelected(false); // Default state is open
                switchCheckBox.setOnAction(e -> {
                    boolean isClosed = switchCheckBox.isSelected();
                    if (isClosed) {
                        System.out.println("Switch " + (switchIndex + 1) + " closed");
                    } else {
                        System.out.println("Switch " + (switchIndex + 1) + " open");
                    }
                });
                container.getChildren().add(switchCheckBox);
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
            container.getChildren().add(maxVoltageField);

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
            container.getChildren().add(maxCurrentField);
        }
    }

    public static class Jumper {
        public double resistance; // Resistance of the jumper (typically very low)

        public static void addControls(VBox container) {
            Label label = new Label("Jumper Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);
        }
    }

    public static class SolderBridge {
        public double resistance; // Resistance of the solder bridge (typically very low)

        public static void addControls(VBox container) {
            Label label = new Label("Solder Bridge Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);
        }
    }

    // Ground Components
    public static class EarthGround {
        public double resistance; // Resistance to earth (typically very low)

        public static void addControls(VBox container) {
            Label label = new Label("Ground Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);
        }
    }

    public static class ChassisGround {
        public double resistance; // Resistance to chassis (typically very low)

        public static void addControls(VBox container) {
            Label label = new Label("Ground Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);
        }
    }

    public static class DigitalGround {
        public double resistance; // Resistance to digital ground (typically very low)

        public static void addControls(VBox container) {
            Label label = new Label("Ground Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);
        }
    }

    // Resistor Components
    public static class ResistorIEEE {
        public double resistance; // Resistance value in ohms
        public double powerRating; // Maximum power the resistor can handle

        public static void addControls(VBox container) {
            Label label = new Label("Resistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);
        }
    }

    public static class ResistorIEC {
        public double resistance;
        public double powerRating;

        public static void addControls(VBox container) {
            Label label = new Label("Resistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);
        }
    }

    public static class PotentiometerIEEE {
        public double resistance;
        public double powerRating;
        public double wiperPosition; // Position of the wiper (0 to 1)

        public static void addControls(VBox container) {
            Label label = new Label("Potentiometer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);

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
            container.getChildren().add(new Label("Wiper Position"));
            container.getChildren().add(wiperSlider);
        }
    }

    public static class PotentiometerIEC {
        public double resistance;
        public double powerRating;
        public double wiperPosition;

        public static void addControls(VBox container) {
            Label label = new Label("Potentiometer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);

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
            container.getChildren().add(new Label("Wiper Position"));
            container.getChildren().add(wiperSlider);
        }
    }

    public static class RheostatIEEE {
        public double resistance;
        public double powerRating;
        public double wiperPosition;

        public static void addControls(VBox container) {
            Label label = new Label("Potentiometer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);

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
            container.getChildren().add(new Label("Wiper Position"));
            container.getChildren().add(wiperSlider);
        }
    }

    public static class RheostatIEC {
        public double resistance;
        public double powerRating;
        public double wiperPosition;

        public static void addControls(VBox container) {
            Label label = new Label("Potentiometer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(powerRatingField);

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
            container.getChildren().add(new Label("Wiper Position"));
            container.getChildren().add(wiperSlider);
        }
    }

    public static class Thermistor {
        public double resistance; // Resistance at a given temperature
        public double temperatureCoefficient; // Temperature coefficient of resistance

        public static void addControls(VBox container) {
            Label label = new Label("Thermistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(temperatureCoefficientField);
        }
    }

    public static class Photoresistor {
        public double resistance; // Resistance under current light conditions
        public double lightIntensity; // Light intensity affecting resistance

        public static void addControls(VBox container) {
            Label label = new Label("Photoresistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resistanceField);

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
            container.getChildren().add(lightIntensityField);
        }
    }

    // Capacitor Components
    public static class Capacitor {
        public double capacitance; // Capacitance in farads
        public double voltageRating; // Maximum voltage the capacitor can handle

        public static void addControls(VBox container) {
            Label label = new Label("Capacitor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(capacitanceField);

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
            container.getChildren().add(voltageRatingField);
        }
    }

    public static class PolarizedCapacitor {
        public double capacitance;
        public double voltageRating;
        public boolean isPolarityRespected; // Whether polarity is respected

        public static void addControls(VBox container) {
            Capacitor.addControls(container); // Same as capacitor

            CheckBox polarityCheckBox = new CheckBox("Polarity Respected");
            polarityCheckBox.setSelected(false);
            polarityCheckBox.setOnAction(e -> {
                // Update component polarity
            });
            container.getChildren().add(polarityCheckBox);
        }
    }

    public static class VariableCapacitor {
        public double capacitance;
        public double voltageRating;
        public double rotationAngle; // Angle of rotation (0 to 360 degrees)

        private static void addControls(VBox container) {
            Capacitor.addControls(container); // Same as capacitor

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
            container.getChildren().add(new Label("Rotation Angle"));
            container.getChildren().add(rotationSlider);
        }
    }

    // Inductor and Coil Components
    public static class Inductor {
        public double inductance; // Inductance in henries
        public double currentRating; // Maximum current the inductor can handle

        public static void addControls(VBox container) {
            Label label = new Label("Inductor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(inductanceField);

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
            container.getChildren().add(currentRatingField);
        }
    }

    public static class IronCoreInductor {
        public double inductance;
        public double currentRating;
        public double corePermeability; // Permeability of the iron core

        public static void addControls(VBox container) {
            Inductor.addControls(container); // Same as inductor

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
            container.getChildren().add(corePermeabilityField);
        }
    }

    public static class VariableInductor {
        public double inductance;
        public double currentRating;
        public double rotationAngle; // Angle of rotation (0 to 360 degrees)

        public static void addControls(VBox container) {
            Inductor.addControls(container); // Same as inductor

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
            container.getChildren().add(new Label("Rotation Angle"));
            container.getChildren().add(rotationSlider);
        }
    }

    // Power Supply Components
    public static class VoltageSource {
        public double voltage; // Output voltage
        public double internalResistance; // Internal resistance of the source

        public static void addControls(VBox container) {
            Label label = new Label("Voltage Source Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageField);

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
            container.getChildren().add(internalResistanceField);
        }
    }

    public static class CurrentSource {
        public double current; // Output current
        public double internalResistance;
        public static void addControls(VBox container) {
            Label label = new Label("Current Source Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentField);

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
            container.getChildren().add(internalResistanceField);
        }

    }

    public static class Generator {
        public double voltage;
        public double frequency; // Frequency of the generated AC signal
        public double internalResistance;

        public static void addControls(VBox container) {
            Label label = new Label("Generator Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageField);

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
            container.getChildren().add(frequencyField);

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
            container.getChildren().add(internalResistanceField);
        }
    }

    public static class BatteryCell {
        public double voltage; // Nominal voltage of the cell
        public double internalResistance;
        public double capacity; // Capacity in ampere-hours (Ah)

        public static void addControls(VBox container) {
            Label label = new Label("Battery Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageField);

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
            container.getChildren().add(internalResistanceField);

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
            container.getChildren().add(capacityField);
        }
    }

    public static class Battery {
        public double voltage; // Total voltage of the battery
        public double internalResistance;
        public double capacity;

        public static void addControls(VBox container) {
            Label label = new Label("Battery Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageField);

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
            container.getChildren().add(internalResistanceField);

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
            container.getChildren().add(capacityField);
        }
    }

    public static class ControlledVoltageSource {
        public double voltage;
        public double controlSignal; // Signal controlling the output voltage

        public static void addControls(VBox container) {
            Label label = new Label("Controlled Voltage Source Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageField);

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
            container.getChildren().add(controlSignalField);
        }
    }

    public static class ControlledCurrentSource {
        public double current;
        public double controlSignal; // Signal controlling the output current

        public static void addControls(VBox container) {
            Label label = new Label("Controlled Current Source Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentField);

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
            container.getChildren().add(controlSignalField);
        }
    }

    // Meter Components
    public static class Voltmeter {
        public double range; // Maximum voltage the voltmeter can measure
        public double internalResistance;

        public static void addControls(VBox container) {
            Label label = new Label("Voltmeter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(rangeField);

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
            container.getChildren().add(internalResistanceField);
        }
    }

    public static class Ammeter {
        public double range; // Maximum current the ammeter can measure
        public double internalResistance;

        public static void addControls(VBox container) {
            Label label = new Label("Ammeter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(rangeField);

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
            container.getChildren().add(internalResistanceField);
        }
    }

    public static class Ohmmeter {
        public double range; // Maximum resistance the ohmmeter can measure

        public static void addControls(VBox container) {
            Label label = new Label("Ohmmeter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(rangeField);
        }
    }

    public static class Wattmeter {
        public double voltageRange;
        public double currentRange;

        public static void addControls(VBox container) {
            Label label = new Label("Wattmeter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageRangeField);

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
            container.getChildren().add(currentRangeField);
        }
    }

    // Diode and LED Components
    public static class Diode {
        public double forwardVoltage; // Forward voltage drop
        public double reverseBreakdownVoltage; // Reverse breakdown voltage

        public static void addControls(VBox container) {
            Label label = new Label("Diode Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(forwardVoltageField);

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
            container.getChildren().add(reverseBreakdownVoltageField);
        }
    }

    public static class ZenerDiode {
        public double zenerVoltage; // Zener breakdown voltage
        public double forwardVoltage;

        public static void addControls(VBox container) {
            Diode.addControls(container); // Same as diode

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
            container.getChildren().add(zenerVoltageField);
        }
    }

    public static class SchottkyDiode {
        public double forwardVoltage;
        public double reverseBreakdownVoltage;

        public static void addControls(VBox container) {
            Diode.addControls(container);
        }
    }

    public static class Varactor {
        public double capacitance; // Capacitance at a given reverse voltage
        public double reverseVoltage; // Applied reverse voltage

        public static void addControls(VBox container) {
            Label label = new Label("Varactor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(capacitanceField);

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
            container.getChildren().add(reverseVoltageField);
        }
    }

    public static class TunnelDiode {
        public double peakVoltage; // Peak voltage in the I-V curve
        public double valleyVoltage; // Valley voltage in the I-V curve

        public static void addControls(VBox container) {
            Label label = new Label("Tunnel Diode Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(peakVoltageField);

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
            container.getChildren().add(valleyVoltageField);
        }
    }

    public static class LightEmittingDiode {
        public double forwardVoltage;
        public double wavelength; // Wavelength of emitted light

        public static void addControls(VBox container) {
            Label label = new Label("Light Emitting Diode Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(forwardVoltageField);

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
            container.getChildren().add(wavelengthField);
        }
    }

    public static class Photodiode {
        public double darkCurrent; // Current in the absence of light
        public double lightCurrent; // Current under illumination

        public static void addControls(VBox container) {
            Label label = new Label("Photodiode Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(darkCurrentField);

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
            container.getChildren().add(lightCurrentField);
        }
    }

    // Transistor Components
    public static class NPNBipolarTransistor {
        public double currentGain; // Current gain (beta)
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("Bipolar Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentGainField);

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
            container.getChildren().add(maxCollectorCurrentField);

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
            container.getChildren().add(maxCollectorEmitterVoltageField);
        }
    }

    public static class PNPBipolarTransistor {
        public double currentGain;
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("Bipolar Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentGainField);

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
            container.getChildren().add(maxCollectorCurrentField);

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
            container.getChildren().add(maxCollectorEmitterVoltageField);
        }
    }

    public static class DarlingtonTransistor {
        public double currentGain;
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("Bipolar Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentGainField);

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
            container.getChildren().add(maxCollectorCurrentField);

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
            container.getChildren().add(maxCollectorEmitterVoltageField);
        }
    }

    public static class JFETNTransistor {
        public double pinchOffVoltage; // Pinch-off voltage
        public double maxDrainSourceVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("JFET Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(pinchOffVoltageField);

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
            container.getChildren().add(maxDrainSourceVoltageField);
        }
    }

    public static class JFETPTransistor {
        public double pinchOffVoltage;
        public double maxDrainSourceVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("JFET Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(pinchOffVoltageField);

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
            container.getChildren().add(maxDrainSourceVoltageField);
        }
    }

    public static class NMOSTransistor {
        public double thresholdVoltage; // Threshold voltage
        public double maxDrainSourceVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("MOS Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(thresholdVoltageField);

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
            container.getChildren().add(maxDrainSourceVoltageField);
        }
    }

    public static class PMOSTransistor {
        public double thresholdVoltage;
        public double maxDrainSourceVoltage;

        public static void addControls(VBox container) {
            Label label = new Label("MOS Transistor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(thresholdVoltageField);

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
            container.getChildren().add(maxDrainSourceVoltageField);
        }
    }

    // Logic Gate Components
    public static class NOTGate {
        public double propagationDelay; // Propagation delay in nanoseconds

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class ANDGate {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class NANDGate {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class ORGate {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class NORGate {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class XORGate {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Logic Gate Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class DFlipFlop {
        public double setupTime; // Setup time in nanoseconds
        public double holdTime; // Hold time in nanoseconds

        public static void addControls(VBox container) {
            Label label = new Label("D Flip-Flop Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(setupTimeField);

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
            container.getChildren().add(holdTimeField);
        }
    }

    public static class Multiplexer2to1 {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Multiplexer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class Multiplexer4to1 {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Multiplexer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    public static class Demultiplexer1to4 {
        public double propagationDelay;

        public static void addControls(VBox container) {
            Label label = new Label("Demultiplexer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(propagationDelayField);
        }
    }

    // Antenna Components
    public static class Antenna {
        public double frequencyRange; // Frequency range the antenna operates in
        public double gain; // Gain in dBi

        public static void addControls(VBox container) {
            Label label = new Label("Antenna Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(frequencyRangeField);

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
            container.getChildren().add(gainField);
        }
    }

    public static class DipoleAntenna {
        public double frequencyRange;
        public double gain;

        public static void addControls(VBox container) {
            Label label = new Label("Antenna Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(frequencyRangeField);

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
            container.getChildren().add(gainField);
        }
    }

    // Miscellaneous Components
    public static class Motor {
        public double ratedVoltage; // Rated operating voltage
        public double ratedCurrent; // Rated operating current

        public static void addControls(VBox container) {
            Label label = new Label("Motor Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(voltageRatingField);

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
            container.getChildren().add(speedField);

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
            container.getChildren().add(torqueField);
        }
    }

    public static class Transformer {
        public double primaryVoltage; // Primary winding voltage
        public double secondaryVoltage; // Secondary winding voltage
        public double turnsRatio; // Turns ratio (N1/N2)

        public static void addControls(VBox container) {
            Label label = new Label("Transformer Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(primaryVoltageField);

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
            container.getChildren().add(secondaryVoltageField);

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
            container.getChildren().add(powerRatingField);
        }
    }

    public static class Fuse {
        public double ratedCurrent; // Current at which the fuse blows
        public double breakingCapacity; // Maximum current the fuse can interrupt

        public static void addControls(VBox container) {
            Label label = new Label("Fuse Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(currentRatingField);

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
            container.getChildren().add(breakingCapacityField);
        }
    }

    public static class Optocoupler {
        public double currentTransferRatio; // Ratio of output current to input current

        public static void addControls(VBox container) {
            Label label = new Label("Optocoupler Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(forwardCurrentField);

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
            container.getChildren().add(isolationVoltageField);
        }
    }

    public static class Loudspeaker {
        public double impedance; // Impedance in ohms
        public double powerRating; // Maximum power the speaker can handle

        public static void addControls(VBox container) {
            Label label = new Label("Loudspeaker Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(impedanceField);

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
            container.getChildren().add(powerRatingField);
        }
    }

    public static class Microphone {
        public double sensitivity; // Sensitivity in dB
        public double frequencyResponse; // Frequency response range

        public static void addControls(VBox container) {
            Label label = new Label("Microphone Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(sensitivityField);

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
            container.getChildren().add(frequencyResponseField);
        }
    }

    public static class OperationalAmplifier {
        public double gainBandwidthProduct; // Gain-bandwidth product in Hz
        public double slewRate; // Slew rate in V/µs

        public static void addControls(VBox container) {
            Label label = new Label("Operational Amplifier Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(gainBandwidthProductField);

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
            container.getChildren().add(slewRateField);
        }
    }

    public static class SchmittTrigger {
        public double upperThreshold; // Upper threshold voltage
        public double lowerThreshold; // Lower threshold voltage

        public static void addControls(VBox container) {
            Label label = new Label("Schmitt Trigger Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(upperThresholdField);

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
            container.getChildren().add(lowerThresholdField);
        }
    }

    public static class AnalogToDigitalConverter {
        public int resolution; // Resolution in bits
        public double samplingRate; // Sampling rate in samples per second

        public static void addControls(VBox container) {
            Label label = new Label("Analog-to-Digital Converter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resolutionField);

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
            container.getChildren().add(samplingRateField);
        }
    }

    public static class DigitalToAnalogConverter {
        public int resolution;
        public double outputVoltageRange; // Output voltage range

        public static void addControls(VBox container) {
            Label label = new Label("Digital-to-Analog Converter Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(resolutionField);

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
            container.getChildren().add(outputVoltageField);
        }
    }

    public static class CrystalOscillator {
        public double frequency; // Oscillation frequency in Hz
        public double stability; // Frequency stability in ppm

        public static void addControls(VBox container) {
            Label label = new Label("Crystal Oscillator Parameters");
            container.getChildren().add(label);

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
            container.getChildren().add(frequencyField);

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
            container.getChildren().add(stabilityField);
        }
    }

    // public static void addControls(VBox container) {}

    // Add similar classes for all other components...


    // Main method to generate parameter controls
    public static void generateParameterControls(ImageComponent component, VBox parametersPane) {
        VBox componentBox = new VBox(10);
        componentBox.setPadding(new Insets(10));
        componentBox.setUserData(component);
        component.parameterControls = componentBox;

        Label componentLabel = new Label("Component: " + component.componentType);
        componentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Button removeButton = new Button("Remove");
        removeButton.setMinWidth(80);
        HBox headerBox = new HBox(10, componentLabel, removeButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        componentBox.getChildren().add(headerBox);

        // Call the appropriate addControls method based on component type
        switch (component.componentType) {
            case "SPSTToggleSwitch":
                SPSTToggleSwitch.addControls(componentBox);
                break;
            case "SPDTToggleSwitch":
                SPDTToggleSwitch.addControls(componentBox);
                break;
            case "PushbuttonSwitchNO":
                PushbuttonSwitchNO.addControls(componentBox);
                break;
            case "PushbuttonSwitchNC":
                PushbuttonSwitchNC.addControls(componentBox);
                break;
            case "DIPSwitch":
                DIPSwitch.addControls(componentBox);
                break;
            case "SPSTRelay":
                SPSTRelay.addControls(componentBox);
                break;
            case "SPDTRelay":
                SPDTRelay.addControls(componentBox);
                break;
            case "Jumper":
                Jumper.addControls(componentBox);
                break;
            case "SolderBridge":
                SolderBridge.addControls(componentBox);
                break;
            case "EarthGround":
                EarthGround.addControls(componentBox);
                break;
            case "ChassisGround":
                ChassisGround.addControls(componentBox);
                break;
            case "DigitalGround":
                DigitalGround.addControls(componentBox);
                break;
            case "ResistorIEEE":
                ResistorIEEE.addControls(componentBox);
                break;
            case "ResistorIEC":
                ResistorIEC.addControls(componentBox);
                break;
            case "PotentiometerIEEE":
                PotentiometerIEEE.addControls(componentBox);
                break;
            case "PotentiometerIEC":
                PotentiometerIEC.addControls(componentBox);
                break;
            case "RheostatIEEE":
                RheostatIEEE.addControls(componentBox);
                break;
            case "RheostatIEC":
                RheostatIEC.addControls(componentBox);
                break;
            case "Thermistor":
                Thermistor.addControls(componentBox);
                break;
            case "Photoresistor":
                Photoresistor.addControls(componentBox);
                break;
            case "Capacitor":
                Capacitor.addControls(componentBox);
                break;
            case "PolarizedCapacitor":
                PolarizedCapacitor.addControls(componentBox);
                break;
            case "VariableCapacitor":
                VariableCapacitor.addControls(componentBox);
                break;
            case "Inductor":
                Inductor.addControls(componentBox);
                break;
            case "IronCoreInductor":
                IronCoreInductor.addControls(componentBox);
                break;
            case "VariableInductor":
                VariableInductor.addControls(componentBox);
                break;
            case "VoltageSource":
                VoltageSource.addControls(componentBox);
                break;
            case "CurrentSource":
                CurrentSource.addControls(componentBox);
                break;
            case "Generator":
                Generator.addControls(componentBox);
                break;
            case "BatteryCell":
                BatteryCell.addControls(componentBox);
                break;
            case "Battery":
                Battery.addControls(componentBox);
                break;
            case "ControlledVoltageSource":
                ControlledVoltageSource.addControls(componentBox);
                break;
            case "ControlledCurrentSource":
                ControlledCurrentSource.addControls(componentBox);
                break;
            case "Voltmeter":
                Voltmeter.addControls(componentBox);
                break;
            case "Ammeter":
                Ammeter.addControls(componentBox);
                break;
            case "Ohmmeter":
                Ohmmeter.addControls(componentBox);
                break;
            case "Wattmeter":
                Wattmeter.addControls(componentBox);
                break;
            case "Diode":
                Diode.addControls(componentBox);
                break;
            case "ZenerDiode":
                ZenerDiode.addControls(componentBox);
                break;
            case "SchottkyDiode":
                SchottkyDiode.addControls(componentBox);
                break;
            case "Varactor":
                Varactor.addControls(componentBox);
                break;
            case "TunnelDiode":
                TunnelDiode.addControls(componentBox);
                break;
            case "LightEmittingDiode":
                LightEmittingDiode.addControls(componentBox);
                break;
            case "Photodiode":
                Photodiode.addControls(componentBox);
                break;
            case "NPNBipolarTransistor":
                NPNBipolarTransistor.addControls(componentBox);
                break;
            case "PNPBipolarTransistor":
                PNPBipolarTransistor.addControls(componentBox);
                break;
            case "DarlingtonTransistor":
                DarlingtonTransistor.addControls(componentBox);
                break;
            case "JFETNTransistor":
                JFETNTransistor.addControls(componentBox);
                break;
            case "JFETPTransistor":
                JFETPTransistor.addControls(componentBox);
                break;
            case "NMOSTransistor":
                NMOSTransistor.addControls(componentBox);
                break;
            case "PMOSTransistor":
                PMOSTransistor.addControls(componentBox);
                break;
            case "NOTGate":
                NOTGate.addControls(componentBox);
                break;
            case "ANDGate":
                ANDGate.addControls(componentBox);
                break;
            case "NANDGate":
                NANDGate.addControls(componentBox);
                break;
            case "ORGate":
                ORGate.addControls(componentBox);
                break;
            case "NORGate":
                NORGate.addControls(componentBox);
                break;
            case "XORGate":
                XORGate.addControls(componentBox);
                break;
            case "DFlipFlop":
                DFlipFlop.addControls(componentBox);
                break;
            case "Multiplexer2to1":
                Multiplexer2to1.addControls(componentBox);
                break;
            case "Multiplexer4to1":
                Multiplexer4to1.addControls(componentBox);
                break;
            case "Demultiplexer1to4":
                Demultiplexer1to4.addControls(componentBox);
                break;
            case "Antenna":
                Antenna.addControls(componentBox);
                break;
            case "DipoleAntenna":
                DipoleAntenna.addControls(componentBox);
                break;
            case "Motor":
                Motor.addControls(componentBox);
                break;
            case "Transformer":
                Transformer.addControls(componentBox);
                break;
            case "Fuse":
                Fuse.addControls(componentBox);
                break;
            case "Optocoupler":
                Optocoupler.addControls(componentBox);
                break;
            case "Loudspeaker":
                Loudspeaker.addControls(componentBox);
                break;
            case "Microphone":
                Microphone.addControls(componentBox);
                break;
            case "OperationalAmplifier":
                OperationalAmplifier.addControls(componentBox);
                break;
            case "SchmittTrigger":
                SchmittTrigger.addControls(componentBox);
                break;
            case "AnalogToDigitalConverter":
                AnalogToDigitalConverter.addControls(componentBox);
                break;
            case "DigitalToAnalogConverter":
                DigitalToAnalogConverter.addControls(componentBox);
                break;
            case "CrystalOscillator":
                CrystalOscillator.addControls(componentBox);
                break;
            default:
                System.out.println("Unknown component type: " + component.componentType);
                break;
        }

        parametersPane.getChildren().add(componentBox);
        parametersPane.requestLayout();
    }
}