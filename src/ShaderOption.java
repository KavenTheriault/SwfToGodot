import java.util.AbstractMap;

public class ShaderOption {
    public ShaderOption(String resourcePath, AbstractMap.SimpleEntry<String, String> parameter) {
        this.resourcePath = resourcePath;
        this.parameter = parameter;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public AbstractMap.SimpleEntry<String, String> getParameter() {
        return parameter;
    }

    private String resourcePath;
    private AbstractMap.SimpleEntry<String, String> parameter;
}
