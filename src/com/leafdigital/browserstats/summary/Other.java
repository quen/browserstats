package com.leafdigital.browserstats.summary;

/**
 * Represents automatically-generated 'other' entries; these are all summed
 * together to result in the 'other' total at bottom.
 */
class Other extends Conditions
{
	/**
	 * Name used for excluded data.
	 */
	static final String NAME = "Other";

	/**
	 * Creates generic 'other' matcher.
	 */
	protected Other()
	{
		super(null, null, null, null, null, null, 0);
	}

	/**
	 * Creates 'other' matcher for unwanted items that match a previous group.
	 * @param original Original group
	 */
	protected Other(Group original)
	{
		super(original.getType(), original.getOs(), original.getEngine(), original.getAgent(),
			null, null, 0);
	}

	@Override
	protected String getName()
	{
		return NAME;
	}
}