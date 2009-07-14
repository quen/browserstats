package com.leafdigital.util.xml;

import java.io.IOException;

/** Exception used for XML errors */
public class XMLException extends IOException
{
	/**
	 * @param message
	 */
	public XMLException(String message)
	{
		super(message);
	}
	/**
	 * @param cause
	 */
	public XMLException(Throwable cause)
	{
		super("Invalid XML");
		initCause(cause);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public XMLException(String message,Throwable cause)
	{
		super(message);
		initCause(cause);
	}
}
