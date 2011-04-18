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

/**
 * Base class for things that can be drawn in SVG or in Java.
 */
public abstract class Drawable
{
	/**
	 * Draws this shape to a graphics context.
	 * @param g Graphics context
	 */
	public abstract void draw(Graphics2D g);

	/**
	 * Draws this shape to SVG file.
	 * @param svg String for SVG output.
	 */
	public abstract void draw(StringBuilder svg);
}
