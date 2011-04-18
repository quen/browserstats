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
import java.util.regex.*;

/**
 * Utilities for colour conversion.
 */
public class ColorUtils
{
	private final static Pattern RGB_REGEX = Pattern.compile(
		"#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})");
	private final static Pattern HSL_REGEX = Pattern.compile(
		"([0-9]{1,3})/([0-9]{1,3})/([0-9]{1,3})");

	/**
	 * Given an input value in #rrggbb or hhh/sss/lll format, returns a Java
	 * colour object.
	 * @param input Input text
	 * @return Colour value
	 * @throws IllegalArgumentException If string isn't in our colour formats
	 */
	public static Color fromString(String input) throws IllegalArgumentException
	{
		try
		{
			Matcher m = RGB_REGEX.matcher(input);
			if(m.matches())
			{
				return new Color(fromHex(m.group(1), 255), fromHex(m.group(2), 255),
					fromHex(m.group(3), 255));
			}

			m = HSL_REGEX.matcher(input);
			if(m.matches())
			{
				int hInt = fromDec(m.group(1), 360), sInt = fromDec(m.group(2), 255),
					lInt = fromDec(m.group(3), 255);
				return fromHsl(hInt, sInt, lInt);
			}
		}
		catch(IllegalArgumentException e)
		{
			// Ignore this one, the next one has a better error message
		}

		throw new IllegalArgumentException(
			"Unrecognised colour value (use #rrggbb or hhh/sss/lll): " + input);
	}

	/**
	 * Creates a colour from HSL values.
	 * @param hInt Hue (0-360)
	 * @param sInt Saturation (0-255)
	 * @param lInt Lightness (0-255)
	 * @return Colour
	 */
	public static Color fromHsl(int hInt, int sInt, int lInt)
	{
		// Algorithm, including the function below, is from
		// http://130.113.54.154/~monger/hsl-rgb.html which cites
		// 'Fundamentals of Interactive Computer Graphics' by Foley and van Dam
		if(sInt==0)
		{
			return new Color(lInt, lInt, lInt);
		}
		double h = (double)hInt / 360.0,
			s = (double)sInt / 255.0, l = (double)lInt / 255.0;

		double temp2 = (l < 0.5) ? l * (1.0 + s) : l + s - (l*s);
		double temp1 = 2.0 * l - temp2;

		double r = finishConversion(temp1, temp2, h + (1.0 / 3.0));
		double g = finishConversion(temp1, temp2, h);
		double b = finishConversion(temp1, temp2, h - (1.0 / 3.0));

		return new Color((int)Math.round(r * 255), (int)Math.round(g*255),
			(int)Math.round(b*255));
	}

	private static double finishConversion(double temp1, double temp2, double temp3)
	{
		while(temp3 < 0)
		{
			temp3 += 1.0;
		}
		while(temp3 > 1.0)
		{
			temp3 -= 1.0;
		}

		if(6.0 * temp3 < 1)
		{
			return temp1 + (temp2 - temp1) * 6.0 * temp3;
		}
		if(2.0 * temp3 < 1)
		{
			return temp2;
		}
		if(3.0 * temp3 < 2)
		{
			return temp1 + (temp2 - temp1) * ( (2.0 / 3.0) - temp3 ) * 6.0;
		}
		return temp1;
	}

	/**
	 * Converts a string from hex to integer.
	 * @param input String
	 * @param max Maximum permitted value
	 * @return Number
	 * @throws IllegalArgumentException If it's invalid or out of range
	 */
	private static int fromHex(String input, int max)
		throws IllegalArgumentException
	{
		try
		{
			int value = Integer.parseInt(input.toLowerCase(), 16);
			if(value < 0 || value > max)
			{
				throw new IllegalArgumentException("Out of range: " + input);
			}
			return value;
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Not valid hex: " + input);
		}
	}

	/**
	 * Converts a string from decimal to integer.
	 * @param input String
	 * @param max Maximum permitted value
	 * @return Number
	 * @throws IllegalArgumentException If it's invalid or out of range
	 */
	private static int fromDec(String input, int max)
		throws IllegalArgumentException
	{
		try
		{
			int value = Integer.parseInt(input);
			if(value < 0 || value > max)
			{
				throw new IllegalArgumentException("Out of range: " + input);
			}
			return value;
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Not valid decimal: " + input);
		}
	}

	/**
	 * Converts a colour to SVG format.
	 * @param c Colour
	 * @return Resulting format
	 */
	public static String getSvgColor(Color c)
	{
		String[] fields =
		{
			Integer.toHexString(c.getRed()),
			Integer.toHexString(c.getGreen()),
			Integer.toHexString(c.getBlue())
		};
		StringBuilder out = new StringBuilder("#");
		for(int i=0; i<fields.length; i++)
		{
			while(fields[i].length() < 2)
			{
				fields[i] = "0" + fields[i];
			}
			out.append(fields[i]);
		}
		return out.toString();
	}
}
