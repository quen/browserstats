package com.leafdigital.browserstats.collator;

import java.io.*;
import java.util.*;

import com.leafdigital.util.xml.XML;

/** Stores data related to a single agent type */
class AgentData
{
	int count;
	private HashMap<Category,Integer> categories = new HashMap<Category, Integer>();
	
	/**
	 * Counts a line with this agent.
	 * @param ip IP address
	 * @param c Category
	 */
	void count(String ip, Category c)
	{
		count++;
		if(!c.equals(Category.NONE))
		{
			Integer i = categories.get(c);
			if(i==null)
			{
				categories.put(c,1);
			}
			else
			{
				categories.put(c, i+1);
			}
		}
	}
	
	/**
	 * Writes information to XML about this agent.
	 * @param w Writer
	 * @param agent Agent name
	 * @param categoryList Available categories
	 * @throws IOException Any error writing
	 */
	void write(Writer w, String agent, Category[] categoryList) throws IOException
	{
		StringBuilder builder = new StringBuilder("<agent count='");
		builder.append(count);
		builder.append("'");
		for(Category c : categoryList)
		{
			builder.append(' ');
			builder.append(c.getName());
			builder.append("='");
			Integer i = categories.get(c);
			int count = i!=null ? i : 0;
			builder.append(count);
			builder.append('\'');
		}
		builder.append('>');
		builder.append(XML.esc(agent, false));
		builder.append("</agent>\n");
		w.write(builder.toString());
	}
}