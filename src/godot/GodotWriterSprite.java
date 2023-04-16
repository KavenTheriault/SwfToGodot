package godot;

import java.awt.geom.Point2D;

public class GodotWriterSprite extends GodotWriterItem {
    private String resourcePath;
    private Point2D.Double position;
    private ShaderOption shaderOption;

    public GodotWriterSprite(String name, String resourcePath, Point2D.Double position, ShaderOption shaderOption) {
        super(name);
        this.resourcePath = resourcePath;
        this.position = position;
        this.shaderOption = shaderOption;
    }

    public ShaderOption getShaderOption() {
        return shaderOption;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}

