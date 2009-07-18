package com.leafdigital.browserstats.agents;

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
		if(name.equals("agent"))
		{
			// Setup categories if not done yet
		  if(categories == null)
		  {
		  	int count = attributes.getLength();
		  	categories = new String[count-1];
		  	int index = 0;
		  	for(int i=0; i<count; i++)
		  	{
		  		String category = attributes.getQName(i);
		  		if(category.equals("count"))
		  		{
		  			continue;
		  		}
		  		if(index == categories.length)
		  		{
		  			throw new SAXException("<agent> must include count=. " + getLocation());
		  		}
		  		categories[index++] = category;
		  	}
		  	h.agentCategories(categories);
		  }
		  
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
