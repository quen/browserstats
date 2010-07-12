package com.leafdigital.browserstats.summary;

/**
 * Represents the -include command-line parameter.
 */
class Include extends Conditions
{
	/**
	 * Initialises conditions from command-line arguments.
	 * @param args Arguments
	 * @param i Position of first condition
	 */
	protected Include(String[] args, int i)
	{
		super(args, i);
	}

	@Override
	boolean match(KnownAgent knownAgent) throws IllegalArgumentException
	{
		// This matches anything EXCEPT the things it matches (so as to eat
		// the 'not-included' values)
		return !super.match(knownAgent);
	}

	@Override
	protected String getName()
	{
		return Exclude.NAME;
	}
}