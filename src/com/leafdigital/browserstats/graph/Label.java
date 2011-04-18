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
 * A label that can be placed on the graph, containing a group name and
 * a percentage.
 */
class Label
{
	private final static int SEPARATOR = 5;

	private int groupIndex;
	private TextDrawable name, percentage;

	private double allocatedHeight, y;

	private int verticalPadding;

	/**
	 * @param groupIndex Index of group this label is for
	 * @param groupName Name of group (text of label)
	 * @param percentage Text of percentage
	 * @param fontName Font name
	 * @param color Colour
	 * @param fontSize Text size
	 * @param verticalPadding Vertical padding (either side) to use when
	 *   calculating height
	 */
	Label(int groupIndex, String groupName, String percentage,
		String fontName, Color color, int fontSize, int verticalPadding)
	{
		this.groupIndex = groupIndex;
		this.name = new TextDrawable(groupName, 0, 0,	color, fontName,
			true, fontSize);
		this.percentage = new TextDrawable(percentage, 0, 0, color, fontName,
			false, fontSize);
		this.verticalPadding = verticalPadding;
		allocatedHeight = getMinHeight();
	}

	double getWidth()
	{
		return (name.getWidth() + percentage.getWidth() + SEPARATOR);
	}

	/**
	 * @return Index of group that this label is for
	 */
	public int getGroup()
	{
		return groupIndex;
	}

	/**
	 * @return Minimum height for label, including text plus padding
	 */
	public double getMinHeight()
	{
		return name.getHeight() + verticalPadding * 2;
	}

	/**
	 * @param height Height to allocate to label
	 */
	public void allocateHeight(double height)
	{
		allocatedHeight = height;
	}

	public void spreadHeight(Label[] allLabels, int index)
	{
		// If this label is big enough, leave it
		double minHeight = getMinHeight();
		double difference = minHeight - allocatedHeight;
		if(difference <= 0)
		{
			return;
		}

		// Try to get height from surrounding labels (evenly on both sides)
		// in three passes

		// Left contribution (up to half required)
		double leftContribution = 0;
		if(index > 0)
		{
			for(int i=index-1; index>=0; index--)
			{
				double available = allLabels[i].allocatedHeight - minHeight;
				double required = difference/2 - leftContribution;
				if(available >= required)
				{
					allLabels[i].allocatedHeight -= required;
					leftContribution += required;
					break;
				}
				else if(available > 0)
				{
					allLabels[i].allocatedHeight -= available;
					leftContribution += available;
				}
			}
		}

		// Right contribution (up to whatever's needed)
		double rightContribution = 0;
		if(index < allLabels.length - 1)
		{
			for(int i=index+1; i<allLabels.length; i++)
			{
				double available = allLabels[i].allocatedHeight - minHeight;
				double required = difference - leftContribution - rightContribution;
				if(available >= required)
				{
					allLabels[i].allocatedHeight -= required;
					rightContribution += required;
					break;
				}
				else if(available > 0)
				{
					allLabels[i].allocatedHeight -= available;
					rightContribution += available;
				}
			}
		}

		// Left contribution (if right contribution didn't pan out)
		if(index > 0)
		{
			for(int i=index-1; index>=0 ; index--)
			{
				double available = allLabels[i].allocatedHeight - minHeight;
				double required = difference - leftContribution - rightContribution;
				if(available >= required)
				{
					allLabels[i].allocatedHeight -= required;
					leftContribution += required;
					break;
				}
				else if(available > 0)
				{
					allLabels[i].allocatedHeight -= available;
					leftContribution += available;
				}
			}
		}

		allocatedHeight += leftContribution + rightContribution;
	}

	/**
	 * Moves the label to its final position.
	 * @param x X
	 * @param y Y
	 */
	public void move(double x, double y)
	{
		this.y = y;
		double baseline = name.getVerticalMiddleY(y, y+allocatedHeight);
		name.move(x, baseline);
		percentage.move(x + name.getWidth() + SEPARATOR, baseline);
	}

	/**
	 * Adds this label to canvas.
	 * @param canvas Canvas
	 */
	public void addTo(Canvas canvas)
	{
		canvas.add(name);
		canvas.add(percentage);
	}

	/**
	 * @return Height allocated to this label
	 */
	public double getAllocatedHeight()
	{
		return allocatedHeight;
	}

	/**
	 * @return Y position of top of label area
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * Arranges the labels vertically, allowing each the minimum size to include
	 * the text plus a bit more.
	 * @param data Trend data
	 * @param point Point to use in trend data
	 * @param x X position to move label to
	 * @param y Y position of graph
	 * @param height Height of graph
	 * @param labels Labels to arrange
	 */
	static void distributeLabelsVertically(
		TrendData data, int point, double x, double y, double height,
		Label[] labels)
	{
		// Start by allocating available space according to the data
		double total = (double)data.getTotal(point);
		for(Label label : labels)
		{
			label.allocateHeight(
				((double)data.getValue(label.getGroup(), point) / total) * height);
		}

		// Now share it out to make everything reach minimums
		for(int i=0; i<labels.length; i++)
		{
			labels[i].spreadHeight(labels, i);
		}

		// Height is finalised, so move the labels
		double currentY = 0;
		for(Label label : labels)
		{
			label.move(x, y + currentY);
			currentY += label.getAllocatedHeight();
		}
	}
}