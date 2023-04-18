package godot.parameters;

import java.awt.geom.Point2D;

public class GodotWriterSprite extends GodotWriterNode {
    private String resourcePath;
    private ShaderOption shaderOption;

    public GodotWriterSprite(String name, String resourcePath, Point2D.Double position, ShaderOption shaderOption) {
        super(name, position);
        this.resourcePath = resourcePath;
        this.shaderOption = shaderOption;
    }

    public ShaderOption getShaderOption() {
        return shaderOption;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}

