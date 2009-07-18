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
