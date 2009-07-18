package com.leafdigital.browserstats.agents;

import java.io.*;
import java.util.*;

/** Tracks data found in the identify process so that it can be written out. */
class IdentifyResults
{
	private String[] categories;	
	private TreeMap<Agent, Counts> agents = new TreeMap<Agent, Counts>();
	private Counts unmatched;
	private int totalCount;
	private int[] totalCategoryCounts;

	IdentifyResults(String[] categories)
	{
		this.categories = categories;
		this.unmatched = new Counts();
		this.totalCategoryCounts = new int[categories.length];
	}

	/** Counts for a specific agent */
	private class Counts
	{
		private int count;
		private int[] categoryCounts = new int[categories.length];

		/**
		 * Add counts for this agent.
		 * @param count Total count of requests
		 * @param categoryCounts Requests in each category
		 */
		void add(int count, int[] categoryCounts)
		{
			this.count += count;
			for(int i=0; i<categoryCounts.length; i++)
			{
				this.categoryCounts[i] += categoryCounts[i];
			}
		}
		
		void write(Writer w) throws IOException
		{
			w.write("count='" + count +"'");
			for(int i=0; i<categories.length; i++)
			{
				w.write(" " + categories[i] + "='" + categoryCounts[i] + "'");
			}
			w.write("/>\n");
		}
	}

	/**
	 * Adds counts to the list.
	 * @param agent Identified user-agent (null = unknown)
	 * @param count Total count of requests
	 * @param categoryCounts Requests in each category
	 */
	public void addCounts(Agent agent, int count, int[] categoryCounts)
	{
		totalCount += count;
		for(int i=0; i<categoryCounts.length; i++)
		{
			totalCategoryCounts[i] += categoryCounts[i];
		}
		Counts counts = agent==null ? unmatched : agents.get(agent);
		if(counts==null)
		{
			counts = new Counts();
			agents.put(agent, counts);
		}		
		counts.add(count, categoryCounts);
	}
	
	/**
	 * Writes out results.
	 * @param f Target file (null = stdout)
	 * @throws IOException Any error when writing
	 */
	void write(File f) throws IOException
	{
		Writer w;
		if(f==null)
		{
			w = new OutputStreamWriter(System.out, "UTF-8");
		}
		else
		{
			w = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(f), "UTF-8"));
		}
		String categoryAttributes = "";
		for(int i=0; i<categories.length; i++)			
		{
			categoryAttributes += " total" + categories[i] + 
				"='" + totalCategoryCounts[i] + "'";
		}
		
		w.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
			"<knownagents totalcount='" + totalCount + "'" + categoryAttributes + ">\n");
		
		w.write("<agent type='unknown' ");
		unmatched.write(w);
		
		for(Map.Entry<Agent, Counts> entry : agents.entrySet())
		{
			entry.getKey().writeAgentTagStart(w);
			entry.getValue().write(w);
		}
		
		w.write("</knownagents>\n");
		if(f!=null)
		{
			w.close();
		}
	}
}
