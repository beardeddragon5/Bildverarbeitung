package Filters;

import java.util.ArrayDeque;
import java.util.Deque;

public class Flood {

  public static class Segment {
    public final int start;
    public int area;

    public Vector2i min;
    public Vector2i max;
    public Vector2i center;
    public Vector2i size;

    public Segment(int start) {
      this.start = start;
      this.min = new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
      this.max = new Vector2i(0, 0);
    }
  }

  public final boolean[] flooded;
  private final byte[] image;
  private final int imageWidth;
  private final int imageHeight;

  public Flood(byte[] image, int imageWidth, int imageHeight) {
    this.flooded = new boolean[image.length];
    this.image = image;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public Segment findSegment(int start, byte color) {
    final Segment out = new Segment(start);
    final Deque<Integer> stack = new ArrayDeque<>();
    stack.push(start);
    while(!stack.isEmpty()) {
      int index = stack.pop();
      if (!flooded[index] && image[index] == color) {
        flooded[index] = true;
        out.area++;

        final int y = index / imageWidth;
        final int x = index % imageWidth;

        out.min.y = y < out.min.y ? y : out.min.y;
        out.min.x = x < out.min.x ? x : out.min.x;
        out.max.y = y > out.max.y ? y : out.max.y;
        out.max.x = x > out.max.x ? x : out.max.x;

        if (y - 1 >= 0) stack.push(index - imageWidth);
        if (y + 1 < imageHeight) stack.push(index + imageWidth);
        if (x - 1 >= 0) stack.push(index - 1);
        if (x + 1 < imageWidth) stack.push(index + 1);
      }
    }
    out.size = Vector2i.sub(out.max, out.min);
    out.center = Vector2i.add(out.min, Vector2i.shiftRight(out.size, 1));
    return out;
  }
}
