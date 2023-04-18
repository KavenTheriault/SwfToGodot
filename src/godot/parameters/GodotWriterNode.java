package godot.parameters;

import java.awt.geom.Point2D;

public class GodotWriterNode {
    private String name;

    private Point2D.Double position;

    public GodotWriterNode(String name, Point2D.Double position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public Point2D.Double getPosition() {
        return position;
    }
}
