package Filters;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.process.ImageProcessor;
import java.awt.Color;

/**
 *
 * @author matthias
 */
public class OverlayUtil {

  //**********************************************************************
  // given a 2-d boolean array, put points (= lines of length 1) into overlay where array == true.
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

}
