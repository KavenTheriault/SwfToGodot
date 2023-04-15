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
import types.FindPlaceObjectResult;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    static int ZOOM = 4;
    static String SWF_FILE_PATH = "C:\\Users\\ZoidQC\\Downloads\\sloche-res\\client\\sloche2007.swf";
    static String RESOURCE_FOLDER_PATH = "C:\\Users\\ZoidQC\\Documents\\projects\\ExportTests";
    static String COLOR_SHADER_RESOURCE_PATH = "res://shaders/change_color.gdshader";
    static AbstractMap.SimpleEntry<String, String> COLOR_SHADER_PARAMETER = new AbstractMap.SimpleEntry<>("shader_parameter/color", "Vector4(0.3, 0.79, 0.94, 1)");

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);

//            int[] headSpriteIds = {860, 883, 865, 828, 843, 880, 818, 833, 855, 870, 813, 838, 888, 808, 875, 848, 823, 893, 904, 911};
//            int[] bodySpriteIds = {605, 621, 631, 650, 673, 627, 642, 658, 677, 615, 666, 609, 648, 662, 668, 654, 635, 681, 900, 907};
//            int[] legIds = {710};
            int[] pantsIds = {715};

//            for (int headSpriteId : headSpriteIds) {
//                generateSpriteScene(swf, headSpriteId, "heads", "hair");
//            }
//
//            for (int bodySpriteId : bodySpriteIds) {
//                generateSpriteScene(swf, bodySpriteId, "bodies", "shirt");
//            }

//            for (int legId : legIds) {
//                generateSpriteScene(swf, legId, "legs", "pant");
//            }

            for (int pantsId : pantsIds) {
                generateSpriteScene(swf, pantsId, "pants", "pant");
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

    static void generateSpriteScene(SWF swf, int spriteId, String containerFolderName, String spriteNameToAddShader) throws Exception {
        var foundSprite = TagUtils.getSprite(swf, spriteId);

        String containerFolderPath = String.format("%s\\%s", RESOURCE_FOLDER_PATH, containerFolderName);
        String spriteFolderPath = String.format("%s\\%s", containerFolderPath, spriteId);

        String[] folderPaths = {containerFolderPath, spriteFolderPath};
        for (String folderPath : folderPaths) {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        var exportImageResults = exportSpritePlaceObjects(foundSprite, spriteFolderPath);
        ArrayList<GodotSprite> sceneSprites = new ArrayList<>();

        for (ExportImageResult exportImageResult : exportImageResults) {
            var translation = getTranslationNeededInGodot(exportImageResult.getExportRect());
            var shouldAddShader = exportImageResult.getName() != null && exportImageResult.getName().contains(spriteNameToAddShader);

            GodotSprite godotSprite = new GodotSprite(
                    exportImageResult.getName(),
                    String.format("res://%s/%s/%s", containerFolderName, spriteId, exportImageResult.getFileName()),
                    translation,
                    shouldAddShader ? new ShaderOption(COLOR_SHADER_RESOURCE_PATH, COLOR_SHADER_PARAMETER) : null);
            sceneSprites.add(godotSprite);
        }

        var godotFileWriter = new GodotFileWriter();
        godotFileWriter.writeScene(String.format("%s\\%s.tscn", spriteFolderPath, spriteId), sceneSprites);
    }

    static ArrayList<ExportImageResult> exportSpritePlaceObjects(DefineSpriteTag sprite, String folderPath) throws Exception {
        var exportImageResults = new ArrayList<ExportImageResult>();

        var findPlaceObjectResults = findPlaceObjectTags(sprite);
        for (FindPlaceObjectResult findPlaceObjectResult : findPlaceObjectResults) {
            var placeObject = findPlaceObjectResult.getPlaceObject();
            var parent = findPlaceObjectResult.getParent();

            String name = String.format("%s", placeObject.getCharacterId());
            if (placeObject.name != null) name += "_" + placeObject.name;
            if (parent != null && parent.name != null) name += "_" + parent.name;

            var fileName = String.format("%s.png", name);
            var childImagePath = String.format("%s\\%s", folderPath, fileName);
            var exportRect = exportSpritePlaceObject(sprite.getCharacterId(), placeObject.getCharacterId(), childImagePath);

            exportImageResults.add(new ExportImageResult(exportRect, fileName, name));
        }

        return exportImageResults;
    }

    static Iterable<Tag> getFirstFrameTags(DefineSpriteTag sprite) {
        if (sprite.isSingleFrame()) return sprite.getTags();
        var timeline = sprite.getTimeline();
        var firstFrame = timeline.getFrame(0);
        return firstFrame.innerTags;
    }

    static ArrayList<FindPlaceObjectResult> findPlaceObjectTags(DefineSpriteTag sprite) throws Exception {
        ArrayList<FindPlaceObjectResult> result = new ArrayList<>();

        var firstFrameTags = getFirstFrameTags(sprite);
        for (Tag spriteChildTag : firstFrameTags) {
            if (!(spriteChildTag instanceof PlaceObject2Tag placeObject)) continue;

            var child = TagUtils.getTagById(sprite.getSwf(), placeObject.getCharacterId());
            if (child instanceof DefineSpriteTag defineSpriteTag) {
                var subPlaceObjects = findPlaceObjectTags(defineSpriteTag);
                if (subPlaceObjects.size() > 1) {
                    for (FindPlaceObjectResult subPlaceObject : subPlaceObjects) {
                        subPlaceObject.setParent(placeObject);
                    }
                    result.addAll(subPlaceObjects);
                    continue;
                }
            }
            result.add(new FindPlaceObjectResult(placeObject));
        }

        return result;
    }

    static RECT exportSpritePlaceObject(int spriteId, int placeObjectId, String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);
            var sprite = TagUtils.getSprite(swf, spriteId);
            var findPlaceObjectResults = findPlaceObjectTags(sprite);

            ArrayList<Tag> tagsToRemove = new ArrayList<>();
            for (FindPlaceObjectResult findPlaceObjectResult : findPlaceObjectResults) {
                var placeObjectTag = findPlaceObjectResult.getPlaceObject();
                if (placeObjectTag.getCharacterId() != placeObjectId) tagsToRemove.add(placeObjectTag);
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