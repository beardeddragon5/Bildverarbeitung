package Filters;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.nio.file.Paths;

/**
 * Main Method for testing Filters
 * @author matthias
 */
public interface Main {

  /**
   * Get Path to Absolute Path String
   * @see java.nio.file.Paths#get(String, String...)
   * equal to:
   * <code>
   *  Paths.get(arg, args).toAbsolutePath().toString()
   * </code>
   * @param arg First path part
   * @param args Remaining path parts
   * @return abolute string of path
   */
  public static String get(String arg, String... args) {
    return Paths.get(arg, args).toAbsolutePath().toString();
  }

  /**
   * Setup ImageJ and setup Environment for testing filters
   * @param args ignored
   */
  public static void main(String... args) {
    final ImageJ img = new ImageJ();
    img.exitWhenQuitting(true);
    img.toFront();
    // {
    //   final ImagePlus image = IJ.openImage(get("..", "Iris.jpg"));
    //   image.show();
    //   IJ.openImage(get("..", "Iris_BF5_20.tif")).show();
    //   IJ.runPlugIn(image, Bilateral_Filter.class.getName(), "");
    // }
    // {
    //   final ImagePlus image = IJ.openImage(get("..", "QR_Japan.png"));
    //   image.show();
    //   IJ.runPlugIn(image, ASB.class.getName(), "");
    // }
    // {
    //   final ImagePlus image = IJ.openImage(get("..", "QR_LKW_grey.png"));
    //   image.show();
    //   IJ.runPlugIn(image, ASB.class.getName(), "");
    // }
    {
      final ImagePlus image = IJ.openImage(get("..", "QR_i.tif"));
      image.show();
      IJ.runPlugIn(image, QRCode_Filter.class.getName(), "");
    }
  }
}
