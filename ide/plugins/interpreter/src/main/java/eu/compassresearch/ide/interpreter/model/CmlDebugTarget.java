package eu.compassresearch.ide.interpreter.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.overture.ide.debug.core.model.DebugEventHelper;

import eu.compassresearch.core.interpreter.api.CmlInterpretationStatus;
import eu.compassresearch.core.interpreter.debug.Breakpoint;
import eu.compassresearch.core.interpreter.debug.Choice;
import eu.compassresearch.core.interpreter.debug.CmlDbgCommandMessage;
import eu.compassresearch.core.interpreter.debug.CmlDbgStatusMessage;
import eu.compassresearch.core.interpreter.debug.CmlDebugCommand;
import eu.compassresearch.core.interpreter.debug.CmlInterpreterStateDTO;
import eu.compassresearch.core.interpreter.debug.CmlProcessDTO;
import eu.compassresearch.core.interpreter.debug.messaging.CmlRequest;
import eu.compassresearch.core.interpreter.debug.messaging.RequestMessage;
import eu.compassresearch.core.interpreter.debug.messaging.ResponseMessage;
import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.interpreter.CmlDebugPlugin;
import eu.compassresearch.ide.interpreter.CmlUtil;
import eu.compassresearch.ide.interpreter.debug.ui.model.CmlLineBreakpoint;
import eu.compassresearch.ide.interpreter.protocol.CmlCommunicationManager;
import eu.compassresearch.ide.interpreter.protocol.CmlThreadManager;
import eu.compassresearch.ide.interpreter.protocol.MessageEventHandler;

public class CmlDebugTarget extends CmlDebugElement implements IDebugTarget
{
	private static final int THREAD_TERMINATION_TIMEOUT = 5000; // 5 seconds
	private ILaunch launch;
	private IProcess process;
	public final ICmlProject project;
	private Map<StyledText, List<StyleRange>> lastSelectedRanges = new HashMap<StyledText, List<StyleRange>>();

	CmlCommunicationManager communicationManager;
	CmlThreadManager threadManager;
	CmlInterpreterStateDTO lastState = null;

