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
package com.leafdigital.browserstats.summarise;

/**
 * Represents the -group command-line parameter.
 */
class Group extends Conditions
{
	private String name;
	private boolean autoVersionGroup;

	/**
	 * Initialises conditions from command-line arguments.
	 * @param group Group name
	 * @param args Arguments
	 * @param i Position of first condition
	 */
	protected Group(String group, String[] args, int i)
	{
		super(args, i);
		this.name = group;
	}

	/**
	 * @param number1k Number in 1k format
	 * @return String representing number (8500 => "8.5", 3000 => "3")
	 */
	public String display1k(int number1k)
	{
		String display = number1k + "";
		while(display.length() < 4)
		{
			display = "0" + display;
		}
		display = display.substring(0, display.length()-3) + "."
			+ display.substring(display.length() - 3);
		while(display.endsWith("0"))
		{
			display = display.substring(0, display.length()-1);
		}
		if(display.endsWith("."))
		{
			display = display.substring(0, display.length()-1);
		}
		return display;
	}

	/**
	 * Initialises conditions as auto-version group.
	 * @param original Original group
	 * @param operator Version condition operator
	 * @param version1k Required version number in 1k format
	 */
	protected Group(Group original, VersionOperator operator, int version1k)
	{
		super(original.getType(), original.getOs(), original.getEngine(),
			original.getAgent(), original.getVersion(), operator, version1k);
		this.autoVersionGroup = true;
		this.name = original.getName() + " (" + operator.getDisplay()
			+ display1k(version1k) + ")";
	}

	/**
	 * Initialises conditions as auto-version fallback group.
	 * @param original Original group
	 */
	protected Group(Group original)
	{
		super(original.getType(), original.getOs(), original.getEngine(),
			original.getAgent(), original.getVersion(), null, 0);
		this.autoVersionGroup = true;
		this.name = original.getName() + " (other)";
	}

	/**
	 * @return True if this was created automatically by the auto-version system
	 */
	public boolean isAutoVersionGroup()
	{
		return autoVersionGroup;
	}

	/**
	 * @return Group name
	 */
	@Override
	public String getName()
	{
		return name;
	}
}