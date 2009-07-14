package com.leafdigital.browserstats.collator;

/** A single line of log data. */
public class LogLine
{
	private String userAgent, isoDate, isoTime, ip;
	private Category category;

	/**
	 * @param userAgent User-agent string
	 * @param isoDate Date in ISO YYYY-MM-DD format
	 * @param isoTime Time in ISO HH:mm:ss format
	 * @param ip IP address (or other unique identifier)
	 * @param category Category (may be Category.NONE)
	 */
	LogLine(String userAgent, String isoDate, String isoTime, String ip, Category category)
	{
		this.userAgent = userAgent;
		this.isoDate = isoDate;
		this.isoTime = isoTime;
		this.ip = ip;
		this.category = category;
	}

	/** @return User-agent string */
	public String getUserAgent()
	{
		return userAgent;
	}

	/** @return Date in ISO YYYY-MM-DD format */
	public String getIsoDate()
	{
		return isoDate;
	}
	
	/** @return Time in ISO HH:mm:ss format */
	public String getIsoTime()
	{
		return isoTime;
	}

	/** @return IP address (or other unique identifier) */
	public String getIp()
	{
		return ip;
	}
	
	/** @return Category (may be Category.NONE) */
	public Category getCategory()
	{
		return category;
	}
	
	@Override
	public String toString()
	{
		return getIsoDate() + ":" + getIp() + ":" + category + ":" + getUserAgent();
	}
}
