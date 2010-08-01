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

import java.text.*;
import java.util.regex.*;

/** Format describing method of reading log lines. */
public class LogFormat
{
	private Pattern regex, skip;
	private int ipField, dateField, timeField, agentField, pathField, statusField;
	private SimpleDateFormat dateFormat, timeFormat, isoDateFormat, isoTimeFormat;
	private boolean decodeAgent;

	/**
	 * @param regex Regular expression to parse line
	 * @param ipField Index of field that contains IP address or other unique
	 *   identifier
	 * @param dateField Index of field that contains date
	 * @param timeField Index of field that contains time
	 * @param agentField Index of field that contains user agent
	 * @param pathField Index of field that contains path
	 * @param statusField Index of field that contains HTTP status code
	 * @param dateFormat Format for date (SimpleDateFormat style)
	 * @param timeFormat Format for time (SimpleDateFormat style)
	 * @throws IllegalArgumentException If any of the arguments are invalid
	 */
	LogFormat(String regex, String ipField, String dateField, String timeField,
		String agentField, String pathField, String statusField, String dateFormat, String timeFormat)
		throws IllegalArgumentException
	{
		try
		{
			this.regex = Pattern.compile(regex);
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException("Invalid log format <line>: " +
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
		if(agentField.endsWith("+"))
		{
			decodeAgent = true;
			agentField = agentField.substring(0, agentField.length()-1);
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
		this.statusField = parseInt(statusField, "Invalid status field index");
		if(this.statusField <= 0 || this.statusField > groups)
		{
			throw new IllegalArgumentException("Status field index out of range: "
				+ this.statusField);
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
	 * Sets the pattern used for lines to skip.
	 * @param skip Regular expression
	 * @throws IllegalArgumentException If the pattern isn't valid
	 */
	void setSkip(String skip) throws IllegalArgumentException
	{
		try
		{
			this.skip = Pattern.compile(skip);
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException("Invalid log format <skip>: " +
				e.getDescription());
		}
	}

	/**
	 * Parses a single line from the log file.
	 * @param line Line text
	 * @param c Categoriser used to assign categories
	 * @return Line in processed form, or null if the line is to be skipped
	 * @throws IllegalArgumentException If the input line does not match
	 *   the specified format
	 */
	LogLine parse(String line, Categoriser c) throws IllegalArgumentException
	{
		if(skip!=null && skip.matcher(line).find())
		{
			return null;
		}

		Matcher m = regex.matcher(line);
		if(!m.find())
		{
			throw new IllegalArgumentException("Doesn't match <line> regex");
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
		if(agent == null)
		{
			agent = "";
		}
		if(decodeAgent)
		{
			agent = agent.replace('+', ' ');
		}
		String ip = m.group(ipField);
		if(ip == null)
		{
			ip = "";
		}
		String path = m.group(pathField);
		if(path == null)
		{
			path = "";
		}
		String status = m.group(statusField);
		if(status == null)
		{
			status = "";
		}
		LogLine result = new LogLine(line, agent, isoDate, isoTime, ip, path, status);
		result.initCategory(c.categorise(result));
		return result;
	}


}
