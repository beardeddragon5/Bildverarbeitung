package Filters;

import ij.*;
import ij.process.*;

public class Rotate_Bilinear {

  public static ImageProcessor rotate(ImageProcessor ip, double alpha) {
  final int	undefinedPixel	= 0xffffff; // that's just white
  final byte	undefinedPixel_byte = (byte) undefinedPixel;

  ImageProcessor ip_rota;

  double cos_alpha = Math.cos(Math.PI*alpha/180.0);
  double sin_alpha = Math.sin(Math.PI*alpha/180.0);

  // the rotated image should be centered rather than suspended by the upper left corner
  // compute where the rotated image center goes
  double x_ctr =  0.5*ip.getWidth()*cos_alpha + 0.5*ip.getHeight()*sin_alpha;
  double y_ctr = -0.5*ip.getWidth()*sin_alpha + 0.5*ip.getHeight()*cos_alpha;

  double offset_x = 0.5*ip.getWidth()  - x_ctr;
  double offset_y = 0.5*ip.getHeight() - y_ctr;

  if (ip instanceof ColorProcessor) {
    ip_rota = new ColorProcessor(ip.getWidth(), ip.getHeight());
    int[] rota_pixels = (int[]) ip_rota.getPixels();

    int idx = 0;
    for (int i=0; i<ip.getHeight(); i++)
      for (int j=0; j<ip.getWidth(); j++) {
        double x_ori =  j*cos_alpha + i*sin_alpha + offset_x;
        double y_ori = -j*sin_alpha + i*cos_alpha + offset_y;

        if (x_ori>=0 && y_ori>=0 && x_ori<ip.getWidth()-1 && y_ori<ip.getHeight()-1)
          rota_pixels[idx] = interpol_int(x_ori, y_ori, (ColorProcessor) ip, undefinedPixel);
        else
          rota_pixels[idx] = undefinedPixel;
        idx++;
      }
  } else  // assume ByteProcessor
  {
    ip_rota = new  ByteProcessor(ip.getWidth(), ip.getHeight());
    byte[] rota_pixels = (byte[]) ip_rota.getPixels();

    int idx = 0;
    for (int i=0; i<ip.getHeight(); i++)
      for (int j=0; j<ip.getWidth(); j++) {
        double x_ori =  j*cos_alpha + i*sin_alpha + offset_x;
        double y_ori = -j*sin_alpha + i*cos_alpha + offset_y;

        if (x_ori>=0 && y_ori>=0 && x_ori<ip.getWidth()-1 && y_ori<ip.getHeight()-1)
          rota_pixels[idx] = (byte) interpol_byte(x_ori, y_ori, (ByteProcessor) ip, undefinedPixel);
        else
          rota_pixels[idx] = undefinedPixel_byte;
        idx++;
      }
  }

  return ip_rota;
}

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  // interpol_byte() returns a bilinearly interpolated int value
  static int interpol_byte(double x_ori, double y_ori, ByteProcessor ip, int undefinedPixel) {
    // compute coordinates of upper left corner
    int ul_x = (int) x_ori;
    int ul_y = (int) y_ori;

    // compute distance to upper left corner
    double dx = x_ori - ul_x;
    double dy = y_ori - ul_y;

    // make sure all four pixels are defined
    if (ul_x >= ip.getWidth()-1)
      return undefinedPixel;
    if (ul_y >= ip.getHeight()-1)
      return undefinedPixel;

    // extract int values at grid points
    byte[] pixels = (byte[]) ip.getPixels();
    int ulPos = ul_y*ip.getWidth() + ul_x;

    int ul = pixels[ulPos] & 0xff;
    int ur = pixels[ulPos+1] & 0xff;
    int ll = pixels[ulPos+ip.getWidth()] & 0xff;
    int lr = pixels[ulPos+ip.getWidth()+1] & 0xff;

    double bilin = bilinear(dx, dy, ul, ur, ll, lr);

    return (int) bilin;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  // interpol_int() returns a bilinearly interpolated int value (=RGB)
  static int interpol_int(double x_ori, double y_ori, ColorProcessor ip, int undefinedPixel) {
    int[] ul_rgb = new int[3];
    int[] ur_rgb = new int[3];
    int[] ll_rgb = new int[3];
    int[] lr_rgb = new int[3];
    int[] bl_rgb = new int[3];

    // compute coordinates of upper left corner
    int ul_x = (int) x_ori;
    int ul_y = (int) y_ori;

    // compute distance to upper left corner
    double dx = x_ori - ul_x;
    double dy = y_ori - ul_y;

    // make sure all four pixels are defined
    if (ul_x >= ip.getWidth()-1)
      return undefinedPixel;
    if (ul_y >= ip.getHeight()-1)
      return undefinedPixel;

    int[] pixels = (int[]) ip.getPixels();
    int ulPos = ul_y*ip.getWidth() + ul_x;

    // extract int values at grid points
    // red
    ul_rgb[0] = (pixels[ulPos] & 0xff0000) >> 16;
    ur_rgb[0] = (pixels[ulPos+1] & 0xff0000) >> 16;
    ll_rgb[0] = (pixels[ulPos+ip.getWidth()] & 0xff0000) >> 16;
    lr_rgb[0] = (pixels[ulPos+ip.getWidth()+1] & 0xff0000) >> 16;
    // green
    ul_rgb[1] = (pixels[ulPos] & 0x00ff00) >> 8;
    ur_rgb[1] = (pixels[ulPos+1] & 0x00ff00) >> 8;
    ll_rgb[1] = (pixels[ulPos+ip.getWidth()] & 0x00ff00) >> 8;
    lr_rgb[1] = (pixels[ulPos+ip.getWidth()+1] & 0x00ff00) >> 8;
    // blue
    ul_rgb[2] = (pixels[ulPos] & 0x0000ff);
    ur_rgb[2] = (pixels[ulPos+1] & 0x0000ff);
    ll_rgb[2] = (pixels[ulPos+ip.getWidth()] & 0x0000ff);
    lr_rgb[2] = (pixels[ulPos+ip.getWidth()+1] & 0x0000ff);

    // interpolate each color channel separately
    for (int i=0; i<3; i++)
      bl_rgb[i] = (int)(bilinear(dx, dy, ul_rgb[i], ur_rgb[i], ll_rgb[i], lr_rgb[i]) + 0.5);

    return ((bl_rgb[0] & 0xff)<<16) + ((bl_rgb[1] & 0xff)<<8) + (bl_rgb[2] & 0xff);
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  // bilinear() returns the value at a distance dx, dy away from the upper left corner
  static double bilinear (double dx, double dy, int ul, int ur, int ll, int lr) {
    double up = ul + (ur-ul)*dx;
    double lp = ll + (lr-ll)*dx;
    return      up + (lp-up)*dy;
  }

}
