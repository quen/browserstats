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

import org.w3c.dom.Element;

import com.leafdigital.browserstats.shared.SpecialNames;

/**
 * Stores a group name and associated count.
 */
public class GroupCount
{
	private String name;
	private int count;

	/**
	 * @param e XML element
	 * @param attribute Attribute name for count
	 * @throws IOException Any problem with this attribute
	 */
	public GroupCount(Element e, String attribute) throws IOException
	{
		name = e.getAttribute("name");
		if(name.isEmpty())
		{
			throw new IOException("Missing name for group");
		}
		try
		{
			count = Integer.parseInt(e.getAttribute(attribute));
		}
		catch(NumberFormatException x)
		{
			throw new IOException("Group '" + name + "' has invalid or missing "
				+ "count attribute: " + attribute);
		}
	}

	/**
	 * @return Name of this group
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Number of requests to this group
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return True if this is the 'excluded' count
	 */
	public boolean isExcluded()
	{
		return name.equals(SpecialNames.GROUP_EXCLUDED);
	}
}
