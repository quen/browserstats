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
package com.leafdigital.browserstats.summary;

import java.util.LinkedList;
import java.util.regex.*;

/**
 * Represents conditions that can be used to categorise data from a user
 * agent.
 */
abstract class Conditions
{
	private int argsUsed;

	private Pattern type, os, engine, agent, version;
	private VersionOperator versionOperator;
	private int versionNumber1k;

	private int count;
	private int[] categoryCounts;
	private LinkedList<KnownAgent> knownAgents;

	protected Conditions(Pattern type, Pattern os, Pattern engine,
		Pattern agent, Pattern version, VersionOperator versionOperator,
		int versionNumber1k)
	{
		this.type = type;
		this.os = os;
		this.engine = engine;
		this.agent = agent;
		this.version = version;
		this.versionOperator = versionOperator;
		this.versionNumber1k = versionNumber1k;
	}

	/**
	 * Initialises conditions from command-line arguments.
	 * @param args Arguments
	 * @param i Position of first condition
	 */
	protected Conditions(String[] args, int i)
	{
		int start = i;
		while(i<args.length && !args[i].startsWith("-"))
		{
			if(i+1 == args.length)
			{
				throw new IllegalArgumentException(
					"Expected value (regular expression) after " + args[i]);
			}
			if(args[i].equals("type"))
			{
				if(type != null)
				{
					throw new IllegalArgumentException("Cannot specify " + args[i]
						+ " twice");
				}
				i++;
				type = initRegex(args[i]);
			}
			else if(args[i].equals("os"))
			{
				if(os != null)
				{
					throw new IllegalArgumentException("Cannot specify " + args[i]
						+ " twice");
				}
				i++;
				os = initRegex(args[i]);
			}
			else if(args[i].equals("engine"))
			{
				if(engine != null)
				{
					throw new IllegalArgumentException("Cannot specify " + args[i]
						+ " twice");
				}
				i++;
				engine = initRegex(args[i]);
			}
			else if(args[i].equals("agent"))
			{
				if(agent != null)
				{
					throw new IllegalArgumentException("Cannot specify " + args[i]
						+ " twice");
				}
				i++;
				agent = initRegex(args[i]);
			}
			else if(args[i].equals("version"))
			{
				if(version != null || versionOperator != null)
				{
					throw new IllegalArgumentException("Cannot specify " + args[i]
						+ " twice");
				}
				i++;
				versionOperator = VersionOperator.get(args[i]);
				if(versionOperator == null)
				{
					version = initRegex(args[i]);
				}
				else
				{
					if(i+1 == args.length)
					{
						throw new IllegalArgumentException(
							"Expected number after " + args[i-1] + " " + args[i]);
					}
					i++;
					try
					{
						versionNumber1k = (int)(1000.0*Double.parseDouble(args[i]));
					}
					catch(NumberFormatException e)
					{
						throw new IllegalArgumentException("Not a valid number: "
							+ args[i]);
					}
				}
			}
			else
			{
				throw new IllegalArgumentException("Unknown field: " + args[i]);
			}

			i++;
		}

		argsUsed = i-start;
	}

	private static Pattern initRegex(String regex)
		throws IllegalArgumentException
	{
		try
		{
			return Pattern.compile(regex);
		}
		catch(PatternSyntaxException e)
		{
			throw new IllegalArgumentException(
				"Invalid value (must follow regular expression format): " + regex);
		}
	}

	/**
	 * @return Type or null if unspecified
	 */
	public Pattern getType()
	{
		return type;
	}

	/**
	 * @return OS or null if unspecitied
	 */
	public Pattern getOs()
	{
		return os;
	}

	/**
	 * @return Engine or null if unspecified
	 */
	public Pattern getEngine()
	{
		return engine;
	}

	/**
	 * @return Agent or null if unspecified
	 */
	public Pattern getAgent()
	{
		return agent;
	}

	/**
	 * @return Version or null if unspecified
	 */
	public Pattern getVersion()
	{
		return version;
	}

	/**
	 * @return Version operator or null if unspecified
	 */
	public VersionOperator getVersionOperator()
	{
		return versionOperator;
	}

	/**
	 * @return Version operator number or 0 if unspecified
	 */
	public int getVersionNumber1k()
	{
		return versionNumber1k;
	}

	/**
	 * @return Total count
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return Category counts
	 */
	public int[] getCategoryCounts()
	{
		return categoryCounts;
	}

	/**
	 * @return Number of arguments eaten by conditions
	 */
	int getArgsUsed()
	{
		return argsUsed;
	}

	/**
	 * Initialise and reset counts.
	 * @param categories List of categories
	 */
	void prepare(String[] categories)
	{
		count = 0;
		categoryCounts = new int[categories.length];
		knownAgents = new LinkedList<KnownAgent>();
	}

	/**
	 * @return True if there are no counts stored yet
	 */
	boolean isEmpty()
	{
		return count == 0;
	}

	/**
	 * Checks whether a known agent matches these conditions.
	 * @param knownAgent Agent details
	 * @return True if it matches
	 * @throws IllegalArgumentException If you try to match something with
	 *   a numeric version condition on an agent which has no numeric version
	 */
	boolean match(KnownAgent knownAgent) throws IllegalArgumentException
	{
		if(type != null)
		{
			if(!type.matcher(knownAgent.getType()).find())
			{
				return false;
			}
		}
		if(os != null)
		{
			if(!os.matcher(knownAgent.getOs()).find())
			{
				return false;
			}
		}
		if(engine != null)
		{
			if(!engine.matcher(knownAgent.getEngine()).find())
			{
				return false;
			}
		}
		if(agent != null)
		{
			if(!agent.matcher(knownAgent.getAgent()).find())
			{
				return false;
			}
		}
		if(version != null)
		{
			if(!version.matcher(knownAgent.getVersion()).find())
			{
				return false;
			}
		}
		if(versionOperator != null)
		{
			if(!knownAgent.isNumericVersion())
			{
				throw new IllegalArgumentException("Cannot use numeric version "
					+ "condition on agent with non-numeric version: " + knownAgent);
			}
			if(!versionOperator.match(versionNumber1k, knownAgent.getNumericVersion1k()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Adds values from a known agent into this group.
	 * @param agent Agent details
	 * @throws IllegalStateException If you haven't called
	 *   {@link #prepare(String[])}
	 */
	void add(KnownAgent agent) throws IllegalStateException
	{
		knownAgents.add(agent);
		count += agent.getCount();
		for(int i=0; i<categoryCounts.length; i++)
		{
			categoryCounts[i] += agent.getCategoryCounts()[i];
		}
	}

	/**
	 * @return Known agents that mapped to this set of conditions
	 */
	public KnownAgent[] getKnownAgents()
	{
		return knownAgents.toArray(new KnownAgent[knownAgents.size()]);
	}

	/**
	 * @return Display name of this set of conditions
	 */
	protected abstract String getName();
}