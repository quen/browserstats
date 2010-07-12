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
package com.leafdigital.browserstats.agents;

import org.w3c.dom.Element;

/** Information required to detect a group of browsers. */
public class Group extends MatchElement
{
	/**
	 * Constructs from XML.
	 * @param parent Parent match element
	 * @param e Browser element
	 * @throws InvalidElementException If the input format is wrong
	 */
	Group(MatchElement parent, Element e) throws InvalidElementException
	{
		super(parent);
		initSelf(e);
		initChildren(e);
	}
}
