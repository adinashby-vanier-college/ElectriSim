package controllers;

import javafx.scene.layout.VBox;
import java.util.List;

public abstract class UndoableAction {
    protected List<ComponentsController.Drawable> drawables;
    protected VBox parametersPane;
    protected SimulationController controller;

    public UndoableAction(List<ComponentsController.Drawable> drawables, VBox parametersPane, SimulationController controller) {
        this.drawables = drawables;
        this.parametersPane = parametersPane;
        this.controller = controller;
    }

    public abstract void undo();
    public abstract void redo();
}

class AddComponentAction extends UndoableAction {
    private ComponentsController.Drawable component;

    public AddComponentAction(List<ComponentsController.Drawable> drawables, VBox parametersPane,
                              SimulationController controller, ComponentsController.Drawable component) {
        super(drawables, parametersPane, controller);
        this.component = component;
    }

    @Override
    public void undo() {
        System.out.println("Undoing AddComponentAction");
        drawables.remove(component);
        if (component instanceof ComponentsController.ImageComponent) {
            ComponentsController.ImageComponent imageComponent = (ComponentsController.ImageComponent) component;
            if (imageComponent.parameterControls != null) {
            parametersPane.getChildren().remove(imageComponent.parameterControls);
        }
    }
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }

    @Override
    public void redo() {
        System.out.println("Redoing AddComponentAction");
        drawables.add(component);
        if (component instanceof ComponentsController.ImageComponent) {
            ComponentsController.ImageComponent imageComponent = (ComponentsController.ImageComponent) component;
            // Recreate parameter controls if they don't exist
            if (imageComponent.parameterControls == null) {
                ComponentsController.generateParameterControls(imageComponent, parametersPane);
            } else {
                parametersPane.getChildren().add(imageComponent.parameterControls);
            }
        }
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }
}

class DeleteComponentAction extends UndoableAction {
    private ComponentsController.Drawable component;
    private int index;

    public DeleteComponentAction(List<ComponentsController.Drawable> drawables, VBox parametersPane,
                                 SimulationController controller, ComponentsController.Drawable component) {
        super(drawables, parametersPane, controller);
        this.component = component;
        this.index = drawables.indexOf(component);
    }

    @Override
    public void undo() {
        System.out.println("Undoing DeleteComponentAction");
        drawables.add(index, component);
        if (component instanceof ComponentsController.ImageComponent) {
            ComponentsController.ImageComponent imageComponent = (ComponentsController.ImageComponent) component;
            parametersPane.getChildren().add(imageComponent.parameterControls);
        }
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }

    @Override
    public void redo() {
        System.out.println("Redoing DeleteComponentAction");
        drawables.remove(component);
        if (component instanceof ComponentsController.ImageComponent) {
            ComponentsController.ImageComponent imageComponent = (ComponentsController.ImageComponent) component;
            parametersPane.getChildren().remove(imageComponent.parameterControls);
        }
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }
}

class MoveComponentAction extends UndoableAction {
    private ComponentsController.ImageComponent component;
    private double oldX, oldY;
    private double newX, newY;

    public MoveComponentAction(List<ComponentsController.Drawable> drawables, VBox parametersPane,
                               SimulationController controller, ComponentsController.ImageComponent component,
                               double oldX, double oldY, double newX, double newY) {
        super(drawables, parametersPane, controller);
        this.component = component;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void undo() {
        System.out.println("Undoing MoveComponentAction");
        component.x = oldX;
        component.y = oldY;
        controller.updateWiresForComponent(component);
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }

    @Override
    public void redo() {
        System.out.println("Redoing MoveComponentAction");
        component.x = newX;
        component.y = newY;
        controller.updateWiresForComponent(component);
        controller.redrawCanvas();
        controller.updateCircuitAnalysis();
    }
}

class ParameterChangeAction extends UndoableAction {
    private ComponentsController.ImageComponent component;
    private String parameterName;
    private Object oldValue;
    private Object newValue;

    public ParameterChangeAction(List<ComponentsController.Drawable> drawables, VBox parametersPane,
                                 SimulationController controller, ComponentsController.ImageComponent component,
                                 String parameterName, Object oldValue, Object newValue) {
        super(drawables, parametersPane, controller);
        this.component = component;
        this.parameterName = parameterName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void undo() {
        System.out.println("Undoing ParameterChangeAction");
        setParameterValue(oldValue);
    }

    @Override
    public void redo() {
        System.out.println("Redoing ParameterChangeAction");
        setParameterValue(newValue);
    }

    private void setParameterValue(Object value) {
        if (component instanceof ComponentsController.SPSTToggleSwitch) {
            ComponentsController.SPSTToggleSwitch switch_ = (ComponentsController.SPSTToggleSwitch) component;
            if (parameterName.equals("state")) {
                switch_.isClosed = (boolean) value;
            }
        } else if (component instanceof ComponentsController.ANDGate) {
            ComponentsController.ANDGate gate = (ComponentsController.ANDGate) component;
            if (parameterName.equals("propagationDelay")) {
                gate.propagationDelay = (double) value;
            }
        } else if (component instanceof ComponentsController.ORGate) {
            ComponentsController.ORGate gate = (ComponentsController.ORGate) component;
            if (parameterName.equals("propagationDelay")) {
                gate.propagationDelay = (double) value;
            }
        } else if (component instanceof ComponentsController.XORGate) {
            ComponentsController.XORGate gate = (ComponentsController.XORGate) component;
            if (parameterName.equals("propagationDelay")) {
                gate.propagationDelay = (double) value;
            }
        }
        controller.updateCircuitAnalysis();
    }
}