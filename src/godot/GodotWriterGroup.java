package godot;

import java.util.ArrayList;

public class GodotWriterGroup extends GodotWriterItem {
    private ArrayList<GodotWriterItem> items;

    public GodotWriterGroup(String name) {
        super(name);
        items = new ArrayList<>();
    }

    public ArrayList<GodotWriterItem> getItems() {
        return items;
    }
}
