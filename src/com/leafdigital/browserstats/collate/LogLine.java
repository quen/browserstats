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

import java.util.EnumSet;

/** A single line of log data. */
public class LogLine
{
	/** Field available within the line */
	public enum Field
	{
		/** User-agent string */
		AGENT("agent"),
		/** Date in ISO format 2009-07-16 */
		DATE("date"),
		/** Time in ISO format 20:17:46 */
		TIME("time"),
		/** IP address string */
		IP("ip"),
		/** Request path */
		PATH("path"),
		/** HTTP status code */
		STATUS("status"),
		/** Entire line */
		LINE("line");

		private String name;
		Field(String name)
		{
			this.name = name;
		}

		static Field get(String name)
		{
			for(Field field : EnumSet.allOf(Field.class))
			{
				if(field.name.equals(name))
				{
					return field;
				}
			}
			throw new IllegalArgumentException("No such field name: " + name);
		}
	}

	private String line, userAgent, isoDate, isoTime, ip, path, status;
	private Category category;

	/**
	 * @param line Entire line
	 * @param userAgent User-agent string
	 * @param isoDate Date in ISO YYYY-MM-DD format
	 * @param isoTime Time in ISO HH:mm:ss format
	 * @param ip IP address (or other unique identifier)
	 * @param path Path
	 * @param status Status code
	 */
	LogLine(String line, String userAgent, String isoDate, String isoTime,
		String ip, String path, String status)
	{
		if(line == null)
		{
			throw new NullPointerException("Line may not be null");
		}
		this.line = line;
		if(userAgent == null)
		{
			throw new NullPointerException("User agent may not be null");
		}
		this.userAgent = userAgent;
		if(isoDate == null)
		{
			throw new NullPointerException("Date may not be null");
		}
		this.isoDate = isoDate;
		if(isoTime == null)
		{
			throw new NullPointerException("Time may not be null");
		}
		this.isoTime = isoTime;
		if(ip == null)
		{
			throw new NullPointerException("IP may not be null");
		}
		this.ip = ip;
		if(path == null)
		{
			throw new NullPointerException("Path may not be null");
		}
		this.path = path;
		if(status == null)
		{
			throw new NullPointerException("Status may not be null");
		}
		this.status = status;
	}

	/**
	 * Sets the category of the line. May only be called once.
	 * @param c Category (may be Category.NONE)
	 */
	void initCategory(Category c)
	{
		if(category!=null)
		{
			throw new IllegalStateException("Cannot set category more than once");
		}
		this.category = c;
	}

	/** @return User-agent string */
	public String getUserAgent()
	{
		return userAgent;
	}

	/** @return Date in ISO YYYY-MM-DD format */
	public String getIsoDate()
	{
		return isoDate;
	}

	/** @return Time in ISO HH:mm:ss format */
	public String getIsoTime()
	{
		return isoTime;
	}

	/** @return IP address (or other unique identifier) */
	public String getIp()
	{
		return ip;
	}

	/** @return Request path */
	public String getPath()
	{
		return path;
	}

	/** @return Category (may be Category.NONE) */
	public Category getCategory()
	{
		return category;
	}

	/** @return Entire line */
	public String getLine()
	{
		return line;
	}

	/** @return HTTP status code */
	public String getStatus()
	{
		return status;
	}

	/**
	 * Gets the specified field from this line.
	 * @param field Field
	 * @return Value of field
	 */
	public String get(Field field)
	{
		switch(field)
		{
		case AGENT:
			return userAgent;
		case DATE:
			return isoDate;
		case TIME:
			return isoTime;
		case IP:
			return ip;
		case PATH:
			return path;
		case STATUS:
			return status;
		default:
			return line;
		}
	}

	@Override
	public String toString()
	{
		return line;
	}

	/**
	 * @return Multi-line string showing all fields of the line
	 */
	public String getDescription()
	{
		StringBuilder result = new StringBuilder();
		for(Field f : EnumSet.allOf(Field.class))
		{
			result.append(f + ": [" + get(f) + "]\n");
		}
		result.append("Category: " + category);
		return result.toString();
	}
}
