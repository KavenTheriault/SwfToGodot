package utils;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.RECT;

import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.List;

public class GeoUtils {
    public static Point2D.Double centerRect(RECT rect) {
        double centerX = (double) (rect.Xmax + rect.Xmin) / 2;
        double centerY = (double) (rect.Ymax + rect.Ymin) / 2;
        return new Point2D.Double(centerX, centerY);
    }

    public static Point2D.Double centerRect(ExportRectangle rect) {
        double centerX = (rect.xMax + rect.xMin) / 2;
        double centerY = (rect.yMax + rect.yMin) / 2;
        return new Point2D.Double(centerX, centerY);
    }

    public static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    public static Matrix getTranslation(Point2D.Double origin, Point2D.Double destination) {
        return Matrix.getTranslateInstance(origin.x - destination.x, origin.y - destination.y);
    }

    public static RECT mergeRect(List<RECT> rects) {
        var minX = rects.stream().min(Comparator.comparingInt((RECT r) -> r.Xmin)).get().Xmin;
        var maxX = rects.stream().max(Comparator.comparingInt((RECT r) -> r.Xmax)).get().Xmax;
        var minY = rects.stream().min(Comparator.comparingInt((RECT r) -> r.Ymin)).get().Ymin;
        var maxY = rects.stream().max(Comparator.comparingInt((RECT r) -> r.Ymax)).get().Ymax;
        return new RECT(minX, maxX, minY, maxY);
    }
}
