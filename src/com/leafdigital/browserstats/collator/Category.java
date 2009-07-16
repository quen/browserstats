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
