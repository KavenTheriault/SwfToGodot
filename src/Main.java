import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.RECT;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    static int ZOOM = 4;
    static String SWF_FILE_PATH = "C:\\Users\\ZoidQC\\Downloads\\sloche-res\\client\\sloche2007.swf";
    static String RESOURCE_FOLDER_PATH = "C:\\Users\\ZoidQC\\Documents\\projects\\ExportTests";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);

            int[] spriteIds = {860, 883, 865, 828, 843, 880, 818, 833, 855, 870, 813, 838, 888, 808, 875, 848, 823, 893, 904, 911};

            for (int spriteId : spriteIds) {
                generateSpriteScene(swf, spriteId);
            }

            System.out.println("OK");
        } catch (SwfOpenException ex) {
            System.out.println("ERROR: Invalid SWF file");
        } catch (IOException ex) {
            System.out.println("ERROR: Error during SWF opening");
        } catch (InterruptedException ex) {
            System.out.println("ERROR: Parsing interrupted");
        } catch (Exception ex) {
            System.out.println("ERROR:" + ex);
        }
    }

    static void generateSpriteScene(SWF swf, int spriteId) throws Exception {
        var foundSprite = TagUtils.getSprite(swf, spriteId);

        String spriteFolderPath = String.format("%s\\%s", RESOURCE_FOLDER_PATH, spriteId);
        File folder = new File(spriteFolderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        var exportImageResults = exportSpritePlaceObjects(foundSprite, spriteFolderPath);
        ArrayList<GodotSprite> sceneSprites = new ArrayList<>();

        for (ExportImageResult exportImageResult : exportImageResults) {
            var translation = getTranslationNeededInGodot(exportImageResult.getExportRect());

            GodotSprite godotSprite = new GodotSprite(
                    String.format("%s", exportImageResult.getCharacterId()),
                    String.format("res://%s/%s", spriteId, exportImageResult.getFileName()),
                    translation,
                    0
            );
            sceneSprites.add(godotSprite);
        }

        var godotFileWriter = new GodotFileWriter();
        godotFileWriter.writeScene(String.format("%s\\%s.tscn", spriteFolderPath, spriteId), sceneSprites);
    }

    static ArrayList<ExportImageResult> exportSpritePlaceObjects(DefineSpriteTag sprite, String folderPath) throws Exception {
        var exportImageResults = new ArrayList<ExportImageResult>();

        for (Tag spriteChildTag : sprite.getTags()) {
            if (spriteChildTag instanceof PlaceObject2Tag placeObject) {
                var fileName = String.format("%s.png", placeObject.getCharacterId());
                var childImagePath = String.format("%s\\%s", folderPath, fileName);
                var exportRect = exportSpritePlaceObject(sprite.getCharacterId(), placeObject.getCharacterId(), childImagePath);
                exportImageResults.add(new ExportImageResult(placeObject.getCharacterId(), exportRect, fileName));
            }
        }

        return exportImageResults;
    }

    static RECT exportSpritePlaceObject(int spriteId, int placeObjectId, String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);
            var sprite = TagUtils.getSprite(swf, spriteId);

            ArrayList<Tag> tagsToRemove = new ArrayList<>();
            for (Tag spriteChildTag : sprite.getTags()) {
                if (spriteChildTag instanceof PlaceObject2Tag placeObject) {
                    if (placeObject.getCharacterId() != placeObjectId) tagsToRemove.add(placeObject);
                }
            }

            swf.removeTags(tagsToRemove, false, null);
            swf.computeDependentCharacters();
            swf.computeDependentFrames();

            Timeline timeline = sprite.getTimeline();
            BufferedImage bufferedImage = SWF.frameToImageGet(timeline, 0, 0, null, 0, timeline.displayRect, new Matrix(), null, null, ZOOM, true).getBufferedImage();

            File file = new File(filePath);
            ImageHelper.write(bufferedImage, ImageFormat.PNG, file);

            return timeline.displayRect;
        }
    }

    static Point2D.Double getTranslationNeededInGodot(RECT childRect) {
        var childRectDestination = new ExportRectangle(childRect);
        var centerChildTranslation = GraphUtils.getCenteringTranslation(childRect);
        var childRectOrigin = centerChildTranslation.transform(childRectDestination);
        var resultTranslation = GraphUtils.getTranslation(new Point2D.Double(childRectDestination.xMin, childRectDestination.yMin), new Point2D.Double(childRectOrigin.xMin, childRectOrigin.yMin));

        return new Point2D.Double(GraphUtils.twipToPixel(resultTranslation.translateX * ZOOM), GraphUtils.twipToPixel(resultTranslation.translateY * ZOOM));
    }
}