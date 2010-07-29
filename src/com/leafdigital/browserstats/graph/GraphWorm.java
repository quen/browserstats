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
	private final static double OVERPRINT_PIXELS = 1;

	private LinkedList<GraphWorm.Segment> segments = new LinkedList<GraphWorm.Segment>();
	private double footnoteX = -1, footnoteY;
	private boolean last;

	private double startX, startAboveY, startBelowY;
	private Color color;

	private class Segment
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

		/**
		 * Constructs a copy of this segment for overprinting the bottom line.
		 * @param original Original segment
		 */
		Segment(Segment original)
		{
			this.x = original.x;
			this.aboveY = original.belowY - OVERPRINT_PIXELS/2;
			this.belowY = original.belowY + OVERPRINT_PIXELS/2;
			this.curve = original.curve;
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
			applyBackward(shape, previous.x, previous.belowY, false);
		}

		void applyBackward(ShapeDrawable shape, double previousX, double previousY,
			boolean first)
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
	 * @param last True if this is the last one (don't try to overprint)
	 */
	GraphWorm(double startX, double startAboveY, double startBelowY,
		Color color, boolean last)
	{
		this.color = color;
		this.startX = startX;
		this.startAboveY = startAboveY;
		this.startBelowY = startBelowY;
		this.last = last;
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
		// Draw main shape
		ShapeDrawable shape = new ShapeDrawable(startX, startAboveY, color);
		GraphWorm.Segment[] segmentArray = segments.toArray(new GraphWorm.Segment[segments.size()]);
		draw(canvas, shape, segmentArray);

		// Except for last one, do overprint shape
		if(!last)
		{
			shape = new ShapeDrawable(startX, startBelowY - OVERPRINT_PIXELS/2, color);
			Segment[] overprint = new Segment[segmentArray.length];
			for(int i=0; i<segmentArray.length; i++)
			{
				overprint[i] = new Segment(segmentArray[i]);
			}
			draw(canvas, shape, overprint);
		}
	}

	/**
	 * @param canvas
	 * @param shape
	 * @param segmentArray
	 */
	private void draw(Canvas canvas, ShapeDrawable shape,
		GraphWorm.Segment[] segmentArray)
	{
		for(int i=0; i<segmentArray.length; i++)
		{
			segmentArray[i].applyForward(shape, i==segmentArray.length - 1 ? null
				: segmentArray[i+1], i>0 ? segmentArray[i-1] : null);
		}
		for(int i=segmentArray.length-1; i>0; i--)
		{
			segmentArray[i].applyBackward(shape, segmentArray[i-1]);
		}
		segmentArray[0].applyBackward(shape, startX, startBelowY, true);
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