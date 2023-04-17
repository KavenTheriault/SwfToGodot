package godot.schema;

import java.util.AbstractMap;
import java.util.ArrayList;

public class GodotNode {
    private String name;
    private String type;
    private String parent;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> properties;

    public GodotNode(String name, String type, String parent, ArrayList<AbstractMap.SimpleEntry<String, String>> properties) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getParent() {
        return parent;
    }

    public ArrayList<AbstractMap.SimpleEntry<String, String>> getProperties() {
        return properties;
    }
}
