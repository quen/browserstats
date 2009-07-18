package com.leafdigital.browserstats.agents;

import java.io.*;
import java.util.LinkedList;

import org.w3c.dom.Element;

import com.leafdigital.util.xml.XML;

/** Information required to detect a single browser version. */
public class Agent extends MatchElement implements Comparable<Agent>
{
	private String[] testCases;
	
	/**
	 * Constructs from XML.
	 * @param parent Parent match element
	 * @param e Browser element
	 * @throws InvalidElementException If the input format is wrong
	 */
	Agent(MatchElement parent, Element e) throws InvalidElementException	
	{
		super(parent);		
		initSelf(e);

		// Guarantee that none of the information methods will return null
		if(getType()==null)
		{
			throw new InvalidElementException(e, "Requires type=");
		}
		if(getEngine()==null)
		{
			throw new InvalidElementException(e, "Requires engine=");
		}
		if(getName()==null)
		{
			throw new InvalidElementException(e, "Requires name=");
		}
		if(getVersion()==null)
		{
			throw new InvalidElementException(e, "Requires version=");
		}
		if(getOs()==null)
		{
			throw new InvalidElementException(e, "Requires os=");
		}

		testCases = XML.getChildTexts(e, "sample");
	}
	
	@Override
	protected void listBrowsers(LinkedList<Agent> browsers)
	{
		browsers.add(this);
	}
	
	/** @return List of one or more test cases that should match this browser */
	public String[] getTestCases()
	{
		return testCases;
	}
	
	@Override
	public Agent match(String agent)
	{
		return matches(agent) ? this : null;
	}
	
	@Override
	public String toString()
	{
		return getName() + "/" + getVersion() + "/" + getOs() + " (" +
		  getEngine() + "; " + getType() + ")";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj==null || !(obj instanceof Agent))
		{
			return false;
		}
		Agent o = (Agent)obj;
		return getType().equals(o.getType()) && getEngine().equals(o.getEngine())
			&& getName().equals(o.getName()) && getVersion().equals(o.getVersion())
			&& getOs().equals(o.getOs());
	}
	
	@Override
	public int hashCode()
	{
		return getType().hashCode() + getEngine().hashCode() + getName().hashCode()
			+ getVersion().hashCode() + getOs().hashCode();
	}

	@Override
	public int compareTo(Agent o)
	{
		int i = getType().compareTo(o.getType());
		if(i!=0)
		{
			return i;
		}
		i = getEngine().compareTo(o.getEngine());
		if(i!=0)
		{
			return i;
		}
		i = getName().compareTo(o.getName());
		if(i!=0)
		{
			return i;
		}
		i = getVersion().compareTo(o.getVersion());
		if(i!=0)
		{
			return i;
		}
		i = getOs().compareTo(o.getOs());
		if(i!=0)
		{
			return i;
		}
		
		return 0;
	}

	/**
	 * Writes the start of an agent tag containing this agent's details to the
	 * given writer.
	 * @param w Target
	 * @throws IOException Any I/O error
	 */
	public void writeAgentTagStart(Writer w) throws IOException
	{
		w.write("<agent ");
		if(getType().length() > 0)
		{
			w.write("type='" + getType() + "' ");
		}
		if(getEngine().length() > 0)
		{
			w.write("engine='"+ getEngine() + "' ");
		}
		if(getName().length() > 0)
		{
			w.write("name='" + getName() + "' ");
		}
		if(getVersion().length() > 0)
		{
			w.write("version='"+ getVersion() + "' ");
		}
		if(getOs().length() > 0)
		{
			w.write("os='" + getOs() + "' ");
		}
	}
}
