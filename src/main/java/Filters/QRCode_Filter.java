package Filters;

import ij.IJ;
import java.util.Arrays;
import ij.gui.GenericDialog;
import java.util.stream.IntStream;
import ij.process.ByteProcessor;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Adaptive Binary Filter Plugin
 * @author matthias
 */
public class QRCode_Filter implements PlugInFilter {

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

  class Markers {
    Flood.Segment dl, ul, ur;

    @Override
    public String toString() {
      return String.format("dl: %s, ul: %s, ur: %s", dl.center, ul.center, ur.center);
    }
  }

  private Markers identifyMarker(Flood.Segment ul, Flood.Segment m1, Flood.Segment m2) {
    final Markers out = new Markers();
    final double angle = Vector2i.getAngle(Vector2i.sub(m1.center, ul.center), Vector2i.sub(m2.center, ul.center));

    out.ul = ul;
    out.dl = angle > 0 && angle < Math.PI ? m2 : m1;
    out.ur = angle > 0 && angle > -Math.PI ? m1 : m2;

    System.out.println(out + " " + Math.toDegrees(angle));
    return out;
  }

  private Markers findMarker(byte[] output, int width) {
    final Flood flood = new Flood(output, width, output.length / width);
    long time = System.currentTimeMillis();
    final Flood.Segment[] quadrats = IntStream.range(0, output.length)
      .filter(i -> !flood.flooded[i])
      .filter(i -> output[i] == 0)
      .mapToObj(i -> flood.findSegment(i, (byte) 0))
      .filter(r -> r.area >= 100)
      .filter(result -> Math.abs(result.size.x - result.size.y) < 5)
      .toArray(size -> new Flood.Segment[size]);

    final Flood.Segment[] amarkers = Arrays.stream(quadrats)
      .parallel().filter(a ->
        Arrays.stream(quadrats)
          .parallel()
          .filter(b -> !b.equals(a))
          .map(b -> a.center.manhatten(b.center))
          .filter(distance -> distance < 20)
          .findAny().isPresent())
      .toArray(size -> new Flood.Segment[size]);

    final double avg = Arrays.stream(amarkers).mapToInt(b -> b.area).average().getAsDouble();
    final Flood.Segment[] markers = Arrays.stream(amarkers).parallel()
      .filter(a -> a.area > avg).toArray(size -> new Flood.Segment[size]);

    if (markers.length != 3) {
      return null;
    }

    final Flood.Segment m1 = markers[0];
    final Flood.Segment m2 = markers[1];
    final Flood.Segment m3 = markers[2];

    final int d12 = m1.min.manhatten(m2.min);
    final int d23 = m2.min.manhatten(m3.min);
    final int d13 = m1.min.manhatten(m3.min);

    System.out.printf("time3: %d ms\n", System.currentTimeMillis() - time);
    time = System.currentTimeMillis();

    if (d12 > d23 && d12 > d13) {
      return identifyMarker(m3, m1, m2);
    } else if (d23 > d12 && d23 > d13) {
      return identifyMarker(m1, m2, m3);
    } else {
      return identifyMarker(m2, m1, m3);
    }
  }

  /**
   * Run Bilaterial Filter on given ImageProcessor
   * @param ip ImageProcessor to use
   */
  @Override
  public void run(ImageProcessor ip) {
    final GenericDialog gd = new GenericDialog("Rotate");
    gd.addNumericField("Angle", 0, 3);
    gd.showDialog();
    final double rotateAngle = gd.getNextNumber();
    ip = Rotate_Bilinear.rotate(ip, rotateAngle);

    new ImagePlus("input", ip).show();

    final byte[] input = (byte[]) ip.getPixels();
    final int width = ip.getWidth();
    final int height = ip.getHeight();
    final ImageProcessor out = new ByteProcessor(width, height);
    final byte[] output = (byte[]) out.getPixels();

    final int blockSizeX = (int)(width * (1.0f / 8.0f));
    final int blockSizeY = (int)(height * (1.0f / 8.0f));

    ASB.asb(output, input, width, height, 0.15f, blockSizeX, blockSizeY);

    Markers markers = findMarker(output, width);
    if (markers == null) {
      IJ.showMessage("No Markers found");
      return;
    }

    final Vector2i upV = Vector2i.sub(markers.dl.center, markers.ul.center);
    final double angle = Vector2i.getAngle(upV, new Vector2i(0, 1));
    final ImageProcessor rotated = Rotate_Bilinear.rotate(out, Math.toDegrees(angle));

    markers = findMarker((byte[]) rotated.getPixels(), width);
    if (markers == null) {
      IJ.showMessage("No Markers found");
      return;
    }

    rotated.setRoi(
      markers.ul.min.x,
      markers.ul.min.y,
      Math.abs(markers.ur.max.x - markers.ul.min.x),
      Math.abs(markers.dl.max.y - markers.ul.min.y)
    );
    new ImagePlus("rotated", rotated.crop()).show();
  }
}
