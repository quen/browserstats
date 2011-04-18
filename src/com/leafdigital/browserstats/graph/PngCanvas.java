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
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

/**
 * Canvas for PNG image.
 */
public class PngCanvas extends Canvas
{
	private BufferedImage image;
	private Graphics2D g2;

	/**
	 *
	 * @param width
	 * @param height
	 * @param background
	 */
	public PngCanvas(int width, int height, Color background)
	{
		super(width, height);
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g2 = image.createGraphics();
		g2.setColor(background);
		g2.fillRect(0, 0, width, height);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
			RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	@Override
	public void add(Drawable drawable)
	{
		drawable.draw(g2);
	}

	@Override
	public byte[] save() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}

	@Override
	public String getExtension()
	{
		return ".png";
	}
}
