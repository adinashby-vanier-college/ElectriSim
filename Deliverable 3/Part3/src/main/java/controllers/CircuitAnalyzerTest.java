package controllers;

import java.util.*;
import java.util.stream.Collectors;

public class CircuitAnalyzerTest {
    private final Map<String, Double> nodeVoltages = new HashMap<>();
    private final Map<String, Double> branchCurrents = new HashMap<>();
    private CircuitGraph CG = new CircuitGraph();

    public CircuitGraph getCG () {
        return CG;
    }

    public void setCircuitGraph (CircuitGraph CG) {
        this.CG = CG;
    }
    List<ComponentsController.Drawable> components = new ArrayList<>();

    public void setComponents (List<ComponentsController.Drawable> components) {
        this.components = components;
    }
    public static class CircuitGraph {
        public static final Map<String, Node> nodes = new HashMap<>();
        public static final List<Edge> globalEdges = new ArrayList<>();
        private ComponentsController.Battery battery; // Reference to the single battery
        private static Node batteryPlusTerminal;
        private static Node batteryMinusTerminal;
        public static class Node {
            public String id;
            double startX;
            double startY;
            double endX;
            double endY;
            public final List<Edge> edges = new ArrayList<>();

            public Node(String id, double startX, double startY, double endX, double endY) {
                this.id = id;
                this.startX = startX;
                this.startY = startY;
                this.endX = endX;
                this.endY = endY;
            }
        }

        public static class Edge {
            public final Node from;
            public final Node to;
            public ComponentsController.Drawable component;

            public Edge(Node from, Node to, ComponentsController.Drawable component) {
                this.from = from;
                this.to = to;
                this.component = component;
            }

            public Node getOtherNode(Node node) {
                return node == from ? to : from;
            }
        }

        public void addNode(String id, double startX, double startY, double endX, double endY) {
            nodes.putIfAbsent(id, new Node(id, startX, startY, endX, endY));
        }

        public void addEdge(String fromId, String toId, ComponentsController.Drawable component) {
            Node from = nodes.get(fromId);
            Node to = nodes.get(toId);
            if (from != null && to != null) {
                Edge edge = new Edge(from, to, component);
                globalEdges.add(edge);
                from.edges.add(edge);
                to.edges.add(edge);

                if (component instanceof ComponentsController.Battery) {
                    this.battery = (ComponentsController.Battery)component;
                    this.battery.positiveTerminal = from;
                    this.battery.negativeTerminal = to;
                }
            }
        }

        public List<Edge> findConductivePath(Node start, Node end) {
            Set<Node> visited = new HashSet<>();
            return dfsFindPath(start, end, visited, new ArrayList<>());
        }

        private List<Edge> dfsFindPath(Node current, Node end,
                                       Set<Node> visited, List<Edge> path) {
            if (current == end) return new ArrayList<>(path);
            visited.add(current);

            for (Edge edge : getConductiveEdges(current)) {
                Node neighbor = edge.getOtherNode(current);
                if (!visited.contains(neighbor)) {
                    path.add(edge);
                    List<Edge> result = dfsFindPath(neighbor, end, visited, path);
                    if (result != null) return result;
                    path.remove(edge);
                }
            }
            return null;
        }

        public List<List<Edge>> findAllLoops() {
            List<List<Edge>> loops = new ArrayList<>();
            Set<Edge> visited = new HashSet<>();
            Stack<Edge> path = new Stack<>();

            for (Node node : nodes.values()) {
                findLoopsDFS(node, null, visited, path, loops);
            }
            return loops;
        }

