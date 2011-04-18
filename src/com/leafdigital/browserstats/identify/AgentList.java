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
				MatchElement result = match(sample);
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
