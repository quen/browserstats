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
import java.util.Map;


/**
 * A footnote for the trend graph, including the number that goes in the
 * graph, then the boxed number and group name that are placed below.
 */
class Footnote
{
	private final static int BOX_HORIZONTAL_PADDING = 4,
		BOX_VERTICAL_PADDING = 2, BOX_BORDER = 1, BOX_SPACING = 5;
	private int groupIndex;
	private TextDrawable name, numberGraph, numberFootnote;

	private double x;
	private int lineIndex;

	Footnote(int groupIndex, TextDrawable name, TextDrawable numberGraph,
		TextDrawable numberFootnote)
	{
		this.groupIndex = groupIndex;
		this.name = name;
		this.numberGraph = numberGraph;
		this.numberFootnote = numberFootnote;
	}

	double getWidth()
	{
		return name.getWidth() + numberFootnote.getWidth() +
			BOX_BORDER * 2 + BOX_HORIZONTAL_PADDING * 2 + BOX_SPACING;
	}

	double getHeight()
	{
		return numberFootnote.getHeight() + BOX_BORDER * 2
			+ BOX_VERTICAL_PADDING * 2;
	}

	/**
	 * @return Group index
	 */
	public int getGroup()
	{
		return groupIndex;
	}

	void setRoughPosition(double x, int lineIndex)
	{
		this.x = x;
		this.lineIndex = lineIndex;
	}

	/**
	 * Sets the position of the number in the graph (inside the first
	 * shape for that group).
	 * @param x X position
	 * @param y Y position
	 */
	void setGraphPosition(double x, double y)
	{
		numberGraph.move(x, numberGraph.getVerticalMiddleY(y, y));
	}

	/**
	 * @param canvas Canvas that receives data
	 * @param baseY Y position of footnote area
	 * @param rowHeight Height of each row (to convert line index into Y)
	 * @param groupNames Group names
	 * @param colours Map from group name to colour
	 * @param border Border colour
	 */
	public void addTo(Canvas canvas, double baseY, double rowHeight,
		String[] groupNames, Map<String, GroupColours> colours,
		Color border)
	{
		double y = baseY + lineIndex * rowHeight;
		double boxHeight = getHeight();
		double boxWidth = numberFootnote.getWidth() + BOX_HORIZONTAL_PADDING * 2
			+ BOX_BORDER * 2;

		// Fill and outline box
		canvas.add(new RectDrawable(x + BOX_BORDER, y + BOX_BORDER,
			boxWidth - 2 * BOX_BORDER, boxHeight - 2 * BOX_BORDER,
			colours.get(groupNames[groupIndex]).getMain()));
		canvas.add(new RectDrawable(x, y, boxWidth, BOX_BORDER, border));
		canvas.add(new RectDrawable(x, y+boxHeight - BOX_BORDER, boxWidth, BOX_BORDER, border));
		canvas.add(new RectDrawable(x, y + BOX_BORDER, BOX_BORDER, boxHeight - 2 * BOX_BORDER, border));
		canvas.add(new RectDrawable(x + boxWidth - BOX_BORDER, y + BOX_BORDER, BOX_BORDER, boxHeight - 2 * BOX_BORDER, border));

		// Draw number
		double baseline = numberFootnote.getVerticalMiddleY(y, y+boxHeight);
		numberFootnote.move(x + BOX_BORDER + BOX_HORIZONTAL_PADDING, baseline);
		canvas.add(numberFootnote);
		name.move(x + boxWidth + BOX_SPACING, baseline);
		canvas.add(name);
		canvas.add(numberGraph);
	}
}