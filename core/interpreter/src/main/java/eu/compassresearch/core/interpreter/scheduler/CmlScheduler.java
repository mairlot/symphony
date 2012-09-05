package eu.compassresearch.core.interpreter.scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.interpreter.runtime.Context;
import eu.compassresearch.core.interpreter.util.Pair;

public class CmlScheduler {

	ConcurrentLinkedQueue<CMLProcess> processes;
	ConcurrentLinkedQueue<CMLProcess> executedInCurrentStep;
	ConcurrentLinkedQueue<CMLProcess> addedProcesses;
	private List<ACommunicationAction> trace = new LinkedList<ACommunicationAction>();
	//private final int numberOfThreads = 100;
	//private ExecutorService threadPool;
	//private List<Future> futures;
	private Object sync = new Object();
	
	public CmlScheduler()
	{
		processes = new ConcurrentLinkedQueue<CMLProcess>();
		addedProcesses = new ConcurrentLinkedQueue<CMLProcess>();
		executedInCurrentStep = new ConcurrentLinkedQueue<CMLProcess>();
		//threadPool = Executors.newFixedThreadPool(numberOfThreads);
	}
	
	public CMLProcess addProcess(CMLProcess process)
	{
		synchronized (sync) {
		
			//ProcessThread processThread = new ProcessThread(context, process); 
			//context.setProcessThread(processThread);
			addedProcesses.add(process);
			
			return process;
		}
	}
		
	public Map<CMLProcess,List<ACommunicationAction>> step()
	{
		HashMap< CMLProcess,List<ACommunicationAction> > allEvents = 
					new HashMap<CMLProcess, List<ACommunicationAction>>();
		
		while(!processes.isEmpty())
		{
			CMLProcess pt = processes.poll();
			
			if(!pt.isSkip()){
				
				List<ACommunicationAction> cActions = pt.WaitForEventOffer();
				
				if(!cActions.isEmpty())
				{
					allEvents.put(pt, cActions);
					//ACommunicationAction a = selectEvent(cActions);
					//pt.eventOccured(a);
					executedInCurrentStep.add(pt);
				}
			}
		}
						
		while(!executedInCurrentStep.isEmpty())
		{
			CMLProcess pt = executedInCurrentStep.poll();
													
			processes.add(pt);
		}
		
		return allEvents;
	}
	
	public void handleAddedProcesses()
	{
		while(!addedProcesses.isEmpty())
		{
			CMLProcess pt = addedProcesses.poll();
			processes.add(pt);
			pt.start();
		}
	}
	
	public void printTrace()
	{
		for(ACommunicationAction a : trace)
		{
			System.out.print("<" + a.getIdentifier() + ">");
		}
		System.out.println();
	}
	
	public void printEvents(List<ACommunicationAction> offeredActions)
	{
		for(ACommunicationAction a : offeredActions)
		{
			System.out.print("<" + a.getIdentifier() + ">");
		}
		System.out.println();
	}
			
	
	public List<Pair<CMLProcess, ACommunicationAction>> selectEvent(Map<CMLProcess,List<ACommunicationAction>> availableEvents)
	{
		List<Pair<CMLProcess, ACommunicationAction>> res = 
				new LinkedList<Pair<CMLProcess,ACommunicationAction>>();
		
						
		for(Entry<CMLProcess,List<ACommunicationAction>> entry : availableEvents.entrySet())
		{
			//For now, select a random event from the set of possible events
			ACommunicationAction event = entry.getValue().get(0);
			res.add(new Pair<CMLProcess, ACommunicationAction>(entry.getKey(),event));
			trace.add(event);
		}
			
		return res;
	}
	
	public void start()
	{
		handleAddedProcesses();
		
		while(!processes.isEmpty())
		{
			handleAddedProcesses();
			
			synchronized (sync) {

				System.out.println("--------begin step---------");
				
				Map<CMLProcess,List<ACommunicationAction>> allEvents = step();
				
				System.out.println("Offered Events:");
				
				
				for(List<ACommunicationAction> ev : allEvents.values())
					printEvents(ev);
				
				
				for(Pair<CMLProcess, ACommunicationAction> actionToExec :
					selectEvent(allEvents))
				{
					actionToExec.first.eventOccured(actionToExec.second);
				}
											
				
				System.out.println("Current Trace:");
				printTrace();
						
				System.out.println("--------end step---------");
			}			
		}
	}

}
