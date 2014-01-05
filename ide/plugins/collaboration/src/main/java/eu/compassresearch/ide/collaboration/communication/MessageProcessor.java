package eu.compassresearch.ide.collaboration.communication;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.AbstractShare;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.sync.SerializationException;

import eu.compassresearch.ide.collaboration.communication.messages.BaseMessage;
import eu.compassresearch.ide.collaboration.notifications.Notification;
import eu.compassresearch.ide.collaboration.ui.menu.CollaborationDialogs;


public class MessageProcessor extends AbstractShare
{
	private String project;
	private int versionNrDummy = 1;
	private IFolder projectFolder;

	List<IMessageHandler> msgHandlers;
	
	public MessageProcessor(IChannelContainerAdapter adapter)
			throws ECFException
	{
		super(adapter);
		
		msgHandlers = new ArrayList<IMessageHandler>();
	}

	public void addMessageHandler(IMessageHandler messageHandler)
	{
		msgHandlers.add(messageHandler);
	}
	
	@Override
	protected void handleMessage(ID fromContainerID, byte[] data)
	{
		try
		{
			final BaseMessage msg = BaseMessage.deserialize(data);
			Assert.isNotNull(msg);
			
			for (IMessageHandler msgHndlr : msgHandlers)
			{
				if(msgHndlr.canHandleMessage(msg)){
					msgHndlr.handleMessage(msg);
				}
			}

		} catch (final SerializationException | InvalidObjectException e)
		{
			Notification.logError(Notification.Collaboration_ERROR_HANDLE_MSG_EXCEPTION_CAUGHT, e);
		}
	}

	public synchronized void dispose()
	{
		super.dispose();
	}
	
	@Override
	public synchronized void sendMessage(ID toID, byte[] data)
	{
		try
		{
			super.sendMessage(toID, data);
		} catch (ECFException e)
		{
			CollaborationDialogs.displayErrorDialog("Send error", Notification.ECF_ERROR_COULD_NOT_SEND);
			e.printStackTrace();
		}
	}
	
	public synchronized void sendMessage(IUser toUser, byte[] data)
	{
		sendMessage(toUser.getID(), data);
	}

	public String getProject()
	{
		return project;
	}

	public void setProject(String project)
	{
		this.project = project;
	}
	
	public IFolder getProjectFolder()
	{
		return projectFolder;
	}

	public IFile nextFilename(String filename)
	{
		return projectFolder.getFile(filename.substring(0, filename.indexOf('.')) + "." + ++versionNrDummy + ".cml");
	}
}