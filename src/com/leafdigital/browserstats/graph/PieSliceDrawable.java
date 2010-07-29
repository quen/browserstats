/*
This file is part of leafdigital browserstats.

browserstats is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

browserstats is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with browserstats.  If not, see <http://www.gnu.org/licenses/>.

Copyright 2010 Samuel Marshall.
*/
package com.leafdigital.browserstats.graph;

import java.awt.*;
import java.awt.geom.*;

import static com.leafdigital.browserstats.graph.SvgCanvas.svgRound;
import static java.lang.Math.*;

/**
 * A pie slice that can be drawn to PNG or SVG.
 */
public class PieSliceDrawable extends Drawable
{
	private double middleX, middleY, radius, startAngle, degrees;
	private Color color;

	/**
	 * Constructs pie slice.
	 * @param middleX Centre point of circle X
	 * @param middleY Centre point of circle Y
	 * @param radius Radius
	 * @param startAngle Starting point in degrees (0 = twelve o'clock)
	 * @param degrees Width of slice in degrees
	 * @param color Colour of slice
	 */
	public PieSliceDrawable(double middleX, double middleY, double radius,
		double startAngle, double degrees, Color color)
	{
		this.middleX = middleX;
		this.middleY = middleY;
		this.radius = radius;
		this.startAngle = startAngle;
		this.degrees = degrees;
		this.color = color;
	}

	@Override
	public void draw(Graphics2D g)
	{
		Arc2D arc = new Arc2D.Double(middleX - radius, middleY - radius,
			radius * 2, radius * 2, 90 - startAngle, -degrees, Arc2D.PIE);
		g.setColor(color);
		g.fill(arc);
	}

	@Override
	public void draw(StringBuilder svg)
	{
		// SVG needs to know if it's bigger than 180 degrees
		String largeArc = degrees>=180 ? "1" : "0";

		// Calculate co-ordinates for end points
		Point2D p1 = getRadius(false), p2 = getRadius(true);

		svg.append("<path");
		svg.append(" fill='" + ColorUtils.getSvgColor(color) + "'");
		svg.append(" d='M " + svgRound(middleX) + "," + svgRound(middleY));
		svg.append(" L " + svgRound(p1.getX()) + "," +svgRound(p1.getY()));
		svg.append(" A " + svgRound(radius) + "," + svgRound(radius) + " 0 "
			+ largeArc + " 1 " + svgRound(p2.getX()) + "," +svgRound(p2.getY()));
		svg.append(" z'/>");
	}

	/**
	 * @return End co-ordinate in radians
	 */
	private double getEndRadians()
	{
		return ((startAngle + degrees) * 2 * PI / 360.0) - PI/2;
	}

	/**
	 * @return Start co-ordinate in radians
	 */
	private double getStartRadians()
	{
		return (startAngle * 2 * PI / 360.0) - PI/2;
	}

	/**
	 * @param second If true, uses second arc, otherwise uses first
	 * @return Co-ordinates of point where arc intersects circumference
	 */
	public Point2D.Double getRadius(boolean second)
	{
		double radians = second ? getEndRadians() : getStartRadians();
		return new Point2D.Double(
			radius * cos(radians) + middleX,
			radius * sin(radians) + middleY);
	}

	/**
	 * Returns a line representing one of the two radii that make up the slice,
	 * which can be used for overprinting.
	 * @param second If true, uses second arc, otherwise uses first
	 * @param width Width of line
	 * @return New line
	 */
	public LineDrawable getOverprintRadius(boolean second, double width)
	{
		LineDrawable radius = new LineDrawable(middleX, middleY, width, color);
		Point2D p = getRadius(second);
		radius.lineTo(p.getX(), p.getY());
		radius.finish();
		return radius;
	}
}
