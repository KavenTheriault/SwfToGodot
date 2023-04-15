import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;

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

            secondTry(foundSprite);

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

    static void secondTry(DefineSpriteTag mainSprite) throws IOException, InterruptedException {
        SWF swf = mainSprite.getSwf();
        ArrayList<Tag> tagsToRemove = new ArrayList<>();

        for (Tag spriteChildTag : mainSprite.getTags()) {
            if (spriteChildTag instanceof PlaceObject2Tag placeObject) {
                if (placeObject.getCharacterId() == 852 || placeObject.getCharacterId() == 851) tagsToRemove.add(placeObject);
            }
        }

        System.out.println("Before:" + mainSprite.getTimeline().displayRect);

        swf.removeTags(tagsToRemove, false, null);
        swf.computeDependentCharacters();
        swf.computeDependentFrames();

        System.out.println("After:" + mainSprite.getTimeline().displayRect);

        Timeline timeline = mainSprite.getTimeline();
        BufferedImage bufferedImage = SWF.frameToImageGet(timeline, 0, 0, null, 0, timeline.displayRect, new Matrix(), null, null, ZOOM, true).getBufferedImage();
        saveImageFile(bufferedImage, mainSprite.getCharacterId());
    }

    static void firstTry(SWF swf, DefineSpriteTag mainSprite) throws Exception {
        var centerSpriteTranslation = GraphUtils.getCenteringTranslation(mainSprite.getRect());

        TagUtils.printTag(mainSprite);
        System.out.println(mainSprite.getRect());
        System.out.println("CenterSpriteTranslation: " + centerSpriteTranslation);
        System.out.println("Centered?: " + centerSpriteTranslation.transform(new ExportRectangle(mainSprite.getRect())));

        for (Tag spriteChildTag : mainSprite.getTags()) {
            TagUtils.printTag(spriteChildTag);

            if (spriteChildTag instanceof PlaceObject2Tag placeObject) {
                TagUtils.printTag(placeObject);
                System.out.println(placeObject.getMatrix());

                var child = TagUtils.getTagById(swf, placeObject.getCharacterId());
                if (child instanceof DefineShapeTag shape) {
                    exportShape(shape);
                    System.out.println(shape.getRect());
                }
                if (child instanceof DefineSpriteTag childSprite) {
                    var originalMatrix = placeObject.getMatrix();

                    var exportMatrix = new Matrix(originalMatrix);
                    // Translation are breaking the export
                    exportMatrix.translateX = 0;
                    exportMatrix.translateY = 0;

                    ExportRectangle exportRect = exportSprite(childSprite, exportMatrix, placeObject.getColorTransform());
                    System.out.println("Original Rect: " + childSprite.getRect());
                    System.out.println("Export Rect: " + exportRect);

                    var translationMatrix = Matrix.getTranslateInstance(originalMatrix.translateX, originalMatrix.translateY);
                    var translationMatrix2 = translationMatrix.concatenate(centerSpriteTranslation);
                    var childRectGoal = translationMatrix2.transform(exportRect);
                    var centerChildTranslation = GraphUtils.getCenteringTranslation(exportRect);
                    var childRectOrigin = centerChildTranslation.transform(exportRect);
                    var bigResult = GraphUtils.getTranslation(new Point2D.Double(childRectGoal.xMin, childRectGoal.yMin), new Point2D.Double(childRectOrigin.xMin, childRectOrigin.yMin));

                    System.out.println("ChildRectGoal: " + childRectGoal);
                    System.out.println("CenterChildTranslation: " + centerChildTranslation);
                    System.out.println("ChildRectOrigin: " + childRectOrigin);
                    System.out.println("BigResult: " + bigResult);
                    System.out.println("BigResult PX: " + GraphUtils.twipToPixel(bigResult.translateX) + " " + GraphUtils.twipToPixel(bigResult.translateY));
                    System.out.println("BigResult PX and Zoom: " + GraphUtils.twipToPixel(bigResult.translateX * ZOOM) + " " + GraphUtils.twipToPixel(bigResult.translateY * ZOOM));
                }
            }
        }
    }

    static void exportShape(DefineShapeTag tag) throws IOException {
        int zoom = ZOOM;

        RECT rect = tag.getRect();
        int newWidth = (int) ((double) rect.getWidth() * zoom / 20.0) + 1;
        int newHeight = (int) ((double) rect.getHeight() * zoom / 20.0) + 1;
        SerializableImage img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
        img.fillTransparent();

        Matrix m = Matrix.getScaleInstance(zoom);
        m.translate(-rect.Xmin, -rect.Ymin);
        tag.toImage(0, 0, 0, new RenderContext(), img, img, false, m, m, m, m, new CXFORMWITHALPHA(), zoom, false, new ExportRectangle(rect), true, 0, 0, true);

        saveImageFile(img.getBufferedImage(), tag.getCharacterId());
    }

    static ExportRectangle exportSprite(DefineSpriteTag tag, Matrix matrix, ColorTransform colorTransform) throws IOException {
        if (matrix == null) matrix = new Matrix();

        var timeline = tag.getTimeline();
        ExportRectangle exportRect = matrix.transform(new ExportRectangle(timeline.displayRect));

        BufferedImage bufferedImage = SWF.frameToImageGet(timeline, 0, 0, null, 0, ExportRectangleToRECT(exportRect), matrix, colorTransform, null, ZOOM, true).getBufferedImage();
        saveImageFile(bufferedImage, tag.getCharacterId());

        return exportRect;
    }

    static RECT ExportRectangleToRECT(ExportRectangle exportRectangle) {
        return new RECT((int) Math.round(exportRectangle.xMin), (int) Math.round(exportRectangle.xMax), (int) Math.round(exportRectangle.yMin), (int) Math.round(exportRectangle.yMax));
    }

    static void saveImageFile(BufferedImage bufferedImage, int id) throws IOException {
        File file = new File(String.format("C:\\Users\\ZoidQC\\Documents\\sloche exports\\%s.png", id));
        ImageHelper.write(bufferedImage, ImageFormat.PNG, file);
    }
}