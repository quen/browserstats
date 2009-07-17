package com.leafdigital.browserstats.collator;

import java.io.IOException;
import java.util.HashMap;

import org.w3c.dom.*;

import com.leafdigital.util.xml.*;

/** Holds list of standard log formats obtained from formats.xml. */
public class StandardFormats
{
	private HashMap<String, LogFormat> formats = new HashMap<String, LogFormat>();
	
	/** 
	 * Initialises formats. 
	 * @throws IOException 
	 */
	StandardFormats() throws IOException
	{
		try
		{
			// Load settings
			Document doc = XML.parse(StandardFormats.class, "formats.xml");		
			for(Element child : XML.getChildren(doc.getDocumentElement()))
			{
				formats.put(XML.getRequiredAttribute(child, "name"),
					new LogFormat(XML.getText(child), 
						XML.getRequiredAttribute(child, "ip"),
						XML.getRequiredAttribute(child, "date"),
						XML.getRequiredAttribute(child, "time"),
						XML.getRequiredAttribute(child, "agent"),
						XML.getRequiredAttribute(child, "path"),
						XML.getRequiredAttribute(child, "status"),
						XML.getRequiredAttribute(child, "dateformat"),
						XML.getRequiredAttribute(child, "timeformat")));
			}
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
	}
	
	/**
	 * Obtains a standard log format.
	 * @param name Name of format
	 * @return Format
	 * @throws IllegalArgumentException If format doesn't exist
	 */
	LogFormat getFormat(String name) throws IllegalArgumentException
	{
		LogFormat result = formats.get(name);
		if(result == null)			
		{
			throw new IllegalArgumentException("Unknown log format: " + name);
		}
		return result;
	}
}
