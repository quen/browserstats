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
import java.nio.charset.Charset;

/**
 * Canvas for SVG image.
 */
public class SvgCanvas extends Canvas
{
	private StringBuilder out;

	/**
	 *
	 * @param width
	 * @param height
	 * @param background
	 */
	public SvgCanvas(int width, int height, Color background)
	{
		super(width, height);

		out = new StringBuilder("<?xml version='1.0' standalone='no'?>\n");
		out.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN'\n");
		out.append("  'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>\n");
		// Note: A width in px could be specified here to force the PNG to display
		// at that particular size, but I decided it was best to leave it
		// scalable.
		out.append("<svg viewBox='0,0," + width + "," + height + "' ");
		out.append("fill='" + ColorUtils.getSvgColor(background) + "' ");
		out.append("xmlns='http://www.w3.org/2000/svg' version='1.1'>\n");
	}

	@Override
	public void add(Drawable drawable)
	{
		drawable.draw(out);
	}

	@Override
	public byte[] save()
	{
		return (out.toString() + "</svg>").getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public String getExtension()
	{
		return ".svg";
	}

	/**
	 * Converts a number suitable for inclusion in SVG file (one decimal place).
	 * @param number Number
	 * @return SVG rounded version
	 */
	public static String svgRound(double number)
	{
		String result = String.format("%.1f", number);
		if(result.endsWith(".0"))
		{
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}
}
