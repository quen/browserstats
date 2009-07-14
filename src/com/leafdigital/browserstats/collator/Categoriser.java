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
	 * @param line Entire line
	 * @param agent User agent
	 * @param isoDate ISO format date e.g. 2009-07-20
	 * @param isoTime ISO format time e.g. 23:00:14
	 * @param ip IP address
	 * @return Category for line (may be null if none match)
	 */
	public Category categorise(String line, String agent, String isoDate, String isoTime, String ip)
	{
		for(Category c : categories)
		{
			if(c.match(line, agent, isoDate, isoTime, ip))
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
