package com.leafdigital.browserstats.collator;

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
		/** Entire line */
		LINE("line"),
		/** Request path */
		PATH("path"),
		/** HTTP status code */
		STATUS("status");
		
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
		this.line = line;
		this.userAgent = userAgent;
		this.isoDate = isoDate;
		this.isoTime = isoTime;
		this.ip = ip;
		this.path = path;
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
