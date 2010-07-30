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
 * A legend entry consists of a label plus a colour box. Used in the pie
 * chart.
 */
class LegendEntry
{
	private final static int SEPARATOR = 5, BOX_OUTLINE = 1;
	private Label label;
	private Color groupColour, foreground;
	private double x, y;

	/**
	 * @param groupIndex Index of group this label is for
	 * @param groupColour Colour of group
	 * @param groupName Name of group (text of label)
	 * @param percentage Text of percentage
	 * @param fontName Font name
	 * @param foreground Colour of text
	 * @param fontSize Text size
	 */
	LegendEntry(int groupIndex, Color groupColour, String groupName,
		String percentage, String fontName, Color foreground, int fontSize)
	{
		this.label = new Label(groupIndex, groupName, percentage, fontName,
			foreground, fontSize, 0);
		this.groupColour = groupColour;
		this.foreground = foreground;
		label.allocateHeight(getHeight());
	}

	/**
	 * @return Required width for legend entry
	 */
	double getWidth()
	{
		return label.getWidth() + SEPARATOR + label.getMinHeight();
	}

	/**
	 * @return Height of legend entry (does not include any padding)
	 */
	double getHeight()
	{
		return label.getMinHeight();
	}

	/**
	 * Moves this legend to its final position.
	 * @param x X position (top left)
	 * @param y Y position (top left)
	 */
	void move(double x, double y)
	{
		this.x = x;
		this.y = y;
		label.move(x + label.getMinHeight() + SEPARATOR, y);
	}

	/**
	 * Adds this legend to a canvas.
	 * @param canvas Canvas that will receive drawable objects
	 */
	void addTo(Canvas canvas)
	{
		RectDrawable outer = new RectDrawable(x, y, label.getMinHeight(),
			label.getMinHeight(), foreground);
		RectDrawable inner = new RectDrawable(x + BOX_OUTLINE, y + BOX_OUTLINE,
			label.getMinHeight() - 2 * BOX_OUTLINE,
			label.getMinHeight() - 2 * BOX_OUTLINE, groupColour);

		canvas.add(outer);
		canvas.add(inner);
		label.addTo(canvas);
	}
}