	public CmlDebugTarget(ILaunch launch, IProcess process,
			ICmlProject project, int communicationPort) throws CoreException,
			IOException
	{
		this.launch = launch;
		this.process = process;
		this.project = project;

		threadManager = new CmlThreadManager(this);
		communicationManager = new CmlCommunicationManager(this, threadManager, initializeRequestHandlers(), initializeStatusHandlers(), communicationPort);
		communicationManager.connect();

		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	// public void initializeHandlers()
	// {
	// requestHandlers = initializeRequestHandlers();
	// statusHandlers = initializeStatusHandlers();
	//
	// }

	/**
	 * Initialises all the request message handlers
	 * 
	 * @return
	 */
	private Map<String, MessageEventHandler<RequestMessage>> initializeRequestHandlers()
	{
		Map<String, MessageEventHandler<RequestMessage>> handlers = new HashMap<String, MessageEventHandler<RequestMessage>>();

		// Handler for the Choice request
//		handlers.put(CmlRequest.CHOICE.toString(), new MessageEventHandler<RequestMessage>()
//		{
//
//			@Override
//			public boolean handleMessage(RequestMessage message)
//			{
//				// Type listType = new TypeToken<List<String>>(){}.getType();
//
//				final List<Choice> events = message.getContent();
//				new CmlChoiceMediator(CmlDebugTarget.this, communicationManager).setChoiceOptions(events, message);
//				return true;
//			}
//		});

		handlers.put(CmlRequest.SETUP.toString(), new MessageEventHandler<RequestMessage>()
		{
			@Override
			public boolean handleMessage(RequestMessage message)
			{
				if (getBreakpointManager().isEnabled())
				{
					for (IBreakpoint bp : getBreakpoints())
					{
						if (bp instanceof CmlLineBreakpoint)
						{
							CmlLineBreakpoint cmlBp = (CmlLineBreakpoint) bp;
							try
							{
								communicationManager.addBreakpoint(cmlBp.getResourceURI(), cmlBp.getLineNumber(), cmlBp.isEnabled());
							} catch (CoreException e)
							{
								CmlDebugPlugin.logError("Failed to set breakpoint", e);
							}
						} else
						{
							System.err.println("unknown type of breakpoint found: "
									+ bp);
						}
					}
				}

				communicationManager.sendMessage(new ResponseMessage(message.getRequestId(), CmlRequest.SETUP, ""));
				return true;
			}
		});

		return handlers;
	}

	/**
	 * Initializes all the status message handlers
	 * 
	 * @return
	 */
	private Map<String, MessageEventHandler<CmlDbgStatusMessage>> initializeStatusHandlers()
	{
		Map<String, MessageEventHandler<CmlDbgStatusMessage>> handlers = new HashMap<String, MessageEventHandler<CmlDbgStatusMessage>>();

		handlers.put(CmlInterpretationStatus.INITIALIZED.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(CmlDbgStatusMessage message)
			{
				// for (IBreakpoint b : getBreakpoints())
				// {
				// try
				// {
				// if (b.isEnabled())
				// {
				// System.out.println("Adding breakpoint: " + b);
				// // TODO communnicate the setting of the breakpoint to the interpreter
				// }
				// } catch (CoreException e)
				// {
				// CmlDebugPlugin.logError("Failed to set breakpoint", e);
				// }
				// }
				lastState = message.getInterpreterStatus();
				threadManager.started(message.getInterpreterStatus());

				Display.getDefault().syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						CmlUtil.clearAllSelections();
					}
				});
				return true;
			}
		});

		handlers.put(CmlInterpretationStatus.RUNNING.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
				Display.getDefault().syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						CmlUtil.clearSelections(lastSelectedRanges);
					}
				});
				return true;
			}
		});

		handlers.put(CmlInterpretationStatus.WAITING_FOR_ENVIRONMENT.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(final CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
//				Display.getDefault().asyncExec(new Runnable()
//				{
//					
//					@Override
//					public void run()
//					{
//						threadManager.updateThreads(message.getInterpreterStatus(), communicationManager);
//					}
//				});
				Job setupThreads = new Job("setup cml threads")
				{
					
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						threadManager.updateThreads(message.getInterpreterStatus(), communicationManager);
						try
						{
							suspend();
						} catch (DebugException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return Status.OK_STATUS;
					}
				};
				setupThreads.setSystem(true);
				setupThreads.schedule();
				
				return true;
			}
		});

		handlers.put(CmlInterpretationStatus.SUSPENDED.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{

			@Override
			public boolean handleMessage(final CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
				Job setupThreads = new Job("setup cml threads")
				{
					
					@Override
					protected IStatus run(IProgressMonitor monitor)
					{
						threadManager.updateThreads(message.getInterpreterStatus(), communicationManager);
						if (message.getInterpreterStatus().hasActiveBreakpoint())
						{
							Breakpoint bp = message.getInterpreterStatus().getActiveBreakpoint();
							for (CmlProcessDTO pi : message.getInterpreterStatus().getAllProcesses())
								if (pi.getLocation().getStartLine() == bp.getLine())
								{
									CmlUtil.setSelectionFromLocation(pi.getLocation(), lastSelectedRanges);
									break;
								}
						}
						
						return Status.OK_STATUS;
					}
				};
				setupThreads.setSystem(true);
				setupThreads.schedule();

				try
				{
					suspend();
				} catch (DebugException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});

		handlers.put(CmlInterpretationStatus.FAILED.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
				// threadManager.stopping();
				if (lastState.hasErrors())
				{
					if (lastState.getErrors().get(0).getLocation() != null)
					{
						Display.getDefault().syncExec(new Runnable()
						{
							@Override
							public void run()
							{
								Map<StyledText, List<StyleRange>> map = new HashMap<StyledText, List<StyleRange>>();
								CmlUtil.setSelectionFromLocation(lastState.getErrors().get(0).getLocation(), map);
								CmlUtil.showLocation(map.keySet().iterator().next(), lastState.getErrors().get(0).getLocation());
								MessageDialog.openError(null, "Simulation Error", lastState.getErrors().get(0).getErrorMessage());
								CmlUtil.clearSelections(map);
							}
						});
					}
				}
				CmlDebugPlugin.logWarning(message + " : "
						+ message.getInterpreterStatus().getErrors());
				return false;
			}
		});

		handlers.put(CmlInterpretationStatus.FINISHED.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
				// threadManager.stopping();
				return false;
			}
		});

		handlers.put(CmlInterpretationStatus.TERMINATED.toString(), new MessageEventHandler<CmlDbgStatusMessage>()
		{
			@Override
			public boolean handleMessage(CmlDbgStatusMessage message)
			{
				lastState = message.getInterpreterStatus();
				// communicationManager.connectionClosed();
				return false;
			}
		});

		return handlers;
	}

	@Override
	public ILaunch getLaunch()
	{
		return launch;
	}

	@Override
	public boolean canTerminate()
	{
		return(process!=null &&process.canTerminate());
	}

	@Override
	public boolean isTerminated()
	{
		return (process!=null &&process.isTerminated());
	}

	public void terminate() throws DebugException
	{
		terminate(true);
	}

	protected void terminate(boolean waitTermination) throws DebugException
	{
		// fireTargetTerminating();

		communicationManager.terminate();
		if (waitTermination)
		{
			final IProcess p = getProcess();
			final int CHUNK = 500;
			if (!(waitTerminated(threadManager, CHUNK, THREAD_TERMINATION_TIMEOUT) && (p == null || waitTerminated(p, CHUNK, THREAD_TERMINATION_TIMEOUT))))
			{
				// Debugging process is not answering, so terminating it
				if (p != null && p.canTerminate())
				{
					p.terminate();
				}
			}
		}

		DebugEventHelper.fireTerminateEvent(this);
	}

	protected static boolean waitTerminated(ITerminate terminate, int chunk,
			long timeout)
	{
		final long start = System.currentTimeMillis();
		while (!terminate.isTerminated())
		{
			if (System.currentTimeMillis() - start > timeout)
			{
				return false;
			}
			try
			{
				Thread.sleep(chunk);
			} catch (InterruptedException e)
			{
				// interrupted
			}
		}
		return true;
	}

	@Override
	public boolean canResume()
	{
		return isSuspended();
	}

	@Override
	public boolean canSuspend()
	{
		return lastState != null && 
				lastState.getInterpreterState() ==  CmlInterpretationStatus.RUNNING;
	}

	@Override
	public boolean isSuspended()
	{
		return lastState != null && 
				(lastState.getInterpreterState() ==  CmlInterpretationStatus.SUSPENDED ||
						lastState.getInterpreterState() == CmlInterpretationStatus.WAITING_FOR_ENVIRONMENT);
	}

	@Override
	public void resume() throws DebugException
	{
		this.communicationManager.sendCommandMessage(CmlDebugCommand.RESUME);
		fireResumeEvent(0);
	}
	
	public void select(Choice choice)
	{
		this.communicationManager.sendMessage(new CmlDbgCommandMessage(CmlDebugCommand.SET_CHOICE,choice));
		fireResumeEvent(0);
	}

	@Override
	public void suspend() throws DebugException
	{
		fireSuspendEvent(0);
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint)
	{
		if (breakpoint instanceof CmlLineBreakpoint)
		{
			CmlLineBreakpoint cb = (CmlLineBreakpoint) breakpoint;
			try
			{
				communicationManager.addBreakpoint(cb.getResourceURI(), ((CmlLineBreakpoint) breakpoint).getLineNumber(), breakpoint.isEnabled());
			} catch (CoreException e)
			{
				CmlDebugPlugin.logError("Faild to add breakpoint", e);
			}
		}
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta)
	{
		if (breakpoint instanceof CmlLineBreakpoint)
		{
			CmlLineBreakpoint cb = (CmlLineBreakpoint) breakpoint;
			try
			{
				communicationManager.removeBreakpoint(cb.getResourceURI(), ((CmlLineBreakpoint) breakpoint).getLineNumber());
			} catch (CoreException e)
			{
				CmlDebugPlugin.logError("Faild to remove breakpoint", e);
			}
		}
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta)
	{
		if (breakpoint instanceof CmlLineBreakpoint)
		{
			CmlLineBreakpoint cb = (CmlLineBreakpoint) breakpoint;
			try
			{
				communicationManager.updateBreakpoint(cb.getResourceURI(), ((CmlLineBreakpoint) breakpoint).getLineNumber(), breakpoint.isEnabled());
			} catch (CoreException e)
			{
				CmlDebugPlugin.logError("Faild to update breakpoint", e);
			}
		}
	}

	public List<IBreakpoint> getBreakpoints()
	{
		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(getModelIdentifier());

		List<IBreakpoint> targetBreakpoints = new Vector<IBreakpoint>();

		for (int i = 0; i < breakpoints.length; i++)
		{
			try
			{
				final IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint.getMarker().getResource().getProject().getName().equals(project.getName()))
				{
					targetBreakpoints.add(breakpoint);
				}

			} catch (Exception e)
			{
				CmlDebugPlugin.logWarning(NLS.bind("ErrorSetupDeferredBreakpoints", e.getMessage()), e);
				if (CmlDebugPlugin.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
		return targetBreakpoints;
	}

	// Utility methods
	protected static IBreakpointManager getBreakpointManager()
	{
		return DebugPlugin.getDefault().getBreakpointManager();
	}

	@Override
	public boolean canDisconnect()
	{
		return communicationManager.isConnected();
	}

	@Override
	public void disconnect() throws DebugException
	{
		communicationManager.disconnect();
	}

	@Override
	public boolean isDisconnected()
	{
		return !communicationManager.isConnected();
	}

	@Override
	public boolean supportsStorageRetrieval()
	{
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException
	{
		return null;
	}

	@Override
	public IProcess getProcess()
	{
		return process;
	}

	@Override
	public IThread[] getThreads() throws DebugException
	{
		return threadManager.getThreads().toArray(new IThread[threadManager.getThreads().size()]);
	}

	@Override
	public boolean hasThreads() throws DebugException
	{
		return !threadManager.getThreads().isEmpty();
	}

	@Override
	public String getName() throws DebugException
	{
		return "CML Debug Target";
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint)
	{
		return (breakpoint instanceof CmlLineBreakpoint);
	}

	/**
	 * Called when this debug target terminates.
	 */
	public void terminated()
	{
		// terminated = true;
		// suspended = false;
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		fireTerminateEvent();

		// take the process down
		final IProcess p = getProcess();
		// Debugging process is not answering, so terminating it
		if (p != null && p.canTerminate())
		{
			try
			{
				p.terminate();
			} catch (DebugException e)
			{
				CmlDebugPlugin.logError("Failed to take down the interpreter process", e);
			}
		}
	}

	@Override
	public IDebugTarget getDebugTarget()
	{
		return this;
	}
	
	public CmlInterpreterStateDTO getLastState()
	{
		return lastState;
	}

}
