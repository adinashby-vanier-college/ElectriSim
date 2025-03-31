package controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Arrays;

/**
 * Base class for all electrical components containing all possible variables
 * used by any component in the system.
 */
public class ComponentBase {

    // Type of Component
    public String componentType;

    // Common electrical properties
    public double voltage;
    public double current;
    public double resistance;
    public double powerRating;
    public double internalResistance;
    public double frequency;

    // Switch/Relay specific
    public boolean isClosed;
    public boolean isPressed;
    public boolean isEnergized;
    public boolean[] switchStates;
    public double coilVoltage;

    // Potentiometer/Rheostat specific
    public double wiperPosition;

    // Capacitor specific
    public double capacitance;
    public double voltageRating;
    public boolean isPolarityRespected;

    // Inductor specific
    public double inductance;
    public double corePermeability;

    // Variable component specific
    public double rotationAngle;

    // Diode specific
    public double forwardVoltage;
    public double reverseBreakdownVoltage;
    public double zenerVoltage;
    public double peakVoltage;
    public double valleyVoltage;
    public double darkCurrent;
    public double lightCurrent;

    // LED specific
    public double wavelength;

    // Transistor specific
    public double currentGain;
    public double maxCollectorCurrent;
    public double maxCollectorEmitterVoltage;
    public double pinchOffVoltage;
    public double maxDrainSourceVoltage;
    public double thresholdVoltage;

    // Power source specific
    public double controlSignal;
    public double capacity;

    // Meter specific
    public double range;

    // Logic gate specific
    public double propagationDelay;
    public double setupTime;
    public double holdTime;

    // Antenna specific
    public double frequencyRange;
    public double gain;

    // Motor specific
    public double ratedVoltage;
    public double ratedCurrent;
    public double speed;
    public double torque;

    // Transformer specific
    public double primaryVoltage;
    public double secondaryVoltage;
    public double turnsRatio;

    // Fuse specific
    public double ratedCurrentFuse;
    public double breakingCapacity;

    // Optocoupler specific
    public double currentTransferRatio;
    public double forwardCurrent;
    public double isolationVoltage;

    // Audio specific
    public double impedance;
    public double sensitivity;
    public double frequencyResponse;

    // Op-amp specific
    public double gainBandwidthProduct;
    public double slewRate;

    // Schmitt trigger specific
    public double upperThreshold;
    public double lowerThreshold;

    // Converter specific
    public int resolution;
    public double samplingRate;
    public double outputVoltageRange;

    // Crystal oscillator specific
    public double stability;

    // Thermistor/Photoresistor specific
    public double temperatureCoefficient;
    public double lightIntensity;

    // Varactor specific
    public double reverseVoltage;

    // Visual properties (from ImageComponent)
    @JsonIgnore
    public Image image;
    public double x, y;
    public double width, height;
    public double rotation;
    public double startX, startY, endX, endY;
    @JsonIgnore
    public Circle startCircle, endCircle;
    public String imageURL;
    @JsonIgnore
    public VBox parameterControls;

    // Wire specific
    public boolean selected;

    // Getter and Setters

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public double getPowerRating() {
        return powerRating;
    }

    public void setPowerRating(double powerRating) {
        this.powerRating = powerRating;
    }

    public double getInternalResistance() {
        return internalResistance;
    }

    public void setInternalResistance(double internalResistance) {
        this.internalResistance = internalResistance;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean pressed) {
        isPressed = pressed;
    }

    public boolean isEnergized() {
        return isEnergized;
    }

    public void setEnergized(boolean energized) {
        isEnergized = energized;
    }

    public boolean[] getSwitchStates() {
        return switchStates;
    }

    public void setSwitchStates(boolean[] switchStates) {
        this.switchStates = switchStates;
    }

    public double getCoilVoltage() {
        return coilVoltage;
    }

    public void setCoilVoltage(double coilVoltage) {
        this.coilVoltage = coilVoltage;
    }

    public double getWiperPosition() {
        return wiperPosition;
    }

    public void setWiperPosition(double wiperPosition) {
        this.wiperPosition = wiperPosition;
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
    }

    public double getVoltageRating() {
        return voltageRating;
    }

    public void setVoltageRating(double voltageRating) {
        this.voltageRating = voltageRating;
    }

    public boolean isPolarityRespected() {
        return isPolarityRespected;
    }

    public void setPolarityRespected(boolean polarityRespected) {
        isPolarityRespected = polarityRespected;
    }

    public double getInductance() {
        return inductance;
    }

