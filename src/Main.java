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
import godot.GodotWriter;
import godot.parameters.*;
import types.ExportImageResult;
import types.TagTreeItem;
import utils.GeoUtils;
import utils.TagUtils;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;
import static utils.GeoUtils.twipToPixel;

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
            int[] pantsIds = {731};

//            for (int headSpriteId : headSpriteIds) {
//                generateSpriteScene(swf, headSpriteId, "heads", "hair");
//            }
//
//            for (int bodySpriteId : bodySpriteIds) {
//                generateSpriteScene(swf, bodySpriteId, "bodies", "shirt");
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
        var sprite = TagUtils.getSprite(swf, spriteId);

        String containerFolderPath = String.format("%s\\%s", RESOURCE_FOLDER_PATH, containerFolderName);
        String spriteFolderPath = String.format("%s\\%s", containerFolderPath, spriteId);

        String[] folderPaths = {containerFolderPath, spriteFolderPath};
        for (String folderPath : folderPaths) {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
        }

        var godotFileWriter = new GodotWriter();

        var tagTreeItems = getTagTreeItems(sprite, true);
        var godotWriteItems = buildGodotWriterItems(sprite, tagTreeItems, spriteFolderPath, containerFolderName, spriteNameToAddShader);

        var allTagTreeItems = getTagTreeItems(sprite, false);
        var animation = buildAnimation(sprite, allTagTreeItems);

        godotFileWriter.writeScene(spriteFolderPath, Integer.toString(spriteId), godotWriteItems, animation);
    }

    static ArrayList<GodotWriterNode> buildGodotWriterItems(DefineSpriteTag sprite, ArrayList<TagTreeItem> tagTreeItems, String folderPath, String containerFolderName, String spriteNameToAddShader) {
        var result = new ArrayList<GodotWriterNode>();
        for (TagTreeItem tagTreeItem : tagTreeItems) {
            if (tagTreeItem.getChildren() != null) {
                var godotWriteGroup = new GodotWriterGroup(tagTreeItem.getTag().name);
                godotWriteGroup.getNodes().addAll(buildGodotWriterItems(sprite, tagTreeItem.getChildren(), folderPath, containerFolderName, spriteNameToAddShader));
                result.add(godotWriteGroup);
            } else {
                var exportImageResult = exportPlaceObjectImage(sprite, tagTreeItem, folderPath);
                result.add(buildGodotWriterSprite(exportImageResult, sprite.getCharacterId(), containerFolderName, spriteNameToAddShader));
            }
        }
        return result;
    }

    static GodotWriterAnimation buildAnimation(DefineSpriteTag sprite, ArrayList<TagTreeItem> tagTreeItems) {
        if (sprite.isSingleFrame()) return null;

        var tracks = new ArrayList<GodotWriterAnimationTrack>();

        var tagsByDepth = tagTreeItems.stream().collect(groupingBy(t -> t.getTag().depth));
        for (var depthTags : tagsByDepth.entrySet()) {
            if (depthTags.getValue().size() == 1) continue;

            Matrix originMatrix = null;
            var firstFrameTag = depthTags.getValue().get(0);
            var positions = new ArrayList<Point2D.Double>();
            var scales = new ArrayList<Point2D.Double>();

            for (var tag : depthTags.getValue()) {
                if (originMatrix != null) {
                    var currentMatrix = new Matrix(tag.getTag().getMatrix());

                    var translateX = twipToPixel(currentMatrix.translateX - originMatrix.translateX) * 5;
                    var translateY = twipToPixel(currentMatrix.translateY - originMatrix.translateY) * 5;
                    positions.add(new Point2D.Double(translateX, translateY));

                    var scaleX = 1 + currentMatrix.scaleX - originMatrix.scaleX;
                    var scaleY = 1 + currentMatrix.scaleY - originMatrix.scaleY;
                    scales.add(new Point2D.Double(scaleX, scaleY));

//                    var rotateSkew0 = currentMatrix.rotateSkew0 - originMatrix.rotateSkew0;
//                    var rotateSkew1 = currentMatrix.rotateSkew1 - originMatrix.rotateSkew1;
//                    System.out.println("ROTATE: " + rotateSkew0 + " " + rotateSkew1);
                } else {
                    originMatrix = new Matrix(tag.getTag().getMatrix());
                    positions.add(new Point2D.Double(0, 0));
                    scales.add(new Point2D.Double(1, 1));
                }
            }

            tracks.add(new GodotWriterAnimationTrack(firstFrameTag.getTag().name, "position", positions));
            tracks.add(new GodotWriterAnimationTrack(firstFrameTag.getTag().name, "scale", scales));
        }

        if (tracks.size() > 0) {
            var step = 1.0 / sprite.getSwf().frameRate;
            return new GodotWriterAnimation((tracks.get(0).getValues().size() - 1) * step, step, tracks);
        }

        return null;
    }

    static GodotWriterSprite buildGodotWriterSprite(ExportImageResult exportImageResult, int spriteId, String containerFolderName, String spriteNameToAddShader) {
        var translation = getTranslationNeededInGodot(exportImageResult.getExportRect());
        var shouldAddShader = exportImageResult.getName() != null && exportImageResult.getName().contains(spriteNameToAddShader);
        return new GodotWriterSprite(
                exportImageResult.getName(),
                String.format("res://%s/%s/%s", containerFolderName, spriteId, exportImageResult.getFileName()),
                translation,
                shouldAddShader ? new ShaderOption(COLOR_SHADER_RESOURCE_PATH, COLOR_SHADER_PARAMETER) : null);
    }

    static ExportImageResult exportPlaceObjectImage(DefineSpriteTag sprite, TagTreeItem tagTreeItem, String folderPath) {
        var placeObject = tagTreeItem.getTag();
        var parent = tagTreeItem.getParent();

        String name = String.format("%s", placeObject.getCharacterId());
        if (placeObject.name != null) name += "_" + placeObject.name;
        if (parent != null && parent.name != null) name += "_" + parent.name;

        var fileName = String.format("%s.png", name);
        var childImagePath = String.format("%s\\%s", folderPath, fileName);

        try {
            var exportRect = exportSpritePlaceObject(sprite.getCharacterId(), tagTreeItem.getName(), childImagePath);
            return new ExportImageResult(exportRect, fileName, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Iterable<Tag> getFirstFrameTags(DefineSpriteTag sprite) {
        if (sprite.isSingleFrame()) return sprite.getTags();
        var timeline = sprite.getTimeline();
        var firstFrame = timeline.getFrame(0);
        return firstFrame.innerTags;
    }

    static ArrayList<TagTreeItem> getTagTreeItems(DefineSpriteTag sprite, boolean onlyFirstFrame) throws Exception {
        var result = new ArrayList<TagTreeItem>();
        var childTags = onlyFirstFrame ? getFirstFrameTags(sprite) : sprite.getTags();

        for (Tag childTag : childTags) {
            if (!(childTag instanceof PlaceObject2Tag placeObject)) continue;

            var tagTreeItem = new TagTreeItem(placeObject);
            var placedObject = TagUtils.getTagById(sprite.getSwf(), placeObject.getCharacterId());

            if (placedObject instanceof DefineSpriteTag defineSpriteTag) {
                var subTagTreeItems = getTagTreeItems(defineSpriteTag, onlyFirstFrame);
                if (subTagTreeItems.size() > 1) {
                    for (TagTreeItem subTagTreeItem : subTagTreeItems) {
                        subTagTreeItem.setParent(placeObject);
                    }
                    tagTreeItem.setChildren(subTagTreeItems);
                }
            }

            result.add(tagTreeItem);
        }

        return result;
    }

    static ArrayList<TagTreeItem> findTagTreeItemBranch(String tagTreeItemName, ArrayList<TagTreeItem> tagTreeItems) {
        var result = new ArrayList<TagTreeItem>();
        for (TagTreeItem tagTreeItem : tagTreeItems) {
            if (Objects.equals(tagTreeItem.getName(), tagTreeItemName)) {
                result.add(tagTreeItem);
                return result;
            }
            if (tagTreeItem.getChildren() != null) {
                var foundBranchItems = findTagTreeItemBranch(tagTreeItemName, tagTreeItem.getChildren());
                if (foundBranchItems.size() > 0) {
                    result.add(tagTreeItem);
                    result.addAll(foundBranchItems);
                    return result;
                }
            }
        }
        return result;
    }

    static ArrayList<Tag> findTagsToRemove(List<PlaceObject2Tag> tagsToKeep, ArrayList<TagTreeItem> tagTreeItems) {
        var result = new ArrayList<Tag>();
        for (TagTreeItem tagTreeItem : tagTreeItems) {
            if (!tagsToKeep.contains(tagTreeItem.getTag())) result.add(tagTreeItem.getTag());
            if (tagTreeItem.getChildren() != null) {
                var childrenResult = findTagsToRemove(tagsToKeep, tagTreeItem.getChildren());
                result.addAll(childrenResult);
            }
        }
        return result;
    }

    static RECT exportSpritePlaceObject(int spriteId, String tagTreeItemName, String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);
            var sprite = TagUtils.getSprite(swf, spriteId);

            var tagTreeItems = getTagTreeItems(sprite, false);
            var branchToKeep = findTagTreeItemBranch(tagTreeItemName, tagTreeItems);
            var tagsToRemove = findTagsToRemove(branchToKeep.stream().map(TagTreeItem::getTag).toList(), tagTreeItems);

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
        var centerChildTranslation = GeoUtils.getCenteringTranslation(childRect);
        var childRectOrigin = centerChildTranslation.transform(childRectDestination);
        var resultTranslation = GeoUtils.getTranslation(new Point2D.Double(childRectDestination.xMin, childRectDestination.yMin), new Point2D.Double(childRectOrigin.xMin, childRectOrigin.yMin));

        return new Point2D.Double(twipToPixel(resultTranslation.translateX * ZOOM), twipToPixel(resultTranslation.translateY * ZOOM));
    }
}