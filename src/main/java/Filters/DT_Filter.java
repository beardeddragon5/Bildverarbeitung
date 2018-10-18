package Filters;

import java.awt.Color;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 *
 * @author matthias
 */
public class DT_Filter implements PlugInFilter {

  private final static byte B_BLACK = (byte) 0x00;
  private final static short S_BLACK = (short) 0x00;

  @Override
  public int setup(String string, ImagePlus ip) {
    return DOES_8G + NO_UNDO + NO_CHANGES;
  }

  private static int dtfilter(int width, int height, byte[] input, short[] output) {
    int maxDT = 0;
    for (int i = 0; i < width; i++) {
      // set first row
      output[i] = input[i] == B_BLACK ? 1 : S_BLACK;
    }
    for (int i = width; i < output.length; i += width) {
      // set first col
      output[i] = input[i] == B_BLACK ? 1 : S_BLACK;
    }

    for (int y = 1, index = width; y < height; y++, index++) {
      for (int x = 1; x < width; x++, index++) {
        if (input[index] == B_BLACK) {
          output[index] = (short)(1 + Math.min(output[index - 1], output[index - width]));
        } else {
          output[index] = S_BLACK;
        }
      }
    }

    for (int i = output.length - width; i < output.length; i++) {
      // set last row
      output[i] = input[i] == B_BLACK ? 1 : S_BLACK;
    }

    for (int i = width - 1; i < output.length; i += width) {
      // set last col
      output[i] = input[i] == B_BLACK ? 1 : S_BLACK;
    }

    for (int y = height - 2, index = output.length - width - 1; y >= 0; y--, index--) {
      for (int x = width - 2; x >= 0; x--, index--) {
        if (input[index] == B_BLACK) {
          final short value = (short) Math.min(output[index], 1 + Math.min(output[index + 1], output[index + width]));
          maxDT = Math.max(maxDT, value);
          output[index] = value;
        } else {
          output[index] = S_BLACK;
        }
      }
    }
    return maxDT;
  }

  private static boolean[][] genLocalMaxima(int width, int height, short[] output) {
    final boolean[][] localMaxima = new boolean[height][width];
    for (int y = 1, index = width + 1; y < height - 1; y++, index += 2) {
        for (int x = 1; x < width - 1; x++, index++) {
          localMaxima[y][x] = output[index] > 0 &&
              output[index] >= output[index+1] &&
              output[index] >= output[index-1] &&
              output[index] >= output[index+width] &&
              output[index] >= output[index-width] &&
              output[index] >= output[index+1+width] &&
              output[index] >= output[index-1+width] &&
              output[index] >= output[index+1-width] &&
              output[index] >= output[index-1-width];
        }
    }
    return localMaxima;
  }

  @Override
  public void run(ImageProcessor ip) {
    final ImageProcessor out = new ShortProcessor(ip.getWidth(), ip.getHeight());
    final byte[] input = (byte[]) ip.getPixels();
    final short[] output = (short[]) out.getPixels();
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final int maxDT = dtfilter(width, height, input, output);

    final ImagePlus outputImage = new ImagePlus("DT Filter Image", out);
    outputImage.setDisplayRange(0, maxDT);
    outputImage.show();

    final boolean[][] localMaxima = genLocalMaxima(width, height, output);

    final Overlay overMaxis = new Overlay();
    OverlayUtil.computeOverMaxis(overMaxis, Color.yellow, localMaxima);

    final ImagePlus impOverlay = new ImagePlus("Maxima", ip);
    impOverlay.setOverlay(overMaxis);
    impOverlay.show();
  }
}
