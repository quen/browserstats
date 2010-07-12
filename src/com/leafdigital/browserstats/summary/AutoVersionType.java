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

enum AutoVersionType
{
	FULL_OTHER("full+"), FULL_DISCARD("full"),
	MIN_OTHER("min+"), MIN_DISCARD("min");

	private String name;
	AutoVersionType(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the type matching the given name.
	 * @param name Type name e.g. "full+"
	 * @return Type value or null if no match
	 */
	static AutoVersionType get(String name)
	{
		for(AutoVersionType possible : AutoVersionType.values())
		{
			if(possible.name.equals(name))
			{
				return possible;
			}
		}
		return null;
	}
}