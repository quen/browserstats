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

import java.util.*;

/**
 * Gathers data from all input files into a single grid of data with all groups.
 */
public class TrendData
{
	private InputFile[] points;
	private String[] groups;
	private int[][] values;

	/**
	 * Constructs data
	 * @param points Input files (in order)
	 * @throws IllegalArgumentException If there aren't at least two input files
	 */
	public TrendData(InputFile[] points) throws IllegalArgumentException
	{
		if(points.length < 2)
		{
			throw new IllegalArgumentException("Must have at least two points");
		}
		this.points = points;

		// Set up groups from first input file
		LinkedList<String> groupList = new LinkedList<String>(
			Arrays.asList(points[0].getGroupNames()));
		HashSet<String> knownGroups = new HashSet<String>(groupList);

		// Process each other input file, adding in group names
		for(int point=1; point<points.length; point++)
		{
			String[] names = points[point].getGroupNames();
			for(int name=0; name<names.length; name++)
			{
				// If the group is already included, stop
				if(knownGroups.contains(names[name]))
				{
					continue;
				}
				// We're about to know it...
				knownGroups.add(names[name]);
				if(name == 0)
				{
					// Add at the start if it's at the start...
					groupList.addFirst(names[name]);
				}
				else if(name == names.length - 1)
				{
					// ...and the end if it's at the end...
					groupList.addLast(names[name]);
				}
				else
				{
					// Add next to the thing before it (which we know we already added)
					int beforeIndex = groupList.indexOf(names[name-1]);
					groupList.add(beforeIndex+1, names[name]);
				}
			}
		}

		// Store group names
		groups = groupList.toArray(new String[groupList.size()]);

		// Get values
		values = new int[groups.length][];
		for(int group=0; group<groups.length; group++)
		{
			values[group] = new int[points.length];
			for(int point=0; point<points.length; point++)
			{
				values[group][point] = points[point].getCount(groups[group]);
			}
		}
	}

	/**
	 * @return Number of points along horizontal axis of graph (at least 2)
	 */
	public int getNumPoints()
	{
		return points.length;
	}

	/**
	 * @param point Point index
	 * @return Linear date (days since 1970) of the start of the point
	 * @throws IllegalArgumentException If point is out of range
	 */
	public int getPointLinearDateStart(int point) throws IllegalArgumentException
	{
		checkPoint(point);
		return points[point].getLinearDateStart();
	}

	/**
	 * @param point Point index
	 * @return Linear date (days since 1970) of the end of the point
	 * @throws IllegalArgumentException If point is out of range
	 */
	public int getPointLinearDateEnd(int point) throws IllegalArgumentException
	{
		checkPoint(point);
		return points[point].getLinearDateEnd();
	}

	/**
	 * @param point Point index
	 * @return Date text for that point
	 * @throws IllegalArgumentException If point is out of range
	 */
	public String getPointDate(int point) throws IllegalArgumentException
	{
		checkPoint(point);
		return points[point].getDate();
	}

	private void checkPoint(int point) throws IllegalArgumentException
	{
		if(point < 0 || point > points.length)
		{
			throw new IllegalArgumentException("Invalid point: " + point);
		}
	}

	/**
	 * @return Number of groups along vertical axis of graph
	 */
	public int getNumGroups()
	{
		return groups.length;
	}

	/**
	 * @param group Group index
	 * @return Group name for that index
	 * @throws IllegalArgumentException If group is out of range
	 */
	public String getGroupName(int group) throws IllegalArgumentException
	{
		checkGroup(group);
		return groups[group];
	}

	/**
	 * @return All group names (do not modify)
	 */
	public String[] getGroupNames()
	{
		return groups;
	}

	/**
	 * @param group Group index
	 * @param point Point index
	 * @return Value for that point
	 * @throws IllegalArgumentException If group or point is out of range
	 */
	public int getValue(int group, int point) throws IllegalArgumentException
	{
		checkGroup(group);
		checkPoint(point);
		return values[group][point];
	}

	/**
	 * @param group Group index
	 * @param point Point index
	 * @return Value of all groups before, but not including, this one at this
	 *   point
	 * @throws IllegalArgumentException If group or point is out of range
	 */
	public int getCumulativeValueBefore(int group, int point) throws IllegalArgumentException
	{
		checkGroup(group);
		checkPoint(point);
		int total = 0;
		for(int i=0; i<group; i++)
		{
			total += values[i][point];
		}
		return total;
	}

	/**
	 * @param point Point index
	 * @return Total of all counts at that point
	 * @throws IllegalArgumentException
	 */
	public int getTotal(int point) throws IllegalArgumentException
	{
		checkPoint(point);
		int total = 0;
		for(int i=0; i<values.length; i++)
		{
			total += values[i][point];
		}
		return total;
	}

	/**
	 * Converts a point value into a percentage.
	 * @param group Group index
	 * @param point Point index
	 * @return Percentage rounded to one decimal place, ending in %
	 * @throws IllegalArgumentException
	 */
	public String getPercentage(int group, int point)
		throws IllegalArgumentException
	{
		int total = getTotal(point);
		if(total == 0)
		{
			return "0.0%";
		}
		String value = "" + Math.round(
			(double)getValue(group, point) * 1000.0 / (double)total);
		if(value.length() < 2)
		{
			value = "0" + value;
		}
		return value.substring(0, value.length()-1) + "."
			+ value.substring(value.length()-1) + "%";
	}

	private void checkGroup(int group) throws IllegalArgumentException
	{
		if(group < 0 || group > groups.length)
		{
			throw new IllegalArgumentException("Invalid group: " + group);
		}
	}
}
