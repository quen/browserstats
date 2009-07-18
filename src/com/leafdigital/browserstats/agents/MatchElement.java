package com.leafdigital.browserstats.agents;

import java.util.LinkedList;
import java.util.regex.*;

import org.w3c.dom.Element;

import com.leafdigital.util.xml.*;

/** Information required to detect a group of browsers. */
public abstract class MatchElement 
{
	private String type, engine, name, version, os;
	private Pattern[] regexes;
	private MatchElement parent;
	
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
	 * @param agent User-agent string
	 * @return True if this browser matches the given agent
	 */
	protected boolean matches(String agent)
	{
		if(regexes==null)
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
	public Agent match(String agent)
	{
		if(!matches(agent))
		{
			return null;
		}
		if(children!=null)
		{
			for(MatchElement child : children)
			{
				Agent result = child.match(agent);
				if(result != null)
				{
					return result;
				}
			}
		}
		return null;
	}	
}
