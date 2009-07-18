package com.leafdigital.browserstats.agents;

import java.util.LinkedList;

import org.w3c.dom.Element;

import com.leafdigital.util.xml.*;

/** Root element that holds entire list of browsers to match against. */
public class AgentList extends MatchElement
{
	AgentList() throws InvalidElementException, XMLException
	{
		this(XML.parse(AgentList.class, "agents.xml").getDocumentElement());
	}
	
	/**
	 * Constructs from XML.
	 * @param e Browser element
	 * @throws InvalidElementException If the input format is wrong
	 */
	AgentList(Element e) throws InvalidElementException	
	{
		super(null);
		initChildren(e);
	}
	
	/**
	 * Tests all sample user-agents in the agent lists to make sure they return
	 * the agent tag they're attached to.
	 * @return True if test succeeds
	 * @throws IllegalStateException If anything doesn't match
	 */
	boolean selfTest() throws IllegalStateException
	{
		boolean fail = false;
		LinkedList<Agent> browsers = new LinkedList<Agent>();
		listBrowsers(browsers);
		for(Agent browser : browsers)
		{
			for(String sample : browser.getTestCases())
			{
				Agent result = match(sample);
				if(result != browser)
				{
					System.err.println(browser);
					System.err.println("  Sample: " + sample);
					System.err.println("  Result: " + (result==null ? "[no match]" : result));
					System.err.println();
					fail = true;
				}
			}
		}
		if(fail)
		{
			System.err.println("Self-test failed for above user agent(s).");
			return false;
		}
		else
		{
			System.out.println("Self-test successful.");
			return true;
		}
	}
}
