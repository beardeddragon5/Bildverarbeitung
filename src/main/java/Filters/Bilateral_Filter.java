package Filters;

import java.util.Arrays;
import ij.process.FloatProcessor;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Bilateral Filter Plugin
 * @author matthias
 */
public class Bilateral_Filter implements PlugInFilter {

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

  public int filtersize(float sigma_s) {
    return 2 * (int)(1.5f * sigma_s + 0.5f) + 1;
  }

  public void gaus(float[] weights, int width, int height, float sigma) {
    float b = 2.0f * sigma * sigma;
    for (int i = 0, y = 0; y < height; y++) {
      int dy = y - height / 2;
      for (int x = 0; x < width; x++, i++) {
        int dx = x - width / 2;
        weights[i] = (float) Math.exp(-(dx * dx + dy * dy) / b);
      }
    }
  }

  public void gradiant(float[] out, float sigma) {
    float b = 2.0f * sigma * sigma;
    for (int i = 0; i < out.length; i++) {
      out[i] = (float) Math.exp(-(i * i) / b);
    }
  }

  public void normalize(float[] out) {
    final float sum = sum(out);
    for (int i = 0; i < out.length; i++) {
      out[i] /= sum;
    }
  }

  public float sum(float[] in) {
    float sum = 0;
    for (int i = 0; i < in.length; i++) {
      sum += in[i];
    }
    return sum;
  }

  public void debugshow(String name, float[] out, int width, int height) {
    final ImageProcessor processor = new FloatProcessor(width, height);
    final float[] output = (float[]) processor.getPixels();
    System.arraycopy(out, 0, output, 0, output.length);
    new ImagePlus(name, processor).show();
  }

  public int mirrorIndex(int idx, int size) {
    if (idx < 0) {
      idx = -idx - 1;
    } else if (idx >= size) {
      idx = size - (idx - size) - 1;
    }
    return idx;
  }

  public float[] bilateralfilter(float[] output, byte[] input, int width, int height, float sigma_s, float sigma_r) {
    final int s = filtersize(sigma_s);
    final int halfS = s / 2;
    // final float[] mirror = new float[(width + s) * (height + s)];
    final float[] weights = new float[s * s];
    final float[] lightWeight = new float[256];

    gaus(weights, s, s, sigma_s);
    gradiant(lightWeight, sigma_r);
    normalize(weights);
    normalize(lightWeight);

    for (int y = 0, i = 0; y < height; y++) {
      for (int x = 0; x < width; x++, i++) {
        final int current = input[i] & 0xff;
        float sumW = 0;

        for (int fy = 0, fi = 0; fy < s; fy++) {
          final int nabsolutY = (y - halfS) + fy;
          // final int mirrorY = nabsolutY + halfS;
          final int neighbourY = mirrorIndex(nabsolutY, height);

          for (int fx = 0; fx < s; fx++, fi++) {
            final int nabsolutX = (x - halfS) + fx;
            // final int mirrorX = nabsolutX + halfS;
            final int neighbourX = mirrorIndex(nabsolutX, width);

            final int neighbour = input[neighbourY * width + neighbourX] & 0xff;
            final float w = weights[fi] * lightWeight[Math.abs(neighbour - current)];

            sumW += w;
            output[i] += w * neighbour;
            // mirror[mirrorY * (width + s) + mirrorX] = neighbour;
          }
        }
        output[i] /= sumW;
      }
    }

    // debugshow("gaus", weights, s, s);
    // debugshow("light", lightWeight, 255, 1);
    // debugshow("mirror", mirror, width + s, height + s);
    return output;
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
    final ImageProcessor out = new FloatProcessor(width, height);
    final float[] output = (float[]) out.getPixels();

    bilateralfilter(output, input, width, height, 5.0f, 20.0f);

    new ImagePlus("σs=5 σr=20", out).show();

    /*
      Iris
      σs=5 σr=20 :218
      σs=1 σr=1 :79
      σs=15 σr=30 :1488

      GruenesHaus_grey
      σs=5 σr=20 :4152
      σs=1 σr=1 :622
      σs=15 σr=30 :25107
    */
  }
}
