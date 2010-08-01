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
package com.leafdigital.browserstats.collator;

import java.io.*;
import java.util.*;

import com.leafdigital.browserstats.collator.Collate.TimePeriod;

/**
 * Counts user agents in one or a number of date categories and outputs
 * the result to files.
 */
public class AgentCounter
{
	private File folder;
	private String prefix;
	private TimePeriod period;
	private boolean unordered, overwrite;
	private Category[] categories;
	private boolean stdout;

	private HashMap<String, AgentCount> counts = new HashMap<String, AgentCount>();
	private HashSet<String> past = new HashSet<String>();

	/**
	 * @param folder Folder for output files
	 * @param prefix Prefix for output files
	 * @param period Time period to divide output files
	 * @param unordered True if input lines may be unordered
	 * @param overwrite True if it's OK to overwrite existing files
	 * @param categories List of categories
	 * @param stdout Write to stdout instead of file
	 */
	public AgentCounter(File folder, String prefix, TimePeriod period,
		boolean unordered, boolean overwrite, Category[] categories, boolean stdout)
	{
		this.folder = folder;
		this.prefix = prefix;
		this.period = period;
		this.unordered = unordered;
		this.overwrite = overwrite;
		this.categories = categories;
		this.stdout = stdout;
	}

	/**
	 * Processes a single log line.
	 * @param line Line
	 * @throws IOException If any I/O error occurs
	 */
	void process(LogLine line) throws IOException
	{
		AgentCount count = null;
		String currentPeriod = null;

		switch(period)
		{
		case ALL :
		{
			count = counts.get(null);
		}	break;

		case YEARLY :
		{
			currentPeriod = line.getIsoDate().substring(0, 4);
			count = counts.get(currentPeriod);
			// If there is no data for this month...
			if(count==null)
			{
				// Give an error if that's because this month is in the past
				if(past.contains(currentPeriod))
				{
					throw new IOException("Line out of sequence (try -unordered):\n"
						+ line);
				}
			}
		} break;

		case MONTHLY :
		{
			currentPeriod = line.getIsoDate().substring(0, 7);
			count = counts.get(currentPeriod);
			// If there is no data for this month...
			if(count==null)
			{
				// Give an error if that's because this month is in the past
				if(past.contains(currentPeriod))
				{
					throw new IOException("Line out of sequence (try -unordered):\n"
						+ line);
				}
			}
		} break;

		case DAILY :
		{
			currentPeriod = line.getIsoDate();
			count = counts.get(currentPeriod);
			if(count==null)
			{
				if(past.contains(currentPeriod))
				{
					throw new IOException("Line out of sequence (try -unordered):\n"
						+ line);
				}
			}
		} break;

		}

		// Create new data if required
		if(count==null)
		{
			if(!stdout)
			{
				System.err.print("\n" +
					(currentPeriod == null ? "Output" : currentPeriod) + ":");
			}
			count = new AgentCount();
			counts.put(currentPeriod, count);
		}

		// Flush out older data after 1am on the next day
		if(!unordered && counts.size() > 1
			&& line.getIsoTime().compareTo("01:00:00") > 0)
		{
			for(Iterator<String> i=counts.keySet().iterator(); i.hasNext();)
			{
				String period = i.next();
				if(period.compareTo(currentPeriod) < 0)
				{
					// Flush out old period and free RAM
					flush(period);
					i.remove();
					System.gc(); // Just to make the stats (maybe) work
				}
			}
		}

		// Actually count data
		count.count(line.getUserAgent(), line.getIp(), line.getCategory(), !stdout);
	}

	/**
	 * Flushes a single disk file. Does not actually remove from list.
	 * @param timePeriod Time period
	 * @throws IOException If any I/O error occurs
	 */
	private void flush(String timePeriod) throws IOException
	{
		AgentCount count = counts.get(timePeriod);
		if(stdout)
		{
			count.write(null, timePeriod, categories);
		}
		else
		{
			File target = new File(folder, prefix +
				(timePeriod == null ? "" : "." + timePeriod) + ".useragents");
			if (target.exists() && !overwrite)
			{
				throw new IOException("Would overwrite " + target
					+ ", aborting. (Use -overwrite to allow.)");
			}
			count.write(target, timePeriod, categories);
		}
		past.add(timePeriod);
	}

	/**
	 * Flushes all data to disk. Used at end of process.
	 * @throws IOException If any I/O error occurs
	 */
  void flush() throws IOException
  {
  	for(String period : counts.keySet())
  	{
  		flush(period);
  	}
  	if(!stdout)
  	{
  		System.err.println("\n");
  	}
  }

}
