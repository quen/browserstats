package com.leafdigital.browserstats.collator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

import com.leafdigital.browserstats.collator.LogLine.Field;
import com.leafdigital.browserstats.shared.CommandLineTool;

/** 
 * Main class for collator utility which processes server log files. 
 */
public class Collate extends CommandLineTool
{
	private final static Pattern REGEX_ISO_DATE = 
		Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
	
	/** Time periods for output. */
	public enum TimePeriod
	{
		/** One output file per day */
		DAILY, 
		/** One output file per month */
		MONTHLY, 
		/** Single output file */
		ALL
	};

	/** Special test constants to check options. */
	private enum TestType
	{
		/** Show a single parsed server log line */
		PARSE(0),
		/** Show all included paths */
		SHOWINCLUDES(1),
		/** Show all excluded paths */
		SHOWEXCLUDES(1),
		/** Test matching a line */
		LINE(1);
		
		private int params;
		TestType(int params)
		{
			this.params = params;
		}
		
		/** @return Number of parameters required by the option */
		int getParams()
		{
			return params;
		}
	};
	
	private static class LineMatcher
	{
		private LogLine.Field field;
		private Pattern regex;
		
		private LineMatcher(Field field, Pattern regex)
		{
			this.field = field;
			this.regex = regex;
		}
		
		private boolean match(LogLine line)
		{
			return regex.matcher(line.get(field)).find();
		}
	}
	
	private final static LineMatcher DEFAULTEXCLUDE = new LineMatcher(
		LogLine.Field.PATH, Pattern.compile("(?:j(?:s|pe?g)|png|gif|css|ico)(?:\\?|$)"));

	private final static LineMatcher DEFAULTEXCLUDE2 = new LineMatcher(
		LogLine.Field.AGENT, Pattern.compile("^(?:null|-)?$"));

	private final static LineMatcher DEFAULTINCLUDE = new LineMatcher(
		LogLine.Field.STATUS, Pattern.compile("^200$"));

	private StandardFormats formats;

	private File folder = new File(".");
	private String prefix = "log";
	private TimePeriod period = TimePeriod.ALL;
	private boolean stdout = false;
	private boolean lenient = false;
	private String encoding = "UTF-8";
	private String from = null, to = null;
	private LogFormat format;		
	private Categoriser categoriser = new Categoriser();
	private boolean unordered = false;
	private boolean overwrite = false;
	private boolean verbose = false;	
	private LinkedList<LineMatcher> includes = null, excludes = null;
	private TestType test = null;
	private String[] testParams = null;
	private boolean customFormat = false;
	
	/**
	 * @param args Command-line arguments
	 */
	public static void main(String[] args)
	{
		Collate c = new Collate();
		c.run(args);
	}
	
	private Collate()
	{
		// Load standard formats
		try
		{
			formats = new StandardFormats();
			format = formats.getFormat("apache");
		}
		catch(IOException e)
		{
			System.err.println("Error loading standard formats:\n\n" + e.getMessage());
			failed();
		}
	}
	
