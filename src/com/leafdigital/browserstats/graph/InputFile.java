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

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A single file from the input.
 */
public class InputFile implements Comparable<InputFile>
{
	private String date;
	private File file;
	private int linearDateStart, linearDateEnd;
	private int total;
	private GroupCount[] groupCounts;

	private Pattern DATE_REGEX = Pattern.compile("\\.([0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?)\\.");

	/**
	 * Reads data and initialises values.
	 * @param file Input file or null to use stdin
	 * @param category Category to read or null to use total
	 * @throws IOException If there is any error in the input file
	 */
	public InputFile(File file, String category) throws IOException
	{
		this.file = file;

		// Load input XML
		Document d;
		try
		{
			DocumentBuilder builder =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			if(file != null)
			{
				d = builder.parse(file);

				// For files, try to find date
				Matcher m = DATE_REGEX.matcher(file.getName());
				if(m.find())
				{
					date = m.group(1);
					linearDateStart = getLinearDateStart(date);
					linearDateEnd = getLinearDateEnd(date);
				}
			}
			else
			{
				d = builder.parse(System.in, "stdin");
			}
		}
		catch(ParserConfigurationException e)
		{
			throw new IOException("Misconfigured Java XML support: "
				+ e.getMessage());
		}
		catch(SAXException e)
		{
			throw new IOException("Invalid XML input: " + e.getMessage());
		}

		// Parse XML
		Element root = d.getDocumentElement();
		if(!root.getTagName().equals("summary"))
		{
			throw new IOException("XML root tag <summary> not found");
		}

		// Check category
		String attribute = "count";
		if(category != null)
		{
			String[] categories = new String[0];
			if(root.hasAttribute("categories")
				&& root.getAttribute("categories").length() > 0)
			{
				categories = root.getAttribute("categories").split(",");
			}
			boolean found = false;
			for(String foundCategory : categories)
			{
				if(category.equals(foundCategory))
				{
					found = true;
					break;
				}
			}
			if(!found)
			{
				throw new IOException("File does not contain required category: "
					+ category);
			}
			attribute = category;
		}

		// Get total
		try
		{
			total = Integer.parseInt(root.getAttribute(attribute));
		}
		catch(NumberFormatException e)
		{
			throw new IOException("<summary> attribute " + attribute
				+ " missing or not a valid number");
		}

		LinkedList<GroupCount> groupCountList = new LinkedList<GroupCount>();
		for(Node n = root.getFirstChild(); n!=null;
			n = n.getNextSibling())
		{
			if(n instanceof Element && ((Element)n).getTagName().equals("group"))
			{
				GroupCount groupCount = new GroupCount((Element)n, attribute);
				if(!groupCount.isExcluded())
				{
					groupCountList.add(groupCount);
				}
			}
		}
		groupCounts = groupCountList.toArray(new GroupCount[groupCountList.size()]);
	}

	private static int getLinearDateStart(String date) throws IOException
	{
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(
				date.length() == 10 ? "yyyy-MM-dd" : date.length() == 7 ? "yyyy-MM" :
					"yyyy");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date actualDate = format.parse(date);
			return getLinearDate(actualDate);
		}
		catch(ParseException e)
		{
			throw new IOException("Invalid date: " + date);
		}
	}

	private static int getLinearDateEnd(String date) throws IOException
	{
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(
				date.length() == 10 ? "yyyy-MM-dd" : date.length() == 7 ? "yyyy-MM" :
					"yyyy");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date actualDate = format.parse(date);
			Calendar c = Calendar.getInstance();
			c.setTime(actualDate);
			switch(date.length())
			{
			case 10 : c.add(Calendar.DAY_OF_MONTH, 1); break;
			case 7 : c.add(Calendar.MONTH, 1); break;
			default: c.add(Calendar.YEAR, 1); break;
			}
			return getLinearDate(c.getTime());
		}
		catch(ParseException e)
		{
			throw new IOException("Invalid date: " + date);
		}
	}

	private static int getLinearDate(Date date)
	{
		// Count number of days (this assumes we haven't messed about with the
		// calendar so much since 1970 that it's skipped half a day, and that
		// nobody tries to run weblogs for 345 AD or something)
		long halfDay = 12 * 3600 * 1000;
		long day = 24 * 3600 * 1000;
		return (int)((date.getTime() + halfDay) / day);
	}

	/**
	 * @return Filename or "stdin"
	 */
	public String getName()
	{
		return file == null ? "stdin" : file.getName();
	}

	/**
	 * @return Input file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * @return Linear date (days since 1970) of start of point
	 */
	public int getLinearDateStart()
	{
		return linearDateStart;
	}

	/**
	 * @return Linear date (days since 1970) of end of point
	 */
	public int getLinearDateEnd()
	{
		return linearDateEnd;
	}

	/**
	 * @return Date text, or null if not set
	 */
	public String getDate()
	{
		return date;
	}

	/**
	 * @return Total count for this file
	 */
	public int getTotal()
	{
		return total;
	}

	/**
	 * @return Names of all groups in this file, in the order that they appeasr
	 *   in the file
	 */
	public String[] getGroupNames()
	{
		String[] names = new String[groupCounts.length];
		for(int i=0; i<groupCounts.length; i++)
		{
			names[i] = groupCounts[i].getName();
		}
		return names;
	}

	/**
	 * @param group Named group
	 * @return Group count for specified group in this file; 0 if group is not
	 *   in this file
	 */
	public int getCount(String group)
	{
		for(GroupCount count : groupCounts)
		{
			if(count.getName().equals(group))
			{
				return count.getCount();
			}
		}
		return 0;
	}

	@Override
	public int compareTo(InputFile o)
	{
		if(date == null || o.date == null)
		{
			throw new UnsupportedOperationException(
				"Cannot compare files with no date");
		}

		return date.compareTo(o.date);
	}
}
