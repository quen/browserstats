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
