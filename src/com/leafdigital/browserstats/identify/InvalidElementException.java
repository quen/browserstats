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

import org.w3c.dom.Element;

import com.leafdigital.util.xml.XML;

/** Used to indicate that a particular element is invalid. */
public class InvalidElementException extends Exception
{
	private Element e;

	InvalidElementException(Element e, String message)
	{
		super(message);
		this.e = e;
	}

	@Override
	public String toString()
	{
		StringWriter writer = new StringWriter();
		try
		{
			XML.fastSave(XML.MODE_XML, e, writer);
		}
		catch(IOException e)
		{
			// Can't get IOException when writing to StringWriter
			throw new Error(e);
		}
		return super.toString() + "\n" + writer.toString();
	}
}
