import com.jpexs.decompiler.flash.types.RECT;

public class ExportImageResult {
    private RECT exportRect;
    private String fileName;
    private String name;

    public ExportImageResult(RECT exportRect, String fileName, String name) {
        this.exportRect = exportRect;
        this.fileName = fileName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RECT getExportRect() {
        return exportRect;
    }

    public String getFileName() {
        return fileName;
    }
}
