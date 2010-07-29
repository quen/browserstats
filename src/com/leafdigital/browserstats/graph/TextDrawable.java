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

import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;

import com.leafdigital.util.xml.XML;

import static com.leafdigital.browserstats.graph.SvgCanvas.svgRound;

/**
 * Piece of text that can be drawn.
 */
public class TextDrawable extends Drawable
{
	private String text, fontName;
	private Color color;
	private boolean bold;
	private double x, y;
	private int fontSize;

	private static FontRenderContext fontRenderContext;

	/**
	 * @param text Text to draw
	 * @param x X position
	 * @param y Y position of baseline
	 * @param color Colour of text
	 * @param fontName Font for text
	 * @param bold True for bold text
	 * @param fontSize Font size (pixels)
	 */
	public TextDrawable(String text, double x, double y,
		Color color, String fontName, boolean bold, int fontSize)
	{
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
		this.fontName = fontName;
		this.bold = bold;
		this.fontSize = fontSize;
	}

	/**
	 * Moves a drawable after it was created (useful if the position can only
	 * be worked out after the size is known).
	 * @param x X position
	 * @param y Y position
	 */
	public void move(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	private synchronized static FontRenderContext getFontRenderContext()
	{
		if(fontRenderContext == null)
		{
			fontRenderContext = new BufferedImage(1, 1,
				BufferedImage.TYPE_INT_RGB).createGraphics().getFontRenderContext();
		}
		return fontRenderContext;
	}

	private Font getFont()
	{
		return new Font(fontName, bold ? Font.BOLD : Font.PLAIN, fontSize);
	}

	/**
	 * @return Width of this text object
	 */
	public double getWidth()
	{
		return getFont().getStringBounds(text, getFontRenderContext()).getWidth();
	}

	/**
	 * @return Height of this text object (actually height for any text in this
	 *   font, assuming ascenders and descenders)
	 */
	public double getHeight()
	{
		LineMetrics metrics = getLineMetrics();
		return metrics.getAscent() + metrics.getDescent();
	}

	/**
	 * @return Ascent of this text object (actually for any text in this font)
	 */
	public double getAscent()
	{
		return getLineMetrics().getAscent();
	}

	/**
	 * If this text needs to be displayed in the middle of something, this returns
	 * the Y co-ordinate.
	 * @param aboveY Co-ordinate above
	 * @param belowY Co-ordinate below
	 * @return Y co-ordinate
	 */
	public double getVerticalMiddleY(double aboveY, double belowY)
	{
		double middle = (aboveY + belowY) / 2.0;
		double top = middle - getHeight() / 2.0;
		return top + getAscent();
	}

	/**
	 * Sets the Y position of this text based on the co-ordinate of the top of
	 * the text.
	 * @param top Top co-ordinate
	 */
	public void setVerticalTop(double top)
	{
		y = top + getAscent();
	}

	/**
	 * @return Line metrics for this font
	 */
	private LineMetrics getLineMetrics()
	{
		LineMetrics metrics =
			getFont().getLineMetrics("AHyj", getFontRenderContext());
		return metrics;
	}

	@Override
	public void draw(Graphics2D g)
	{
		g.setFont(getFont());
		g.setColor(color);
		g.drawString(text, (float)x, (float)y);
	}

	@Override
	public void draw(StringBuilder svg)
	{
		svg.append("<text font-family='" + XML.esc(fontName) + ", sans-serif' ");
		svg.append("textLength = '" + getWidth() + "' ");
		svg.append("font-size='" + fontSize + "' ");
		if(bold)
		{
			svg.append("font-weight='bold' ");
		}
		svg.append("fill='" + ColorUtils.getSvgColor(color) + "' x='"
			+ svgRound(x) + "' y='" + svgRound(y) + "'>");
		svg.append(XML.esc(text) + "</text>\n");
	}
}
