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
