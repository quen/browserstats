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
				LogFormat format = new LogFormat(XML.getChildText(child, "line", false),
					XML.getRequiredAttribute(child, "ip"),
					XML.getRequiredAttribute(child, "date"),
					XML.getRequiredAttribute(child, "time"),
					XML.getRequiredAttribute(child, "agent"),
					XML.getRequiredAttribute(child, "path"),
					XML.getRequiredAttribute(child, "status"),
					XML.getRequiredAttribute(child, "dateformat"),
					XML.getRequiredAttribute(child, "timeformat"));
				if(XML.hasChild(child, "skip"))
				{
					format.setSkip(XML.getChildText(child, "skip", false));
				}
				formats.put(XML.getRequiredAttribute(child, "name"), format);
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
