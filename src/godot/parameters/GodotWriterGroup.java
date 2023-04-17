package godot.parameters;

import java.util.ArrayList;

public class GodotWriterGroup extends GodotWriterNode {
    private ArrayList<GodotWriterNode> nodes;

    public GodotWriterGroup(String name) {
        super(name);
        nodes = new ArrayList<>();
    }

    public ArrayList<GodotWriterNode> getNodes() {
        return nodes;
    }
}