    public void setInductance(double inductance) {
        this.inductance = inductance;
    }

    public double getCorePermeability() {
        return corePermeability;
    }

    public void setCorePermeability(double corePermeability) {
        this.corePermeability = corePermeability;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public double getForwardVoltage() {
        return forwardVoltage;
    }

    public void setForwardVoltage(double forwardVoltage) {
        this.forwardVoltage = forwardVoltage;
    }

    public double getReverseBreakdownVoltage() {
        return reverseBreakdownVoltage;
    }

    public void setReverseBreakdownVoltage(double reverseBreakdownVoltage) {
        this.reverseBreakdownVoltage = reverseBreakdownVoltage;
    }

    public double getZenerVoltage() {
        return zenerVoltage;
    }

    public void setZenerVoltage(double zenerVoltage) {
        this.zenerVoltage = zenerVoltage;
    }

    public double getPeakVoltage() {
        return peakVoltage;
    }

    public void setPeakVoltage(double peakVoltage) {
        this.peakVoltage = peakVoltage;
    }

    public double getValleyVoltage() {
        return valleyVoltage;
    }

    public void setValleyVoltage(double valleyVoltage) {
        this.valleyVoltage = valleyVoltage;
    }

    public double getDarkCurrent() {
        return darkCurrent;
    }

    public void setDarkCurrent(double darkCurrent) {
        this.darkCurrent = darkCurrent;
    }

    public double getLightCurrent() {
        return lightCurrent;
    }

    public void setLightCurrent(double lightCurrent) {
        this.lightCurrent = lightCurrent;
    }

    public double getWavelength() {
        return wavelength;
    }

    public void setWavelength(double wavelength) {
        this.wavelength = wavelength;
    }

    public double getCurrentGain() {
        return currentGain;
    }

    public void setCurrentGain(double currentGain) {
        this.currentGain = currentGain;
    }

    public double getMaxCollectorCurrent() {
        return maxCollectorCurrent;
    }

    public void setMaxCollectorCurrent(double maxCollectorCurrent) {
        this.maxCollectorCurrent = maxCollectorCurrent;
    }

    public double getMaxCollectorEmitterVoltage() {
        return maxCollectorEmitterVoltage;
    }

    public void setMaxCollectorEmitterVoltage(double maxCollectorEmitterVoltage) {
        this.maxCollectorEmitterVoltage = maxCollectorEmitterVoltage;
    }

    public double getPinchOffVoltage() {
        return pinchOffVoltage;
    }

    public void setPinchOffVoltage(double pinchOffVoltage) {
        this.pinchOffVoltage = pinchOffVoltage;
    }

    public double getMaxDrainSourceVoltage() {
        return maxDrainSourceVoltage;
    }

    public void setMaxDrainSourceVoltage(double maxDrainSourceVoltage) {
        this.maxDrainSourceVoltage = maxDrainSourceVoltage;
    }

    public double getThresholdVoltage() {
        return thresholdVoltage;
    }

    public void setThresholdVoltage(double thresholdVoltage) {
        this.thresholdVoltage = thresholdVoltage;
    }

    public double getControlSignal() {
        return controlSignal;
    }

    public void setControlSignal(double controlSignal) {
        this.controlSignal = controlSignal;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public double getPropagationDelay() {
        return propagationDelay;
    }

    public void setPropagationDelay(double propagationDelay) {
        this.propagationDelay = propagationDelay;
    }

    public double getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(double setupTime) {
        this.setupTime = setupTime;
    }

    public double getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(double holdTime) {
        this.holdTime = holdTime;
    }

    public double getFrequencyRange() {
        return frequencyRange;
    }

    public void setFrequencyRange(double frequencyRange) {
        this.frequencyRange = frequencyRange;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public double getRatedVoltage() {
        return ratedVoltage;
    }

    public void setRatedVoltage(double ratedVoltage) {
        this.ratedVoltage = ratedVoltage;
    }

    public double getRatedCurrent() {
        return ratedCurrent;
    }

    public void setRatedCurrent(double ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getTorque() {
        return torque;
    }

    public void setTorque(double torque) {
        this.torque = torque;
    }

    public double getPrimaryVoltage() {
        return primaryVoltage;
    }

    public void setPrimaryVoltage(double primaryVoltage) {
        this.primaryVoltage = primaryVoltage;
    }

    public double getSecondaryVoltage() {
        return secondaryVoltage;
    }

    public void setSecondaryVoltage(double secondaryVoltage) {
        this.secondaryVoltage = secondaryVoltage;
    }

    public double getTurnsRatio() {
        return turnsRatio;
    }

    public void setTurnsRatio(double turnsRatio) {
        this.turnsRatio = turnsRatio;
    }

    public double getRatedCurrentFuse() {
        return ratedCurrentFuse;
    }

    public void setRatedCurrentFuse(double ratedCurrentFuse) {
        this.ratedCurrentFuse = ratedCurrentFuse;
    }

    public double getBreakingCapacity() {
        return breakingCapacity;
    }

    public void setBreakingCapacity(double breakingCapacity) {
        this.breakingCapacity = breakingCapacity;
    }

    public double getCurrentTransferRatio() {
        return currentTransferRatio;
    }

    public void setCurrentTransferRatio(double currentTransferRatio) {
        this.currentTransferRatio = currentTransferRatio;
    }

    public double getForwardCurrent() {
        return forwardCurrent;
    }

    public void setForwardCurrent(double forwardCurrent) {
        this.forwardCurrent = forwardCurrent;
    }

    public double getIsolationVoltage() {
        return isolationVoltage;
    }

    public void setIsolationVoltage(double isolationVoltage) {
        this.isolationVoltage = isolationVoltage;
    }

    public double getImpedance() {
        return impedance;
    }

    public void setImpedance(double impedance) {
        this.impedance = impedance;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public double getFrequencyResponse() {
        return frequencyResponse;
    }

    public void setFrequencyResponse(double frequencyResponse) {
        this.frequencyResponse = frequencyResponse;
    }

    public double getGainBandwidthProduct() {
        return gainBandwidthProduct;
    }

    public void setGainBandwidthProduct(double gainBandwidthProduct) {
        this.gainBandwidthProduct = gainBandwidthProduct;
    }

    public double getSlewRate() {
        return slewRate;
    }

    public void setSlewRate(double slewRate) {
        this.slewRate = slewRate;
    }

    public double getUpperThreshold() {
        return upperThreshold;
    }

    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public double getLowerThreshold() {
        return lowerThreshold;
    }

    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    public double getOutputVoltageRange() {
        return outputVoltageRange;
    }

    public void setOutputVoltageRange(double outputVoltageRange) {
        this.outputVoltageRange = outputVoltageRange;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    public double getTemperatureCoefficient() {
        return temperatureCoefficient;
    }

    public void setTemperatureCoefficient(double temperatureCoefficient) {
        this.temperatureCoefficient = temperatureCoefficient;
    }

    public double getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(double lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public double getReverseVoltage() {
        return reverseVoltage;
    }

    public void setReverseVoltage(double reverseVoltage) {
        this.reverseVoltage = reverseVoltage;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public Circle getStartCircle() {
        return startCircle;
    }

    public void setStartCircle(Circle startCircle) {
        this.startCircle = startCircle;
    }

    public Circle getEndCircle() {
        return endCircle;
    }

    public void setEndCircle(Circle endCircle) {
        this.endCircle = endCircle;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public VBox getParameterControls() {
        return parameterControls;
    }

    public void setParameterControls(VBox parameterControls) {
        this.parameterControls = parameterControls;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // toString


    @Override
    public String toString() {
        return "ComponentBase{" +
                "voltage=" + voltage +
                ", current=" + current +
                ", resistance=" + resistance +
                ", powerRating=" + powerRating +
                ", internalResistance=" + internalResistance +
                ", frequency=" + frequency +
                ", isClosed=" + isClosed +
                ", isPressed=" + isPressed +
                ", isEnergized=" + isEnergized +
                ", switchStates=" + Arrays.toString(switchStates) +
                ", coilVoltage=" + coilVoltage +
                ", wiperPosition=" + wiperPosition +
                ", capacitance=" + capacitance +
                ", voltageRating=" + voltageRating +
                ", isPolarityRespected=" + isPolarityRespected +
                ", inductance=" + inductance +
                ", corePermeability=" + corePermeability +
                ", rotationAngle=" + rotationAngle +
                ", forwardVoltage=" + forwardVoltage +
                ", reverseBreakdownVoltage=" + reverseBreakdownVoltage +
                ", zenerVoltage=" + zenerVoltage +
                ", peakVoltage=" + peakVoltage +
                ", valleyVoltage=" + valleyVoltage +
                ", darkCurrent=" + darkCurrent +
                ", lightCurrent=" + lightCurrent +
                ", wavelength=" + wavelength +
                ", currentGain=" + currentGain +
                ", maxCollectorCurrent=" + maxCollectorCurrent +
                ", maxCollectorEmitterVoltage=" + maxCollectorEmitterVoltage +
                ", pinchOffVoltage=" + pinchOffVoltage +
                ", maxDrainSourceVoltage=" + maxDrainSourceVoltage +
                ", thresholdVoltage=" + thresholdVoltage +
                ", controlSignal=" + controlSignal +
                ", capacity=" + capacity +
                ", range=" + range +
                ", propagationDelay=" + propagationDelay +
                ", setupTime=" + setupTime +
                ", holdTime=" + holdTime +
                ", frequencyRange=" + frequencyRange +
                ", gain=" + gain +
                ", ratedVoltage=" + ratedVoltage +
                ", ratedCurrent=" + ratedCurrent +
                ", speed=" + speed +
                ", torque=" + torque +
                ", primaryVoltage=" + primaryVoltage +
                ", secondaryVoltage=" + secondaryVoltage +
                ", turnsRatio=" + turnsRatio +
                ", ratedCurrentFuse=" + ratedCurrentFuse +
                ", breakingCapacity=" + breakingCapacity +
                ", currentTransferRatio=" + currentTransferRatio +
                ", forwardCurrent=" + forwardCurrent +
                ", isolationVoltage=" + isolationVoltage +
                ", impedance=" + impedance +
                ", sensitivity=" + sensitivity +
                ", frequencyResponse=" + frequencyResponse +
                ", gainBandwidthProduct=" + gainBandwidthProduct +
                ", slewRate=" + slewRate +
                ", upperThreshold=" + upperThreshold +
                ", lowerThreshold=" + lowerThreshold +
                ", resolution=" + resolution +
                ", samplingRate=" + samplingRate +
                ", outputVoltageRange=" + outputVoltageRange +
                ", stability=" + stability +
                ", temperatureCoefficient=" + temperatureCoefficient +
                ", lightIntensity=" + lightIntensity +
                ", reverseVoltage=" + reverseVoltage +
                ", image=" + image +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                ", startX=" + startX +
                ", startY=" + startY +
                ", endX=" + endX +
                ", endY=" + endY +
                ", startCircle=" + startCircle +
                ", endCircle=" + endCircle +
                ", componentType='" + componentType + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", parameterControls=" + parameterControls +
                ", selected=" + selected +
                '}';
    }

    // Default constructor
    public ComponentBase() {
        // Initialize all numeric values to 0
        voltage = 0;
        current = 0;
        resistance = 0;
        powerRating = 0;
        internalResistance = 0;
        frequency = 0;
        coilVoltage = 0;
        wiperPosition = 0;
        capacitance = 0;
        voltageRating = 0;
        inductance = 0;
        corePermeability = 0;
        rotationAngle = 0;
        forwardVoltage = 0;
        reverseBreakdownVoltage = 0;
        zenerVoltage = 0;
        peakVoltage = 0;
        valleyVoltage = 0;
        darkCurrent = 0;
        lightCurrent = 0;
        wavelength = 0;
        currentGain = 0;
        maxCollectorCurrent = 0;
        maxCollectorEmitterVoltage = 0;
        pinchOffVoltage = 0;
        maxDrainSourceVoltage = 0;
        thresholdVoltage = 0;
        controlSignal = 0;
        capacity = 0;
        range = 0;
        propagationDelay = 0;
        setupTime = 0;
        holdTime = 0;
        frequencyRange = 0;
        gain = 0;
        ratedVoltage = 0;
        ratedCurrent = 0;
        speed = 0;
        torque = 0;
        primaryVoltage = 0;
        secondaryVoltage = 0;
        turnsRatio = 0;
        ratedCurrentFuse = 0;
        breakingCapacity = 0;
        currentTransferRatio = 0;
        forwardCurrent = 0;
        isolationVoltage = 0;
        impedance = 0;
        sensitivity = 0;
        frequencyResponse = 0;
        gainBandwidthProduct = 0;
        slewRate = 0;
        upperThreshold = 0;
        lowerThreshold = 0;
        samplingRate = 0;
        outputVoltageRange = 0;
        stability = 0;
        temperatureCoefficient = 0;
        lightIntensity = 0;
        reverseVoltage = 0;

        // Initialize boolean values
        isClosed = false;
        isPressed = false;
        isEnergized = false;
        isPolarityRespected = false;
        selected = false;

        // Initialize resolution to 0
        resolution = 0;
    }
}
