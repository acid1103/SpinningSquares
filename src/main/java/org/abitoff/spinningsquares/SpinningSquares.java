package org.abitoff.spinningsquares;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.DoubleBinaryOperator;

import javax.imageio.ImageIO;

import com.seisw.util.geom.Point2D;
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

public class SpinningSquares
{
	public static void main(String[] args) throws IOException
	{
		final int width = 1920;
		final int height = 1080;
		final int frames = 70;
		final double fps = 30;
		final double squareWidth = 100;
		final String outDir = "out";
		final boolean clearOutDir = true;
		DoubleBinaryOperator rotationFunction = (t, d) ->
		{
			return Math.sin(Math.min(Math.max(t - d / 1000, 0) * Math.PI / 2.0, Math.PI / 2.0)) * Math.PI / 2.0;
		};

		String outDirAbs = new File(outDir).getAbsolutePath();
		setupOutDir(outDirAbs, clearOutDir);

		int nSquaresWide = (int) Math.ceil((double) width / squareWidth / 2.0 + 1.5);
		int nSquaresTall = (int) Math.ceil((double) height / squareWidth / 2.0 + 1.5);
		Rect[][] rects = new Rect[2 * nSquaresWide - 1][2 * nSquaresTall - 1];
		for (int y = 0; y < nSquaresTall; y++)
		{
			for (int x = 0; x < nSquaresWide; x++)
			{
				rects[nSquaresWide + x - 1][nSquaresTall + y - 1] =
						new Rect(new Point2D(x * squareWidth, y * squareWidth), squareWidth, squareWidth, 0);
				if (x != 0)
					rects[nSquaresWide - x - 1][nSquaresTall + y - 1] =
							new Rect(new Point2D(-x * squareWidth, y * squareWidth), squareWidth, squareWidth, 0);
				if (y != 0)
					rects[nSquaresWide + x - 1][nSquaresTall - y - 1] =
							new Rect(new Point2D(x * squareWidth, -y * squareWidth), squareWidth, squareWidth, 0);
				if (x != 0 && y != 0)
					rects[nSquaresWide - x - 1][nSquaresTall - y - 1] =
							new Rect(new Point2D(-x * squareWidth, -y * squareWidth), squareWidth, squareWidth, 0);
			}
		}
		for (int i = 0; i < frames; i++)
		{
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			double[][] values = new double[width][height];
			double t = i / fps;
			for (Rect[] rectsSubArray: rects)
			{
				for (Rect r: rectsSubArray)
				{
					double d = Math.sqrt(
							r.getCenter().getX() * r.getCenter().getX() + r.getCenter().getY() * r.getCenter().getY());
					r.setRotation(rotationFunction.applyAsDouble(t, d));
					Rectangle bounds = r.getBounds();
					for (int y = (int) bounds.getMinY(); y < bounds.getMaxY(); y++)
					{
						int realY = y + height / 2;
						if (realY < 0 || realY >= height)
							continue;
						for (int x = (int) bounds.getMinX(); x < bounds.getMaxX(); x++)
						{
							int realX = x + width / 2;
							if (realX < 0 || realX >= width)
								continue;
							Rect pixel = new Rect(new Point2D(x + 0.5, y + 0.5), 1, 1, 0);
							double overlap = overlapArea(r, pixel);
							values[realX][realY] = doubleXor(values[realX][realY], overlap);
						}
					}
				}
			}
			for (int x = 0; x < values.length; x++)
			{
				for (int y = 0; y < values[x].length; y++)
				{
					img.setRGB(x, y, rgb(values[x][y], 0));
				}
			}
			ImageIO.write(img, "png", new File(outDirAbs + "/" + i + ".png"));
		}
	}

	private static int rgb(double d, int bg)
	{
		d = Math.min(Math.max(d, 0), 1);
		int bgr = (int) ((1 - d) * ((~bg >> 16) & 0xff));
		int bgg = (int) ((1 - d) * ((~bg >> 8) & 0xff));
		int bgb = (int) ((1 - d) * ((~bg >> 0) & 0xff));
		int r = ((int) bgr << 16) & 0xff0000;
		int g = ((int) bgg << 8) & 0xff00;
		int b = ((int) bgb << 0) & 0xff;
		return ~(r | g | b);
	}

