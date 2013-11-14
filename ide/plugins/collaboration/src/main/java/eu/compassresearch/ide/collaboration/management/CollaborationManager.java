package eu.compassresearch.ide.collaboration.management;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.AbstractShare;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.SerializationException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import eu.compassresearch.ide.collaboration.Activator;
import eu.compassresearch.ide.collaboration.menu.CollaborationRequestedDialog;
import eu.compassresearch.ide.collaboration.messages.CollaborationRequest;
import eu.compassresearch.ide.collaboration.messages.CollaborationStatusMessage;
import eu.compassresearch.ide.collaboration.messages.NewFileMessage;
import eu.compassresearch.ide.collaboration.messages.FileStatusMessage;
import eu.compassresearch.ide.collaboration.messages.FileStatusMessage.NegotiationStatus;
import eu.compassresearch.ide.collaboration.messages.TestMessage;
import eu.compassresearch.ide.collaboration.notifications.Notification;
import eu.compassresearch.ide.collaboration.treeview.model.CollaborationGroup;
import eu.compassresearch.ide.collaboration.treeview.model.Contract;
import eu.compassresearch.ide.collaboration.treeview.model.Contracts;
import eu.compassresearch.ide.collaboration.treeview.model.Share;
import eu.compassresearch.ide.collaboration.treeview.model.TreeRoot;
import eu.compassresearch.ide.collaboration.treeview.model.User;
import eu.compassresearch.ide.collaboration.treeview.model.Version;
import eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView;

public class CollaborationManager extends AbstractShare
{

	private ID ownID;

	public CollaborationManager(IChannelContainerAdapter adapter)
			throws ECFException
	{
		super(adapter);
	}

