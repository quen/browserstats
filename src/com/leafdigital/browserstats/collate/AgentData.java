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
package com.leafdigital.browserstats.collate;

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