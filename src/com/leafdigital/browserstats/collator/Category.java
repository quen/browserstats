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

import java.util.regex.*;

import com.leafdigital.browserstats.collator.LogLine.Field;

/** Category assigned to a line. */
public class Category
{
	/**
	 * Used to indicate that a line has no particular category.
	 */
	public static final Category NONE = new Category("", "line", ".");

	private String name;
	private Field field;
	private Pattern regex;

	/**
	 * @param name Category name
	 * @param field Description of which field the category applies too
	 * @param regex Regular expression
	 * @throws IllegalArgumentException
	 */
	Category(String name, String field, String regex)
		throws IllegalArgumentException
	{
		this.name = name;
		if(!this.name.matches("[a-z0-9]*"))
		{
			throw new IllegalArgumentException(
				"Invalid category name (lower-case letters and digits only): " + name);
		}
		this.field = Field.get(field);
		try
		{
			this.regex = Pattern.compile(regex);
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException("Invalid category regex: " +
				e.getDescription());
		}
	}

	/** @return Name */
	public String getName()
	{
		return name;
	}

	/**
	 * @param line Log line
	 * @return True if the given line falls into this category
	 */
	public boolean match(LogLine line)
	{
		return regex.matcher(line.get(field)).find();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

}
