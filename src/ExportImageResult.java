import com.jpexs.decompiler.flash.types.RECT;

public class ExportImageResult {
    private int characterId;
    private RECT exportRect;
    private String fileName;
    private String name;

    public ExportImageResult(int characterId, RECT exportRect, String fileName, String name) {
        this.characterId = characterId;
        this.exportRect = exportRect;
        this.fileName = fileName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCharacterId() {
        return characterId;
    }

    public RECT getExportRect() {
        return exportRect;
    }

    public String getFileName() {
        return fileName;
    }
}
