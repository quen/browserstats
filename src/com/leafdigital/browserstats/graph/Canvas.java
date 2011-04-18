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

import java.io.IOException;

/**
 * SVG or PNG drawing object.
 */
public abstract class Canvas
{
	private int width, height;

	/**
	 * @param width Pixel width
	 * @param height Pixel height
	 */
	public Canvas(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * @return With in pixels
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * @return Height in pixels
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Adds a drawable object to this canvas.
	 * @param drawable Object to add
	 */
	public abstract void add(Drawable drawable);

	/**
	 * Saves this canvas and returns the contents as a byte array ready for
	 * writing to a file.
	 * @return Canvas data
	 * @throws IOException Any I/O error during save
	 */
	public abstract byte[] save() throws IOException;

	/**
	 * @return File extension including "."
	 */
	public abstract String getExtension();
}
