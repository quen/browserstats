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

import com.leafdigital.browserstats.shared.SpecialNames;

/**
 * Represents automatically-generated 'other' entries; these are all summed
 * together to result in the 'other' total at bottom.
 */
class Other extends Conditions
{
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
		return SpecialNames.GROUP_OTHER;
	}
}