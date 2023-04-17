package godot.parameters;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GodotWriterAnimationTrack {
    private String targetName;
    private String targetProperty;
    private ArrayList<Point2D.Double> values;

    public GodotWriterAnimationTrack(String targetName, String targetProperty, ArrayList<Point2D.Double> values) {
        this.targetName = targetName;
        this.targetProperty = targetProperty;
        this.values = values;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetProperty() {
        return targetProperty;
    }

    public ArrayList<Point2D.Double> getValues() {
        return values;
    }
}
