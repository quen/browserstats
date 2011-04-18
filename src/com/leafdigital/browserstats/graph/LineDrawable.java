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

/**
 * A line that can be drawn to PNG or SVG.
 */
public class LineDrawable extends ShapeDrawable
{
	private double width;

	/**
	 * Constructs line with initial point.
	 * @param startX X co-ordinate in pixels
	 * @param startY Y co-ordinate in pixels
	 * @param width Width of line
	 * @param color Colour of line
	 */
	public LineDrawable(double startX, double startY, double width, Color color)
	{
		super(startX, startY, color);
		this.width = width;
	}

	@Override
	public void draw(Graphics2D g)
	{
		GeneralPath path = super.getPath();
		g.setColor(getColor());
		g.setStroke(new BasicStroke((float)width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		g.draw(path);
	}

	@Override
	protected String getSvgPathAttributes()
	{
		return " fill='none' stroke='" + ColorUtils.getSvgColor(getColor())
			+ "' stroke-width='" + SvgCanvas.svgRound(width) + "'";
	}

	@Override
	protected boolean closePath()
	{
		return false;
	}
}
