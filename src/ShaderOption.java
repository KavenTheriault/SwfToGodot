public class ShaderOption {
    public ShaderOption(String resourcePath, String parameter) {
        this.resourcePath = resourcePath;
        this.parameter = parameter;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    private String resourcePath;
    private String parameter;
}
