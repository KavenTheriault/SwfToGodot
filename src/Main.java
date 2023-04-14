import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SwfOpenException;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    static int ZOOM = 4;
    static String SWF_FILE_PATH = "C:\\Users\\ZoidQC\\Downloads\\sloche-res\\client\\sloche2007.swf";

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream(SWF_FILE_PATH)) {
            SWF swf = new SWF(fis, true);

            var foundSprite = getSprite(swf, 855);
            var centerSpriteTranslation = getCenteringTranslation(foundSprite.getRect());

            printTag(foundSprite);
            System.out.println(foundSprite.getRect());
            System.out.println("CenterSpriteTranslation: " + centerSpriteTranslation);
            System.out.println("Centered?: " + centerSpriteTranslation.transform(new ExportRectangle(foundSprite.getRect())));

            for (Tag spiteChildTag : foundSprite.getTags()) {
                printTag(spiteChildTag);

                if (spiteChildTag instanceof PlaceObject2Tag placeObject) {
                    printTag(placeObject);
                    System.out.println(placeObject.getMatrix());

                    var child = getTagById(swf, placeObject.getCharacterId());
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
                        var centerChildTranslation = getCenteringTranslation(exportRect);
                        var childRectOrigin = centerChildTranslation.transform(exportRect);
                        var bigResult = getTranslation(new Point2D.Double(childRectGoal.xMin, childRectGoal.yMin), new Point2D.Double(childRectOrigin.xMin, childRectOrigin.yMin));

                        System.out.println("ChildRectGoal: " + childRectGoal);
                        System.out.println("CenterChildTranslation: " + centerChildTranslation);
                        System.out.println("ChildRectOrigin: " + childRectOrigin);
                        System.out.println("BigResult: " + bigResult);
                        System.out.println("BigResult PX: " + twipToPixel(bigResult.translateX) + " " + twipToPixel(bigResult.translateY));
                        System.out.println("BigResult PX and Zoom: " + twipToPixel(bigResult.translateX * ZOOM) + " " + twipToPixel(bigResult.translateY * ZOOM));
                    }
                }
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
        //return new RECT((int) Math.ceil(exportRectangle.xMin), (int) Math.ceil(exportRectangle.xMax), (int) Math.ceil(exportRectangle.yMin), (int) Math.ceil(exportRectangle.yMax));
        return new RECT((int) Math.round(exportRectangle.xMin), (int) Math.round(exportRectangle.xMax), (int) Math.round(exportRectangle.yMin), (int) Math.round(exportRectangle.yMax));
    }

    private static Point2D.Double centerRect(RECT rect) {
        double centerX = (double) (rect.Xmax + rect.Xmin) / 2;
        double centerY = (double) (rect.Ymax + rect.Ymin) / 2;
        return new Point2D.Double(centerX, centerY);
    }

    private static Point2D.Double centerRect(ExportRectangle rect) {
        double centerX = (double) (rect.xMax + rect.xMin) / 2;
        double centerY = (double) (rect.yMax + rect.yMin) / 2;
        return new Point2D.Double(centerX, centerY);
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static Matrix getTranslation(Point2D.Double origin, Point2D.Double destination) {
        return Matrix.getTranslateInstance(origin.x - destination.x, origin.y - destination.y);
    }

    private static Matrix getCenteringTranslation(RECT rect) {
        Point2D.Double rectCenter = centerRect(rect);
        return getTranslation(new Point2D.Double(0, 0), rectCenter);
    }

    private static Matrix getCenteringTranslation(ExportRectangle rect) {
        Point2D.Double rectCenter = centerRect(rect);
        return getTranslation(new Point2D.Double(0, 0), rectCenter);
    }

    static void saveImageFile(BufferedImage bufferedImage, int id) throws IOException {
        File file = new File(String.format("C:\\Users\\ZoidQC\\Documents\\sloche exports\\%s.png", id));
        ImageHelper.write(bufferedImage, ImageFormat.PNG, file);
    }

    static Tag getTagById(SWF swf, int id) throws Exception {
        for (Tag tag : swf.getTags()) {
            if (tag instanceof CharacterIdTag characterIdTag && characterIdTag.getCharacterId() == id) {
                return tag;
            }
        }
        throw new Exception("Tag not found");
    }

    static DefineSpriteTag getSprite(SWF swf, int id) throws Exception {
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineSpriteTag sprite && sprite.getCharacterId() == id) {
                return sprite;
            }
        }
        throw new Exception("Sprite not found");
    }

    static DefineShape2Tag getShape(SWF swf, int id) throws Exception {
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineShape2Tag shape && shape.getCharacterId() == id) {
                return shape;
            }
        }
        throw new Exception("Shape not found");
    }

    static void printTag(Tag t) {
        if (t instanceof CharacterIdTag) {
            System.out.println("Tag " + t.getTagName() + " (" + ((CharacterIdTag) t).getCharacterId() + ")");
        } else {
            System.out.println("Tag " + t.getTagName());
        }
    }
}