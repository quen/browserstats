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

import java.awt.Color;
import java.util.LinkedList;

/**
 * Manages the creation of a 'worm' of data across the graph representing
 * a single browser group.
 */
class GraphWorm
{
	private final static int FOOTNOTE_PADDING = 4;
	private ShapeDrawable shape;
	private LinkedList<GraphWorm.Segment> segments = new LinkedList<GraphWorm.Segment>();
	private double footnoteX = -1, footnoteY;

	private double startX, startBelowY;

	private static class Segment
	{
		double x, aboveY, belowY;
		boolean curve;

		protected Segment(double x, double aboveY, double belowY, boolean curve)
		{
			this.x = x;
			this.aboveY = aboveY;
			this.belowY = belowY;
			this.curve = curve;
		}

		void applyForward(ShapeDrawable shape, GraphWorm.Segment next, GraphWorm.Segment previous)
		{
			if(curve)
			{
				shape.flatCurveTo(x, aboveY);
			}
			else
			{
				if(previous != null)
				{
					shape.lineTo(previous.x, aboveY);
				}
				shape.lineTo(x, aboveY);
			}
			if(next == null)
			{
				shape.lineTo(x, belowY);
			}
		}

		void applyBackward(ShapeDrawable shape, GraphWorm.Segment previous)
		{
			applyBackward(shape, previous.x, previous.belowY);
		}

		void applyBackward(ShapeDrawable shape, double previousX, double previousY)
		{
			if(curve)
			{
				shape.flatCurveTo(previousX, previousY);
			}
			else
			{
				shape.lineTo(previousX, belowY);
				shape.lineTo(previousX, previousY);
			}
		}
	}

	/**
	 * Constructs shape.
	 * @param startX X
	 * @param startAboveY Y (top)
	 * @param startBelowY Y (bottom); may be the same as aboveY if this shape
	 *   begins from a single point
	 * @param color Colour
	 */
	GraphWorm(double startX, double startAboveY, double startBelowY,
		Color color)
	{
		shape = new ShapeDrawable(startX, startAboveY, color);
		this.startX = startX;
		this.startBelowY = startBelowY;
		if(startAboveY != startBelowY)
		{
			footnoteX = startX + FOOTNOTE_PADDING;
			footnoteY = (startAboveY + startBelowY) / 2.0;
		}
	}

	/**
	 * Adds a curved segment.
	 * @param x X of right end of curve
	 * @param aboveY Top Y of right end
	 * @param belowY Bottom Y of right end
	 */
	void makeCurve(double x, double aboveY, double belowY)
	{
		segments.add(new Segment(x, aboveY, belowY, true));
	}

	/**
	 * Adds a straight segment.
	 * @param x X of right end of straight
	 * @param aboveY Top Y of right end
	 * @param belowY Bottom Y of right end
	 */
	void makeStraight(double x, double aboveY, double belowY)
	{
		if(footnoteX < 0)
		{
			footnoteX = x + FOOTNOTE_PADDING;
			footnoteY = (aboveY + belowY) / 2.0;
		}
		segments.add(new Segment(x, aboveY, belowY, false));
	}

	/**
	 * Finishes shape and adds to canvas.
	 * @param canvas
	 */
	void addTo(Canvas canvas)
	{
		GraphWorm.Segment[] segmentArray = segments.toArray(new GraphWorm.Segment[segments.size()]);
		for(int i=0; i<segmentArray.length; i++)
		{
			segmentArray[i].applyForward(shape, i==segmentArray.length - 1 ? null
				: segmentArray[i+1], i>0 ? segmentArray[i-1] : null);
		}
		for(int i=segmentArray.length-1; i>0; i--)
		{
			segmentArray[i].applyBackward(shape, segmentArray[i-1]);
		}
		segmentArray[0].applyBackward(shape, startX, startBelowY);
		shape.finish();
		canvas.add(shape);
	}

	/**
	 * @return X position for footnote number within this shape (if needed)
	 */
	public double getFootnoteX()
	{
		return footnoteX;
	}

	/**
	 * @return Y position for footnote number within this shape (if needed)
	 */
	public double getFootnoteY()
	{
		return footnoteY;
	}
}