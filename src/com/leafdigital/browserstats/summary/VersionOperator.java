package com.leafdigital.browserstats.summary;

enum VersionOperator
{
	GT("> "), GE(">= "), LT("< "), LE("<= "), EQ(""), NE("!= ");

	private String display;

	VersionOperator(String display)
	{
		this.display = display;
	}

	/**
	 * @return Display characters
	 */
	public String getDisplay()
	{
		return display;
	}

	/**
	 * Returns the operator matching the given name.
	 * @param name Operator name e.g. "ne"
	 * @return Operator value or null if no match
	 */
	static VersionOperator get(String name)
	{
		for(VersionOperator possible : VersionOperator.values())
		{
			if(possible.toString().toLowerCase().equals(name))
			{
				return possible;
			}
		}
		return null;
	}

	/**
	 * Checks whther this operator is true.
	 * @param required1k Required value (in 1k format e.g. 8.0 = 8000)
	 * @param actual1k Actual value in 1k format
	 * @return True if this operator matches the value
	 */
	boolean match(int required1k, int actual1k)
	{
		switch(this)
		{
		case GT : return actual1k > required1k;
		case GE : return actual1k >= required1k;
		case LT : return actual1k < required1k;
		case LE : return actual1k <= required1k;
		case EQ : return actual1k == required1k;
		case NE : return actual1k != required1k;
		}
		throw new Error("Unknown version operator");
	}
}