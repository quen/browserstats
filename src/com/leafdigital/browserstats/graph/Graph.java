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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import com.leafdigital.browserstats.shared.CommandLineTool;

/** Draws a graph based on .summary files. */
public class Graph extends CommandLineTool
{
	private boolean overwrite, stdout, png = true, svg,
		startLabels = true, endLabels = true;
	private Color background = Color.WHITE, foreground = Color.BLACK;
	private File folder;
	private int width=800, height=600, labelSize = 12, footnoteSize = 9,
		titleSize = 18;
	private String category, prefix, fontName, title;

	/**
	 * @param args Command-line arguments
	 */
	public static void main(String[] args)
	{
		// Set headless in case we're running on a server
		System.setProperty("java.awt.headless", "true");

		(new Graph()).run(args);
	}

	@Override
	protected int processArg(String[] args, int i)
		throws IllegalArgumentException
	{
		if(args[i].equals("-overwrite"))
		{
			overwrite = true;
			return 1;
		}
		if(args[i].equals("-folder"))
		{
			checkArgs(args, i, 1);
			folder = new File(args[i+1]);
			if(!folder.exists() || !folder.isDirectory())
			{
				throw new IllegalArgumentException("Folder does not exist: " + folder);
			}
			return 2;
		}
		if(args[i].equals("-stdout"))
		{
			stdout = true;
			return 1;
		}
		if(args[i].equals("-size"))
		{
			checkArgs(args, i, 2);
			try
			{
				width = Integer.parseInt(args[i+1]);
				height = Integer.parseInt(args[i+2]);
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid size (format: -size 800 600)");
			}
			if(width<200 || height < 100 || width > 16000 || height > 16000)
			{
				throw new IllegalArgumentException("Invalid size (out of range)");
			}
			return 3;
		}
		if(args[i].equals("-format"))
		{
			checkArgs(args, i, 1);
			String format = args[i+1];
			png = format.equals("png") || format.equals("both");
			svg = format.equals("svg") || format.equals("both");
			if(!(png || svg))
			{
				throw new IllegalArgumentException("-format unknown: " + format);
			}
			return 2;
		}
		if(args[i].equals("-labels"))
		{
			checkArgs(args, i, 1);
			String labels = args[i+1];
			if(labels.equals("both"))
			{
				startLabels = true;
				endLabels = true;
			}
			else if(labels.equals("none"))
			{
				startLabels = false;
				endLabels = false;
			}
			else if(labels.equals("start"))
			{
				startLabels = true;
				endLabels = false;
			}
			else if(labels.equals("end"))
			{
				startLabels = false;
				endLabels = true;
			}
			else
			{
				throw new IllegalArgumentException("-labels unknown: " + labels);
			}
			return 2;
		}
		if(args[i].equals("-category"))
		{
			checkArgs(args, i, 1);
			category = args[i+1];
			return 2;
		}
		if(args[i].equals("-background"))
		{
			checkArgs(args, i, 1);
			background = ColorUtils.fromString(args[i+1]);
			return 2;
		}
		if(args[i].equals("-foreground"))
		{
			checkArgs(args, i, 1);
			foreground = ColorUtils.fromString(args[i+1]);
			return 2;
		}
		if(args[i].equals("-font"))
		{
			checkArgs(args, i, 1);
			fontName = args[i+1];
			boolean ok = false;
			for(String available : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
			{
				if(available.equals(fontName))
				{
					ok = true;
					break;
				}
			}
			if(!ok)
			{
				throw new IllegalArgumentException("Couldn't find font: " + fontName);
			}
			return 2;
		}
		if(args[i].equals("-labelsize"))
		{
			labelSize = getSizeParameter(args, i);
			return 2;
		}
		if(args[i].equals("-footnotesize"))
		{
			footnoteSize = getSizeParameter(args, i);
			return 2;
		}
		if(args[i].equals("-titlesize"))
		{
			titleSize = getSizeParameter(args, i);
			return 2;
		}
		if(args[i].equals("-prefix"))
		{
			checkArgs(args, i, 1);
			prefix = args[i+1];
			return 2;
		}
		if(args[i].equals("-title"))
		{
			checkArgs(args, i, 1);
			title = args[i+1];
			return 2;
		}
		return 0;
	}

	private int getSizeParameter(String[] args, int i)
		throws IllegalArgumentException
	{
		checkArgs(args, i, 1);
		try
		{
			int size = Integer.parseInt(args[i+1]);
			if(size<1 || size > 999)
			{
				throw new IllegalArgumentException("Size out of range: " + args[i+1]);
			}
			return size;
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid size: " + args[i+1]);
		}
	}

	/** @return True if user is on Mac */
	public static boolean isMac()
	{
		// Code from http://developer.apple.com/technotes/tn2002/tn2110.html
		String lcOSName=System.getProperty("os.name").toLowerCase();
		return lcOSName.startsWith("mac os x");
	}

	@Override
	protected void validateArgs() throws IllegalArgumentException
	{
		if(fontName == null)
		{
			if(isMac())
			{
				fontName = "Helvetica Neue";
			}
			else
			{
				fontName = "sans-serif";
			}
		}
	}

	@Override
	protected void go()
	{
		// Process and check input files
		LinkedList<InputFile> inputFiles = new LinkedList<InputFile>();
		File current = null;
		try
		{
			File[] input = getInputFiles();
			if(input == null)
			{
				inputFiles.add(new InputFile(null, category));
			}
			else
			{
				for(File file : input)
				{
					current = file;
					inputFiles.add(new InputFile(file, category));
				}
			}
		}
		catch(IOException e)
		{
			String name = current == null ? "stdin" : current.toString();
			System.err.println(name + ": " + e.getMessage());
			return;
		}

		// Set up canvas(es)
		Canvas[] canvases = new Canvas[png && svg ? 2 : 1];
		if(png)
		{
			canvases[0] = new PngCanvas(width, height, background);
		}
		if(svg)
		{
			canvases[png ? 1 : 0] = new SvgCanvas(width, height, background);
		}

		try
		{
			// Different paths for one or multiple
			InputFile lastInputFile;
			if(inputFiles.size() == 1)
			{
				lastInputFile = inputFiles.getFirst();
				updateLastDetails(lastInputFile);

				for(Canvas canvas : canvases)
				{
					pieChart(inputFiles.getFirst(), canvas);
				}
			}
			else
			{
				// Check each file has a date
				for(InputFile file : inputFiles)
				{
					if(file.getDate() == null)
					{
						throw new IOException(file.getName()
							+ ": cannot determine date from filename");
					}
				}

				// Sort files into order
				InputFile[] inputFilesSorted =
					inputFiles.toArray(new InputFile[inputFiles.size()]);
				Arrays.sort(inputFilesSorted);
				lastInputFile = inputFilesSorted[inputFilesSorted.length - 1];
				updateLastDetails(lastInputFile);

				// Do trend chart
				for(Canvas canvas : canvases)
				{
					trendChart(inputFilesSorted, canvas);
				}
			}

			// Save output
			if(stdout || getInputFiles() == null)
			{
				System.out.write(canvases[0].save());
			}
			else
			{
				for(Canvas canvas : canvases)
				{
					File file = new File(folder, prefix + canvas.getExtension());
					if(file.exists() && !overwrite)
					{
						throw new IOException("File exists (use -overwrite): " + file);
					}
					FileOutputStream out = new FileOutputStream(file);
					out.write(canvas.save());
					out.close();
				}
			}
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}

	private void updateLastDetails(InputFile lastInputFile)
	{
		if(prefix == null)
		{
			prefix = lastInputFile.getName().replaceAll("\\.summary$", "");
		}
		if(title == null)
		{
			title = prefix;
		}
		if(folder == null && lastInputFile.getFile() != null)
		{
			folder = lastInputFile.getFile().getParentFile();
		}
	}

	private void pieChart(InputFile file, Canvas canvas)
	{
		// TODO
		fillGroupColours(file.getGroupNames());
	}

	private static class Label
	{
		private final static int SEPARATOR = 5, MIN_VERTICAL_PADDING = 4;

		private int groupIndex;
		private TextDrawable name, percentage;

		private double allocatedHeight, y;

		Label(int groupIndex, TextDrawable name, TextDrawable percentage)
		{
			this.groupIndex = groupIndex;
			this.name = name;
			this.percentage = percentage;
		}

		double getWidth()
		{
			return (name.getWidth() + percentage.getWidth() + SEPARATOR);
		}

		/**
		 * @return Index of group that this label is for
		 */
		public int getGroup()
		{
			return groupIndex;
		}

		/**
		 * @return Minimum height for label, including text plus padding
		 */
		private double getMinHeight()
		{
			return name.getHeight() + MIN_VERTICAL_PADDING * 2;
		}

		/**
		 * @param height Height to allocate to label
		 */
		public void allocateHeight(double height)
		{
			allocatedHeight = height;
		}

		public void spreadHeight(Label[] allLabels, int index)
		{
			// If this label is big enough, leave it
			double minHeight = getMinHeight();
			double difference = minHeight - allocatedHeight;
			if(difference <= 0)
			{
				return;
			}

			// Try to get height from surrounding labels (evenly on both sides)
			// in three passes

			// Left contribution (up to half required)
			double leftContribution = 0;
			if(index > 0)
			{
				for(int i=index-1; index>=0; index--)
				{
					double available = allLabels[i].allocatedHeight - minHeight;
					double required = difference/2 - leftContribution;
					if(available >= required)
					{
						allLabels[i].allocatedHeight -= required;
						leftContribution += required;
						break;
					}
					else if(available > 0)
					{
						allLabels[i].allocatedHeight -= available;
						leftContribution += available;
					}
				}
			}

			// Right contribution (up to whatever's needed)
			double rightContribution = 0;
			if(index < allLabels.length - 1)
			{
				for(int i=index+1; i<allLabels.length; i++)
				{
					double available = allLabels[i].allocatedHeight - minHeight;
					double required = difference - leftContribution - rightContribution;
					if(available >= required)
					{
						allLabels[i].allocatedHeight -= required;
						rightContribution += required;
						break;
					}
					else if(available > 0)
					{
						allLabels[i].allocatedHeight -= available;
						rightContribution += available;
					}
				}
			}

			// Left contribution (if right contribution didn't pan out)
			if(index > 0)
			{
				for(int i=index-1; index>=0 ; index--)
				{
					double available = allLabels[i].allocatedHeight - minHeight;
					double required = difference - leftContribution - rightContribution;
					if(available >= required)
					{
						allLabels[i].allocatedHeight -= required;
						leftContribution += required;
						break;
					}
					else if(available > 0)
					{
						allLabels[i].allocatedHeight -= available;
						leftContribution += available;
					}
				}
			}

			allocatedHeight += leftContribution + rightContribution;
		}

		/**
		 * Moves the label to its final position.
		 * @param x X
		 * @param y Y
		 */
		public void move(double x, double y)
		{
			this.y = y;
			double middle = y + allocatedHeight/2;
			name.move(x, middle);
			percentage.move(x + name.getWidth() + SEPARATOR, middle);
		}

		/**
		 * Adds this label to canvas.
		 * @param canvas Canvas
		 */
		public void addTo(Canvas canvas)
		{
			canvas.add(name);
			canvas.add(percentage);
		}

		/**
		 * @return Height allocated to this label
		 */
		public double getAllocatedHeight()
		{
			return allocatedHeight;
		}

		/**
		 * @return Y position of top of label area
		 */
		public double getY()
		{
			return y;
		}
	}

	private static class Footnote
	{
		private final static int BOX_HORIZONTAL_PADDING = 4,
			BOX_VERTICAL_PADDING = 2, BOX_BORDER = 1, BOX_SPACING = 5;
		private int groupIndex;
		private TextDrawable name, numberGraph, numberFootnote;

		private double x;
		private int lineIndex;

		Footnote(int groupIndex, TextDrawable name, TextDrawable numberGraph,
			TextDrawable numberFootnote)
		{
			this.groupIndex = groupIndex;
			this.name = name;
			this.numberGraph = numberGraph;
			this.numberFootnote = numberFootnote;
		}

		double getWidth()
		{
			return name.getWidth() + numberFootnote.getWidth() +
				BOX_BORDER * 2 + BOX_HORIZONTAL_PADDING * 2 + BOX_SPACING;
		}

		double getHeight()
		{
			return numberFootnote.getHeight() + BOX_BORDER * 2
				+ BOX_VERTICAL_PADDING * 2;
		}

		/**
		 * @return Group index
		 */
		public int getGroup()
		{
			return groupIndex;
		}

		void setRoughPosition(double x, int lineIndex)
		{
			this.x = x;
			this.lineIndex = lineIndex;
		}

		/**
		 * Sets the position of the number in the graph (inside the first
		 * shape for that group).
		 * @param x X position
		 * @param y Y position
		 */
		void setGraphPosition(double x, double y)
		{
			numberGraph.move(x, y);
		}

		/**
		 * @param canvas Canvas that receives data
		 * @param baseY Y position of footnote area
		 * @param rowHeight Height of each row (to convert line index into Y)
		 * @param groupNames Group names
		 * @param colours Map from group name to colour
		 * @param border Border colour
		 */
		public void addTo(Canvas canvas, double baseY, double rowHeight,
			String[] groupNames, Map<String, GroupColours> colours,
			Color border)
		{
			double y = baseY + lineIndex * rowHeight;
			double boxHeight = getHeight();
			double boxWidth = numberFootnote.getWidth() + BOX_HORIZONTAL_PADDING * 2
				+ BOX_BORDER * 2;

			// Fill and outline box
			canvas.add(new RectDrawable(x + BOX_BORDER, y + BOX_BORDER,
				boxWidth - 2 * BOX_BORDER, boxHeight - 2 * BOX_BORDER,
				colours.get(groupNames[groupIndex]).getMain()));
			canvas.add(new RectDrawable(x, y, boxWidth, BOX_BORDER, border));
			canvas.add(new RectDrawable(x, y+boxHeight - BOX_BORDER, boxWidth, BOX_BORDER, border));
			canvas.add(new RectDrawable(x, y + BOX_BORDER, BOX_BORDER, boxHeight - 2 * BOX_BORDER, border));
			canvas.add(new RectDrawable(x + boxWidth - BOX_BORDER, y + BOX_BORDER, BOX_BORDER, boxHeight - 2 * BOX_BORDER, border));

			// Draw number
			numberFootnote.move(x + BOX_BORDER + BOX_HORIZONTAL_PADDING, y + boxHeight / 2);
			canvas.add(numberFootnote);
			name.move(x + boxWidth + BOX_SPACING, y + boxHeight / 2);
			canvas.add(name);
			canvas.add(numberGraph);
		}
	}

	private void trendChart(InputFile[] files, Canvas canvas)
	{
		// Get data
		TrendData data = new TrendData(files);
		String[] groupNames = data.getGroupNames();
		fillGroupColours(data.getGroupNames());

		// METRICS
		//////////

		int mLabelSidePadding = 10, mLargeCurveWidth = 20, mTitleBottomPadding = 10,
			mLargeCurvePadding = 10, mFootnoteMargin = 20, mFootnoteVerticalSpacing = 10,
			mDateMargin = 20, mDateTopPadding = 5;
		double mOverprint = 0.5;

		// LAYOUT
		/////////

		// Do title and calculate height
		TextDrawable titleLabel = new TextDrawable(title, 0, 0,
			TextDrawable.Alignment.TOP, foreground, fontName, false, titleSize);
		canvas.add(titleLabel);
		double titleHeight = titleLabel.getHeight();
		double graphY = titleHeight + mTitleBottomPadding;

		// Find all end labels and calculate width
		LinkedList<Label> endLabelsList = new LinkedList<Label>();
		int numPoints = data.getNumPoints();
		int lastPoint = numPoints - 1;
		double endLabelsWidth = 0;
		for(int group=0; group<data.getNumGroups(); group++)
		{
			if(data.getValue(group, lastPoint) > 0)
			{
				GroupColours colours = groupColours.get(groupNames[group]);
				Label label = new Label(group,
					new TextDrawable(groupNames[group], 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, true, labelSize),
					new TextDrawable(data.getPercentage(group, lastPoint), 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, false, labelSize));
				endLabelsList.add(label);
				endLabelsWidth = Math.max(endLabelsWidth, label.getWidth());
			}
		}
		Label[] endLabelsArray =
			endLabelsList.toArray(new Label[endLabelsList.size()]);
		endLabelsWidth += mLabelSidePadding * 2 + mLargeCurveWidth
			+ mLargeCurvePadding;

		// Find all start labels and calculate width
		LinkedList<Label> startLabelsList = new LinkedList<Label>();
		double startLabelsMinWidth = 0;
		for(int group=0; group<data.getNumGroups(); group++)
		{
			if(data.getValue(group, 0) > 0)
			{
				GroupColours colours = groupColours.get(groupNames[group]);
				Label label = new Label(group,
					new TextDrawable(groupNames[group], 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, true, labelSize),
					new TextDrawable(data.getPercentage(group, 0), 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, false, labelSize));
				startLabelsList.add(label);
				startLabelsMinWidth = Math.max(startLabelsMinWidth, label.getWidth());
			}
		}
		Label[] startLabelsArray =
			startLabelsList.toArray(new Label[startLabelsList.size()]);
		double startLabelsWidth = startLabelsMinWidth + mLabelSidePadding * 2
			+ mLargeCurveWidth + mLargeCurvePadding;
		double graphX = startLabels ? startLabelsWidth : 0;

		// We now know the width of the graph
		double graphWidth = canvas.getWidth() - graphX
			- (endLabels ? endLabelsWidth : 0);

		// Find all footnotes - these are groups which are not present in either
		// the start or end of the graph.
		int footnoteNumber = 1;
		LinkedList<Footnote> footnotesList = new LinkedList<Footnote>();
		for(int group=0; group<data.getNumGroups(); group++)
		{
			if(data.getValue(group, 0) == 0 && data.getValue(group, lastPoint) == 0)
			{
				GroupColours colours = groupColours.get(groupNames[group]);
				Footnote footnote = new Footnote(group,
					new TextDrawable(groupNames[group], 0, 0, TextDrawable.Alignment.CENTER,
						foreground, fontName, false, footnoteSize),
					new TextDrawable(footnoteNumber + "", 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, false, footnoteSize),
					new TextDrawable(footnoteNumber + "", 0, 0, TextDrawable.Alignment.CENTER,
						colours.getText(), fontName, false, footnoteSize));
				footnotesList.add(footnote);
				footnoteNumber++;
			}
		}

		// Calculate footnote wrapping.
		// Note that line 0 is the date line, we don't put footnotes on there
		// now, so it will always be skipped.
		double lineAvailable = 0;
		double x = 0;
		int lineIndex = 0;
		double rowHeight = 0;
		for(Footnote footnote : footnotesList)
		{
			if(footnote.getWidth() > lineAvailable)
			{
				lineIndex++;
				x = 0;
				lineAvailable = canvas.getWidth() - (endLabels ? endLabelsWidth : 0);
			}

			footnote.setRoughPosition(x, lineIndex);
			double width = footnote.getWidth() + mFootnoteMargin;
			x += width;
			lineAvailable -= width;
			if(rowHeight == 0)
			{
				rowHeight = footnote.getHeight();
			}
		}
		double footnotesHeight = (rowHeight * (lineIndex+1)) + (lineIndex * mFootnoteVerticalSpacing);

		// If there are no footnotes, calculate height using the date row only
		TextDrawable[] dateText = new TextDrawable[numPoints];
		double dateHeight = 0;
		for(int i=0; i<numPoints; i++)
		{
			dateText[i] = new TextDrawable(data.getPointDate(i), 0, 0, TextDrawable.Alignment.TOP,
				foreground, fontName, false, footnoteSize);
			dateHeight = Math.max(dateHeight, dateText[i].getHeight());
		}

		// This is the height of the bottom area below the graph
		double bottomAreaHeight = Math.max(footnotesHeight, dateHeight);

		// And this is the height of the main graph area
		double graphHeight = canvas.getHeight() - bottomAreaHeight -
			titleHeight - mTitleBottomPadding - mDateTopPadding;

		// Work out the main graph positions for each point based on the timeline
		int
			minDate = data.getPointLinearDateStart(0),
			maxDate = data.getPointLinearDateEnd(numPoints - 1);
		double dateScale = graphWidth / (double)(maxDate - minDate);
		PointPosition[] pointPositions = new PointPosition[numPoints];
		for(int i=0; i<pointPositions.length; i++)
		{
			double start = (data.getPointLinearDateStart(i) - minDate) * dateScale;
			double end = (data.getPointLinearDateEnd(i) - minDate) * dateScale;
			pointPositions[i] = new PointPosition(start, end,
				i == pointPositions.length - 1);
		}

		// Position last date
		double dateY = canvas.getHeight() - bottomAreaHeight;
		double lastDateWidth = dateText[dateText.length-1].getWidth();
		double lastDateMiddle = pointPositions[numPoints-1].getMiddle();
		double lastDateX;
		if(lastDateMiddle + (lastDateWidth/2) > graphWidth)
		{
			lastDateX = graphWidth - lastDateWidth;
		}
		else
		{
			lastDateX = lastDateMiddle - lastDateWidth/2;
		}
		dateText[dateText.length-1].move(lastDateX + graphX, dateY);

		// Position first date, unless it overlaps
		double firstDateWidth = dateText[0].getWidth();
		double firstDateMiddle = pointPositions[0].getMiddle();
		double firstDateX;
		if(firstDateMiddle - (firstDateWidth/2) < 0)
		{
			firstDateX = 0;
		}
		else
		{
			firstDateX = firstDateMiddle - firstDateWidth/2;
		}
		if(firstDateX + firstDateWidth >= lastDateX - mDateMargin)
		{
			dateText[0] = null;
		}
		else
		{
			dateText[0].move(firstDateX + graphX, dateY);
		}

		// Position other dates when there is room
		double nextAvailableX = firstDateX + firstDateWidth + mDateMargin;
		for(int i=1; i<numPoints-1; i++)
		{
			double dateWidth = dateText[i].getWidth();
			double dateMiddle = pointPositions[i].getMiddle();
			double dateX = dateMiddle - (dateWidth/2);
			if(dateX > nextAvailableX && dateX + dateWidth < lastDateX - mDateMargin)
			{
				dateText[i].move(dateX + graphX, dateY);
				nextAvailableX = dateX + dateWidth + mDateMargin;
			}
			else
			{
				dateText[i] = null;
			}
		}

		// Actually draw dates
		for(int i=0; i<numPoints; i++)
		{
			if(dateText[i] != null)
			{
				canvas.add(dateText[i]);
			}
		}

		// Position, move, and draw labels
		if(startLabels)
		{
			// Position
			distributeLabelsVertically(data, 0, mLabelSidePadding, graphY, graphHeight,
				startLabelsArray);

			// Draw
			double realAboveY = graphY;
			for(Label label : startLabelsArray)
			{
				double aboveY = label.getY();
				ShapeDrawable shape = new ShapeDrawable(0, aboveY,
					groupColours.get(groupNames[label.getGroup()]).getMain());
				double endX = startLabelsMinWidth + mLabelSidePadding*2;
				shape.lineTo(endX, aboveY);
				shape.flatCurveTo(endX + mLargeCurveWidth, realAboveY);
				double belowY = aboveY + label.getAllocatedHeight();
				double realBelowY = ((double)data.getValue(label.getGroup(), 0)
					* graphHeight / data.getTotal(0)) + realAboveY;
				double overprint = belowY > graphY + graphHeight - mOverprint ? 0 : mOverprint;
				shape.lineTo(endX + mLargeCurveWidth, realBelowY + overprint);
				shape.flatCurveTo(endX, belowY + overprint);
				shape.lineTo(0, belowY + overprint);
				shape.finish();
				canvas.add(shape);
				label.addTo(canvas);
				realAboveY = realBelowY;
			}
		}

		if(endLabels)
		{
			// Position
			distributeLabelsVertically(data, numPoints-1,
				graphX + graphWidth + mLargeCurvePadding + mLargeCurveWidth +
				mLabelSidePadding, 0, canvas.getHeight(),	endLabelsArray);

			// Draw
			double realAboveY = graphY;
			for(Label label : endLabelsArray)
			{
				double startX = graphX + graphWidth + mLargeCurvePadding;

				ShapeDrawable shape = new ShapeDrawable(startX, realAboveY,
					groupColours.get(groupNames[label.getGroup()]).getMain());
				double aboveY = label.getY();
				shape.flatCurveTo(startX + mLargeCurveWidth, aboveY);
				shape.lineTo(canvas.getWidth(), aboveY);
				double belowY = aboveY + label.getAllocatedHeight();
				double overprint = belowY > graphY + graphHeight - mOverprint ? 0 : mOverprint;
				shape.lineTo(canvas.getWidth(), belowY + overprint);
				shape.lineTo(startX + mLargeCurveWidth, belowY + overprint);
				double realBelowY = ((double)data.getValue(label.getGroup(), numPoints-1)
					* graphHeight / data.getTotal(numPoints-1)) + realAboveY;
				shape.flatCurveTo(startX, realBelowY + overprint);
				shape.finish();
				canvas.add(shape);
				label.addTo(canvas);

				realAboveY = realBelowY;
			}
		}

		// Draw actual graph
		for(int group=0; group<data.getNumGroups(); group++)
		{
			// See if there's a footnote for this group
			Footnote footnote = null;
			for(Footnote possible : footnotesList)
			{
				if(possible.getGroup() == group)
				{
					footnote = possible;
					break;
				}
			}
			GraphWorm shape = null;
			for(int point=0; point<numPoints; point++)
			{
				PointPosition position = pointPositions[point];
				PointPosition lastPosition = point==0 ? null : pointPositions[point-1];
				double total = (double)data.getTotal(point);
				double value = (double)data.getValue(group, point);
				double above = (double)data.getCumulativeValueBefore(group, point);
				double aboveY = (above * graphHeight / total) + graphY;
				double belowY = (value * graphHeight / total) + aboveY;

				if(value != 0)
				{
					if(shape == null)
					{
						Color color = groupColours.get(groupNames[group]).getMain();
						shape = new GraphWorm(position.start + graphX, aboveY, belowY,
							color, group==data.getNumGroups()-1);
					}
					// Draw curve from last to this
					if(lastPosition != null && lastPosition.hasCurve())
					{
						shape.makeCurve(position.start + graphX, aboveY, belowY);
					}
					// Draw straight
					shape.makeStraight(
						(position.hasCurve()
							? position.end - PointPosition.POINT_CURVE_SIZE
							: position.end) + graphX,
						aboveY, belowY);
				}
				else if(shape != null)
				{
					finishShape(canvas, footnote, shape);
					shape = null;
				}
			}

			if(shape != null)
			{
				finishShape(canvas, footnote, shape);
				shape = null;
			}
		}

		// Footnotes
		for(Footnote footnote : footnotesList)
		{
			footnote.addTo(canvas, dateY, rowHeight + mFootnoteVerticalSpacing,
				groupNames, groupColours, foreground);
		}
	}

	/**
	 * Finishes off a graph shape.
	 * @param canvas Canvas
	 * @param footnote Footnote to receive position
	 * @param shape Shape to finish
	 */
	private void finishShape(Canvas canvas, Footnote footnote,
		GraphWorm shape)
	{
		// Draw shape
		shape.addTo(canvas);
		// Add footnote
		if(footnote != null)
		{
			footnote.setGraphPosition(shape.getFootnoteX(),
				shape.getFootnoteY());
		}
	}

	/**
	 * Arranges the labels vertically, allowing each the minimum size to include
	 * the text plus a bit more.
	 * @param data Trend data
	 * @param point Point to use in trend data
	 * @param x X position to move label to
	 * @param y Y position of graph
	 * @param height Height of graph
	 * @param labels Labels to arrange
	 */
	private static void distributeLabelsVertically(
		TrendData data, int point, double x, double y, double height,
		Label[] labels)
	{
		// Start by allocating available space according to the data
		double total = (double)data.getTotal(point);
		for(Label label : labels)
		{
			label.allocateHeight(
				((double)data.getValue(label.getGroup(), point) / total) * height);
		}

		// Now share it out to make everything reach minimums
		for(int i=0; i<labels.length; i++)
		{
			labels[i].spreadHeight(labels, i);
		}

		// Height is finalised, so move the labels
		double currentY = 0;
		for(Label label : labels)
		{
			label.move(x, y + currentY);
			currentY += label.getAllocatedHeight();
		}
	}

	private Map<String, GroupColours> groupColours =
		new HashMap<String, GroupColours>();

	private static class GroupColours
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

	private final static int
		COLOUR_SATURATION_MAX = 200, COLOUR_SATURATION_MIN = 35,
		COLOUR_LIGHTNESS_BRIGHT = 230, COLOUR_LIGHTNESS_BRIGHT_MIN = 180,
		COLOUR_LIGHTNESS_DARK = 60, COLOUR_LIGHTNESS_DARK_MAX = 110,
		COLOUR_HUE_STEP = 30;

	private void fillGroupColours(final String[] groupNames)
	{
		// Organise into list of similar name
		Map<String, List<Integer>> similar = new HashMap<String, List<Integer>>();
		for(int group = 0; group < groupNames.length; group++)
		{
			String base = getNameBase(groupNames[group]);
			List<Integer> list = similar.get(base);
			if(list == null)
			{
				list = new LinkedList<Integer>();
				similar.put(base, list);
			}
			list.add(group);
		}

		// Now go through in original order
		int hue = 0;
		boolean light = true;
		for(int group = 0; group < groupNames.length; group++)
		{
			String base = getNameBase(groupNames[group]);
			List<Integer> list = similar.get(base);
			if(list == null)
			{
				// Already done this group
				continue;
			}

			// If there's only one, colour it normally
			if(list.size() == 1)
			{
				Color colour = ColorUtils.fromHsl(hue, COLOUR_SATURATION_MAX,
					light ? COLOUR_LIGHTNESS_BRIGHT : COLOUR_LIGHTNESS_DARK);
				if(!groupColours.containsKey(groupNames[group]))
				{
					groupColours.put(groupNames[group],
						new GroupColours(colour, light ? Color.BLACK : Color.WHITE));
				}
			}
			else
			{
				// Sort list into order
				Collections.sort(list, new Comparator<Integer>()
				{
					@Override
					public int compare(Integer o1, Integer o2)
					{
						// Work out double value for each group.
						return (int)Math.signum(getGroupNumericOrder(o1, groupNames[o1])
							- getGroupNumericOrder(o2, groupNames[o2]));
					}
				});

				// Now assign each colour according to evenly divided-out saturations
				for(int i=0; i<list.size(); i++)
				{
					int targetGroup = list.get(i);
					float proportion = (float)i / (float)(list.size()-1);
					int saturation = Math.round(COLOUR_SATURATION_MIN +
						(COLOUR_SATURATION_MAX - COLOUR_SATURATION_MIN) * proportion);
					int lightness;
					if(light)
					{
						lightness = Math.round(COLOUR_LIGHTNESS_BRIGHT_MIN
							+ (COLOUR_LIGHTNESS_BRIGHT - COLOUR_LIGHTNESS_BRIGHT_MIN) * (proportion));
					}
					else
					{
						lightness = Math.round(COLOUR_LIGHTNESS_DARK
							+ (COLOUR_LIGHTNESS_DARK_MAX - COLOUR_LIGHTNESS_DARK) * (1.0f - proportion));
					}
					Color colour = ColorUtils.fromHsl(hue, saturation, lightness);
					if(!groupColours.containsKey(groupNames[targetGroup]))
					{
						groupColours.put(groupNames[targetGroup],
							new GroupColours(colour, light ? Color.BLACK : Color.WHITE));
					}
				}
			}

			// Mark it done
			similar.remove(base);

			// Next colour index
			hue += COLOUR_HUE_STEP;
			if(hue >= 360)
			{
				hue -= 360;
			}
			light = !light;
		}
	}

	private final static Pattern NUMBER_REGEX =
		Pattern.compile("[0-9]+(\\.[0-9]+)?");

	private static double getGroupNumericOrder(int index, String name)
	{
		// See if we can find a number (possibly decimal) in the name
		Matcher m = NUMBER_REGEX.matcher(name);
		if(m.find())
		{
			double value = Double.parseDouble(m.group());

			// Use index as a tie-breaker by subtracting a miniscule proportion of it
			return value - 0.0000001 * (double)index;
		}

		// No number, use negative index (ensures that the first one is brightest)
		return -(index+1);
	}

	/**
	 * Get base name used for identifying similar groups. This is the name up to
	 * the first characters that is not a letter or whitespace.
	 * @param name Full name
	 * @return Base name
	 */
	private static String getNameBase(String name)
	{
		for(int i=0; i<name.length(); i++)
		{
			char c = name.charAt(i);
			if(!(Character.isLetter(c) || Character.isWhitespace(c)))
			{
				if(i==0)
				{
					// Don't try to do similarity if the name *starts* with one of these
					return name;
				}
				return name.substring(0, i);
			}
		}
		return name;
	}
}
