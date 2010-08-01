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
package com.leafdigital.browserstats.identify;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/** Reads a user-agent file. */
public class UserAgentReader extends DefaultHandler
{
	private File input;
	private Handler h;

	private String[] categories;

	private Locator locator;

	private String currentAgent;
	private int currentCount;
	private int[] currentCounts;

	/**
	 * Class that handles user-agent information read from file.
	 */
	public interface Handler
	{
		/**
		 * Called at the start of the file. Provides list of all categories in use.
		 * @param categories Category names
		 */
		public void agentCategories(String[] categories);

		/**
		 * Called each time a new agent is read.
		 * @param agent User-agent string
		 * @param count Total number of requests with this agent
		 * @param categoryCounts Number of requests from each category to this agent
		 */
		public void agentCounts(String agent, int count, int[] categoryCounts);
	}

	/**
	 * Parses the input file.
	 * @param input Input file or null to use stdin
	 * @param h Handler that receives data from file
	 * @throws IOException Any error parsing
	 */
	UserAgentReader(File input, Handler h) throws IOException
	{
		this.input = input;
		this.h = h;
		SAXParserFactory factory = SAXParserFactory.newInstance();
	  try
	  {
      SAXParser saxParser = factory.newSAXParser();
      if(input==null)
      {
      	saxParser.parse(new InputSource(
      		new InputStreamReader(System.in, "UTF-8")), this);
      }
      else
      {
      	saxParser.parse(input, this);
      }
	  }
		catch(ParserConfigurationException e)
		{
			throw new IOException("Problem with Java XML system", e);
		}
		catch(SAXException e)
		{
			throw new IOException("Invalid user-agent data: " + e.getMessage());
		}
	}

	@Override
	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}

	private String getLocation()
	{
		return input.getName()+ (locator==null ? "" : ":"+locator.getLineNumber());
	}

	@Override
	public void startElement(String uri, String localName, String name,
		Attributes attributes) throws SAXException
	{
		// Main tag includes category list
		if(name.equals("useragents"))
		{
			// Setup categories list
			String categoryList = attributes.getValue("categories");
			if(categoryList==null)
			{
				categories = new String[0];
			}
			else
			{
				categories = categoryList.split(",");
			}
			h.agentCategories(categories);
		}

		// Agent tag includes counts
		if(name.equals("agent"))
		{
		  // Get all values
			currentCount = getCount(attributes, "count");
			// Create a new array so that the array values can be stored directly
		  currentCounts = new int[categories.length];
			for(int i=0; i<categories.length; i++)
			{
				currentCounts[i] = getCount(attributes, categories[i]);
			}

		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		currentAgent = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String name)
		throws SAXException
	{
		if(name.equals("agent"))
		{
			h.agentCounts(currentAgent, currentCount, currentCounts);
		}
	}

	/**
	 * Obtains a count from the supplied SAX attributes.
	 * @param currentAttributes Attributes
	 * @param name Name of attribute
	 * @return Count
	 * @throws SAXException If the attribute doesn't exist or isn't an integer
	 */
	private int getCount(Attributes currentAttributes, String name) throws SAXException
	{
	  String value = currentAttributes.getValue(name);
	  if(value==null)
	  {
	  	throw new SAXException("<agent> missing required attribute " + name
	  		+ "=. " + getLocation());
	  }
	  try
	  {
	  	return Integer.parseInt(value);
	  }
	  catch(NumberFormatException e)
	  {
	  	throw new SAXException("<agent> " + name + "= not a valid integer ("
	  		+ value + "). " + getLocation());
	  }
	}

}