        private void findLoopsDFS(Node current, Node parent,
                                  Set<Edge> visited, Stack<Edge> path,
                                  List<List<Edge>> loops) {
            for (Edge edge : getConductiveEdges(current)) {
                if (!path.isEmpty() && edge == path.peek()) continue;

                if (visited.contains(edge)) {
                    if (path.contains(edge)) {
                        loops.add(extractLoop(path, edge));
                    }
                    continue;
                }

                visited.add(edge);
                path.push(edge);

                Node neighbor = edge.getOtherNode(current);
                if (neighbor != parent) {
                    findLoopsDFS(neighbor, current, visited, path, loops);
                }

                path.pop();
                visited.remove(edge);
            }
        }

        private List<Edge> getConductiveEdges(Node node) {
            return node.edges.stream()
                    .filter(edge -> isConductive(edge.component))
                    .collect(Collectors.toList());
        }

        private boolean isConductive(ComponentsController.Drawable comp) {
            if (comp instanceof ComponentsController.SPSTToggleSwitch) {
                return ((ComponentsController.SPSTToggleSwitch)comp).isClosed();
            }
            return !(comp instanceof ComponentsController.Voltmeter);
        }

        private List<Edge> extractLoop(Stack<Edge> path, Edge closingEdge) {
            int startIdx = path.indexOf(closingEdge);
            return new ArrayList<>(path.subList(startIdx, path.size()));
        }

        // Getters
        public Node getBatteryPositiveTerminal() {
            return battery.positiveTerminal;
        }

        public Node getBatteryNegativeTerminal() {
            return battery.negativeTerminal;
        }

        public List<Edge> getEdges() {
            return Collections.unmodifiableList(globalEdges);
        }

        public static void locateBatteryTerminals() {
            for (Edge edge : globalEdges) {
                if (edge.component instanceof ComponentsController.Battery battery) {
                    batteryPlusTerminal = edge.from;
                    batteryMinusTerminal = edge.to;
                    battery.positiveTerminal = edge.from;
                    battery.negativeTerminal = edge.to;
                    return;
                }
            }
            throw new IllegalStateException("No battery found in circuit");
        }
        public Node getBatteryPlusTerminal() {
            if (batteryPlusTerminal == null) {
                locateBatteryTerminals();
            }
            return batteryPlusTerminal;
        }

        public Node getBatteryMinusTerminal() {
            if (batteryMinusTerminal == null) {
                locateBatteryTerminals();
            }
            return batteryMinusTerminal;
        }

    }

    public class CircuitScanner {
        private final CircuitGraph graph;
        private CircuitGraph.Node batteryPlus;
        private CircuitGraph.Node batteryMinus;

        public CircuitScanner(CircuitGraph graph) {
            this.graph = graph;
            CircuitGraph.locateBatteryTerminals();
        }

        public boolean hasCompleteCircuit() {
            return findConductivePath(batteryPlus, batteryMinus) != null;
        }

        public void analyze() {
            if (!hasCompleteCircuit()) {
                System.out.println("Open circuit detected");
                return;
            }

            if (isSeriesCircuit()) {
                analyzeSeries();
            } else if (isPureParallel()) {
                analyzeParallel();
            } else {
                analyzeComplex();
            }
        }

        private void analyzeSeries() {
            List<CircuitGraph.Edge> path = findConductivePath(batteryPlus, batteryMinus);
            double totalR = calculateTotalResistance(path);
            double current = graph.battery.voltage / totalR;

            for (CircuitGraph.Edge edge : path) {
                updateComponentValues(edge, current);
            }
        }

        private void analyzeParallel() {
            Map<String, List<CircuitGraph.Edge>> parallelGroups = groupParallelBranches();
            double totalR = 0;

            for (List<CircuitGraph.Edge> branch : parallelGroups.values()) {
                double branchR = calculateTotalResistance(branch);
                totalR += 1.0 / branchR;
            }
            totalR = 1.0 / totalR;

            double totalCurrent = graph.battery.voltage / totalR;

            for (List<CircuitGraph.Edge> branch : parallelGroups.values()) {
                double branchR = calculateTotalResistance(branch);
                double branchCurrent = graph.battery.voltage / branchR;

                for (CircuitGraph.Edge edge : branch) {
                    updateComponentValues(edge, branchCurrent);
                }
            }
        }

