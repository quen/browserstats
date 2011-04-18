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

import java.util.LinkedList;
import java.util.regex.*;

import org.w3c.dom.Element;

import com.leafdigital.util.xml.*;

/** Information required to detect a group of browsers. */
public abstract class MatchElement implements Comparable<MatchElement>
{
	private String type, engine, name, version, os;
	private Pattern[] regexes;
	private Pattern[] notRegexes;
	private MatchElement parent;
	private boolean exclusive;

	private LinkedList<MatchElement> children;

	/**
	 * Constructs from XML.
	 * @param parent Parent match element, or null if none (root)
	 */
	MatchElement(MatchElement parent)
	{
		this.parent = parent;
	}

	/**
	 * Initialises options that belong to this individual element.
	 * @param e Browser element
	 * @throws InvalidElementException If the input format is wrong
	 */
	protected void initSelf(Element e) throws InvalidElementException
	{
		type = e.hasAttribute("type") ? e.getAttribute("type") : null;
		engine = e.hasAttribute("engine") ? e.getAttribute("engine") : null;
		name = e.hasAttribute("name") ? e.getAttribute("name") : null;
		version = e.hasAttribute("version") ? e.getAttribute("version") : null;
		os = e.hasAttribute("os") ? e.getAttribute("os") : null;

		Element[] regexElements = XML.getChildren(e, "regex");
		regexes = regexElements.length == 0 ? null : new Pattern[regexElements.length];
		for(int i=0; i<regexElements.length; i++)
		{
			String regexText = XML.getText(regexElements[i]);
			try
			{
				regexes[i] = Pattern.compile(regexText);
			}
			catch(PatternSyntaxException x)
			{
				throw new InvalidElementException(e, "Invalid regex: " + regexText);
			}
		}

		Element[] notRegexElements = XML.getChildren(e, "notregex");
		notRegexes = notRegexElements.length == 0 ? null : new Pattern[notRegexElements.length];
		for(int i=0; i<notRegexElements.length; i++)
		{
			String notRegexText = XML.getText(notRegexElements[i]);
			try
			{
				notRegexes[i] = Pattern.compile(notRegexText);
			}
			catch(PatternSyntaxException x)
			{
				throw new InvalidElementException(e, "Invalid regex: " + notRegexText);
			}
		}


		// By default, a group is exclusive if it has any regular expressions
		exclusive = e.hasAttribute("exclusive")
			? e.getAttribute("exclusive").equals("y")
			: regexes != null;
	}

	/**
	 * Initialises children of this element.
	 * @param e
	 * @throws InvalidElementException
	 */
	protected void initChildren(Element e) throws InvalidElementException
	{
		children = new LinkedList<MatchElement>();
		Element[] childElements = XML.getChildren(e);
		for(Element child : childElements)
		{
			if(child.getTagName().equals("agent"))
			{
				children.add(new Agent(this, child));
			}
			else if(child.getTagName().equals("group"))
			{
				children.add(new Group(this, child));
			}
		}
		if(children.isEmpty())
		{
			throw new InvalidElementException(e, "No children for group");
		}
	}

	/**
	 * Lists all browsers within this element.
	 * @param browsers List that receives browsers
	 */
	protected void listBrowsers(LinkedList<Agent> browsers)
	{
		for(MatchElement child : children)
		{
			child.listBrowsers(browsers);
		}
	}

	/** @return Identifier describing type of agent (browser, feedreader, etc) */
	public String getType()
	{
		if(type==null && parent!=null)
		{
			return parent.getType();
		}
		else
		{
			return type;
		}
	}

	private static String blankForNull(String s)
	{
		return s != null ? s : "";
	}

	/**
	 * @return Type or blank if none
	 */
	public String getTypeS()
	{
		return blankForNull(getType());
	}

	/** @return Browser engine identifier */
	public String getEngine()
	{
		if(engine==null && parent!=null)
		{
			return parent.getEngine();
		}
		else
		{
			return engine;
		}
	}

	/**
	 * @return Engine or blank if none
	 */
	public String getEngineS()
	{
		return blankForNull(getEngine());
	}

	/** @return Browser identifier */
	public String getName()
	{
		if(name==null && parent!=null)
		{
			return parent.getName();
		}
		else
		{
			return name;
		}
	}

	/**
	 * @return Name or blank if none
	 */
	public String getNameS()
	{
		return blankForNull(getName());
	}

	/** @return Browser version */
	public String getVersion()
	{
		if(version==null && parent!=null)
		{
			return parent.getVersion();
		}
		else
		{
			return version;
		}
	}

	/**
	 * @return Engine or blank if none
	 */
	public String getVersionS()
	{
		return blankForNull(getVersion());
	}

	/** @return Operating system identifier */
	public String getOs()
	{
		if(os==null && parent!=null)
		{
			return parent.getOs();
		}
		else
		{
			return os;
		}
	}

	/**
	 * @return OS or blank if none
	 */
	public String getOsS()
	{
		return blankForNull(getOs());
	}

	/**
	 * @param agent User-agent string
	 * @return True if this browser matches the given agent
	 */
	protected boolean matches(String agent)
	{
		if(notRegexes != null)
		{
			for(Pattern regex : notRegexes)
			{
				if(regex.matcher(agent).find())
				{
					return false;
				}
			}
		}

		if(regexes == null)
		{
			return true;
		}

		for(Pattern regex : regexes)
		{
			if(regex.matcher(agent).find())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the browser that matches the given agent.
	 * @param agent User-agent string
	 * @return Matching browser or null if none
	 */
	public MatchElement match(String agent)
	{
		if(!matches(agent))
		{
			return null;
		}
		if(children!=null)
		{
			for(MatchElement child : children)
			{
				MatchElement result = child.match(agent);
				if(result != null)
				{
					return result;
				}
			}
		}
		return exclusive ? this : null;
	}

	@Override
	public String toString()
	{
		return getNameS() + "/" + getVersionS() + "/" + getOsS() + " (" +
		  getEngineS() + "; " + getTypeS() + ")";
	}

	@Override
	public int compareTo(MatchElement o)
	{
		int i = getTypeS().compareTo(o.getTypeS());
		if(i!=0)
		{
			return i;
		}
		i = getEngineS().compareTo(o.getEngineS());
		if(i!=0)
		{
			return i;
		}
		i = getNameS().compareTo(o.getNameS());
		if(i!=0)
		{
			return i;
		}
		i = getVersionS().compareTo(o.getVersionS());
		if(i!=0)
		{
			return i;
		}
		i = getOsS().compareTo(o.getOsS());
		if(i!=0)
		{
			return i;
		}

		return 0;
	}
}
