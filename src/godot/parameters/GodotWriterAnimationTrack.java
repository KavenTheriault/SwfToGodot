package godot.parameters;

import java.awt.geom.Point2D;

public class GodotWriterAnimationTrack {
    private String targetName;
    private String targetProperty;
    private Point2D.Double[] values;

    public GodotWriterAnimationTrack(String targetName, String targetProperty, Point2D.Double[] values) {
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

    public Point2D.Double[] getValues() {
        return values;
    }
}
