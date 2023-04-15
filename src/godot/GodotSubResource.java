package godot;

import java.util.AbstractMap;
import java.util.ArrayList;

public class GodotSubResource {
    private String type;
    private String id;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> properties;

    public GodotSubResource(String id, String type, ArrayList<AbstractMap.SimpleEntry<String, String>> properties) {
        this.type = type;
        this.id = id;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public ArrayList<AbstractMap.SimpleEntry<String, String>> getProperties() {
        return properties;
    }
}
