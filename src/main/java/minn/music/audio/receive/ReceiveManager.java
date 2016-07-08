/*
package minn.music.audio.receive;

import net.dv8tion.jda.audio.AudioPacket;
import net.dv8tion.jda.audio.AudioReceiveHandler;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.utils.SimpleLog;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class ReceiveManager
{

	protected AudioReceiveHandler handler;
	public static final SimpleLog LOG = SimpleLog.getLog("ReceiveManager");

	public ReceiveManager()
	{

		try
		{
			handler = new Handler();
		} catch (LineUnavailableException e)
		{
			LOG.log(e);
		}
	}

	public void registerJDA(Guild g)
	{
		AudioReceiveHandler handlerList = g.getAudioManager().getReceiveHandler();
		if(handlerList == null)
			g.getAudioManager().setReceivingHandler(handler);
	}


	protected class Handler implements AudioReceiveHandler
	{
		private SourceDataLine dataLine = AudioSystem.getSourceDataLine(new AudioFormat(4.5f, 8, 16, false, false));

		public Handler() throws LineUnavailableException
		{
		}

		@Override
		public boolean canReceive()
		{
			return dataLine.isActive();
		}

		@Override
		public void handleReceivedAudio(AudioPacket packet)
		{
			dataLine.write(packet.getEncodedAudio(),0 , packet.getEncodedAudio().length);
		}
	}
}
*/
