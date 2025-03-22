package controllers;

/**
 * This class contains nested classes for each electrical component in the FXML file.
 * Each component is represented as a separate class with relevant electrical properties.
 */
public class ComponentsController {

    // Switch and Relay Components
    public static class SPSTToggleSwitch {
        private boolean isClosed; // State of the switch (open/closed)
        private double maxVoltage; // Maximum voltage the switch can handle
        private double maxCurrent; // Maximum current the switch can handle
    }

    public static class SPDTToggleSwitch {
        private boolean isClosed; // State of the switch (open/closed)
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class PushbuttonSwitchNO {
        private boolean isPressed; // State of the pushbutton (pressed/released)
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class PushbuttonSwitchNC {
        private boolean isPressed; // State of the pushbutton (pressed/released)
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class DIPSwitch {
        private boolean[] switchStates; // Array of states for each switch in the DIP
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class SPSTRelay {
        private boolean isEnergized; // State of the relay coil (energized/de-energized)
        private double coilVoltage; // Voltage required to energize the coil
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class SPDTRelay {
        private boolean isEnergized;
        private double coilVoltage;
        private double maxVoltage;
        private double maxCurrent;
    }

    public static class Jumper {
        private double resistance; // Resistance of the jumper (typically very low)
    }

    public static class SolderBridge {
        private double resistance; // Resistance of the solder bridge (typically very low)
    }

    // Ground Components
    public static class EarthGround {
        private double resistance; // Resistance to earth (typically very low)
    }

    public static class ChassisGround {
        private double resistance; // Resistance to chassis (typically very low)
    }

    public static class DigitalGround {
        private double resistance; // Resistance to digital ground (typically very low)
    }

    // Resistor Components
    public static class ResistorIEEE {
        private double resistance; // Resistance value in ohms
        private double powerRating; // Maximum power the resistor can handle
    }

    public static class ResistorIEC {
        private double resistance;
        private double powerRating;
    }

    public static class PotentiometerIEEE {
        private double resistance;
        private double powerRating;
        private double wiperPosition; // Position of the wiper (0 to 1)
    }

    public static class PotentiometerIEC {
        private double resistance;
        private double powerRating;
        private double wiperPosition;
    }

    public static class RheostatIEEE {
        private double resistance;
        private double powerRating;
        private double wiperPosition;
    }

    public static class RheostatIEC {
        private double resistance;
        private double powerRating;
        private double wiperPosition;
    }

    public static class Thermistor {
        private double resistance; // Resistance at a given temperature
        private double temperatureCoefficient; // Temperature coefficient of resistance
    }

    public static class Photoresistor {
        private double resistance; // Resistance under current light conditions
        private double lightIntensity; // Light intensity affecting resistance
    }

    // Capacitor Components
    public static class Capacitor {
        private double capacitance; // Capacitance in farads
        private double voltageRating; // Maximum voltage the capacitor can handle
    }

    public static class PolarizedCapacitor {
        private double capacitance;
        private double voltageRating;
        private boolean isPolarityRespected; // Whether polarity is respected
    }

    public static class VariableCapacitor {
        private double capacitance;
        private double voltageRating;
        private double rotationAngle; // Angle of rotation (0 to 360 degrees)
    }

    // Inductor and Coil Components
    public static class Inductor {
        private double inductance; // Inductance in henries
        private double currentRating; // Maximum current the inductor can handle
    }

    public static class IronCoreInductor {
        private double inductance;
        private double currentRating;
        private double corePermeability; // Permeability of the iron core
    }

    public static class VariableInductor {
        private double inductance;
        private double currentRating;
        private double rotationAngle; // Angle of rotation (0 to 360 degrees)
    }

    // Power Supply Components
    public static class VoltageSource {
        private double voltage; // Output voltage
        private double internalResistance; // Internal resistance of the source
    }

    public static class CurrentSource {
        private double current; // Output current
        private double internalResistance;
    }

    public static class Generator {
        private double voltage;
        private double frequency; // Frequency of the generated AC signal
        private double internalResistance;
    }

    public static class BatteryCell {
        private double voltage; // Nominal voltage of the cell
        private double internalResistance;
        private double capacity; // Capacity in ampere-hours (Ah)
    }

    public static class Battery {
        private double voltage; // Total voltage of the battery
        private double internalResistance;
        private double capacity;
    }

    public static class ControlledVoltageSource {
        private double voltage;
        private double controlSignal; // Signal controlling the output voltage
    }

    public static class ControlledCurrentSource {
        private double current;
        private double controlSignal; // Signal controlling the output current
    }

    // Meter Components
    public static class Voltmeter {
        private double range; // Maximum voltage the voltmeter can measure
        private double internalResistance;
    }

    public static class Ammeter {
        private double range; // Maximum current the ammeter can measure
        private double internalResistance;
    }

    public static class Ohmmeter {
        private double range; // Maximum resistance the ohmmeter can measure
    }

    public static class Wattmeter {
        private double voltageRange;
        private double currentRange;
    }

    // Diode and LED Components
    public static class Diode {
        private double forwardVoltage; // Forward voltage drop
        private double reverseBreakdownVoltage; // Reverse breakdown voltage
    }

    public static class ZenerDiode {
        private double zenerVoltage; // Zener breakdown voltage
        private double forwardVoltage;
    }

    public static class SchottkyDiode {
        private double forwardVoltage;
        private double reverseBreakdownVoltage;
    }

    public static class Varactor {
        private double capacitance; // Capacitance at a given reverse voltage
        private double reverseVoltage; // Applied reverse voltage
    }

    public static class TunnelDiode {
        private double peakVoltage; // Peak voltage in the I-V curve
        private double valleyVoltage; // Valley voltage in the I-V curve
    }

    public static class LightEmittingDiode {
        private double forwardVoltage;
        private double wavelength; // Wavelength of emitted light
    }

    public static class Photodiode {
        private double darkCurrent; // Current in the absence of light
        private double lightCurrent; // Current under illumination
    }

    // Transistor Components
    public static class NPNBipolarTransistor {
        private double currentGain; // Current gain (beta)
        private double maxCollectorCurrent;
        private double maxCollectorEmitterVoltage;
    }

    public static class PNPBipolarTransistor {
        private double currentGain;
        private double maxCollectorCurrent;
        private double maxCollectorEmitterVoltage;
    }

    public static class DarlingtonTransistor {
        private double currentGain;
        private double maxCollectorCurrent;
        private double maxCollectorEmitterVoltage;
    }

    public static class JFETNTransistor {
        private double pinchOffVoltage; // Pinch-off voltage
        private double maxDrainSourceVoltage;
    }

    public static class JFETPTransistor {
        private double pinchOffVoltage;
        private double maxDrainSourceVoltage;
    }

    public static class NMOSTransistor {
        private double thresholdVoltage; // Threshold voltage
        private double maxDrainSourceVoltage;
    }

    public static class PMOSTransistor {
        private double thresholdVoltage;
        private double maxDrainSourceVoltage;
    }

    // Logic Gate Components
    public static class NOTGate {
        private double propagationDelay; // Propagation delay in nanoseconds
    }

    public static class ANDGate {
        private double propagationDelay;
    }

    public static class NANDGate {
        private double propagationDelay;
    }

    public static class ORGate {
        private double propagationDelay;
    }

    public static class NORGate {
        private double propagationDelay;
    }

    public static class XORGate {
        private double propagationDelay;
    }

    public static class DFlipFlop {
        private double setupTime; // Setup time in nanoseconds
        private double holdTime; // Hold time in nanoseconds
    }

    public static class Multiplexer2to1 {
        private double propagationDelay;
    }

    public static class Multiplexer4to1 {
        private double propagationDelay;
    }

    public static class Demultiplexer1to4 {
        private double propagationDelay;
    }

    // Antenna Components
    public static class Antenna {
        private double frequencyRange; // Frequency range the antenna operates in
        private double gain; // Gain in dBi
    }

    public static class DipoleAntenna {
        private double frequencyRange;
        private double gain;
    }

    // Miscellaneous Components
    public static class Motor {
        private double ratedVoltage; // Rated operating voltage
        private double ratedCurrent; // Rated operating current
    }

    public static class Transformer {
        private double primaryVoltage; // Primary winding voltage
        private double secondaryVoltage; // Secondary winding voltage
        private double turnsRatio; // Turns ratio (N1/N2)
    }

    public static class Fuse {
        private double ratedCurrent; // Current at which the fuse blows
        private double breakingCapacity; // Maximum current the fuse can interrupt
    }

    public static class Optocoupler {
        private double currentTransferRatio; // Ratio of output current to input current
    }

    public static class Loudspeaker {
        private double impedance; // Impedance in ohms
        private double powerRating; // Maximum power the speaker can handle
    }

    public static class Microphone {
        private double sensitivity; // Sensitivity in dB
        private double frequencyResponse; // Frequency response range
    }

    public static class OperationalAmplifier {
        private double gainBandwidthProduct; // Gain-bandwidth product in Hz
        private double slewRate; // Slew rate in V/µs
    }

    public static class SchmittTrigger {
        private double upperThreshold; // Upper threshold voltage
        private double lowerThreshold; // Lower threshold voltage
    }

    public static class AnalogToDigitalConverter {
        private int resolution; // Resolution in bits
        private double samplingRate; // Sampling rate in samples per second
    }

    public static class DigitalToAnalogConverter {
        private int resolution;
        private double outputVoltageRange; // Output voltage range
    }

    public static class CrystalOscillator {
        private double frequency; // Oscillation frequency in Hz
        private double stability; // Frequency stability in ppm
    }
}