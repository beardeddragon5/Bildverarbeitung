package Filters;

import ij.gui.Line;
import ij.gui.Overlay;
import java.awt.Color;

/**
 * Utility Functions to display overlays
 * @author matthias
 */
public interface OverlayUtil {

  /**
   * Given a 2-d boolean array, put points (= lines of length 1) into overlay where array == true.
   * @param overlay Overlay to add points to
   * @param color Witch color should the points have
   * @param maxis array of points to set
   */
  public static void computeOverMaxis(Overlay overlay, Color color, boolean[][] maxis) {
    final int lineWidth = 1;

    final Line ptLine = new Line(0, 0, 0, 0);
    ptLine.setStrokeColor(color);
    ptLine.setStrokeWidth(lineWidth);

    // insert a one-pixel line for each maximum
    for (int row = 0; row < maxis.length; row++) {
      for (int col = 0; col < maxis[row].length; col++) {
        if (maxis[row][col] == true) {
          final Line out = (Line) ptLine.clone();
          out.setLocation(col, row);
          overlay.add(out);
        }
      }
    }
  }

  public static void drawRectangle(Overlay overlay, Color color, Vector2i pos, Vector2i size) {
    final int lineWidth = 1;

    final Line up = new Line(pos.x, pos.y, pos.x + size.x, pos.y);
    final Line down = new Line(pos.x, pos.y + size.y, pos.x + size.x, pos.y + size.y);
    final Line left = new Line(pos.x, pos.y, pos.x, pos.y + size.y);
    final Line right = new Line(pos.x + size.x, pos.y, pos.x + size.x, pos.y + size.y);

    up.setStrokeColor(color);
    up.setStrokeWidth(lineWidth);
    down.setStrokeColor(color);
    down.setStrokeWidth(lineWidth);
    left.setStrokeColor(color);
    left.setStrokeWidth(lineWidth);
    right.setStrokeColor(color);
    right.setStrokeWidth(lineWidth);

    overlay.add(up);
    overlay.add(down);
    overlay.add(left);
    overlay.add(right);
  }
}
