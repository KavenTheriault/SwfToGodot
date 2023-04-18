package godot.parameters;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GodotWriterAnimationTrack {
    private String targetName;
    private String targetProperty;
    private ArrayList<Point2D.Double> vectorValues;
    private ArrayList<Double> doubleValues;

    public GodotWriterAnimationTrack(String targetName, String targetProperty, ArrayList<Point2D.Double> vectorValues, ArrayList<Double> doubleValues) {
        this.targetName = targetName;
        this.targetProperty = targetProperty;
        this.vectorValues = vectorValues;
        this.doubleValues = doubleValues;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetProperty() {
        return targetProperty;
    }

    public ArrayList<Point2D.Double> getVectorValues() {
        return vectorValues;
    }

    public ArrayList<Double> getDoubleValues() {
        return doubleValues;
    }
}
