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

  /**
   * Run Bilaterial Filter on given ImageProcessor
   * @param ip ImageProcessor to use
   */
  @Override
  public void run(ImageProcessor ip) {
    final ImageProcessor out = new FloatProcessor(ip.getWidth(), ip.getHeight());
    final byte[] input = (byte[]) ip.getPixels();
    final float[] output = (float[]) out.getPixels();
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final float sigma_s = 5.0f;
    final float sigma_r = 20.0f;//20.0f;

    final int s = filtersize(sigma_s);
    final float[] weights = new float[s * s];
    final float[] lightWeight = new float[256];
    gaus(weights, s, s, sigma_s);
    gradiant(lightWeight, sigma_r);

    System.out.println(weights[5 * s + 5] + " " + lightWeight[5]);

    normalize(weights);
    normalize(lightWeight);

    final int halfS = s / 2;
    for (int i = 0, y = 0; y < height; y++) {
      for (int x = 0; x < width; x++, i++) {
        final int current = input[i] & 0xff;
        double sumW = 0;

        for (int fi = 0, fy = 0; fy < s; fy++) {
          int neighbourY = Math.abs((y - halfS) + fy);
          if (neighbourY >= height) {
            neighbourY = height - (neighbourY - height) - 1;
          }
          for (int fx = 0; fx < s; fx++, fi++) {
            int neighbourX = Math.abs((x - halfS) + fx);
            if (neighbourX >= width) {
              neighbourX = width - (neighbourX - width) - 1;
            }

            final int neighbour = input[neighbourY * width + neighbourX] & 0xff;
            final float w = weights[fi] * lightWeight[Math.abs(neighbour - current)];
            sumW += w;
            output[i] += w * neighbour;
          }
        }

        output[i] /= sumW;
      }
    }

    debugshow("gaus", weights, s, s);
    debugshow("light", lightWeight, 255, 1);
    final ImagePlus outputImage = new ImagePlus("Bilateral Filter Image", out);
    outputImage.show();

  }
}
