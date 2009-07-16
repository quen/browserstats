package com.leafdigital.browserstats.collator;

import java.text.*;
import java.util.regex.*;

/** Format describing method of reading log lines. */
public class LogFormat
{
	private Pattern regex;
	private int ipField, dateField, timeField, agentField, pathField;
	private SimpleDateFormat dateFormat, timeFormat, isoDateFormat, isoTimeFormat;
	
	/**
	 * @param regex Regular expression to parse line
	 * @param ipField Index of field that contains IP address or other unique
	 *   identifier
	 * @param dateField Index of field that contains date
	 * @param timeField Index of field that contains time
	 * @param agentField Index of field that contains user agent
	 * @param pathField Index of field that contains path
	 * @param dateFormat Format for date (SimpleDateFormat style)
	 * @param timeFormat Format for time (SimpleDateFormat style)
	 * @throws IllegalArgumentException If any of the arguments are invalid
	 */
	LogFormat(String regex, String ipField, String dateField, String timeField, 
		String agentField, String pathField, String dateFormat, String timeFormat) 
		throws IllegalArgumentException
	{
		try
		{
			this.regex = Pattern.compile(regex);		
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException("Invalid log format: " + 
				e.getDescription());
		}
		int groups = this.regex.matcher("").groupCount();
		this.ipField = parseInt(ipField, "Invalid IP field index");
		if(this.ipField <= 0 || this.ipField > groups)
		{
			throw new IllegalArgumentException("IP field index out of range: " 
				+ this.ipField);
		}
		this.dateField = parseInt(dateField, "Invalid date field index");
		if(this.dateField <= 0 || this.dateField > groups)
		{
			throw new IllegalArgumentException("Date field index out of range: " 
				+ this.dateField);
		}
		this.timeField = parseInt(timeField, "Invalid time field index");
		if(this.timeField <= 0 || this.timeField > groups)
		{
			throw new IllegalArgumentException("Time field index out of range: " 
				+ this.timeField);
		}
		this.agentField = parseInt(agentField, "Invalid agent field index");
		if(this.agentField <= 0 || this.agentField > groups)
		{
			throw new IllegalArgumentException("Agent field index out of range: " 
				+ this.agentField);
		}
		this.pathField = parseInt(pathField, "Invalid path field index");
		if(this.pathField <= 0 || this.pathField > groups)
		{
			throw new IllegalArgumentException("Path field index out of range: " 
				+ this.pathField);
		}
		try
		{
			this.dateFormat = new SimpleDateFormat(dateFormat);
		}
		catch(IllegalArgumentException e)		
		{
			throw new IllegalArgumentException("Invalid date format: " + dateFormat);
		}
		try
		{
			this.timeFormat = new SimpleDateFormat(timeFormat);
		}
		catch(IllegalArgumentException e)		
		{
			throw new IllegalArgumentException("Invalid time format: " + timeFormat);
		}
		isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		isoTimeFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	private static int parseInt(String field, String message)
	{
		try
		{
			return Integer.parseInt(field);
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException(message + ": " + field);
		}
	}

	/**
	 * Parses a single line from the log file.
	 * @param line Line text
	 * @param c Categoriser used to assign categories
	 * @return Line in processed form
	 * @throws IllegalArgumentException If the input line does not match
	 *   the specified format
	 */
	LogLine parse(String line, Categoriser c) throws IllegalArgumentException
	{
		Matcher m = regex.matcher(line);
		if(!m.matches())
		{
			throw new IllegalArgumentException("Invalid input line");
		}
		
		String isoDate;
		try
		{
			isoDate = isoDateFormat.format(dateFormat.parse(m.group(dateField)));
		}
		catch(ParseException e)
		{
			throw new IllegalArgumentException("Invalid date format: " +
				m.group(dateField));
		}
		String isoTime;
		try
		{
			isoTime = isoTimeFormat.format(timeFormat.parse(m.group(timeField)));
		}
		catch(ParseException e)
		{
			throw new IllegalArgumentException("Invalid time format: " +
				m.group(timeField));
		}
		
		String agent = m.group(agentField);
		String ip = m.group(ipField);
		String path = m.group(pathField);
		LogLine result = new LogLine(line, agent, isoDate, isoTime, ip, path);
		result.initCategory(c.categorise(result));
		return result;
	}
	

}
