package com.leafdigital.browserstats.collator;

import java.io.*;

/** Input stream that uses a separate thread to read as fast as it can. */
public class ThreadedInputStream extends InputStream implements Runnable
{
	private final static int NUMBUFFERS = 64, BUFFERSIZE = 65536;
	
	private InputStream input;
	
	private byte[][] buffers = new byte[NUMBUFFERS][BUFFERSIZE];
	
	// readXX variables may only be accessed by the reading thread
	private int readEndBuffer = -999, readEndBufferSize = -1;
	private int readCurrentBuffer = -1, readCurrentPos = BUFFERSIZE, 
		readLastSafeBuffer = -1;
	
	private Object synch = new Object();
	
	// writeXX variables can be accessed by both threads, but only when
	// synchronized
	private boolean writeGotData = false;
	private int writeLastBuffer = -1, writeLastSafeBuffer = NUMBUFFERS-1, 
		writeEndBufferSize=-1;
	
	private boolean close, closed;
	
	private IOException exception = null;
	
	private long blockTime = 0, idleTime = 0;
	
	/**
	 * @param input Input stream that this class buffers
	 * @throws UnsupportedEncodingException If the encoding is unknown
	 */
	public ThreadedInputStream(InputStream input) throws UnsupportedEncodingException
	{
		this.input = input;
		
		(new Thread(this, "ThreadedInputStream")).start();
	}
	
	@Override
	public void run()
	{
		try
		{
			int writeBuffer = -1;
			while(true)
			{
				synchronized(synch)
				{
					// Did we just write a buffer?
					if(writeBuffer != -1)
					{
						writeLastBuffer = writeBuffer;
						if(!writeGotData)
						{
							writeGotData = true;
							synch.notifyAll();
						}
					}
					
					// Wait until we have a buffer available to fill
					while(writeLastBuffer == writeLastSafeBuffer && !close)
					{
						long before = System.currentTimeMillis();
						synch.wait();
						idleTime += System.currentTimeMillis() - before;						
					}
					if(close)
					{
						return;
					}
					writeBuffer = writeLastBuffer+1;
					if(writeBuffer >= NUMBUFFERS)
					{
						writeBuffer = 0;
					}
				}
				
				// Read from stream into that buffer
				int pos = 0;
				while(pos < BUFFERSIZE)
				{
					int read = input.read(buffers[writeBuffer], pos, BUFFERSIZE-pos);
					if(read == -1)
					{
						// EOF
						synchronized(synch)
						{
							if(pos == 0)
							{
								// When failure is at start of buffer, call it end of last one
								writeEndBufferSize = BUFFERSIZE;
								return;
							}
							else
							{
								// Mark failure position and update last-buffer count
								writeEndBufferSize = pos;
								writeLastBuffer = writeBuffer;
								return;
							}
						}
					}
					pos+=read;
				}
			}
		}
		catch(IOException e)
		{
			synchronized(synch)
			{
				exception = e;
				writeEndBufferSize = BUFFERSIZE;
			}
		}
		catch(InterruptedException e)
		{
		}
		finally
		{
			synchronized(synch)
			{
				writeGotData = true;
				closed = true;
				synch.notifyAll();
			}
		}
	}
	
	@Override
	public void close() throws IOException
	{
		synchronized(synch)
		{
			close = true;
			synch.notifyAll();
			while(!closed)
			{
				try
				{
					synch.wait();
				}
				catch(InterruptedException e)
				{
				}
			}
		}
		input.close();
	}
	
	private boolean moveReadBuffer() throws IOException
	{
		// Unusual (buffer-end) operation, so synchronize
		synchronized(synch)
		{
			do
			{
				// Update EOF information
				if(writeEndBufferSize!=-1)
				{
					readEndBuffer = writeLastBuffer;
					readEndBufferSize = writeEndBufferSize;
				}
				
				// OK, are we at the end of file?
				if(readCurrentBuffer == readEndBuffer)
				{
					if(exception != null)
					{
						throw exception;
					}
					return false;
				}
				
				// Update last-safe write buffer (to the one before the one we just finished)
				if(readCurrentBuffer != -1)
				{
					writeLastSafeBuffer = readCurrentBuffer - 1;
					if(writeLastSafeBuffer == -1)
					{
						writeLastSafeBuffer = NUMBUFFERS-1;
					}
					synch.notifyAll();
				}
				
				// See if there are new buffers available from the writing thread
				if(writeGotData)
				{
					readLastSafeBuffer = writeLastBuffer;
					writeGotData = false;
				}
				
				// Now, do we have a new buffer?
				if(readCurrentBuffer != readLastSafeBuffer)
				{
					readCurrentBuffer++;
					if(readCurrentBuffer >= NUMBUFFERS)
					{
						readCurrentBuffer = 0;
					}
					readCurrentPos = 0;
					return true;
				}
				
				// No? Damnit, we need to block
				while(!writeGotData)
				{
					try
					{
						long before = System.currentTimeMillis();
						synch.wait();
						blockTime += System.currentTimeMillis() - before;
					}
					catch(InterruptedException e)
					{
						throw new InterruptedIOException();
					}
				}
			}
			while(true);
		}
	}

	@Override
	public int read() throws IOException
	{
		// See if we're at the end of a buffer
		if(readCurrentPos == BUFFERSIZE ||
			(readCurrentPos == readEndBufferSize && readCurrentBuffer == readEndBuffer))
		{
			// Move to next buffer, and return if EOF
			if(!moveReadBuffer())
			{
				return -1;
			}
		}
		
		// Not at the end of any buffer, so just return current data
		return buffers[readCurrentBuffer][readCurrentPos++];
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int done = 0;
		while(len > 0)
		{
			// See if we're at the end of a buffer
			if(readCurrentPos == BUFFERSIZE ||
				(readCurrentPos == readEndBufferSize && readCurrentBuffer == readEndBuffer))
			{
				// Move to next buffer, and return if EOF
				if(!moveReadBuffer())
				{
					return done==0 ? -1 : done;
				}
			}
			
			// Copy data into target
			int available = readCurrentBuffer == readEndBuffer 
				? readEndBufferSize - readCurrentPos
				: BUFFERSIZE - readCurrentPos;
			
			int read = Math.min(available, len);
			System.arraycopy(buffers[readCurrentBuffer], readCurrentPos,
				b, off, read);
			off += read;
			len -= read;
			done += read;
			readCurrentPos += read;
		}
		return done;
	}

	/**
	 * @return Total time (ms) where I/O thread idled waiting for existing buffers
	 *   to be used
	 */
	public long getIdleTime()
	{
		return idleTime;
	}	
	
	/** @return Total time (ms) spent waiting for I/O */
	public long getBlockTime()
	{
		return blockTime;
	}
}
