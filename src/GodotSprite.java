import java.awt.geom.Point2D;

public class GodotSprite {
    private String name;
    private String resourcePath;
    private Point2D.Double position;
    private int zIndex;

    public GodotSprite(String name, String resourcePath, Point2D.Double position, int zIndex) {
        this.name = name;
        this.resourcePath = resourcePath;
        this.position = position;
        this.zIndex = zIndex;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
