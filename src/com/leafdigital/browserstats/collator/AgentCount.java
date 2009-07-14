package com.leafdigital.browserstats.collator;

import java.io.*;
import java.util.*;

/** Holds counts of each user agent, organised by category if specified. */
public class AgentCount
{
	private int lines = 0;
	private TreeMap<String, AgentData> agents = new TreeMap<String, AgentData>();
	
	/**
	 * Counts a log line into this count object.
	 * @param agent User-agent
	 * @param ip IP address
	 * @param c Category
	 */
	void count(String agent, String ip, Category c)
	{
		AgentData data = agents.get(agent);
		if(data==null)
		{
			data = new AgentData();
			agents.put(agent, data);
		}
		data.count(ip, c);
		
		lines++;
		if((lines & 0x3fff)==0)
		{
			System.err.print(".");
		}
	}
	
	/**
	 * Writes this out as XML.
	 * @param f Target file
	 * @param period Time period (null if in ALL mode)
	 * @param categories Category list
	 * @throws IOException Any error writing file
	 */
	void write(File f, String period, Category[] categories) throws IOException
	{
		Writer w = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream(f), "UTF-8"));
		String periodAttribute = "";
		if(period != null)
		{
			periodAttribute = " date='" + period + "'";
		}
		w.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
			"<useragents" + periodAttribute + ">\n");
		
		for(Map.Entry<String, AgentData> data : agents.entrySet())
		{
			data.getValue().write(w, data.getKey(), categories);
		}
		
		w.write("</useragents>\n");
		w.close();
	}
}
