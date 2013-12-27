package com.ssb.droidsound.service;

import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.ssb.droidsound.utils.Log;

public class AsyncAudioPlayer implements Runnable
{	
	private static final String TAG = AsyncAudioPlayer.class.getSimpleName();
	
	private AudioTrack audioTrack;
	private static int channels;
	private static int bufSize;
	private static int FREQ;
	private static int silence;
	private static int SEC;
		
	private static class SampleArray {
		SampleArray(short [] s, int l)
		{
			samples = s;
			len = l;
		}
		public short [] samples;
		public int len;
	}
		
	private LinkedList<SampleArray> buffers;

	private boolean doStart;

	public static enum Command
	{
		START,
		STOP,
		PAUSE,
		RELEASE,
		PLAY,
		FLUSH
	};
	
	private LinkedList<Command> commands = new LinkedList<Command>();

	private volatile int playbackPosition;
	private volatile int startPlaybackHead;
	private volatile int playPosOffset;
	private int framesWritten;

	@SuppressWarnings("unused")
	private volatile int framesRead;
	@SuppressWarnings("unused")
	private volatile int byteswritten;
	private volatile boolean stopped;
	private volatile boolean isPaused;
	private volatile boolean doExit;
	private volatile boolean holdData;
	private int markPosition;
	private volatile int bufferTotal;

	public AsyncAudioPlayer(int bs, int freq, int chancount)
	{
		bufSize = bs;
		silence = 0;
		FREQ = freq;
		channels = chancount;
		SEC = freq * 2;
	}
	
	@Override
	public void run()
	{
		init();

		try 
		{
			while(true)
			{		
				if(doStart)
				{
					isPaused = false;
					Log.d(TAG, "Starting playback");
					audioTrack.play();
					startPlaybackHead = audioTrack.getPlaybackHeadPosition();
					playPosOffset = 0;
					doStart = false;
					stopped = false;
					holdData = false;
				}	
				
				boolean doSleep = true; // <---- SUPER IMPORTANT MUST ALWAYS BE TRUE
				if(!isPaused && !stopped)
				{
					playbackPosition = audioTrack.getPlaybackHeadPosition();
					SampleArray data = null;
					synchronized (buffers)
					{
						if(!buffers.isEmpty()) {
							data = buffers.removeFirst();
							bufferTotal -= (data.len/2); // bufferTotal is length in frames
						}
					}
					if(data != null)
					{
						audioTrack.write(data.samples, 0, data.len);		
						framesRead += data.len/2;
						returnArray(data.samples);
						doSleep = false;
					}
				

				}
				runCommands();
				if(doExit)
					return;
				if(doSleep)
					Thread.sleep(100);
				playbackPosition = audioTrack.getPlaybackHeadPosition();
							
			}
		} 
		catch (InterruptedException e)
		{
			return;
		}
	}
	
	private void runCommands() {
	
			Command cmd;
									
			while(true)
			{
				synchronized (commands)
				{					
					if(!commands.isEmpty())
						cmd = commands.removeFirst();
					else
						break;
				}
				Log.d(TAG, "Received %s", cmd.name());
				switch(cmd) {
				case START:
					_start();
					break;
				case STOP:
					_stop();
					break;
				case PAUSE:
					audioTrack.pause();
					isPaused = true;
					break;
				case PLAY:
					audioTrack.play();
					isPaused = false;
					break;
				case RELEASE:
					audioTrack.release();
					doExit = true;
					break;
				case FLUSH:
					_flush();
					break;
				}
			}
	}
	
	
	// One frame in stereo == 2 Samples == 4 bytes
	
	private void init()
	{
		int set_channels = AudioFormat.CHANNEL_OUT_STEREO;
		
		if (channels == 1)
			set_channels = AudioFormat.CHANNEL_OUT_MONO;
	
		audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 
				FREQ, 
				set_channels,
				AudioFormat.ENCODING_PCM_16BIT, 
				bufSize,  // buffer size
				AudioTrack.MODE_STREAM);
		
