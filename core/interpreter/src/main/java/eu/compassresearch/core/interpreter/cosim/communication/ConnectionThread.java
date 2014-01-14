package eu.compassresearch.core.interpreter.cosim.communication;

/*******************************************************************************
 * Copyright (c) 2009 Fujitsu Services Ltd. Author: Nick Battle This file is part of VDMJ. VDMJ is free software: you
 * can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version. VDMJ is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with VDMJ. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.compassresearch.core.interpreter.api.transitions.CmlTransition;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionSet;
import eu.compassresearch.core.interpreter.cosim.IProcessBehaviourDelegationManager;
import eu.compassresearch.core.interpreter.cosim.MessageManager;
import eu.compassresearch.core.interpreter.cosim.ProcessDelegate;
import eu.compassresearch.core.interpreter.debug.messaging.Message;

public class ConnectionThread extends Thread
{
	private final boolean principal;
	private final Socket socket;

	private String id = "";
	// private long xid = 0;

	private boolean connected;
	private static boolean trace = false;
	private static boolean quiet = false;

	/**
	 * The delegation manager that hold a registry of avaliable delegates for processes
	 */
	IProcessBehaviourDelegationManager delegationManager;

	/**
	 * utility class responsible for sending and receiving messages
	 */
	MessageManager comm;

	/**
	 * synchronization for message parsing
	 */
	private Map<String, SynchronousQueue<CmlTransitionSet>> availableTransitionsMap = new HashMap<String, SynchronousQueue<CmlTransitionSet>>();
	/**
	 * synchronization for message parsing for is finished
	 */
	private Map<String, SynchronousQueue<Boolean>> isFinishedMap = new HashMap<String, SynchronousQueue<Boolean>>();

	public ConnectionThread(ThreadGroup group, Socket conn, boolean principal,
			IProcessBehaviourDelegationManager delegationManager)
			throws IOException
	{
		super(group, null, "DBGp Connection");

		this.socket = conn;
		this.socket.setSoTimeout(0);
		this.principal = principal;
		this.comm = new MessageManager(conn);
		this.delegationManager = delegationManager;
		setDaemon(true);
	}

	public String getIdeId()
	{
		return id;
	}

	public static synchronized boolean setTrace()
	{
		trace = !trace;
		return trace;
	}

	public static synchronized boolean setQuiet()
	{
		quiet = !quiet;
		return quiet;
	}

	@Override
	public void run()
	{
		connected = true;
		try
		{
			if (!principal)
			{
				// runme(); // Send run command to start new thread
			}

			while (connected)
			{
				receive(); // Blocking
			}
		} catch (SocketException e)
		{
			// Caused by die(), and CDMJ death
			e.printStackTrace();
		} catch (IOException e)
		{
			System.out.println("Connection exception: " + e.getMessage());

		} finally
		{
			die();
		}

		if (!principal && !quiet)
		{
			System.out.println("Thread stopped: " + this);
		}
	}

	public synchronized void die()
	{
		try
		{
			connected = false;
			socket.close();
		} catch (IOException e)
		{
			// ?
		}
	}

	private void receive() throws IOException
	{
		Message message = comm.receive();

		if (message instanceof ProvidesImplementationMessage)
		{
			for (String processName : ((ProvidesImplementationMessage) message).getProcesses())
			{
				this.delegationManager.addDelegate(new ProcessDelegate(processName, this));
				availableTransitionsMap.put(processName, new SynchronousQueue<CmlTransitionSet>());
			}
		} else if (message instanceof InspectionReplyMessage)
		{
			InspectionReplyMessage replyMsg = (InspectionReplyMessage) message;
			availableTransitionsMap.get(replyMsg.getProcess()).add(replyMsg.getTransitions());
		} else if (message instanceof IsFinishedReplyMessage)
		{
			IsFinishedReplyMessage replyMsg = (IsFinishedReplyMessage) message;
			isFinishedMap.get(replyMsg.getProcess()).offer(replyMsg.isFinished());
		}
	}

	public CmlTransitionSet inspect(String processName)
			throws JsonGenerationException, JsonMappingException, IOException,
			InterruptedException
	{
		comm.send(new InspectMessage(processName));
		return availableTransitionsMap.get(processName).take();
	}

	public void execute(CmlTransition transition)
			throws JsonGenerationException, JsonMappingException, IOException
	{
		comm.send(new ExecuteMessage(transition));
	}

	public boolean isFinished(String processName)
			throws JsonGenerationException, JsonMappingException, IOException,
			InterruptedException
	{
		if (!isFinishedMap.containsKey(processName))
		{
			isFinishedMap.put(processName, new SynchronousQueue<Boolean>());
		}
		comm.send(new IsFinishedMessage(processName));
		return isFinishedMap.get(processName).take();
	}

	public void disconnect() throws JsonGenerationException,
			JsonMappingException, IOException
	{
		comm.send(new DisconnectMessage());
		connected = false;
	}

}
