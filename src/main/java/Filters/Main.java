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

    final ImagePlus image = IJ.openImage(get("..", "BinaryObjects.png"));
    image.show();

    IJ.openImage(get("..", "DT.png")).show();

    IJ.runPlugIn(image, DT_Filter.class.getName(), "");
  }
}
