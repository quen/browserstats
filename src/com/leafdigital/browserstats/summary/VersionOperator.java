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
package com.leafdigital.browserstats.summary;

enum VersionOperator
{
	GT("> "), GE(">= "), LT("< "), LE("<= "), EQ(""), NE("!= ");

	private String display;

	VersionOperator(String display)
	{
		this.display = display;
	}

	/**
	 * @return Display characters
	 */
	public String getDisplay()
	{
		return display;
	}

	/**
	 * Returns the operator matching the given name.
	 * @param name Operator name e.g. "ne"
	 * @return Operator value or null if no match
	 */
	static VersionOperator get(String name)
	{
		for(VersionOperator possible : VersionOperator.values())
		{
			if(possible.toString().toLowerCase().equals(name))
			{
				return possible;
			}
		}
		return null;
	}

	/**
	 * Checks whther this operator is true.
	 * @param required1k Required value (in 1k format e.g. 8.0 = 8000)
	 * @param actual1k Actual value in 1k format
	 * @return True if this operator matches the value
	 */
	boolean match(int required1k, int actual1k)
	{
		switch(this)
		{
		case GT : return actual1k > required1k;
		case GE : return actual1k >= required1k;
		case LT : return actual1k < required1k;
		case LE : return actual1k <= required1k;
		case EQ : return actual1k == required1k;
		case NE : return actual1k != required1k;
		}
		throw new Error("Unknown version operator");
	}
}