package controllers;

import java.util.*;

public class CircuitAnalyzer {
    private final List<ComponentsController.Drawable> components;
    private final Map<String, Double> nodeVoltages;
    private final Map<String, Double> branchCurrents;
    private final Map<String, Double> componentValues;

    public CircuitAnalyzer(List<ComponentsController.Drawable> components) {
        this.components = components;
        this.nodeVoltages = new HashMap<>();
        this.branchCurrents = new HashMap<>();
        this.componentValues = new HashMap<>();
    }

    // Analyze the circuit and determine which theorems to use
    public void analyzeCircuit() {
        // First, identify the circuit topology
        CircuitTopology topology = identifyCircuitTopology();
        
        // Then apply appropriate theorems based on topology
        switch (topology) {
            case SERIES:
                applySeriesAnalysis();
                break;
            case PARALLEL:
                applyParallelAnalysis();
                break;
            case COMPLEX:
                applyComplexAnalysis();
                break;
        }
    }

    // Identify the circuit topology
    private CircuitTopology identifyCircuitTopology() {
        // Count the number of branches and nodes
        int branchCount = 0;
        int nodeCount = 0;
        Set<String> nodes = new HashSet<>();

        for (ComponentsController.Drawable component : components) {
            if (component instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) component;
                nodes.add(wire.startX + "," + wire.startY);
                nodes.add(wire.endX + "," + wire.endY);
                branchCount++;
            }
        }

        nodeCount = nodes.size();

