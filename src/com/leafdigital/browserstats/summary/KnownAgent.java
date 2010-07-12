package com.leafdigital.browserstats.summary;

import java.io.IOException;

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
	 * Constructs from XML.
	 * @param tag Agent tag for this agent
	 * @param categories List of categories
	 * @throws IOException Any invalid data
	 */
	KnownAgent(Element tag, String[] categories) throws IOException
	{
		type = tag.getAttribute("type");
		os = tag.getAttribute("os");
		engine = tag.getAttribute("engine");
		agent = tag.getAttribute("name");
		version = tag.getAttribute("version");
		try
		{
			numericVersion1k = (int)(1000.0*Double.parseDouble(version));
			isNumericVersion = true;
		}
		catch(NumberFormatException e)
		{
		}

		try
		{
			count = Integer.parseInt(tag.getAttribute("count"));
			categoryCounts = new int[categories.length];
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

	@Override
	public String toString()
	{
		return type + ":" + os + ":" + engine + ":" + agent + ":" + version;
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
}