package eu.compassresearch.ide.collaboration.communication.handlers;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import eu.compassresearch.ide.collaboration.Activator;
import eu.compassresearch.ide.collaboration.communication.MessageProcessor;
import eu.compassresearch.ide.collaboration.communication.messages.CollaborationRequest;
import eu.compassresearch.ide.collaboration.communication.messages.CollaborationStatusMessage;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationDataModelManager;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationDataModelRoot;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationGroup;
import eu.compassresearch.ide.collaboration.ui.menu.CollaborationRequestedDialog;

public class CollaborationRequestHandler extends BaseMessageHandler<CollaborationRequest>
{		
	public CollaborationRequestHandler(
			MessageProcessor processor)
	{
		super(CollaborationRequest.class, processor);
	}

	@Override
	public void process(final CollaborationRequest msg)
	{
		Activator.getDefault().setConnections(msg.getReceiverID(), msg.getSenderID());
		
		final String senderName = msg.getSenderID().getName();
		final String collabProjectId = msg.getProjectID();
		
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				try
				{
					CollaborationRequestedDialog collabRequestedDialog = new CollaborationRequestedDialog(senderName, msg.getTitle(), msg.getMessage(), null);
					collabRequestedDialog.create();
					boolean join = collabRequestedDialog.open() == Window.OK; 
					
					if(join) {
						CollaborationDataModelManager modelMgm = Activator.getDefault().getDataModelManager();
						CollaborationDataModelRoot root = modelMgm.getDataModel();
						
						root.addCollaborationProject(collabRequestedDialog.getProject(), msg.getTitle(), msg.getMessage(), collabProjectId);
						
						CollaborationGroup collabGrp = (CollaborationGroup) root.getCollaborationProjects().get(0).getCollaboratorGroup();
						collabGrp.addCollaborator(senderName, true);	
					}
					
					//send reply
					CollaborationStatusMessage statusMsg = new CollaborationStatusMessage(msg.getReceiverID(), msg.getSenderID(), msg.getProjectID(), join);
					messageProcessor.sendMessage(statusMsg.getReceiverID(), statusMsg.serialize());
					
				} catch (ECFException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		});	
	}
}