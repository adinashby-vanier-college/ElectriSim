package controllers;

import java.util.*;

public class CircuitAnalyzer {
    private final List<ComponentsController.Drawable> components;
    private final Map<String, Double> nodeVoltages;
    private final Map<String, Double> branchCurrents;
    private final Map<String, Double> componentValues;
    private CircuitGraph circuitGraph;

    // Inner class to represent a circuit node
    private static class Node {
        String id;
        double x, y;
        List<Edge> edges;

        Node(String id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.edges = new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return id.equals(node.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    // Inner class to represent a circuit edge (component or wire)
    private static class Edge {
        Node from;
        Node to;
        ComponentsController.Drawable component;
        double resistance;
        double voltage;
        double current;

        Edge(Node from, Node to, ComponentsController.Drawable component) {
            this.from = from;
            this.to = to;
            this.component = component;
            this.resistance = calculateResistance(component);
            this.voltage = 0;
            this.current = 0;
        }

        private double calculateResistance(ComponentsController.Drawable component) {
            if (component instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent comp = (ComponentsController.ImageComponent) component;
                if (comp instanceof ComponentsController.ResistorIEEE || 
                    comp instanceof ComponentsController.ResistorIEC) {
                    return comp.resistance;
                } else if (comp instanceof ComponentsController.SPSTToggleSwitch) {
                    ComponentsController.SPSTToggleSwitch spstSwitch = (ComponentsController.SPSTToggleSwitch) comp;
                    return spstSwitch.isClosed ? 0.001 : 1e9;
                } else if (comp instanceof ComponentsController.Voltmeter) {
                    return 1e6; // High internal resistance
                } else if (comp instanceof ComponentsController.Ammeter) {
                    return 0.1; // Low internal resistance
                } else if (comp.componentType.equals("NOTGate") ||
                          comp.componentType.equals("ANDGate") ||
                          comp.componentType.equals("NANDGate") ||
                          comp.componentType.equals("ORGate") ||
                          comp.componentType.equals("NORGate") ||
                          comp.componentType.equals("XORGate")) {
                    return 1e3; // Logic gate internal resistance
                }
            }
            return 0.001; // Default wire resistance
        }
    }

    // Inner class to represent the circuit graph
    private static class CircuitGraph {
        Map<String, Node> nodes;
        List<Edge> edges;
        List<List<Node>> loops;

        CircuitGraph() {
            this.nodes = new HashMap<>();
            this.edges = new ArrayList<>();
            this.loops = new ArrayList<>();
        }

        void addNode(Node node) {
            nodes.put(node.id, node);
        }

        void addEdge(Edge edge) {
            edges.add(edge);
            edge.from.edges.add(edge);
            edge.to.edges.add(edge);
        }

        // Find all loops in the circuit using DFS
        void findLoops() {
            loops.clear();
            Set<Node> visited = new HashSet<>();
            List<Node> currentPath = new ArrayList<>();

            for (Node startNode : nodes.values()) {
                if (!visited.contains(startNode)) {
                    findLoopsDFS(startNode, startNode, visited, currentPath);
                }
            }
        }

        private void findLoopsDFS(Node current, Node start, Set<Node> visited, List<Node> currentPath) {
            visited.add(current);
            currentPath.add(current);

            for (Edge edge : current.edges) {
                Node next = (edge.from == current) ? edge.to : edge.from;
                
                if (next == start && currentPath.size() > 2) {
                    // Found a loop
                    List<Node> loop = new ArrayList<>(currentPath);
                    loop.add(start);
                    loops.add(loop);
                } else if (!visited.contains(next)) {
                    findLoopsDFS(next, start, visited, currentPath);
                }
            }

            visited.remove(current);
            currentPath.remove(currentPath.size() - 1);
        }

        // Apply Kirchhoff's Voltage Law (KVL) to all loops
        void applyKVL() {
            for (List<Node> loop : loops) {
                double sumVoltage = 0;
                for (int i = 0; i < loop.size() - 1; i++) {
                    Node current = loop.get(i);
                    Node next = loop.get(i + 1);
                    Edge edge = findEdge(current, next);
                    if (edge != null) {
                        sumVoltage += edge.voltage;
                    }
                }
                // KVL states that sum of voltages in a loop should be zero
                // This can be used to verify circuit validity or solve for unknown voltages
            }
        }

        // Apply Kirchhoff's Current Law (KCL) to all nodes
        void applyKCL() {
            for (Node node : nodes.values()) {
                double sumCurrent = 0;
                for (Edge edge : node.edges) {
                    if (edge.from == node) {
                        sumCurrent += edge.current;
                    } else {
                        sumCurrent -= edge.current;
                    }
                }
                // KCL states that sum of currents at a node should be zero
                // This can be used to verify circuit validity or solve for unknown currents
            }
        }

        private Edge findEdge(Node from, Node to) {
            for (Edge edge : edges) {
                if ((edge.from == from && edge.to == to) || 
                    (edge.from == to && edge.to == from)) {
                    return edge;
                }
            }
            return null;
        }
    }

    public CircuitAnalyzer(List<ComponentsController.Drawable> components) {
        this.components = components;
        this.nodeVoltages = new HashMap<>();
        this.branchCurrents = new HashMap<>();
        this.componentValues = new HashMap<>();
        this.circuitGraph = new CircuitGraph();
        buildCircuitGraph();
    }

    private void buildCircuitGraph() {
        // Create nodes from component endpoints
        for (ComponentsController.Drawable component : components) {
            if (component instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent comp = (ComponentsController.ImageComponent) component;
                String startNodeId = comp.startX + "," + comp.startY;
                String endNodeId = comp.endX + "," + comp.endY;
                
                Node startNode = new Node(startNodeId, comp.startX, comp.startY);
                Node endNode = new Node(endNodeId, comp.endX, comp.endY);
                
                circuitGraph.addNode(startNode);
                circuitGraph.addNode(endNode);
                
                Edge edge = new Edge(startNode, endNode, component);
                circuitGraph.addEdge(edge);
            } else if (component instanceof ComponentsController.Wire) {
                ComponentsController.Wire wire = (ComponentsController.Wire) component;
                String startNodeId = wire.startX + "," + wire.startY;
                String endNodeId = wire.endX + "," + wire.endY;
                
                Node startNode = new Node(startNodeId, wire.startX, wire.startY);
                Node endNode = new Node(endNodeId, wire.endX, wire.endY);
                
                circuitGraph.addNode(startNode);
                circuitGraph.addNode(endNode);
                
                Edge edge = new Edge(startNode, endNode, component);
                circuitGraph.addEdge(edge);
            }
        }

        // Find loops in the circuit
        circuitGraph.findLoops();
    }

    // Analyze the circuit and determine which theorems to use
    public void analyzeCircuit() {
        // First, identify the circuit topology
        CircuitTopology topology = identifyCircuitTopology();
        
        // Apply graph-based analysis
        circuitGraph.applyKVL();
        circuitGraph.applyKCL();
        
        // Analyze logic gates
        analyzeLogicGates();
        
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
                    ComponentsController.SPSTToggleSwitch spstSwitch = (ComponentsController.SPSTToggleSwitch) component;
                    conductanceMatrix[i][i] = spstSwitch.isClosed ? 1000.0 : 1e-9;
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
                String nodeId = component.startX + "," + component.startY;
                this.nodeVoltages.put(nodeId, nodeVoltages[i]);
            }
        }

        // Calculate branch currents
        for (ComponentsController.Drawable component : components) {
            if (component instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent comp = (ComponentsController.ImageComponent) component;
                String startNode = comp.startX + "," + comp.startY;
                String endNode = comp.endX + "," + comp.endY;
                
                // Get voltages with null checks
                Double startVoltage = this.nodeVoltages.get(startNode);
                Double endVoltage = this.nodeVoltages.get(endNode);
                
                if (startVoltage != null && endVoltage != null) {
                    double voltageDiff = startVoltage - endVoltage;
                    
                    if (comp instanceof ComponentsController.ResistorIEEE || 
                        comp instanceof ComponentsController.ResistorIEC) {
                        double current = voltageDiff / comp.resistance;
                        String branchKey = startNode + "->" + endNode;
                        this.branchCurrents.put(branchKey, current);
                    } else if (comp instanceof ComponentsController.SPSTToggleSwitch) {
                        ComponentsController.SPSTToggleSwitch switch_ = (ComponentsController.SPSTToggleSwitch) comp;
                        double resistance = switch_.isClosed ? 0.001 : 1e9;
                        double current = voltageDiff / resistance;
                        String branchKey = startNode + "->" + endNode;
                        this.branchCurrents.put(branchKey, current);
                    } 
                }
            }
        }

        // Update component voltages and currents
        for (int i = 0; i < n; i++) {
            ComponentsController.Drawable drawable = components.get(i);
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                String startNode = component.startX + "," + component.startY;
                String endNode = component.endX + "," + component.endY;
                
                Double startVoltage = this.nodeVoltages.get(startNode);
                Double endVoltage = this.nodeVoltages.get(endNode);
                
                if (startVoltage != null && endVoltage != null) {
                    if (component instanceof ComponentsController.Voltmeter) {
                        component.voltage = startVoltage - endVoltage;
                    } else if (component instanceof ComponentsController.Ammeter) {
                        component.current = currentVector[i];
                    } else {
                        component.voltage = startVoltage - endVoltage;
                        component.current = currentVector[i];
                    }
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
    public ComponentsController.ImageComponent findMeasuredComponent(ComponentsController.ImageComponent voltmeter) {
        // Find components connected to the same nodes as the voltmeter
        String voltStartNode = voltmeter.startX + "," + voltmeter.startY;
        String voltEndNode = voltmeter.endX + "," + voltmeter.endY;
        
        // First try exact node match
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
        
        // If no exact match, look for components in parallel
        // A component is in parallel if it shares both nodes with the voltmeter
        // but might be connected in a different order
        for (ComponentsController.Drawable drawable : components) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component != voltmeter) {  // Don't return the voltmeter itself
                    String compStartNode = component.startX + "," + component.startY;
                    String compEndNode = component.endX + "," + component.endY;
                    
                    // Check if the component shares both nodes with the voltmeter
                    if ((voltStartNode.equals(compStartNode) || voltStartNode.equals(compEndNode)) &&
                        (voltEndNode.equals(compStartNode) || voltEndNode.equals(compEndNode))) {
                        return component;
                    }
                }
            }
        }
        
        // If still no match, look for components connected to either node
        // This handles cases where the voltmeter might be measuring across multiple components
        for (ComponentsController.Drawable drawable : components) {
            if (drawable instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent component = (ComponentsController.ImageComponent) drawable;
                if (component != voltmeter) {  // Don't return the voltmeter itself
                    String compStartNode = component.startX + "," + component.startY;
                    String compEndNode = component.endX + "," + component.endY;
                    
                    // Check if the component shares either node with the voltmeter
                    if (voltStartNode.equals(compStartNode) || voltStartNode.equals(compEndNode) ||
                        voltEndNode.equals(compStartNode) || voltEndNode.equals(compEndNode)) {
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

    // Debug method to print circuit state
    public void debugPrintState() {
        // Debug information removed
    }

    // Enum for circuit topology
    private enum CircuitTopology {
        SERIES,
        PARALLEL,
        COMPLEX
    }

    // Helper method to calculate logic gate output
    private boolean calculateLogicGateOutput(ComponentsController.ImageComponent component, List<Boolean> inputs) {
        switch (component.componentType) {
            case "NOTGate":
                return !inputs.get(0);
            case "ANDGate":
                return inputs.stream().allMatch(input -> input);
            case "NANDGate":
                return !inputs.stream().allMatch(input -> input);
            case "ORGate":
                return inputs.stream().anyMatch(input -> input);
            case "NORGate":
                return !inputs.stream().anyMatch(input -> input);
            case "XORGate":
                return inputs.stream().filter(input -> input).count() % 2 == 1;
            default:
                return false;
        }
    }

    // Helper method to convert voltage to logic level
    private boolean voltageToLogic(double voltage) {
        // Assuming 5V logic levels
        return voltage >= 2.5;
    }

    // Helper method to convert logic level to voltage
    private double logicToVoltage(boolean logic) {
        // Assuming 5V logic levels
        return logic ? 5.0 : 0.0;
    }

    // Helper method to analyze logic gates
    private void analyzeLogicGates() {
        for (ComponentsController.Drawable component : components) {
            if (component instanceof ComponentsController.ImageComponent) {
                ComponentsController.ImageComponent comp = (ComponentsController.ImageComponent) component;
                if (comp.isLogicGate()) {
                    // Get input voltages
                    List<Boolean> inputs = new ArrayList<>();
                    for (Edge edge : circuitGraph.edges) {
                        if (edge.to.id.equals(comp.startX + "," + comp.startY)) {
                            inputs.add(voltageToLogic(edge.voltage));
                        }
                    }
                    
                    // Calculate output
                    boolean output = calculateLogicGateOutput(comp, inputs);
                    
                    // Set output voltage
                    for (Edge edge : circuitGraph.edges) {
                        if (edge.from.id.equals(comp.endX + "," + comp.endY)) {
                            edge.voltage = logicToVoltage(output);
                        }
                    }
                }
            }
        }
    }
} 