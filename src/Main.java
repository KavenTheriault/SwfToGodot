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

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);

            var foundSprite = TagUtils.getSprite(swf, 855);

            var childrenRect = exportSpritePlaceObjects(foundSprite);
            var translations = getTranslationNeededInGodot(foundSprite.getRect(), childrenRect);
            System.out.println("Translations: " + translations);

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



    static ArrayList<RECT> exportSpritePlaceObjects(DefineSpriteTag sprite) throws Exception {
        var childrenRect = new ArrayList<RECT>();

        for (Tag spriteChildTag : sprite.getTags()) {
            if (spriteChildTag instanceof PlaceObject2Tag placeObject) {
                var exportRect = exportSpritePlaceObject(sprite.getCharacterId(), placeObject.getCharacterId());
                childrenRect.add(exportRect);
            }
        }

        return childrenRect;
    }

    static RECT exportSpritePlaceObject(int spriteId, int placeObjectId) throws Exception {
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
            saveImageFile(bufferedImage, placeObjectId);

            return timeline.displayRect;
        }
    }
    static ArrayList<Point2D.Double> getTranslationNeededInGodot(RECT parentRect, ArrayList<RECT> childrenRect) {
        var centerSpriteTranslation = GraphUtils.getCenteringTranslation(parentRect);

        var translationPoints = new ArrayList<Point2D.Double>();
        for (RECT childRect : childrenRect) {
            var childRectDestination = centerSpriteTranslation.transform(new ExportRectangle(childRect));
            var centerChildTranslation = GraphUtils.getCenteringTranslation(childRect);
            var childRectOrigin = centerChildTranslation.transform(new ExportRectangle(childRect));
            var resultTranslation = GraphUtils.getTranslation(new Point2D.Double(childRectDestination.xMin, childRectDestination.yMin), new Point2D.Double(childRectOrigin.xMin, childRectOrigin.yMin));
            var resultTranslationInPxAndZoomed = new Point2D.Double(GraphUtils.twipToPixel(resultTranslation.translateX * ZOOM), GraphUtils.twipToPixel(resultTranslation.translateY * ZOOM));

            translationPoints.add(resultTranslationInPxAndZoomed);
        }

        return translationPoints;
    }

    static void saveImageFile(BufferedImage bufferedImage, int id) throws IOException {
        File file = new File(String.format("C:\\Users\\ZoidQC\\Documents\\sloche exports\\%s.png", id));
        ImageHelper.write(bufferedImage, ImageFormat.PNG, file);
    }
}