		audioTrack.setPlaybackRate(FREQ);
		buffers = new LinkedList<SampleArray>();
		framesWritten = 0;
		framesRead = 0;
	}
	
	private LinkedList<short []> bucket = new LinkedList<short []>();
	private int arraySize = -1;
	
	public short [] getArray(int size)
	{
		synchronized (bucket)
		{
			
			if(size != arraySize)
			{
				arraySize = size;
				bucket.clear();
			}
			if(!bucket.isEmpty())
				return bucket.remove();
		}

		return new short [arraySize];
	}
	
	public void returnArray(short [] data)
	{
		synchronized (bucket)
		{
			bucket.add(data);			
		}
	}

	public boolean update(short [] samples, int len)
	{
		framesWritten += (len/2);
		int i = len-1;
		for(; i>=0; i-=3)
		{
			if(samples[i] > 128 || samples[i] < -128)
			{				
				break;			
			}
		}
		if(i >= 0)
		{
			silence = (len+1-i)/2;
		} 
		else
			silence += (len/2);

		//Log.d(TAG, "Writing %d shorts", len);			
		synchronized (buffers)
		{
			buffers.add(new SampleArray(samples, len));
			bufferTotal += len/2;
		}
				
		return true;

	}
	
	public int getLeft()
	{
		if(stopped || holdData) // Don't queue anything when stopped
			return 0;
		return  FREQ*4*channels - bufferTotal;// buffer.remaining();	
		
	}
	
	public int getSilence()
	{
		
		int startOfSilence = framesWritten - silence;
		if((playbackPosition - startPlaybackHead) >= startOfSilence)
		{
			return (silence * 2 * 1000 / SEC);	
		}
		return 0;
	}
	
	// Get number of seconds played since start
	public int getPlaybackPosition()
	{
		
		if(stopped || holdData)
			return 0;
		
		int pos = playbackPosition - startPlaybackHead;
		int playPos = pos * 10 / (FREQ / 100);
		
		return playPos + playPosOffset;
	}
	
	// Inform player that playback has seeked to a new position. Affects playback position
	public void seekTo(int msec)
	{
		synchronized (buffers) {
			
			framesWritten -= bufferTotal;			
			buffers.clear();
			bufferTotal = 0;			
			silence = 0;
		}
		if(msec > 0)
		{
			int playPos = (playbackPosition  - startPlaybackHead) * 10 / (FREQ / 100);
			playPosOffset = msec - playPos;
			Log.d(TAG, "Offset %d - %d = %d", msec, playPos, playPosOffset);
		}		
		_flush();

	}

	// Stop playback
	public void stop()
	{
		synchronized (commands)
		{
			holdData = true;
			commands.add(Command.STOP);			
		}
	}
	
	private void _stop()
	{
		Log.d(TAG, "Flush & stop");
		
		buffers.clear();
		framesWritten = framesRead = silence = 0;
		bufferTotal = 0;
		
		stopped = true;
		holdData = false;
		audioTrack.pause();
		
		audioTrack.flush();
		try
		{
			Thread.sleep(100);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		audioTrack.stop();
	}


	public void start()
	{
		synchronized (commands)
		{
			commands.add(Command.START);			
		}
	}
	
	private void _start() {
		
		Log.d(TAG, "Order start");
		buffers.clear();
		bufferTotal = 0;
		framesWritten = framesRead = silence = 0;
		holdData = false;
		stopped = false;
		doStart = true;
	}
	
	public void pause() {
		synchronized (commands) {
			commands.add(Command.PAUSE);			
		}
	}

	public void flush() {
		synchronized (commands) {
			commands.add(Command.FLUSH);			
		}
	}

	public void _flush()
	{
		audioTrack.flush();		
	}

	public void exit()
	{
		synchronized (commands)
		{
			commands.add(Command.RELEASE);			
		}
	}

	public void play()
	{
		synchronized (commands)
		{
			commands.add(Command.PLAY);			
		}
	}

	public void setBufferSize(int bs)
	{
	}
	
	private int toMSec(int frames)
	{
		return frames * 10 / (FREQ/100);
	}

	public void mark()
	{
		markPosition = framesWritten;//playbackPosition;
		Log.d(TAG, "Mark %d (play at %d)", toMSec(markPosition),  toMSec(playbackPosition - startPlaybackHead));
	}
	
	public boolean markReached()
	{
		return (playbackPosition - startPlaybackHead) >= markPosition;
	}
	
}
