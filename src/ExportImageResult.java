import com.jpexs.decompiler.flash.types.RECT;

public class ExportImageResult {
    private int characterId;
    private RECT exportRect;
    private String fileName;

    public ExportImageResult(int characterId, RECT exportRect, String fileName) {
        this.characterId = characterId;
        this.exportRect = exportRect;
        this.fileName = fileName;
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
