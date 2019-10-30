package Filters;

public class Vector2i {

  public static double getAngle(Vector2i a, Vector2i b) {
    return Math.atan2(b.y, b.x) - Math.atan2(a.y, a.x);
  }

  public static Vector2i sub(Vector2i a, Vector2i b) {
    return new Vector2i(a.x - b.x, a.y - b.y);
  }

  public static Vector2i add(Vector2i a, Vector2i b) {
    return new Vector2i(a.x + b.x, a.y + b.y);
  }

  public static Vector2i shiftRight(Vector2i a, int shift) {
    return new Vector2i(a.x >> shift, a.y >> shift);
  }

  public int x;
  public int y;

  public Vector2i(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Vector2i() {
    this(0, 0);
  }

  public int manhatten() {
    return Math.abs(x) + Math.abs(y);
  }

  public int manhatten(final Vector2i v) {
    return Math.abs(x - v.x) + Math.abs(y - v.y);
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public String toString() {
    return String.format("(%d, %d)", x, y);
  }
}
