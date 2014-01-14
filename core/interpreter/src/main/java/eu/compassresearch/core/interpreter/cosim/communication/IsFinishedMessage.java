package eu.compassresearch.core.interpreter.cosim.communication;

import eu.compassresearch.core.interpreter.debug.messaging.Message;
import eu.compassresearch.core.interpreter.debug.messaging.MessageType;

public class IsFinishedMessage extends Message
{

	private String process;

	/**
	 * default for message parsing
	 */
	public IsFinishedMessage()
	{
	}

	public IsFinishedMessage(String process)
	{
		this.process = process;
	}

	@Override
	public MessageType getType()
	{
		return MessageType.RESPONSE;
	}

	@Override
	public String getKey()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "Is finished: " + process;
	}

	public String getProcess()
	{
		return this.process;
	}
	

}