	@Override
	protected int processArg(String[] args, int i)
	{
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
		if(args[i].equals("-prefix"))
		{
			checkArgs(args, i, 1);
			prefix = args[i+1];
			return 2;
		}
		if(args[i].equals("-from"))
		{
			checkArgs(args, i, 1);
			from = args[i+1];
			if(!REGEX_ISO_DATE.matcher(from).matches())
			{
				throw new IllegalArgumentException("Invalid ISO date: " + from);
			}
			return 2;
		}
		if(args[i].equals("-to"))
		{
			checkArgs(args, i, 1);
			to = args[i+1];
			if(!REGEX_ISO_DATE.matcher(to).matches())
			{
				throw new IllegalArgumentException("Invalid ISO date: " + to);
			}
			return 2;
		}
		if(args[i].equals("-daily"))
		{
			period = TimePeriod.DAILY;
			return 1;
		}
		if(args[i].equals("-monthly"))
		{
			period = TimePeriod.MONTHLY;
			return 1;
		}
		if(args[i].equals("-single"))
		{
			period = TimePeriod.ALL;
			return 1;
		}
		if(args[i].equals("-overwrite"))
		{
			overwrite = true;
			return 1; 
		}
		if(args[i].equals("-stdout"))
		{
			stdout = true;
			return 1;
		}
		if(args[i].equals("-lenient"))
		{
			lenient = true;
			return 1;
		}
		if(args[i].equals("-unordered"))
		{
			unordered = true;
			return 1;
		}
		if(args[i].equals("-verbose"))
		{
			verbose = true;
			return 1;
		}
		if(args[i].equals("-encoding"))
		{
			checkArgs(args, i, 1);
			encoding = args[i+1];
			if(!Charset.isSupported(encoding))
			{
				throw new IllegalArgumentException(
					"Unsupported character encoding: " + encoding);
			}
			return 2;
		}
		if(args[i].equals("-format"))
		{
			checkArgs(args, i, 1);
			format = formats.getFormat(args[i+1]);
			return 2;
		}
		if(args[i].equals("-test"))
		{
			checkArgs(args, i, 1);
			try
			{
				test = TestType.valueOf(args[i+1].toUpperCase());
			}
			catch(IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
					"Unrecognised test type: " + args[i+1]);
			}
			checkArgs(args, i, 1+test.getParams());
			testParams = new String[test.getParams()];
			for(int j=0; j<test.getParams(); j++)
			{
				testParams[j] = args[i+2+j];
			}
			
			return 2+test.getParams();
		}
		if(args[i].equals("-customformat"))
		{
			checkArgs(args, i, 7);
			format = new LogFormat(args[i+1], args[i+2], args[i+3], args[i+4],
				args[i+5], args[i+6], args[i+7], args[i+8], args[i+9]);
			customFormat = true;
			return 8;
		}
		if(args[i].equals("-customskip"))
		{
			checkArgs(args, i, 1);
			if(!customFormat)
			{
				throw new IllegalArgumentException(
					"Cannot use -customskip except with -customformat");
			}
			format.setSkip(args[i+1]);
			return 2;
		}
		if(args[i].equals("-category"))
		{
			checkArgs(args, i, 3);
			Category c = new Category(args[i+1], args[i+2], args[i+3]);
			categoriser.addCategory(c);
			return 4;
		}
		if(args[i].equals("-include"))
		{
			checkArgs(args, i, 2);
			try
			{
				LineMatcher matcher = new LineMatcher(
					LogLine.Field.get(args[i+1]),Pattern.compile(args[i+2]));
				if(includes==null)
				{
					includes = new LinkedList<LineMatcher>();
				}
				includes.add(matcher);						
			}
			catch(PatternSyntaxException e)
			{
				throw new IllegalArgumentException(
					"Invalid -include regex: " + args[i+2]);
			}
			catch(IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
					"Invalid -include field: " + args[i+1]);
			}
			return 3;
		}
		if(args[i].equals("-exclude"))
		{
			checkArgs(args, i, 2);
			try
			{
				LineMatcher matcher = new LineMatcher(
					LogLine.Field.get(args[i+1]),Pattern.compile(args[i+2]));
				if(excludes==null)
				{
					excludes = new LinkedList<LineMatcher>();
				}
				excludes.add(matcher);						
			}
			catch(PatternSyntaxException e)
			{
				throw new IllegalArgumentException(
					"Invalid -exclude regex: " + args[i+2]);
			}
			catch(IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
					"Invalid -exclude field: " + args[i+1]);
			}
			return 3;
		}
		return 0;
	}
	
	@Override
	protected void validateArgs() throws IllegalArgumentException
	{		
		if(stdout && period!=TimePeriod.ALL)
		{
			throw new IllegalArgumentException(
				"Cannot specify -stdout with -daily or -monthly");
		}
		
		if(includes==null)
		{
			includes = new LinkedList<LineMatcher>();
			includes.add(DEFAULTINCLUDE);
		}
		if(excludes==null)
		{
			excludes = new LinkedList<LineMatcher>();
			excludes.add(DEFAULTEXCLUDE);
			excludes.add(DEFAULTEXCLUDE2);
		}
	}
	
	private boolean include(LogLine line)
	{
		boolean include = false;
		for(LineMatcher matcher : includes)
		{
			if(matcher.match(line))					
			{
				include = true;
				break;
			}
		}
		if(!include)
		{
			return false;
		}
		for(LineMatcher matcher : excludes)
		{
			if(matcher.match(line))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void go()
	{
		if(test!=null)
		{
			switch(test)
			{
			case LINE:
				testLine();
				return;
			}
		}
		// Construct counter
		AgentCounter counter = new AgentCounter(folder, prefix, period, 
			unordered, overwrite, categoriser.getCategories(), stdout);
		
		long maxRam = 0;
		int count = 0;
		int filtered = 0;
		long startTime = System.currentTimeMillis();
		
		try
		{
			// Process files
			LogReader reader = new LogReader(
				format, encoding, lenient, getInputFiles(), categoriser, from, to);
			try
			{
				if(test!=null)
				{
					switch(test)
					{
					case PARSE:
						testParse(reader);
						break;
					case SHOWINCLUDES:
						testIncludes(reader, true);
						break;
					case SHOWEXCLUDES:
						testIncludes(reader, false);
						break;
					}
					return;
				}

				for(LogLine line : reader)
				{
					boolean filter = !include(line);
					if(filter)
					{
						filtered++;
					}
					else
					{
						try
						{
							counter.process(line);
						}
						catch(IOException e)
						{
							System.err.println("\n\nError writing output:\n\n" + e.getMessage());
							return;
						}
					}
					// About every 1024 lines, check RAM
					if((count & 0x3ff) == 0)
					{
						maxRam = Math.max(maxRam, Runtime.getRuntime().totalMemory()
							- Runtime.getRuntime().freeMemory());
					}
					count++;
				}
			}
			finally
			{
				reader.close();
			}
			if(reader.getException() != null)
			{
				throw reader.getException();
			}

			// Flush output
			try
			{
				counter.flush();
			}
			catch(IOException e)
			{
				System.err.println("\n\nError writing output:\n\n" + e.getMessage());
				return;
			}
			
			// Output information
			if(!stdout)
			{
				System.err.println("Total lines read: " + reader.getTotalLines());
				if(reader.getInvalidLines() > 0)
				{
					System.err.println("Skipped (invalid): " + reader.getInvalidLines());
				}
				if(reader.getWrongTimeLines() > 0)
				{
					System.err.println("Skipped (date out of range): " + reader.getWrongTimeLines());
				}
				if(filtered > 0)
				{
					System.err.println("Skipped (include/exclude): " + filtered);
				}
				if(verbose)
				{
					System.err.println();
					System.err.println("Total time: " + (System.currentTimeMillis()-startTime) + " ms");
					System.err.println("I/O blocks (processing waits for I/O): "
						+ reader.getIoBlockTime() + "ms");
					System.err.println("I/O idles (I/O waits for processing): "
						+ reader.getIoIdleTime() + "ms");
					System.err.println("Max RAM usage: " + ((maxRam+(512*1024))/(1024*1024)) + " MB");
				}
			}
		}
		catch(IOException e)
		{
			System.err.println("\n\nError reading logs:\n\n" + e.getMessage());
			return;
		}
	}
	
	private void testParse(LogReader reader)
	{
		for(LogLine line : reader)
		{
			System.err.println(line.getDescription());
			return;
		}
	}
	
	private void testIncludes(LogReader reader, boolean includes)
	{
		LogLine.Field field = LogLine.Field.valueOf(testParams[0].toUpperCase());
		
		HashSet<String> values = new HashSet<String>();
		for(LogLine line : reader)
		{
			if(include(line) == includes)
			{
				String value = line.get(field);
				if(values.add(value))
				{
					System.out.println(value);
				}
			}
		}
	}
	
	private void testLine()
	{
		System.out.println("Line:");
		System.out.println(testParams[0]);
		System.out.println();
		try
		{
			LogLine line = format.parse(testParams[0], categoriser);
			if(line==null)
			{
				System.out.println("Match: skip.");
			}
			else
			{
				System.out.println("Match.");
				System.out.println();
				System.out.println(line.getDescription());
			}
		}
		catch(IllegalArgumentException e)
		{
			System.out.println("No match.");
		}
	}
}
