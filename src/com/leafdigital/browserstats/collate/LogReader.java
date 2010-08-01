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
package com.leafdigital.browserstats.collate;

import java.io.*;
import java.util.*;

/** Handles reading of log files / input */
public class LogReader implements Iterable<LogLine>
{
	private int fileIndex = -1;
	private BufferedReader reader = null;
	private ThreadedInputStream stream;

	private int invalidLines = 0, wrongTimeLines = 0, processedLines = 0;
	private long ioIdleTime=0, ioBlockTime=0;

	private LogFormat format;
	private String encoding;
	private boolean lenient;
	private File[] files;
	private Categoriser categoriser;
	private String from, to;

	private LogLine nextLine;
	private IOException ioException;

	private Iterator<LogLine> iterator;

	/**
	 *
	 * @param format Format of log lines
	 * @param encoding Character encoding
	 * @param lenient True to ignore malformed lines
	 * @param files Array of files or null to use stdin
	 * @param categoriser Categoriser
	 * @param from ISO date to skip lines before (null if none)
	 * @param to ISO date to skip lines after (null if none)
	 * @throws IOException If there is a problem opening data
	 */
	LogReader(LogFormat format, String encoding, boolean lenient,
		File[] files, Categoriser categoriser, String from,
		String to)
		throws IOException
	{
		this.format = format;
		this.lenient = lenient;
		this.encoding = encoding;
		this.files = files;
		this.categoriser = categoriser;
		this.from = from;
		this.to = to;

		try
		{
			openNext();
			nextLine = readLine();
			iterator = new LogIterator();
		}
		catch(IOException e)
		{
			close();
			throw e;
		}
		catch(RuntimeException e)
		{
			close();
			throw e;
		}
	}

	private LogLine readLine() throws IOException
	{
		while(true)
		{
			// Try to read a line
			String line = reader.readLine();

			// If EOF, try next file
			if(line==null)
			{
				// If no next file, return
				if(!openNext()) return null;
				continue;
			}

			// If line is empty, skip it
			if(line.trim().equals(""))
			{
				continue;
			}

			// Parse line
			LogLine result;
			try
			{
				result = format.parse(line, categoriser);
				if(result==null)
				{
					// Format wants to skip this line
					continue;
				}
			}
			catch(IllegalArgumentException e)
			{
				if(lenient)
				{
					invalidLines++;
					// Print out invalid line (with a few extra spaces)
					System.err.println("\n\nSkipping invalid input line (" + e.getMessage()
						+ "):\n[" + line + "]\n");
					continue;
				}
				else
				{
					throw new IOException("Invalid input line (" + e.getMessage() +
						")\n[" + line + "]");
				}
			}

			// Skip lines outside date range
			if( (from!=null && result.getIsoDate().compareTo(from) < 0)
				|| (to!=null && result.getIsoDate().compareTo(to) > 0))
			{
				wrongTimeLines++;
				continue;
			}

			processedLines++;
			return result;
		}
	}

	/**
	 * Opens the next file/input stream.
	 * @return True if the next stream has been opened, false otherwise
	 * @throws IOException
	 */
	private boolean openNext() throws IOException
	{
		closeReader();

		fileIndex++;
		if(files==null)
		{
			if(fileIndex > 0)
			{
				return false;
			}
			stream = new ThreadedInputStream(System.in);
		}
		else
		{
			if(fileIndex >= files.length)
			{
				return false;
			}
			stream = new ThreadedInputStream(new FileInputStream(files[fileIndex]));
		}
		reader = new BufferedReader(new InputStreamReader(stream, encoding));
		return true;
	}

	/** @return Number of invalid lines skipped (if lenient mode is on) */
	public int getInvalidLines()
	{
		return invalidLines;
	}

	/** @return Number of lines that were outside the date range (if date range given) */
	public int getWrongTimeLines()
	{
		return wrongTimeLines;
	}

	/** @return Number of lines processed */
	public int getProcessedLines()
	{
		return processedLines;
	}

	/** @return Total lines handled, including those skipped */
	public int getTotalLines()
	{
		return processedLines + invalidLines + wrongTimeLines;
	}

	/** @return Time in milliseconds that IO was idle (waiting for main thread
	 * to use up existing buffers) */
	public long getIoIdleTime()
	{
		return ioIdleTime;
	}

	/** @return Time in milliseconds that IO blocked */
	public long getIoBlockTime()
	{
		return ioBlockTime;
	}

	/** @return IO exception that terminated reading, or null if none) */
	public IOException getException()
	{
		return ioException;
	}

	private class LogIterator implements Iterator<LogLine>
	{
		@Override
		public boolean hasNext()
		{
			return nextLine != null;
		}

		@Override
		public LogLine next()
		{
			if(nextLine == null)
			{
				throw new NoSuchElementException();
			}
			LogLine result = nextLine;

			try
			{
				nextLine = readLine();
			}
			catch(IOException e)
			{
				ioException = e;
				nextLine = null;
			}

			return result;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<LogLine> iterator()
	{
		return iterator;
	}

	/**
	 * Closes the reader if necessary (can be called midway through iterating).
	 */
	public void close()
	{
		closeReader();
	}

	private void closeReader()
	{
		if(reader==null)
		{
			return;
		}
		try
		{
			ioIdleTime += stream.getIdleTime();
			ioBlockTime += stream.getBlockTime();
			reader.close();
			reader = null;
			stream = null;
		}
		catch(IOException e)
		{
		}
		reader = null;
	}
}

