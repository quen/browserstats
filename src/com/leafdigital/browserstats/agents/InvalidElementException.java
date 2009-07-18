package com.leafdigital.browserstats.agents;

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
