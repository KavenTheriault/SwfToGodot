package godot.parameters;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GodotWriterGroup extends GodotWriterNode {
    private ArrayList<GodotWriterNode> nodes;

    public GodotWriterGroup(String name, Point2D.Double position) {
        super(name, position);
        nodes = new ArrayList<>();
    }

    public ArrayList<GodotWriterNode> getNodes() {
        return nodes;
    }
}
