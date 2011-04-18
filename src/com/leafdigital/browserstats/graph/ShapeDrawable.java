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
import java.awt.geom.GeneralPath;
import java.util.LinkedList;

import static com.leafdigital.browserstats.graph.SvgCanvas.svgRound;

/**
 * A shape that can be drawn to PNG or SVG.
 */
public class ShapeDrawable extends Drawable
{
	private Color color;
	private double startX, startY, currentX, currentY;
	private LinkedList<Segment> segments = new LinkedList<Segment>();
	private boolean finished;

	/**
	 * One segment of the shape.
	 */
	private static abstract class Segment
	{
		/**
		 * Applies this segment to a Java2D path object.
		 * @param path Path
		 */
		abstract void apply(GeneralPath path);

		/**
		 * Applies this segment to an SVG path d= attribute.
		 * @param svg String builder (currently in midst of d= attribute)
		 */
		abstract void apply(StringBuilder svg);
	}

	/**
	 * A line segment.
	 */
	private static class Line extends Segment
	{
		private double nextX, nextY;

		/**
		 * @param nextX X position for end of line
		 * @param nextY Y position for end of line
		 */
		Line(double nextX, double nextY)
		{
			this.nextX = nextX;
			this.nextY = nextY;
		}

		@Override
		void apply(GeneralPath path)
		{
			path.lineTo(nextX, nextY);
		}

		@Override
		void apply(StringBuilder svg)
		{
			svg.append(" L " + svgRound(nextX) + "," + svgRound(nextY));
		}
	}

	/**
	 * A curve segment.
	 */
	private static class Curve extends Segment
	{
		private double control1X, control1Y, control2X, control2Y, nextX, nextY;

		/**
		 * @param control1X X position of first control point
		 * @param control1Y Y position of first control point
		 * @param control2X X position of second control point
		 * @param control2Y Y position of second control point
		 * @param nextX X position for end of curve
		 * @param nextY Y position for end of curve
		 */
		Curve(double control1X, double control1Y, double control2X,
			double control2Y, double nextX, double nextY)
		{
			this.control1X = control1X;
			this.control1Y = control1Y;
			this.control2X = control2X;
			this.control2Y = control2Y;
			this.nextX = nextX;
			this.nextY = nextY;
		}

		@Override
		void apply(GeneralPath path)
		{
			path.curveTo(control1X, control1Y, control2X, control2Y, nextX, nextY);
		}

		@Override
		void apply(StringBuilder svg)
		{
			svg.append("C " + svgRound(control1X) + "," + svgRound(control1Y) + ","
				+ svgRound(control2X) + "," + svgRound(control2Y) + ","
				+ svgRound(nextX) + "," + svgRound(nextY));
		}
	}

	/**
	 * Flag that can be used from API to make it trace output.
	 */
	public static boolean DEBUG = false;
	private static boolean DEBUG_INNER = false;

	/**
	 * Constructs shape with initial point.
	 * @param startX X co-ordinate in pixels
	 * @param startY Y co-ordinate in pixels
	 * @param color Colour of shape
	 */
	public ShapeDrawable(double startX, double startY, Color color)
	{
		this.startX = startX;
		this.startY = startY;
		this.color = color;
		currentX = startX;
		currentY = startY;
		if(DEBUG)
		{
			System.err.println("Shape: " + svgRound(startX) + "," + svgRound(startY));
		}
	}

	/**
	 * Adds a line to the shape.
	 * @param nextX X position for end of line
	 * @param nextY Y position for end of line
	 */
	public void lineTo(double nextX, double nextY)
	{
		checkNotFinished();
		segments.add(new Line(nextX, nextY));
		currentX = nextX;
		currentY = nextY;
		if(DEBUG)
		{
			System.err.println("Line: " + svgRound(nextX) + "," + svgRound(nextY));
		}
	}

	/**
	 * Adds a curve to the shape.
	 * @param control1X X position of first control point
	 * @param control1Y Y position of first control point
	 * @param control2X X position of second control point
	 * @param control2Y Y position of second control point
	 * @param nextX X position for end of curve
	 * @param nextY Y position for end of curve
	 */
	public void curveTo(double control1X, double control1Y,
		double control2X, double control2Y, double nextX, double nextY)
	{
		checkNotFinished();
		segments.add(new Curve(control1X, control1Y, control2X, control2Y,
			nextX, nextY));
		currentX = nextX;
		currentY = nextY;
		if(DEBUG && !DEBUG_INNER)
		{
			System.err.println("Curve: " + svgRound(nextX) + "," + svgRound(nextY)
				+ " (controls " + svgRound(control1X) + "," + svgRound(control1Y) + " "
				+ svgRound(control2X) + "," + svgRound(control2Y) + ")");
		}
	}

	/**
	 * Adds a horizontal curve to the shape.
	 * @param nextX X position for end of curve
	 * @param nextY Y position for end of curve
	 */
	public void flatCurveTo(double nextX, double nextY)
	{
		double midpoint = (nextX + currentX) / 2;
		if(DEBUG)
		{
			DEBUG_INNER = true;
		}
		curveTo(midpoint, currentY, midpoint, nextY, nextX, nextY);
		if(DEBUG)
		{
			DEBUG_INNER = false;
			System.err.println("Flat curve: " + nextX + "," + nextY);
		}
	}

	/**
	 * Finishes the shape. This will close it with a straight line back to the
	 * start position.
	 */
	public void finish()
	{
		if(DEBUG)
		{
			System.err.println("Finish");
		}
		finished = true;
	}

	private void checkNotFinished() throws IllegalStateException
	{
		if(finished)
		{
			throw new IllegalStateException("Shape finished");
		}
	}

	private void checkFinished() throws IllegalStateException
	{
		if(!finished)
		{
			throw new IllegalStateException("Shape not finished");
		}
	}

	/**
	 * @return Current X position
	 */
	public double getCurrentX()
	{
		return currentX;
	}

	/**
	 * @return Current Y position
	 */
	public double getCurrentY()
	{
		return currentY;
	}

	/**
	 * @return Colour
	 */
	public Color getColor()
	{
		return color;
	}

	@Override
	public void draw(Graphics2D g)
	{
		GeneralPath path = getPath();
		g.setColor(color);
		g.fill(path);
	}

	/**
	 * @return Java2D path for shape (not closed yet)
	 */
	protected GeneralPath getPath()
	{
		checkFinished();
		GeneralPath path = new GeneralPath();
		path.moveTo(startX, startY);
		for(Segment segment : segments)
		{
			segment.apply(path);
		}
		if(closePath())
		{
			path.closePath();
		}
		return path;
	}

	@Override
	public void draw(StringBuilder svg)
	{
		checkFinished();
		svg.append("<path");
		svg.append(getSvgPathAttributes());
		svg.append(" d='M ");
		svg.append(svgRound(startX));
		svg.append(",");
		svg.append(svgRound(startY));
		for(Segment segment : segments)
		{
			segment.apply(svg);
		}
		if(closePath())
		{
			svg.append(" z");
		}
		svg.append("'/>\n");
	}

	/**
	 * @return SVG attributes for path tag, beginning with a space
	 */
	protected String getSvgPathAttributes()
	{
		return " fill='" + ColorUtils.getSvgColor(color) + "'";
	}

	/**
	 * @return True to close the paths
	 */
	protected boolean closePath()
	{
		return true;
	}
}
