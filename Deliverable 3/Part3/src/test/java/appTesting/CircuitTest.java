package appTesting;

import controllers.CircuitAnalyzer;
import controllers.ComponentsController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CircuitTest {

    @Test
    public void testCircuitFunctionality() {
        // Create components
        ComponentsController.Battery battery = new ComponentsController.Battery();
        battery.startX = 0;
        battery.startY = 0;
        battery.endX = 1;
        battery.endY = 0;
        battery.voltage = 9.0;

        ComponentsController.ResistorIEEE resistor1 = new ComponentsController.ResistorIEEE();
        resistor1.startX = 1;
        resistor1.startY = 0;
        resistor1.endX = 2;
        resistor1.endY = 0;
        resistor1.resistance = 100.0;

        ComponentsController.ResistorIEEE resistor2 = new ComponentsController.ResistorIEEE();
        resistor2.startX = 2;
        resistor2.startY = 0;
        resistor2.endX = 3;
        resistor2.endY = 0;
        resistor2.resistance = 200.0;

        ComponentsController.Wire wire1 = new ComponentsController.Wire();
        wire1.startX = 3;
        wire1.startY = 0;
        wire1.endX = 0;
        wire1.endY = 0;

        // Assemble components
        List<ComponentsController.Drawable> components = new ArrayList<>();
        components.add(battery);
        components.add(resistor1);
        components.add(resistor2);
        components.add(wire1);

        // Initialize CircuitAnalyzer
        CircuitAnalyzer analyzer = new CircuitAnalyzer(components);

        // Analyze the circuit
        analyzer.analyzeCircuit();

        // 1. Check if the circuit is closed
        List<List<CircuitAnalyzer.Edge>> loops = analyzer.findAllLoops();
        assertTrue(analyzer.findAllLoops().isEmpty(), "Circuit should be closed.");

        // 2. Calculate total resistance
        double expectedTotalResistance = resistor1.resistance + resistor2.resistance;
        double calculatedResistance = analyzer.getResistance(resistor1) + analyzer.getResistance(resistor2);
        assertEquals(expectedTotalResistance, calculatedResistance, 0.001, "Total resistance should be 300 ohms.");

        // 3. Calculate current using Ohm's Law: I = V / R
        double expectedCurrent = battery.voltage / expectedTotalResistance;
        double actualCurrent = analyzer.getCurrentThrough(resistor1);
        assertEquals(expectedCurrent, actualCurrent, 0.001, "Current should be 0.03 A.");

        // 4. Check voltage drop across resistor1
        double expectedVoltageDrop = expectedCurrent * resistor1.resistance;
        double actualVoltageDrop = analyzer.getVoltageAcross(resistor1);
        assertEquals(expectedVoltageDrop, actualVoltageDrop, 0.001, "Voltage across resistor1 should be 3 V.");

        // Print circuit values
        analyzer.printCircuitValues();
    }
}
