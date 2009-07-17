package com.leafdigital.browserstats.collator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

import com.leafdigital.browserstats.collator.LogLine.Field;

/** 
 * Main class for collator utility which processes server log files. 
 */
public class Collator
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
	public enum TestType
	{
		/** Show a single parsed server log line */
		PARSE(0),
		/** Show all included paths */
		SHOWINCLUDES(1),
		/** Show all excluded paths */
		SHOWEXCLUDES(1);
		
		private int params;
		TestType(int params)
		{
			this.params = params;
		}
		
		/** @return Number of parameters required by the option */
		public int getParams()
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
		LogLine.Field.PATH, Pattern.compile("(jpg|jpeg|png|gif|css|js|ico)(\\?|$)"));
	private final static LineMatcher DEFAULTINCLUDE = new LineMatcher(
		LogLine.Field.STATUS, Pattern.compile("200"));


	private StandardFormats formats;
	private LinkedList<File> inputFileList = new LinkedList<File>();

	private File folder = new File(".");
	private String prefix = "log";
	private TimePeriod period = TimePeriod.ALL;
	private boolean stdin = false, stdout = false;
	private boolean lenient = false;
	private String encoding = "UTF-8";
	private String from = null, to = null;
	private LogFormat format;		
	private File[] inputFiles = null;
	private Categoriser categoriser = new Categoriser();
	private boolean unordered = false;
	private boolean overwrite = false;
	private boolean verbose = false;	
	private LinkedList<LineMatcher> includes = null, excludes = null;
	private TestType test = null;
	private String[] testParams = null;
	
	private boolean showHelp = false;
	
	/**
	 * @param args Command-line arguments
	 */
	public static void main(String[] args)
	{
		Collator c = new Collator();
		try
		{
			c.processArgs(args, true);
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("Error processing command-line arguments:\n\n" +
				e.getMessage());			
			return;
		}
		c.go();
	}
	
	private Collator()
	{
		// Load standard formats
		try
		{
			formats = new StandardFormats();
		}
		catch(IOException e)
		{
			System.err.println("Error loading standard formats:\n\n" + e.getMessage());
			return;
		}
		format = formats.getFormat("tomcat");		
	}
	
	private void processArgsFile(File f) throws IOException, IllegalArgumentException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(			
			new FileInputStream(f), "UTF-8"));
		try
		{
			LinkedList<String> args = new LinkedList<String>();
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
				{
					// EOF
					break;
				}
				
				line = line.trim();
				if(line.equals("") || line.startsWith("#"))
				{
					// Skip blank lines and comments
					continue;
				}
				
				args.add(line);
			}
			processArgs(args.toArray(new String[args.size()]), false);
		}
		finally
		{
			reader.close();
		}
	}
	
	private void processArgs(String[] args, boolean commandLine) 
		throws IllegalArgumentException
	{
		if(args.length==0 && commandLine)
		{
			showHelp = true;
			return;			
		}
		
		int i=0;
		for(; i<args.length; i++)
		{
			if(args[i].startsWith("@"))
			{
				String argFileName = args[i].substring(1);
				if(argFileName.equals(""))
				{
					checkArgs(args, i, 1);
					argFileName = args[i+1];
					i++;
				}
				File argFile = new File(argFileName);
				if(!argFile.exists())
				{
					throw new IllegalArgumentException("Arguments file does not exist: " + argFile);
				}
				try
				{
					processArgsFile(argFile);					
				}
				catch(IOException e)
				{
					throw new IllegalArgumentException("Error reading args file");
				}
				continue;
			}
			if(args[i].equals("-folder"))
			{
				checkArgs(args, i, 1);
				folder = new File(args[i+1]);
				if(!folder.exists() || !folder.isDirectory())
				{
					throw new IllegalArgumentException("Folder does not exist: " + folder);
				}
				i++;
				continue;
			}
			if(args[i].equals("-prefix"))
			{
				checkArgs(args, i, 1);
				prefix = args[i+1];
				i++;
				continue;
			}
			if(args[i].equals("-from"))
			{
				checkArgs(args, i, 1);
				from = args[i+1];
				if(!REGEX_ISO_DATE.matcher(from).matches())
				{
					throw new IllegalArgumentException("Invalid ISO date: " + from);
				}
				i++;
				continue;
			}
			if(args[i].equals("-to"))
			{
				checkArgs(args, i, 1);
				to = args[i+1];
				if(!REGEX_ISO_DATE.matcher(to).matches())
				{
					throw new IllegalArgumentException("Invalid ISO date: " + to);
				}
				i++;
				continue;
			}
			if(args[i].equals("-daily"))
			{
				period = TimePeriod.DAILY;
				continue;
			}
			if(args[i].equals("-help"))
			{
				showHelp = true;
				return;
			}
			if(args[i].equals("-monthly"))
			{
				period = TimePeriod.MONTHLY;
				continue;
			}
			if(args[i].equals("-single"))
			{
				period = TimePeriod.ALL;
				continue;
			}
			if(args[i].equals("-overwrite"))
			{
				overwrite = true;
				continue;
			}
			if(args[i].equals("-stdin"))
			{
				stdin = true;
				continue;
			}
			if(args[i].equals("-stdout"))
			{
				stdout = true;
				continue;
			}
			if(args[i].equals("-lenient"))
			{
				lenient = true;
				continue;
			}
			if(args[i].equals("-unordered"))
			{
				unordered = true;
				continue;
			}
			if(args[i].equals("-verbose"))
			{
				verbose = true;
				continue;
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
				i++;
				continue;
			}
			if(args[i].equals("-format"))
			{
				checkArgs(args, i, 1);
				format = formats.getFormat(args[i+1]);
				i++;
				continue;
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
				
				i += 1+test.getParams();
				continue;
			}
			if(args[i].equals("-customformat"))
			{
				checkArgs(args, i, 7);
				format = new LogFormat(args[i+1], args[i+2], args[i+3], args[i+4],
					args[i+5], args[i+6], args[i+7], args[i+8], args[i+9]);
				i+=7;
				continue;
			}
			if(args[i].equals("-category"))
			{
				checkArgs(args, i, 3);
				Category c = new Category(args[i+1], args[i+2], args[i+3]);
				categoriser.addCategory(c);
				i+=3;
				continue;
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
				i+=2;
				continue;
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
				i+=2;
				continue;
			}
			if(args[i].equals("--"))
			{
				i++;
				break;
			}
			if(args[i].startsWith("-"))
			{
				throw new IllegalArgumentException("Unknown option: " + args[i]);
			}
			break;
		}
		
		for(; i<args.length; i++)
		{
			File f = new File(args[i]);				
			if(!f.exists())
			{
				throw new IllegalArgumentException("Input file not found: " + f);
			}
			if(!f.canRead())
			{
				throw new IllegalArgumentException("Input file not readable: " + f);
			}
			inputFileList.add(f);
		}
		
		if (commandLine)
		{
			inputFiles = inputFileList.toArray(new File[inputFileList.size()]);
			inputFileList = null;
			if(inputFiles.length > 0 && stdin)
			{
				throw new IllegalArgumentException(
					"Cannot specify both input files and -stdin");
			}
			if(inputFiles.length == 0 && !stdin)
			{
				throw new IllegalArgumentException(
					"Must specify either -stdin or input file(s)");
			}
			if(stdin)
			{
				inputFiles = null;
			}
			
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
			}
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
	
	private static void checkArgs(String[] args, int i, int required)
		throws IllegalArgumentException
	{
		if(i+required >= args.length)
		{
			throw new IllegalArgumentException("Option " + args[i] + " requires " +
				required + "parameters");
		}
	}
	
	private void go()
	{
		if(showHelp)
		{
			showHelp();
			return;
		}
		
		// Construct counter
		AgentCounter counter = new AgentCounter(folder, prefix, period, 
			unordered, overwrite, categoriser.getCategories(), stdout);
		
		long maxRam = 0;
		int count = 0;
		int filtered = 0;
		
		try
		{
			// Process files
			LogReader reader = new LogReader(
				format, encoding, lenient, inputFiles, categoriser, from, to);
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
	
	private void showHelp()
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				Collator.class.getResourceAsStream("commandline.txt"), "UTF-8"));
			while(true)
			{
				String line = reader.readLine();
				if(line==null)
				{
					break;
				}
				System.out.println(line);
			}
		}
		catch(IOException e)
		{
			// Come on
			throw new Error("Cannot load command-line help");
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
}
