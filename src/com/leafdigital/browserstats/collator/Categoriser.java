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
package com.leafdigital.browserstats.collator;

import java.util.LinkedList;

/** Class that categorises log lines according to one or more rules. */
public class Categoriser
{
	private LinkedList<Category> categories = new LinkedList<Category>();

	void addCategory(Category c)
	{
		categories.addLast(c);
	}

	/**
	 * @param line Log line
	 * @return Category for line (Category.NONE if none match)
	 */
	public Category categorise(LogLine line)
	{
		for(Category c : categories)
		{
			if(c.match(line))
			{
				return c;
			}
		}
		return Category.NONE;
	}

	/**
	 * @return All categories
	 */
	public Category[] getCategories()
	{
		return categories.toArray(new Category[categories.size()]);
	}
}
