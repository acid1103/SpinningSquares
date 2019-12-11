package org.abitoff.spinningsquares;

import java.awt.Rectangle;

import com.seisw.util.geom.Point2D;

public class Rect
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