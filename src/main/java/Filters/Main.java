package Filters;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.nio.file.Paths;

/**
 *
 * @author matthias
 */
public class Main {
  public static void main(String... args) {
    final ImageJ img = new ImageJ();
    img.exitWhenQuitting(true);

    ImagePlus image = IJ.openImage(Paths.get("..", "BinaryObjects.png").toAbsolutePath().toString());
    image.show();


    ImagePlus image2 = IJ.openImage(Paths.get("..", "DT.png").toAbsolutePath().toString());
    image2.show();


    img.toFront();

    IJ.runPlugIn(image, DT_Filter.class.getName(), "");
  }
}