        // Determine topology based on branch and node count
        if (branchCount == nodeCount - 1) {
            return CircuitTopology.SERIES;
        } else if (branchCount > nodeCount - 1) {
            return CircuitTopology.PARALLEL;
        } else {
            return CircuitTopology.COMPLEX;
        }
    }

    // Apply series circuit analysis
    private void applySeriesAnalysis() {
        double totalResistance = 0;
        double totalVoltage = 0;
        List<ComponentsController.ImageComponent> seriesComponents = new ArrayList<>();

        // Find power source and series components
        for (ComponentsController.Drawable drawable : components) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (isPowerSource(component)) {
                    totalVoltage = component.voltage;
                } else if (!(component instanceof ComponentsController.Voltmeter)) {
                    // Skip voltmeters as they are treated as open circuits
                    if (component instanceof ComponentsController.Ammeter) {
                        // Ammeters are treated as short circuits (0 resistance)
                        continue;
                    }
                    seriesComponents.add(component);
                    if (component instanceof ComponentsController.SPSTToggleSwitch) {
                        totalResistance += component.isClosed ? 0.001 : 1e9;
                    } else if (component instanceof ComponentsController.PushbuttonSwitchNO) {
                        totalResistance += component.isPressed ? 0.001 : 1e9;
                    } else {
                        totalResistance += component.resistance;
                    }
                }
            }
        }

        // Calculate current through the series circuit
        double current = totalVoltage / totalResistance;

        // Update component voltages and currents
        for (ComponentsController.ImageComponent component : seriesComponents) {
            if (component instanceof ComponentsController.Ammeter) {
                component.current = current;
            } else if (!(component instanceof ComponentsController.Voltmeter)) {
                component.current = current;
                if (component instanceof ComponentsController.SPSTToggleSwitch) {
                    component.voltage = current * (component.isClosed ? 0.001 : 1e9);
                } else if (component instanceof ComponentsController.PushbuttonSwitchNO) {
                    component.voltage = current * (component.isPressed ? 0.001 : 1e9);
                } else {
                    component.voltage = current * component.resistance;
                }
            }
        }
    }

    // Apply parallel circuit analysis
    private void applyParallelAnalysis() {
        double totalVoltage = 0;
        List<ComponentsController.ImageComponent> parallelComponents = new ArrayList<>();

        // Find power source and parallel components
        for (ComponentsController.Drawable drawable : components) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (isPowerSource(component)) {
                    totalVoltage = component.voltage;
                } else if (!(component instanceof ComponentsController.Voltmeter)) {
                    // Skip voltmeters as they are treated as open circuits
                    if (component instanceof ComponentsController.Ammeter) {
                        // Ammeters are treated as short circuits (0 resistance)
                        continue;
                    }
                    parallelComponents.add(component);
                }
            }
        }

        // Calculate equivalent resistance
        double equivalentResistance = 0;
        for (ComponentsController.ImageComponent component : parallelComponents) {
            if (component instanceof ComponentsController.SPSTToggleSwitch) {
                equivalentResistance += 1.0 / (component.isClosed ? 0.001 : 1e9);
            } else if (component instanceof ComponentsController.PushbuttonSwitchNO) {
                equivalentResistance += 1.0 / (component.isPressed ? 0.001 : 1e9);
            } else {
                equivalentResistance += 1.0 / component.resistance;
            }
        }
        equivalentResistance = 1.0 / equivalentResistance;

        // Calculate total current
        double totalCurrent = totalVoltage / equivalentResistance;

        // Update component voltages and currents
        for (ComponentsController.ImageComponent component : parallelComponents) {
            if (component instanceof ComponentsController.Ammeter) {
                component.current = totalCurrent;
            } else if (!(component instanceof ComponentsController.Voltmeter)) {
                component.voltage = totalVoltage;
                if (component instanceof ComponentsController.SPSTToggleSwitch) {
                    component.current = totalVoltage / (component.isClosed ? 0.001 : 1e9);
                } else if (component instanceof ComponentsController.PushbuttonSwitchNO) {
                    component.current = totalVoltage / (component.isPressed ? 0.001 : 1e9);
                } else {
                    component.current = totalVoltage / component.resistance;
                }
            }
        }
    }

    // Apply complex circuit analysis using Kirchhoff's Laws
    private void applyComplexAnalysis() {
        // Create conductance matrix
        int n = components.size();
        double[][] conductanceMatrix = new double[n][n];
        double[] currentVector = new double[n];
        double[] voltageVector = new double[n];

        // Initialize matrices
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                conductanceMatrix[i][j] = 0;
            }
            currentVector[i] = 0;
            voltageVector[i] = 0;
        }

        // Fill conductance matrix
        for (int i = 0; i < n; i++) {
            ComponentsController.Drawable drawable = components.get(i);
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component instanceof ComponentsController.Voltmeter) {
                    // Voltmeter is treated as an open circuit (infinite resistance)
                    conductanceMatrix[i][i] = 1e-9;
                } else if (component instanceof ComponentsController.Ammeter) {
                    // Ammeter is treated as a short circuit (zero resistance)
                    conductanceMatrix[i][i] = 1e9;
                } else if (component instanceof ComponentsController.SPSTToggleSwitch) {
                    conductanceMatrix[i][i] = component.isClosed ? 1000.0 : 1e-9;
                } else if (component instanceof ComponentsController.PushbuttonSwitchNO) {
                    conductanceMatrix[i][i] = component.isPressed ? 1000.0 : 1e-9;
                } else {
                    conductanceMatrix[i][i] = 1.0 / component.resistance;
                }
            }
        }

        // Solve the system of equations
        double[] nodeVoltages = solveLinearSystem(conductanceMatrix, currentVector);

        // Store the results
        for (int i = 0; i < n; i++) {
            ComponentsController.Drawable drawable = components.get(i);
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                this.nodeVoltages.put(component.startX + "," + component.startY, nodeVoltages[i]);
            }
        }

        // Calculate branch currents
        for (ComponentsController.Drawable component : components) {
            if (component instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent comp = (ComponentsController.ImageComponent) component;
                if (comp instanceof ComponentsController.ResistorIEEE || 
                    comp instanceof ComponentsController.ResistorIEC) {
                    String startNode = comp.startX + "," + comp.startY;
                    String endNode = comp.endX + "," + comp.endY;
                    double voltageDiff = this.nodeVoltages.get(startNode) - this.nodeVoltages.get(endNode);
                    double current = voltageDiff / comp.resistance;
                    String branchKey = startNode + "->" + endNode;
                    this.branchCurrents.put(branchKey, current);
                } else if (comp instanceof ComponentsController.SPSTToggleSwitch) {
                    ComponentsController.SPSTToggleSwitch switch_ = (ComponentsController.SPSTToggleSwitch) comp;
                    String startNode = comp.startX + "," + comp.startY;
                    String endNode = comp.endX + "," + comp.endY;
                    double voltageDiff = this.nodeVoltages.get(startNode) - this.nodeVoltages.get(endNode);
                    double resistance = switch_.isClosed ? 0.001 : 1e9;
                    double current = voltageDiff / resistance;
                    String branchKey = startNode + "->" + endNode;
                    this.branchCurrents.put(branchKey, current);
                } else if (comp instanceof ComponentsController.PushbuttonSwitchNO) {
                    ComponentsController.PushbuttonSwitchNO switch_ = (ComponentsController.PushbuttonSwitchNO) comp;
                    String startNode = comp.startX + "," + comp.startY;
                    String endNode = comp.endX + "," + comp.endY;
                    double voltageDiff = this.nodeVoltages.get(startNode) - this.nodeVoltages.get(endNode);
                    double resistance = switch_.isPressed ? 0.001 : 1e9;
                    double current = voltageDiff / resistance;
                    String branchKey = startNode + "->" + endNode;
                    this.branchCurrents.put(branchKey, current);
                }
            }
        }

        // Update component voltages and currents
        for (int i = 0; i < n; i++) {
            ComponentsController.Drawable drawable = components.get(i);
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component instanceof ComponentsController.Voltmeter) {
                    component.voltage = nodeVoltages[i];
                } else if (component instanceof ComponentsController.Ammeter) {
                    component.current = currentVector[i];
                } else {
                    component.voltage = nodeVoltages[i];
                    component.current = currentVector[i];
                }
            }
        }
    }

    // Helper method to solve system of linear equations using Gaussian elimination
    private double[] solveLinearSystem(double[][] matrix, double[] vector) {
        int n = vector.length;
        double[] solution = new double[n];
        
        // Forward elimination
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double factor = matrix[j][i] / matrix[i][i];
                for (int k = i; k < n; k++) {
                    matrix[j][k] -= factor * matrix[i][k];
                }
                vector[j] -= factor * vector[i];
            }
        }
        
        // Back substitution
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += matrix[i][j] * solution[j];
            }
            solution[i] = (vector[i] - sum) / matrix[i][i];
        }
        
        return solution;
    }

    // Get voltage across a component
    public double getVoltageAcross(ComponentsController.ImageComponent component) {
        String startNode = component.startX + "," + component.startY;
        String endNode = component.endX + "," + component.endY;
        
        // If the component is a voltmeter, find the component it's measuring
        if (component instanceof ComponentsController.Voltmeter) {
            ComponentsController.ImageComponent measuredComponent = findMeasuredComponent(component);
            if (measuredComponent != null) {
                startNode = measuredComponent.startX + "," + measuredComponent.startY;
                endNode = measuredComponent.endX + "," + measuredComponent.endY;
            }
        }
        
        return nodeVoltages.getOrDefault(startNode, 0.0) - nodeVoltages.getOrDefault(endNode, 0.0);
    }

    // Helper method to find the component being measured by a voltmeter
    private ComponentsController.ImageComponent findMeasuredComponent(ComponentsController.ImageComponent voltmeter) {
        // Find components connected to the same nodes as the voltmeter
        String voltStartNode = voltmeter.startX + "," + voltmeter.startY;
        String voltEndNode = voltmeter.endX + "," + voltmeter.endY;
        
        for (ComponentsController.Drawable drawable : components) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component != voltmeter) {  // Don't return the voltmeter itself
                    String compStartNode = component.startX + "," + component.startY;
                    String compEndNode = component.endX + "," + component.endY;
                    
                    if ((voltStartNode.equals(compStartNode) && voltEndNode.equals(compEndNode)) ||
                        (voltStartNode.equals(compEndNode) && voltEndNode.equals(compStartNode))) {
                        return component;
                    }
                }
            }
        }
        return null;
    }

    // Get current through a component
    public double getCurrentThrough(ComponentsController.ImageComponent component) {
        String branchKey = component.startX + "," + component.startY + "->" + 
                          component.endX + "," + component.endY;
        return branchCurrents.getOrDefault(branchKey, 0.0);
    }

    // Get resistance of a component
    public double getResistance(ComponentsController.ImageComponent component) {
        if (component instanceof ComponentsController.ResistorIEEE || 
            component instanceof ComponentsController.ResistorIEC) {
            return component.resistance;
        }
        return 0.0;
    }

    // Helper method to check if a component is a power supply
    private boolean isPowerSupply(ComponentsController.ImageComponent component) {
        return component instanceof ComponentsController.VoltageSource ||
               component instanceof ComponentsController.BatteryCell ||
               component instanceof ComponentsController.Battery;
    }

    // Helper method to check if a component is a power source
    private boolean isPowerSource(ComponentsController.ImageComponent component) {
        return component instanceof ComponentsController.VoltageSource ||
               component instanceof ComponentsController.BatteryCell ||
               component instanceof ComponentsController.Battery;
    }

    // Enum for circuit topology
    private enum CircuitTopology {
        SERIES,
        PARALLEL,
        COMPLEX
    }
} 