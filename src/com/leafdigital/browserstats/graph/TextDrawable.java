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
	/**
	 * Text vertical alignment. (Note: Alignment is relative to an assumed piece
	 * of text with ascenders and descenders, not to the specific text used.)
	 */
	public enum Alignment
	{
		/**
		 * Provided Y co-ordinate is the centre of the text.
		 */
		CENTER,
		/**
		 * Provided Y co-ordinate is the top of the text.
		 */
		TOP,
		/**
		 * Provided Y co-ordinate is baseline.
		 */
		BASELINE;
	}

	private String text, fontName;
	private Alignment alignment;
	private Color color;
	private boolean bold;
	private double x, y;
	private int fontSize;

	private static FontRenderContext fontRenderContext;

	/**
	 * @param text Text to draw
	 * @param x X position
	 * @param y Y position
	 * @param alignment Alignment relative to Y position
	 * @param color Colour of text
	 * @param fontName Font for text
	 * @param bold True for bold text
	 * @param fontSize Font size (pixels)
	 */
	public TextDrawable(String text, double x, double y, Alignment alignment,
		Color color, String fontName, boolean bold, int fontSize)
	{
		this.text = text;
		this.x = x;
		this.y = y;
		this.alignment = alignment;
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

	private double getEffectiveY()
	{
		LineMetrics metrics =
			getFont().getLineMetrics("AHyj", getFontRenderContext());
		switch(alignment)
		{
		case TOP:
			return y + metrics.getAscent();
		case CENTER:
			return y + (metrics.getAscent() - metrics.getDescent()) / 2;
		case BASELINE:
			return y;
		default:
			throw new IllegalStateException("Unexpected alignment");
		}
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
		LineMetrics metrics =
			getFont().getLineMetrics("AHyj", getFontRenderContext());
		return metrics.getAscent() + metrics.getDescent();
	}

	@Override
	public void draw(Graphics2D g)
	{
		g.setFont(getFont());
		g.setColor(color);
		g.drawString(text, (float)x, (float)getEffectiveY());
	}

	@Override
	public void draw(StringBuilder svg)
	{
		svg.append("<text font-family='" + XML.esc(fontName) + ", sans-serif' ");
		svg.append("font-size='" + fontSize + "' ");
		if(bold)
		{
			svg.append("font-weight='bold' ");
		}
		svg.append("fill='" + ColorUtils.getSvgColor(color) + "' x='"
			+ svgRound(x) + "' y='" + svgRound(getEffectiveY()) + "'>");
		svg.append(XML.esc(text) + "</text>\n");
	}
}