	@Override
	protected void handleMessage(ID fromContainerID, byte[] data)
	{
		try
		{
			final IModelChangeMessage msg = TestMessage.deserialize(data);
			Assert.isNotNull(msg);

			if (msg instanceof TestMessage)
			{
				TestMessage recvMsg = (TestMessage) msg;
				final String recvString = "Received message: "
						+ recvMsg.getData() + " from "
						+ recvMsg.getFromUsername() + "("
						+ recvMsg.getSenderID() + ")";

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							ErrorDialog.openError(null, "Message", recvString, new Status(IStatus.ERROR, Activator.PLUGIN_ID, recvString, null));

						} finally
						{
						}
					}
				});

			} else if (msg instanceof NewFileMessage)
			{

				final NewFileMessage fileMsg = (NewFileMessage) msg;
				final String filename = fileMsg.getFilename();
				final String fileContents = fileMsg.getContents();
				final String senderName = fileMsg.getSenderID().getName();
				final Date time = new Date();
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							boolean recv = MessageDialog.openQuestion(null, "Receiving new file", "Receiving new file: "
									+ filename
									+ " from: "
									+ senderName
									+ "\n Do you want to receive this file?");

							if (recv)
							{
								final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								final IViewPart view = page.findView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
								final CollaborationView collabview = (CollaborationView) view;

								IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
								IFolder folder = project.getFolder("Collaboration");
								IFile file = folder.getFile(filename);
								// at this point, no resources have been created
								if (!project.exists())
									project.create(null);
								if (!project.isOpen())
									project.open(null);
								if (!folder.exists())
									folder.create(IResource.NONE, true, null);

								if (!file.exists())
								{
									byte[] bytes = fileContents.getBytes();
									InputStream source = new ByteArrayInputStream(bytes);
									file.create(source, IResource.NONE, null);

									TreeRoot root = collabview.getRoot();

									Contracts contracts = (Contracts) root.getContracts().get(0);
									Contract contract = new Contract(filename, fileMsg.getSenderID(), fileMsg.getReceiverID());
									contract.addShare(new Share(senderName));
									contract.addShare(new Share("You"));

									contract.addVersion(new Version(contract.getName()
											+ " Received: " + new Date()));

									contracts.addContract(contract);
								}

								FileStatusMessage statusMsg = new FileStatusMessage(fileMsg.getReceiverID(), fileMsg.getSenderID(), filename, NegotiationStatus.RECEIVED, time);
								sendMessage(statusMsg.getReceiverID(), statusMsg.serialize());

								IEditorPart openEditor = IDE.openEditor(page, file, true);
								IEditorInput editorInput = openEditor.getEditorInput();

								page.showView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
							}

						} catch (CoreException e)
						{
							MessageDialog.openError(null, "Error", e.toString());
							e.printStackTrace();
						} finally
						{
						}
					}
				});

			} else if (msg instanceof FileStatusMessage)
			{
				final FileStatusMessage statusMsg = (FileStatusMessage) msg;

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try
						{
							NegotiationStatus status = statusMsg.getStatus();
							if(status == NegotiationStatus.RECEIVED) {
							MessageDialog.openInformation(null, "File received", statusMsg.getSenderID().getName() + " received " + statusMsg.getFilename());
							
							final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							final IViewPart view = page.findView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
							final CollaborationView collabview = (CollaborationView) view;
	
							TreeRoot root = collabview.getRoot();
	
							String senderName = statusMsg.getSenderID().getName();
							Contracts contracts = (Contracts) root.getContracts().get(0);
							Contract contract = new Contract(statusMsg.getFilename(), statusMsg.getSenderID(), statusMsg.getReceiverID());
							contract.addShare(new Share(senderName));
							contract.addShare(new Share("You"));
	
							contract.addVersion(new Version(contract.getName()
									+ " Received by " + senderName + " on "
									+ statusMsg.getTimestamp()));
	
							contracts.addContract(contract);
							
							page.showView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
							
							} else if (status == NegotiationStatus.ACCEPT || status == NegotiationStatus.REJECT) {
								
								final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								final IViewPart view = page.findView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
								final CollaborationView collabview = (CollaborationView) view;
								
								Contracts cs = (Contracts) collabview.getRoot().getContracts().get(0);
								Contract c = (Contract) cs.getContracts().get(0);
								c.setStatus(status);
							}
							
						} catch (PartInitException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});

			} else if (msg instanceof CollaborationRequest)
			{
				final CollaborationRequest collabRequest = (CollaborationRequest) msg;
				
				final String senderName = collabRequest.getSenderID().getName();
				
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						try
						{
						
							CollaborationRequestedDialog collabRequestedDialog = new CollaborationRequestedDialog(senderName, collabRequest.getTitle(), collabRequest.getMessage(), null);
							collabRequestedDialog.create();
							boolean join = collabRequestedDialog.open() == Window.OK; 
							
							if(join) {
								
								final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
								final IViewPart view = page.findView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
								final CollaborationView collabview = (CollaborationView) view;
								
								TreeRoot root = collabview.getRoot();
								
								CollaborationGroup collabGrp = (CollaborationGroup) root.getCollaboratorGroups().get(0);
								User usr = new User(senderName);
								collabGrp.addCollaborator(usr);
								
								page.showView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
	
	
							}
							
							CollaborationStatusMessage statusMsg = new CollaborationStatusMessage(collabRequest.getReceiverID(), collabRequest.getSenderID(), join);
							sendMessage(statusMsg.getReceiverID(), statusMsg.serialize());
							
						} catch (ECFException | PartInitException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}	
				});
	
			} else if(msg instanceof CollaborationStatusMessage)
			{
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						final CollaborationStatusMessage collabRequest = (CollaborationStatusMessage) msg;
					
						final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						final IViewPart view = page.findView("eu.compassresearch.ide.collaboration.treeview.ui.CollaborationView");
						final CollaborationView collabview = (CollaborationView) view;
						
						TreeRoot root = collabview.getRoot();
						
						CollaborationGroup collabGrp = (CollaborationGroup) root.getCollaboratorGroups().get(0);
						
						String userName = collabRequest.getSenderID().getName();
						User usr = collabGrp.getUser(userName);
						
						if(collabRequest.isJoining()){
							usr.setPostfix("");					
						} else{
							usr.setPostfix("(Declined collaboration)");		
						}
					}
				});
			}
			else
			{
				throw new InvalidObjectException(Notification.Collaboration_ERROR_UNKNOWN_MESSAGE_TYPE
						+ msg.getClass().getName());
			}

		} catch (final Exception e)
		{
			Notification.logError(Notification.Collaboration_ERROR_HANDLE_MSG_EXCEPTION_CAUGHT, e);
		}
	}

	public synchronized void dispose()
	{
		super.dispose();
	}
	
	public synchronized void sendMessage(IUser toUser, byte[] data)
			throws ECFException
	{
		this.sendMessage(toUser.getID(), data);
	}
}