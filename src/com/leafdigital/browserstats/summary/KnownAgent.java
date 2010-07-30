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
package com.leafdigital.browserstats.summary;

import java.io.IOException;
import java.util.*;

import org.w3c.dom.Element;

/**
 * Represents information about a single known agent loaded from the file.
 */
class KnownAgent
{
	private String type, os, engine, agent, version;
	private boolean isNumericVersion;
	private int numericVersion1k;
	private int count;
	private int[] categoryCounts;

	/**
	 * Enum recording the different availble fields.
	 */
	enum Field
	{
		/**
		 * Type field.
		 */
		TYPE,
		/**
		 * OS field.
		 */
		OS,
		/**
		 * Engine field.
		 */
		ENGINE,
		/**
		 * Agent field.
		 */
		AGENT,
		/**
		 * Version field.
		 */
		VERSION
	};

	/**
	 * Constructs from data.
	 * @param type Type
	 * @param os OS
	 * @param engine Browser engine
	 * @param agent Agent name
	 * @param version Version
	 * @param count Number of requests from this agent (0 if not tracking)
	 * @param categoryCounts Number of requests for each category (empty array
	 *   if not tracking)
	 */
	private KnownAgent(String type, String os, String engine, String agent,
		String version, int count, int[] categoryCounts)
	{
		this.type = type;
		this.os = os;
		this.engine = engine;
		this.agent = agent;
		this.version = version;
		this.count = count;
		this.categoryCounts = categoryCounts;

		try
		{
			numericVersion1k = (int)(1000.0*Double.parseDouble(version));
			isNumericVersion = true;
		}
		catch(NumberFormatException e)
		{
		}
	}

	/**
	 * Constructs from XML.
	 * @param tag Agent tag for this agent
	 * @param categories List of categories
	 * @throws IOException Any invalid data
	 */
	KnownAgent(Element tag, String[] categories) throws IOException
	{
		this(tag.getAttribute("type"), tag.getAttribute("os"),
			tag.getAttribute("engine"), tag.getAttribute("name"),
			tag.getAttribute("version"), 0, new int[categories.length]);

		try
		{
			count = Integer.parseInt(tag.getAttribute("count"));
			for(int i=0; i<categories.length; i++)
			{
				categoryCounts[i] = Integer.parseInt(tag.getAttribute(categories[i]));
			}
		}
		catch(NumberFormatException e)
		{
			throw new IOException("Invalid number for count attribute");
		}
	}

	/**
	 * @return Copy of this agent without the count information
	 */
	KnownAgent cloneWithoutCountData()
	{
		return new KnownAgent(type, os, engine, agent, version, 0, new int[0]);
	}

	@Override
	public String toString()
	{
		return type + ":" + os + ":" + engine + ":" + agent + ":" + version;
	}

	/**
	 * @param fields Fields to include
	 * @return Identifying string including only specified fields
	 */
	public String toStringWith(EnumSet<Field> fields)
	{
		String result = "";
		if(fields.contains(Field.TYPE))
		{
			if(!result.isEmpty())
			{
				result += ":";
			}
			result += type;
		}
		if(fields.contains(Field.OS))
		{
			if(!result.isEmpty())
			{
				result += ":";
			}
			result += os;
		}
		if(fields.contains(Field.ENGINE))
		{
			if(!result.isEmpty())
			{
				result += ":";
			}
			result += engine;
		}
		if(fields.contains(Field.AGENT))
		{
			if(!result.isEmpty())
			{
				result += ":";
			}
			result += agent;
		}
		if(fields.contains(Field.VERSION))
		{
			if(!result.isEmpty())
			{
				result += ":";
			}
			result += version;
		}

		return result;
	}

	private String toStringFull()
	{
		return toString() + "/" + count + "/" + Arrays.toString(categoryCounts);
	}

	@Override
	public int hashCode()
	{
		return toStringFull().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof KnownAgent))
		{
			return false;
		}
		KnownAgent other = (KnownAgent)obj;
		return toStringFull().equals(other.toStringFull());
	}

	/**
	 * @return Type text (may be "")
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @return OS text (may be "")
	 */
	public String getOs()
	{
		return os;
	}

	/**
	 * @return Engine text (may be "")
	 */
	public String getEngine()
	{
		return engine;
	}

	/**
	 * @return Agent text (may be "")
	 */
	public String getAgent()
	{
		return agent;
	}

	/**
	 * @return Version text (may be "")
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @return True if this agent has a numeric version number
	 */
	public boolean isNumericVersion()
	{
		return isNumericVersion;
	}

	/**
	 * @return Numeric version
	 */
	public int getNumericVersion1k()
	{
		return numericVersion1k;
	}

	/**
	 * @return Total number of request with this agent
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return Number of requests for each category with this agent
	 */
	public int[] getCategoryCounts()
	{
		return categoryCounts;
	}

	/**
	 * @param fields Fields that are supposed to be the same
	 * @param list List of different agents, all of which are the same except
	 *   for version
	 * @return New agent that combines counts from all of them (and has empty
	 *   version)
	 * @throws IllegalArgumentException If there is a problem with the parameters
	 */
	public static KnownAgent combineCountsWithSameFields(
		EnumSet<Field> fields, List<KnownAgent> list) throws IllegalArgumentException
	{
		// Check input
		if(list.isEmpty())
		{
			throw new IllegalArgumentException("Cannot combine empty list");
		}
		KnownAgent first = list.get(0);
		int count = 0;
		int[] categoryCounts = new int[first.categoryCounts.length];
		for(KnownAgent agent : list)
		{
			if(!agent.toStringWith(fields).equals(first.toStringWith(fields)))
			{
				throw new IllegalArgumentException("Agents do not match");
			}
			count += agent.count;
			for(int category=0; category<categoryCounts.length; category++)
			{
				categoryCounts[category] += agent.categoryCounts[category];
			}
		}

		return new KnownAgent(fields.contains(Field.TYPE) ? first.type : "",
			fields.contains(Field.OS) ? first.os : "",
			fields.contains(Field.ENGINE) ? first.engine : "",
			fields.contains(Field.AGENT) ? first.agent : "",
			fields.contains(Field.VERSION) ? first.version : "",
			count, categoryCounts);
	}
}