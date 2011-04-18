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

import java.util.*;
import java.util.regex.*;

/**
 * Class used when categorising agent identity into 'patterns' of similar
 * user agents.
 */
class AgentPatterns
{
	private final static int MAX_DISPLAY = 3;

	private static class Component
	{
		private String letter;
		private Pattern pattern;

		Component(String letter, String pattern)
		{
			this.letter = letter;
			this.pattern = Pattern.compile(pattern);
		}
	}

	private final static AgentPatterns.Component[] PATTERN_COMPONENTS =	new AgentPatterns.Component[]
	{
		new Component("U", "^https?:[a-zA-Z0-9/_\\-.]*"),
		new Component("0", "^[0-9.]+"),
		new Component("A", "^[A-Za-z][A-Za-z\\-_0-9]*")
	};

	private static class AgentPatternData
	{
		private LinkedList<String> examples = new LinkedList<String>();
		private int count;

		private void display()
		{
			for(String example : examples)
			{
				System.out.println(example);
			}
		}

		private void add(String agent)
		{
			if(examples.size() < MAX_DISPLAY)
			{
				examples.add(agent);
			}
			count ++;
		}
	}

	private Map<String, AgentPatterns.AgentPatternData> patternExamples =
		new TreeMap<String, AgentPatterns.AgentPatternData>();

	/**
	 * Obtains the 'pattern' of a user-agent string. This abstracts out details
	 * such as exact numbers, leaving a pattern that defines the whitespace
	 * etc. that is involved.
	 * @param agent Agent string
	 * @return Pattern
	 */
	private String getPattern(String agent)
	{
		String pattern = "";
		outer: while(!agent.isEmpty())
		{
			// Add special characters for components
			for(AgentPatterns.Component component : PATTERN_COMPONENTS)
			{
				Matcher m = component.pattern.matcher(agent);
				if(m.find())
				{
					agent = agent.substring(m.end());
					pattern += component.letter;
					continue outer;
				}
			}

			// Add other characters as-is
			pattern += agent.substring(0, 1);
			agent = agent.substring(1);
		}

		return pattern;
	}

	public void display()
	{
		for(Map.Entry<String, AgentPatterns.AgentPatternData> entry : patternExamples.entrySet())
		{
			entry.getValue().display();
		}
	}

	/**
	 * Records an agent for this.
	 * @param agent Agent name
	 */
	public void agent(String agent)
	{
		String pattern = getPattern(agent);
		AgentPatterns.AgentPatternData data = patternExamples.get(pattern);
		if(data == null)
		{
			data = new AgentPatternData();
			patternExamples.put(pattern, data);
		}
		data.add(agent);
	}
}