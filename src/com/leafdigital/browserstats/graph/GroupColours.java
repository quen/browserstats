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
package com.leafdigital.browserstats.graph;

import java.awt.Color;

/**
 * Tracks colours (main colour and text colour) for a particular group.
 */
class GroupColours
{
	private Color main, text;

	protected GroupColours(Color main, Color text)
	{
		this.main = main;
		this.text = text;
	}

	/**
	 * @return Main colour for this group
	 */
	public Color getMain()
	{
		return main;
	}

	/**
	 * @return Text colour for this group
	 */
	public Color getText()
	{
		return text;
	}
}