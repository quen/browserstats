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

/**
 * Stores co-ordinate information for each graph point.
 */
class PointPosition
{
	private final static int POINT_CURVE_THRESHOLD = 20;
	final static int POINT_CURVE_SIZE = 5;
	double start, end;
	boolean curve;

	PointPosition(double start, double end, boolean last)
	{
		this.start = start;
		this.end = end;
		curve = last ? false : end - start > POINT_CURVE_THRESHOLD;
	}

	/**
	 * @return True if this point gets a curve after it.
	 */
	boolean hasCurve()
	{
		return curve;
	}

	/**
	 * @return Midpoint co-ordinate for text labels
	 */
	double getMiddle()
	{
		if(curve)
		{
			return ((end - POINT_CURVE_SIZE) + start) / 2.0;
		}
		else
		{
			return (end + start) / 2.0;
		}
	}
}