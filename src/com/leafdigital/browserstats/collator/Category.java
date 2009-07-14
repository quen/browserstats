package com.leafdigital.browserstats.collator;

import java.util.EnumSet;
import java.util.regex.*;

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
	
	private enum Field
	{
		AGENT("agent"), DATE("date"), TIME("time"), IP("ip"), LINE("line");
		
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
	};
	
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
	 * @param line Full line
	 * @param agent User agent
	 * @param isoDate ISO format date e.g. 2009-07-20
	 * @param isoTime ISO format time e.g. 23:00:14
	 * @param ip IP address
	 * @return True if the given line falls into this category
	 */
	public boolean match(String line, String agent, String isoDate, 
		String isoTime, String ip)
	{
		String compare;
		switch(field)
		{
		case AGENT:
			compare = agent;
			break;
		case DATE:
			compare = isoDate;
			break;
		case TIME:
			compare = isoTime;
			break;
		case IP:
			compare = ip;
			break;
		default:
			compare = line;
			break;
		}
		
		return regex.matcher(compare).find();
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
