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
    public static Overlay computeOverMaxis(boolean[][] maxis) {
        Overlay myOverlay = new Overlay();
        Color lineColor = Color.yellow;
        int lineWidth = 1;

        // insert a one-pixel line for each maximum
        for (int row = 0; row < maxis.length; row++) {
            for (int col = 0; col < maxis[row].length; col++) {
                if (maxis[row][col] == true) {
                    Line ptLine = new Line(col, row, col, row);
                    ptLine.setStrokeColor(lineColor);
                    ptLine.setStrokeWidth(lineWidth);
                    myOverlay.add(ptLine);
                }
            }
        }

        return myOverlay;
    }

    //**********************************************************************
    // show image ip with overlay
    public static void showOverlay(ImageProcessor ip, Overlay myOverlay, String title) {
        ImagePlus impOverlay = new ImagePlus(title, ip);
        impOverlay.setOverlay(myOverlay);
        impOverlay.show();
    }

}
