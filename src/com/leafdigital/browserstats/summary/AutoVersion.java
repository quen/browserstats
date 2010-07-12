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

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents the -autoversion command line parameter.
 */
class AutoVersion
{
	private Pattern group;
	private AutoVersionType type;

	/**
	 * @param group Group regular expression
	 * @param type Type of auto-version support
	 */
	AutoVersion(Pattern group, AutoVersionType type)
	{
		this.group = group;
		this.type = type;
	}

	/**
	 * @return Regular expression matching group name
	 */
	public Pattern getGroup()
	{
		return group;
	}

	/**
	 * Must be called after numbers have already been calculated for the given
	 * parameters. Replaces existing parameters with auto-version groups ready
	 * for recalculation.
	 * @param parameters Parameter list
	 * @return New parameter list
	 * @throws IllegalArgumentException If any agent covered by this -autoversion
	 *   does not have a numeric version
	 */
	public LinkedList<Conditions> apply(Collection<Conditions> parameters)
		throws IllegalArgumentException
	{
		LinkedList<Conditions> result = new LinkedList<Conditions>(parameters);
		while(true)
		{
			// Look for matching groups
			Group match = null;
			for(Conditions parameter : result)
			{
				if(parameter instanceof Group)
				{
					// Must match regular expression, not be one already created with
					// auto version groups, and not have a version restriction of its
					// own, and have some data
					Group possible = (Group)parameter;
					if(group.matcher(possible.getName()).find()
						&& !possible.isAutoVersionGroup() && possible.getVersion()==null
						&& possible.getVersionOperator()==null && !possible.isEmpty())
					{
						match = possible;
						break;
					}
				}
			}

			// If there are no more matches, break out of infinite loop
			if(match == null)
			{
				break;
			}

			// Group matches. Get its index and remove it
			int index = result.indexOf(match);
			result.remove(index);

			// Build a map of version count totals
			TreeMap<Integer, Integer> versionCounts = new TreeMap<Integer, Integer>();
			for(KnownAgent knownAgent : match.getKnownAgents())
			{
				if(!knownAgent.isNumericVersion())
				{
					throw new IllegalArgumentException(
						"-autoversion agent does not have numeric version: " + knownAgent);
				}
				int version1k = knownAgent.getNumericVersion1k();
				Integer existing = versionCounts.get(version1k);
				int count = existing == null ? 0 : existing.intValue();
				count += knownAgent.getCount();
				versionCounts.put(version1k, count);
			}

			// Get threshold (5% of total count)
			int threshold = match.getCount() / 20;
			switch(type)
			{
			case FULL_DISCARD:
			case FULL_OTHER:
				boolean all = true;
				for(Map.Entry<Integer, Integer> entry : versionCounts.entrySet())
				{
					// Add group for entry if it is over threshold
					if(entry.getValue() >= threshold)
					{
						result.add(index++,
							new Group(match, VersionOperator.EQ, entry.getKey()));
					}
					else
					{
						all = false;
					}
				}
				// Add 'other' (individual or general)
				if (!all)
				{
					if(type == AutoVersionType.FULL_DISCARD)
					{
						result.add(index, new Other(match));
					}
					else
					{
						result.add(index, new Group(match));
					}
				}
				break;

			case MIN_DISCARD:
			case MIN_OTHER:
				int total = 0, before1k = 0, after1k = 0;
				boolean gotNext = false;
				for(Map.Entry<Integer, Integer> entry : versionCounts.entrySet())
				{
					// If we already found it, just record that we should use >=
					if(after1k != 0)
					{
						gotNext = true;
						break;
					}
					total += entry.getValue();
					if(total > threshold)
					{
						after1k = entry.getKey();
						continue;
					}
					before1k = entry.getKey();
				}
				// Add a new match for >= the threshold version
				result.add(index++, new Group(match,
					gotNext ? VersionOperator.GE : VersionOperator.EQ, after1k));

				// If there are other values, add an 'other' entry
				if (before1k != 0)
				{
					if(type == AutoVersionType.MIN_DISCARD)
					{
						result.add(index, new Other(match));
					}
					else
					{
						result.add(index, new Group(match, VersionOperator.LE, before1k));
					}
				}
			}
		}

		return result;
	}
}