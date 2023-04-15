import java.awt.geom.Point2D;

public class GodotSprite {
    private String name;
    private String resourcePath;
    private Point2D.Double position;
    private ShaderOption shaderOption;

    public GodotSprite(String name, String resourcePath, Point2D.Double position, ShaderOption shaderOption) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
