package com.leafdigital.browserstats.collator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.regex.*;

/** 
 * Main class for collator utility which processes server log files. 
 * <p>
 * Command-line arguments: [options] [input file(s)]
 * <p>
 * Input options:
 * <ul>
 * <li><strong>-stdin</strong>: Read data from stdin instead of files.</li>
 * <li><strong>-lenient</strong>: Ignore log lines that don't match.</li>
 * <li><strong>-encoding {encoding}</strong>: Specify input encoding (default
 *   is UTF-8).</li>
 * <li><strong>-format {format}</strong>: Specify input format. Available formats
 *   are: tomcat, ?</li>
 * <li><strong>-customformat {regex} {ip field} {date field} {time field}
 *   {agent field} {path field} {date format} {time format}</strong>: Specify 
 *   an arbitrary regular expression that should match lines in the input 
 *   logfile; which bracketed sections correspond to the user agent, user's IP, 
 *   and date; and the format (Java SimpleDateFormat) of the date and time 
 *   fields.</li>
 * <li><strong>-category {name} {field} {regex}</strong>: Specify an expression
 *   which, if it matches lines, defines them as belonging to a category. 
 *   Multiple categories may be specified; the first to match will be used.
 *   The field must be either 'agent' 'ip' 'date' (match is against ISO format)
 *   'time' (ditto), 'path', or 'line' (whole line).</li>
 * <li><strong>-include {field} {regex}</strong>: Includes only lines which match
 *   the regular expression (field and regex as above)</li>
 * <li><strong>-exclude {field} {regex}</strong>: Excludes lines which match
 *   the regular expression (field and regex as above). Default includes
 *   common image and resource formats.</li>
 * <li><strong>-from {date}</strong>: Include only lines beginning from the
 *   specified date in ISO format, e.g. 2009-07-01.</li>
 * <li><strong>-to {date}</strong>: Include only lines up to the
 *   specified date (inclusive) in ISO format, e.g. 2009-07-31.</li>
 * <li><strong>-unordered</strong>: Indicates that input files may not be
 *   supplied in date order; increases memory consumption. (Without specifying
 *   this option, if the input files are unordered, the software attempts to
 *   detect that fact and gives an error.)</li>
 * <li><strong>--</strong> Indicate that this is the end of the options section
 *   (required if the first input file starts with a -).</strong></li>
 * </ul>
 * Output options:
 * <ul>
 * <li><strong>-folder {path}</strong>: Write output files to specified folder
 *   (default: current folder).</li>
 * <li><strong>-prefix {prefix}</strong>: Use the given prefix on output files.
 *   Default prefix is <tt>useragents</tt></li>
 * <li><strong>-daily</strong>: Write one output file per calendar day.</li>
 * <li><strong>-monthly</strong>: Write one output file per calendar month.</li>
 * <li><strong>-single</strong>: Write only one output file (default).</li>
 * <li><strong>-overwrite</strong>: Overwrite existing output files. (Without
 *   specifying this option, if the system would overwrite an existing file,
 *   it exits with an error.)</li>
 * </ul>
 * It is also possible to specify a file that contains command-line arguments.
 * This file may contain blank lines and comment lines beginning with #. All
 * other lines must be single arguments (for example, if specifying "-format
 * tomcat", there must be a line break between those two words). To specify this
 * file, use @{file}. You can combine command-line arguments with
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
	
	public enum TestType
	{
		PARSE
	};
	
	private StandardFormats formats;
	private LinkedList<File> inputFileList = new LinkedList<File>();

	private File folder = new File(".");
	private String prefix = "log";
	private TimePeriod period = TimePeriod.ALL;
	private boolean stdin = false;
	private boolean lenient = false;
	private String encoding = "UTF-8";
	private String from = null, to = null;
	private LogFormat format;		
	private File[] inputFiles = null;
	private Categoriser categoriser = new Categoriser();
	private boolean unordered = false;
	private boolean overwrite = false;
	private boolean verbose = false;
	private Pattern include = null, exclude = Pattern.compile(
		"(jpg|jpeg|png|gif|css|js)(\\?|$)");
	private LogLine.Field includeField, excludeField=LogLine.Field.PATH;
	private TestType test = null;
	
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
				i++;
				continue;
			}
			if(args[i].equals("-customformat"))
			{
				checkArgs(args, i, 7);
				format = new LogFormat(args[i+1], args[i+2], args[i+3], args[i+4],
					args[i+5], args[i+6], args[i+7], args[i+8]);
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
					includeField = LogLine.Field.get(args[i+1]);
					include = Pattern.compile(args[i+2]);
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
					excludeField = LogLine.Field.get(args[i+1]);
					exclude = Pattern.compile(args[i+2]);
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
		}
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
		// Construct counter
		AgentCounter counter = new AgentCounter(folder, prefix, period, 
			unordered, overwrite, categoriser.getCategories());
		
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
				}
				return;
			}

			for(LogLine line : reader)
			{
				boolean filter = false;
				if(include!=null && !include.matcher(line.get(includeField)).find())					
				{
					filter = true;
				}
				if(exclude!=null && exclude.matcher(line.get(excludeField)).find())
				{
					filter = true;
				}
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
}
