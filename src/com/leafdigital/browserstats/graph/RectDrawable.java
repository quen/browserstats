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

/**
 * A rectangle that can be drawn to PNG or SVG.
 */
public class RectDrawable extends ShapeDrawable
{
	/**
	 * @param startX X of top left
	 * @param startY Y of top left
	 * @param width Width of box
	 * @param height Height of box
	 * @param color Colour
	 */
	public RectDrawable(double startX, double startY, double width, double height,
		Color color)
	{
		super(startX, startY, color);
		lineTo(startX + width, startY);
		lineTo(startX + width, startY + height);
		lineTo(startX, startY + height);
		finish();
	}

}
