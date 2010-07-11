package com.leafdigital.browserstats.summary;

enum AutoVersionType
{
	FULL_OTHER("full+"), FULL_DISCARD("full"),
	MIN_OTHER("min+"), MIN_DISCARD("min");

	private String name;
	AutoVersionType(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the type matching the given name.
	 * @param name Type name e.g. "full+"
	 * @return Type value or null if no match
	 */
	static AutoVersionType get(String name)
	{
		for(AutoVersionType possible : AutoVersionType.values())
		{
			if(possible.name.equals(name))
			{
				return possible;
			}
		}
		return null;
	}
}