        private void analyzeComplex() {
            List<List<CircuitGraph.Edge>> loops = findAllLoops();

            for (List<CircuitGraph.Edge> loop : loops) {
                double loopR = calculateTotalResistance(loop);
                double loopCurrent = graph.battery.voltage / loopR;

                for (CircuitGraph.Edge edge : loop) {
                    if (edge.component instanceof ComponentsController.ImageComponent) {
                        if (((ComponentsController.ImageComponent) edge.component).getCurrent() == 0) {
                            updateComponentValues(edge, loopCurrent);
                        }
                    }
                }
            }
        }

        private List<CircuitGraph.Edge> findConductivePath(CircuitGraph.Node start, CircuitGraph.Node end) {
            Set<CircuitGraph.Node> visited = new HashSet<>();
            return dfsFindPath(start, end, visited, new ArrayList<>());
        }

        private List<CircuitGraph.Edge> dfsFindPath(CircuitGraph.Node current, CircuitGraph.Node end,
                                       Set<CircuitGraph.Node> visited, List<CircuitGraph.Edge> path) {
            if (current == end) return new ArrayList<>(path);
            visited.add(current);

            for (CircuitGraph.Edge edge : getConductiveEdges(current)) {
                CircuitGraph.Node neighbor = (edge.from == current) ? edge.to : edge.from;
                if (!visited.contains(neighbor)) {
                    path.add(edge);
                    List<CircuitGraph.Edge> result = dfsFindPath(neighbor, end, visited, path);
                    if (result != null) return result;
                    path.remove(edge);
                }
            }
            return null;
        }

        private double calculateTotalResistance(List<CircuitGraph.Edge> path) {
            return path.stream()
                    .mapToDouble(edge -> getEffectiveResistance(edge.component))
                    .sum();
        }

        private void updateComponentValues(CircuitGraph.Edge edge, double current) {
            ComponentsController.Drawable comp = edge.component;
            if (edge.component instanceof ComponentsController.ImageComponent) {
                ((ComponentsController.ImageComponent) edge.component).setCurrent(current);
            }

        }

        private boolean isSeriesCircuit() {
            return findAllLoops().isEmpty() &&
                    getConductiveEdges(batteryPlus).size() == 1 &&
                    getConductiveEdges(batteryMinus).size() == 1;
        }

        public boolean isPureParallel() {
            return findAllLoops().size() == 1 &&
                    getConductiveEdges(batteryPlus).size() > 1 &&
                    getConductiveEdges(batteryMinus).size() > 1;
        }

        public void debugPrintState() {
            System.out.println("\n=== Circuit Analysis Results ===");
            System.out.println("Topology: " + getTopology());

            System.out.println("\nNode Voltages:");
            nodeVoltages.forEach((node, voltage) ->
                    System.out.printf("%s: %.2f V%n", node, voltage));

            System.out.println("\nComponent States:");
            components.stream()
                    .filter(c -> c instanceof ComponentsController.ImageComponent)
                    .map(c -> (ComponentsController.ImageComponent)c)
                    .forEach(comp -> {
                        String key = comp.startX + "," + comp.startY + "->" + comp.endX + "," + comp.endY;
                        System.out.printf("%s [%s]: %.2f V, %.2f A%n",
                                comp.componentType,
                                comp instanceof ComponentsController.SPSTToggleSwitch ?
                                        (((ComponentsController.SPSTToggleSwitch)comp).isClosed() ? "CLOSED" : "OPEN") : "",
                                comp.voltage,
                                comp.current);
                    });
        }

        public String getTopology() {
            if (findAllLoops().isEmpty()) return "SERIES";
            if (isPureParallel()) return "PARALLEL";
            return "COMPLEX";
        }

