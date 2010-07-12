package com.leafdigital.browserstats.summary;

/**
 * Represents the -exclude command-line parameter.
 */
class Exclude extends Conditions
{
	/**
	 * Name used for excluded data.
	 */
	static final String NAME = "(Excluded)";

	/**
	 * Initialises conditions from command-line arguments.
	 * @param args Arguments
	 * @param i Position of first condition
	 */
	protected Exclude(String[] args, int i)
	{
		super(args, i);
	}

	@Override
	protected String getName()
	{
		return NAME;
	}
}