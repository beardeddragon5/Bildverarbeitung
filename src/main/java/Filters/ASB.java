package Filters;

import java.util.stream.IntStream;
import ij.process.ByteProcessor;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Adaptive Binary Filter Plugin
 * @author matthias
 */
public class ASB implements PlugInFilter {

  /**
   * Process 8bit greyscale images. Does not support UNDO.
   * @param string ignored
   * @param ip ignored
   * @return 8bit greyscale, no undo, no changes
   */
  @Override
  public int setup(String string, ImagePlus ip) {
    return DOES_8G + NO_UNDO + NO_CHANGES;
  }

  private static int[][] accumulateImage(byte[] input, int width, int height) {
    final int[][] output = new int[height][width];
    for (int x = 0, sum = 0; x < width; x++) {
        sum += input[x] & 0xff;
        output[0][x] = sum;
    }
    for (int y = 1, i = width; y < height; y++) {
      for (int x = 0, sum = 0; x < width; x++, i++) {
        sum += input[i] & 0xff;
        output[y][x] = sum + output[y - 1][x];
      }
    }
    return output;
  }

  private static float average(int[][] sum, int blockWidth, int blockHeight, int x, int y) {
    final int hWidth = blockWidth / 2;
    final int hHeight = blockHeight / 2;
    final int x1 = x - hWidth < 0               ? 0                   : x - hWidth;
    final int y1 = y - hHeight < 0              ? 0                   : y - hHeight;
    final int x2 = x + hWidth >= sum[0].length  ? sum[0].length - 1   : x + hWidth;
    final int y2 = y + hHeight >= sum.length    ? sum.length - 1      : y + hHeight;

    final int y1s = y1 - 1 >= 0 ? y1 - 1 : 0; // nothing above
    final int x1s = x1 - 1 >= 0 ? x1 - 1 : 0; // nothing left

    final float acc = sum[y2][x2] - sum[y1s][x2] - sum[y2][x1s] + sum[y1s][x1s];
    return acc / ((x2 - x1) * (y2 - y1));
  }

  public static void asb(byte[] output, byte[] input, int width, int height, float c, int blockSizeX, int blockSizeY) {
    final int[][] sumImage = accumulateImage(input, width, height);
    final float cInv = 1.0f - c;
    IntStream.range(0, input.length)
      .parallel()
      .forEach(i -> {
        final float avg = average(sumImage, blockSizeX, blockSizeY, i % width, i / width);
        output[i] = (byte) ((input[i] & 0xff) < cInv * avg ? 0 : 255);
      });
  }

  /**
   * Run Bilaterial Filter on given ImageProcessor
   * @param ip ImageProcessor to use
   */
  @Override
  public void run(ImageProcessor ip) {
    final byte[] input = (byte[]) ip.getPixels();
    final int width = ip.getWidth();
    final int height = ip.getHeight();
    final ImageProcessor out = new ByteProcessor(width, height);
    final byte[] output = (byte[]) out.getPixels();

    final int blockSizeX = (int)(width * (1.0f / 8.0f));
    final int blockSizeY = (int)(height * (1.0f / 8.0f));

    System.out.println("blocksize: (" + blockSizeX + ", " + blockSizeY + ")");

    asb(output, input, width, height, 0.15f, blockSizeX, blockSizeY);

    final ImagePlus p = new ImagePlus("adaptive blocksize=1/8 c=0.15", out);
    p.show();
  }
}