        public double getVoltageAcross(ComponentsController.ImageComponent component) {
            String startNode = component.startX + "," + component.startY;
            String endNode = component.endX + "," + component.endY;
            return nodeVoltages.getOrDefault(startNode, 0.0) -
                    nodeVoltages.getOrDefault(endNode, 0.0);
        }

        public double getCurrentThrough(ComponentsController.ImageComponent component) {
            String key = component.startX + "," + component.startY + "->" +
                    component.endX + "," + component.endY;
            return branchCurrents.getOrDefault(key, 0.0);
        }

    }

    public Map<String, List<CircuitGraph.Edge>> groupParallelBranches() {
        Map<String, List<CircuitGraph.Edge>> parallelGroups = new HashMap<>();

        Set<CircuitGraph.Node> junctionNodes = CircuitGraph.nodes.values().stream()
                .filter(node -> getConductiveEdges(node).size() > 2)
                .collect(Collectors.toSet());


        for (CircuitGraph.Edge edge : CircuitGraph.globalEdges) {
            if (!isConductive(edge.component)) continue;

            String key = generateEdgeKey(edge.from, edge.to);
            parallelGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(edge);
        }

        return parallelGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String generateEdgeKey(CircuitGraph.Node a, CircuitGraph.Node b) {
        return a.id.compareTo(b.id) < 0 ? a.id + "-" + b.id : b.id + "-" + a.id;
    }

    public List<List<CircuitGraph.Edge>> findAllLoops() {
        List<List<CircuitGraph.Edge>> loops = new ArrayList<>();
        Set<CircuitGraph.Edge> visited = new HashSet<>();
        Stack<CircuitGraph.Edge> path = new Stack<>();

        for (CircuitGraph.Node node : CircuitGraph.nodes.values()) {
            findLoopsDFS(node, null, visited, path, loops);
        }
        return loops;
    }

    private void findLoopsDFS(CircuitGraph.Node current, CircuitGraph.Node parent,
                              Set<CircuitGraph.Edge> visited, Stack<CircuitGraph.Edge> path,
                              List<List<CircuitGraph.Edge>> loops) {
        for (CircuitGraph.Edge edge : getConductiveEdges(current)) {
            if (!path.isEmpty() && edge == path.peek()) continue;

            if (visited.contains(edge)) {
                if (path.contains(edge)) {
                    loops.add(extractLoop(path, edge));
                }
                continue;
            }

            visited.add(edge);
            path.push(edge);

            CircuitGraph.Node neighbor = edge.getOtherNode(current);
            if (neighbor != parent) {
                findLoopsDFS(neighbor, current, visited, path, loops);
            }

            path.pop();
            visited.remove(edge);
        }
    }

    private List<CircuitGraph.Edge> extractLoop(Stack<CircuitGraph.Edge> path, CircuitGraph.Edge closingEdge) {
        int startIdx = path.indexOf(closingEdge);
        return new ArrayList<>(path.subList(startIdx, path.size()));
    }

    public List<CircuitGraph.Edge> getConductiveEdges(CircuitGraph.Node node) {
        return node.edges.stream()
                .filter(edge -> isConductive(edge.component))
                .collect(Collectors.toList());
    }

    public double getEffectiveResistance(ComponentsController.Drawable component) {
        if (component instanceof ComponentsController.SPSTToggleSwitch) {
            return ((ComponentsController.SPSTToggleSwitch) component).isClosed() ? 0.001 : 1_000_000;
        }
        if (component instanceof ComponentsController.ResistorIEEE) {
            return ((ComponentsController.ResistorIEEE) component).resistance;
        }
        if (component instanceof ComponentsController.Battery) {
            return 0;
        }
        return 0.001;
    }

    private boolean isConductive(ComponentsController.Drawable component) {
        if (component instanceof ComponentsController.Voltmeter) {
            return false;
        }
        if (component instanceof ComponentsController.SPSTToggleSwitch) {
            return ((ComponentsController.SPSTToggleSwitch) component).isClosed();
        }
        return true;
    }

}
