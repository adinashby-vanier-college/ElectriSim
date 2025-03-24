package controllers;

/**
 * This class contains nested classes for each electrical component in the FXML file.
 * Each component is represented as a separate class with relevant electrical properties.
 */
public class ComponentsController {

    // Switch and Relay Components
    public static class SPSTToggleSwitch {
        public boolean isClosed; // State of the switch (open/closed)
        public double maxVoltage; // Maximum voltage the switch can handle
        public double maxCurrent; // Maximum current the switch can handle
    }

    public static class SPDTToggleSwitch {
        public boolean isClosed; // State of the switch (open/closed)
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class PushbuttonSwitchNO {
        public boolean isPressed; // State of the pushbutton (pressed/released)
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class PushbuttonSwitchNC {
        public boolean isPressed; // State of the pushbutton (pressed/released)
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class DIPSwitch {
        public boolean[] switchStates; // Array of states for each switch in the DIP
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class SPSTRelay {
        public boolean isEnergized; // State of the relay coil (energized/de-energized)
        public double coilVoltage; // Voltage required to energize the coil
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class SPDTRelay {
        public boolean isEnergized;
        public double coilVoltage;
        public double maxVoltage;
        public double maxCurrent;
    }

    public static class Jumper {
        public double resistance; // Resistance of the jumper (typically very low)
    }

    public static class SolderBridge {
        public double resistance; // Resistance of the solder bridge (typically very low)
    }

    // Ground Components
    public static class EarthGround {
        public double resistance; // Resistance to earth (typically very low)
    }

    public static class ChassisGround {
        public double resistance; // Resistance to chassis (typically very low)
    }

    public static class DigitalGround {
        public double resistance; // Resistance to digital ground (typically very low)
    }

    // Resistor Components
    public static class ResistorIEEE {
        public double resistance; // Resistance value in ohms
        public double powerRating; // Maximum power the resistor can handle
    }

    public static class ResistorIEC {
        public double resistance;
        public double powerRating;
    }

    public static class PotentiometerIEEE {
        public double resistance;
        public double powerRating;
        public double wiperPosition; // Position of the wiper (0 to 1)
    }

    public static class PotentiometerIEC {
        public double resistance;
        public double powerRating;
        public double wiperPosition;
    }

    public static class RheostatIEEE {
        public double resistance;
        public double powerRating;
        public double wiperPosition;
    }

    public static class RheostatIEC {
        public double resistance;
        public double powerRating;
        public double wiperPosition;
    }

    public static class Thermistor {
        public double resistance; // Resistance at a given temperature
        public double temperatureCoefficient; // Temperature coefficient of resistance
    }

    public static class Photoresistor {
        public double resistance; // Resistance under current light conditions
        public double lightIntensity; // Light intensity affecting resistance
    }

    // Capacitor Components
    public static class Capacitor {
        public double capacitance; // Capacitance in farads
        public double voltageRating; // Maximum voltage the capacitor can handle
    }

    public static class PolarizedCapacitor {
        public double capacitance;
        public double voltageRating;
        public boolean isPolarityRespected; // Whether polarity is respected
    }

    public static class VariableCapacitor {
        public double capacitance;
        public double voltageRating;
        public double rotationAngle; // Angle of rotation (0 to 360 degrees)
    }

    // Inductor and Coil Components
    public static class Inductor {
        public double inductance; // Inductance in henries
        public double currentRating; // Maximum current the inductor can handle
    }

    public static class IronCoreInductor {
        public double inductance;
        public double currentRating;
        public double corePermeability; // Permeability of the iron core
    }

    public static class VariableInductor {
        public double inductance;
        public double currentRating;
        public double rotationAngle; // Angle of rotation (0 to 360 degrees)
    }

    // Power Supply Components
    public static class VoltageSource {
        public double voltage; // Output voltage
        public double internalResistance; // Internal resistance of the source
    }

    public static class CurrentSource {
        public double current; // Output current
        public double internalResistance;
    }

    public static class Generator {
        public double voltage;
        public double frequency; // Frequency of the generated AC signal
        public double internalResistance;
    }

    public static class BatteryCell {
        public double voltage; // Nominal voltage of the cell
        public double internalResistance;
        public double capacity; // Capacity in ampere-hours (Ah)
    }

    public static class Battery {
        public double voltage; // Total voltage of the battery
        public double internalResistance;
        public double capacity;
    }

    public static class ControlledVoltageSource {
        public double voltage;
        public double controlSignal; // Signal controlling the output voltage
    }

    public static class ControlledCurrentSource {
        public double current;
        public double controlSignal; // Signal controlling the output current
    }

    // Meter Components
    public static class Voltmeter {
        public double range; // Maximum voltage the voltmeter can measure
        public double internalResistance;
    }

    public static class Ammeter {
        public double range; // Maximum current the ammeter can measure
        public double internalResistance;
    }

    public static class Ohmmeter {
        public double range; // Maximum resistance the ohmmeter can measure
    }

    public static class Wattmeter {
        public double voltageRange;
        public double currentRange;
    }

    // Diode and LED Components
    public static class Diode {
        public double forwardVoltage; // Forward voltage drop
        public double reverseBreakdownVoltage; // Reverse breakdown voltage
    }

    public static class ZenerDiode {
        public double zenerVoltage; // Zener breakdown voltage
        public double forwardVoltage;
    }

    public static class SchottkyDiode {
        public double forwardVoltage;
        public double reverseBreakdownVoltage;
    }

    public static class Varactor {
        public double capacitance; // Capacitance at a given reverse voltage
        public double reverseVoltage; // Applied reverse voltage
    }

    public static class TunnelDiode {
        public double peakVoltage; // Peak voltage in the I-V curve
        public double valleyVoltage; // Valley voltage in the I-V curve
    }

    public static class LightEmittingDiode {
        public double forwardVoltage;
        public double wavelength; // Wavelength of emitted light
    }

    public static class Photodiode {
        public double darkCurrent; // Current in the absence of light
        public double lightCurrent; // Current under illumination
    }

    // Transistor Components
    public static class NPNBipolarTransistor {
        public double currentGain; // Current gain (beta)
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;
    }

    public static class PNPBipolarTransistor {
        public double currentGain;
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;
    }

    public static class DarlingtonTransistor {
        public double currentGain;
        public double maxCollectorCurrent;
        public double maxCollectorEmitterVoltage;
    }

    public static class JFETNTransistor {
        public double pinchOffVoltage; // Pinch-off voltage
        public double maxDrainSourceVoltage;
    }

    public static class JFETPTransistor {
        public double pinchOffVoltage;
        public double maxDrainSourceVoltage;
    }

    public static class NMOSTransistor {
        public double thresholdVoltage; // Threshold voltage
        public double maxDrainSourceVoltage;
    }

    public static class PMOSTransistor {
        public double thresholdVoltage;
        public double maxDrainSourceVoltage;
    }

    // Logic Gate Components
    public static class NOTGate {
        public double propagationDelay; // Propagation delay in nanoseconds
    }

    public static class ANDGate {
        public double propagationDelay;
    }

    public static class NANDGate {
        public double propagationDelay;
    }

    public static class ORGate {
        public double propagationDelay;
    }

    public static class NORGate {
        public double propagationDelay;
    }

    public static class XORGate {
        public double propagationDelay;
    }

    public static class DFlipFlop {
        public double setupTime; // Setup time in nanoseconds
        public double holdTime; // Hold time in nanoseconds
    }

    public static class Multiplexer2to1 {
        public double propagationDelay;
    }

    public static class Multiplexer4to1 {
        public double propagationDelay;
    }

    public static class Demultiplexer1to4 {
        public double propagationDelay;
    }

    // Antenna Components
    public static class Antenna {
        public double frequencyRange; // Frequency range the antenna operates in
        public double gain; // Gain in dBi
    }

    public static class DipoleAntenna {
        public double frequencyRange;
        public double gain;
    }

    // Miscellaneous Components
    public static class Motor {
        public double ratedVoltage; // Rated operating voltage
        public double ratedCurrent; // Rated operating current
    }

    public static class Transformer {
        public double primaryVoltage; // Primary winding voltage
        public double secondaryVoltage; // Secondary winding voltage
        public double turnsRatio; // Turns ratio (N1/N2)
    }

    public static class Fuse {
        public double ratedCurrent; // Current at which the fuse blows
        public double breakingCapacity; // Maximum current the fuse can interrupt
    }

    public static class Optocoupler {
        public double currentTransferRatio; // Ratio of output current to input current
    }

    public static class Loudspeaker {
        public double impedance; // Impedance in ohms
        public double powerRating; // Maximum power the speaker can handle
    }

    public static class Microphone {
        public double sensitivity; // Sensitivity in dB
        public double frequencyResponse; // Frequency response range
    }

    public static class OperationalAmplifier {
        public double gainBandwidthProduct; // Gain-bandwidth product in Hz
        public double slewRate; // Slew rate in V/µs
    }

    public static class SchmittTrigger {
        public double upperThreshold; // Upper threshold voltage
        public double lowerThreshold; // Lower threshold voltage
    }

    public static class AnalogToDigitalConverter {
        public int resolution; // Resolution in bits
        public double samplingRate; // Sampling rate in samples per second
    }

    public static class DigitalToAnalogConverter {
        public int resolution;
        public double outputVoltageRange; // Output voltage range
    }

    public static class CrystalOscillator {
        public double frequency; // Oscillation frequency in Hz
        public double stability; // Frequency stability in ppm
    }
}