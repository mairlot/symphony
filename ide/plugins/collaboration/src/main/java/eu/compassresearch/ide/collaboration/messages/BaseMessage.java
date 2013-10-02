package eu.compassresearch.ide.collaboration.messages;

import java.io.*;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.SerializationException;

import eu.compassresearch.ide.collaboration.notifications.Notification;


public class BaseMessage implements IModelChangeMessage, Serializable {

	private static final long serialVersionUID = -70529594981572196L;

	private final ID senderID;
	
	public BaseMessage(){
		senderID = null;
	}
	
	public BaseMessage(ID sender)
	{
		senderID = sender;
	}
	
	public static IModelChangeMessage deserialize(byte[] bytes) throws SerializationException {
		try {
			final ByteArrayInputStream bins = new ByteArrayInputStream(bytes);
			final ObjectInputStream oins = new ObjectInputStream(bins);
			return (IModelChangeMessage) oins.readObject();
		} catch (final Exception e) {
			throw new SerializationException(Notification.Collaboration_ERROR_SERIALIZATION_FAILED, e);
		}
	}

	public byte[] serialize() throws SerializationException {
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			return bos.toByteArray();
		} catch (final Exception e) {
			throw new SerializationException(Notification.Collaboration_ERROR_DESERIALIZATION_FAILED, e);
		}
	}

	public ID getSenderID()
	{
		return senderID;
	}
	
	public Object getAdapter(Class adapter) {
		return null;
	}

}