	private static double doubleXor(double a, double b)
	{
		return Math.abs(a - b);
	}

	private static void setupOutDir(String outDir, boolean clearOutDir)
	{
		File out = new File(outDir);
		if (!out.exists())
			if (!out.mkdir())
				throw new RuntimeException("Making directory " + outDir + " failed!");
		if (clearOutDir)
			for (File f: out.listFiles())
				if (!f.delete())
					throw new RuntimeException("Could not delete " + f.getAbsolutePath() + "!");
	}

	private static double overlapArea(Rect r1, Rect r2)
	{
		Poly p1 = new PolyDefault();
		Poly p2 = new PolyDefault();
		for (Point2D p: r1.getPoints())
			p1.add(p.getX(), p.getY());
		for (Point2D p: r2.getPoints())
			p2.add(p.getX(), p.getY());
		Poly intersection = p1.intersection(p2);
		return intersection.getArea();
	}

	@SuppressWarnings("unused")
	private static final class Rect
	{
		private static final double PI_2 = Math.PI / 2.0;
		private boolean cacheValid = false;
		private Point2D[] pointCache;
		private Point2D center;
		private double width, height, rotation;

		public Rect(Point2D center, double width, double height, double rotation)
		{
			assert width > 0 && height > 0:"";
			this.center = center;
			this.width = width;
			this.height = height;
			this.rotation = rotation;
		}

		public final Point2D getCenter()
		{
			return center;
		}

		public final void setCenter(Point2D center)
		{
			cacheValid = false;
			this.center = center;
		}

		public final double getWidth()
		{
			return width;
		}

		public final void setWidth(double width)
		{
			cacheValid = false;
			this.width = width;
		}

		public final double getHeight()
		{
			return height;
		}

		public final void setHeight(double height)
		{
			cacheValid = false;
			this.height = height;
		}

		public final double getRotation()
		{
			return rotation;
		}

		public final void setRotation(double rotation)
		{
			cacheValid = false;
			this.rotation = rotation;
		}

		public Point2D[] getPoints()
		{
			if (cacheValid)
				return pointCache;
			double d = Math.sqrt(width * width + height * height) / 2.0;
			double a = Math.atan(height / width);
			rotation = rotation % Math.PI;
			if (rotation < 0)
				rotation += Math.PI;
			double a1 = a + rotation;
			double a2 = a - rotation;
			double c1 = Math.cos(a1);
			double c2 = Math.cos(a2);
			double s1 = Math.sin(a1);
			double s2 = Math.sin(a2);
			Point2D p1 = new Point2D(center.getX() - d * c1, center.getY() - d * s1);
			Point2D p2 = new Point2D(center.getX() + d * c2, center.getY() - d * s2);
			Point2D p3 = new Point2D(center.getX() + d * c1, center.getY() + d * s1);
			Point2D p4 = new Point2D(center.getX() - d * c2, center.getY() + d * s2);
			Point2D[] points;
			if (rotation < PI_2)
				points = new Point2D[] {p1, p2, p3, p4};
			else
				points = new Point2D[] {p4, p1, p2, p3};
			pointCache = points;
			cacheValid = true;
			return points;
		}

		public Rectangle getBounds()
		{
			Point2D[] points = getPoints();
			int minX = roundAwayFromZero(points[3].getX());
			int minY = roundAwayFromZero(points[0].getY());
			int maxX = roundAwayFromZero(points[1].getX());
			int maxY = roundAwayFromZero(points[2].getY());
			return new Rectangle(minX, minY, maxX - minX, maxY - minY);
		}

		private static int roundAwayFromZero(double n)
		{
			return (int) (n < 0 ? Math.floor(n) : Math.ceil(n));
		}
	}
}
