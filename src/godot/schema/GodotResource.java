package godot.schema;

public class GodotResource {
    private String type;
    private String uid;
    private String path;
    private String id;

    public GodotResource(String id, String type, String path, String uid) {
        this.type = type;
        this.uid = uid;
        this.path = path;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getUid() {
        return uid;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }
}
