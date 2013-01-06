package eu.compassresearch.core.interpreter.cml.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.compassresearch.core.interpreter.cml.CmlAlphabet;
import eu.compassresearch.core.interpreter.cml.CmlBehaviourThread;
import eu.compassresearch.core.interpreter.events.ChannelObserver;
import eu.compassresearch.core.interpreter.values.CMLChannelValue;

public class SynchronisationEvent extends ObservableEvent {

	private Set<ObservableEvent> synchronisingEvents; 
	
	public SynchronisationEvent(CmlBehaviourThread eventSource, ObservableEvent first, ObservableEvent second) {
		super(eventSource, first.getChannel());
		synchronisingEvents = new HashSet<ObservableEvent>();
		synchronisingEvents.add(first);
		synchronisingEvents.add(second);
	}
	
	public SynchronisationEvent(CmlBehaviourThread eventSource, Set<ObservableEvent> synchronisingEvents) {
		super(eventSource, extractChannelValue(synchronisingEvents));
		this.synchronisingEvents = synchronisingEvents;
	}
	
	private static CMLChannelValue extractChannelValue(Set<ObservableEvent> synchronisingEvents)
	{
		CMLChannelValue channel = synchronisingEvents.iterator().next().getChannel();
		//TODO Check that the all the channels are equal
//		for(ObservableEvent oe : synchronisingEvents)
//		{
//			if(channel == null || (channel != null && channel))
//			{
//				channel = oe.getChannel();
//			}
//		}
		return channel;
	}
	
	public void addSyncEvent(ObservableEvent event)
	{
		synchronisingEvents.add(event);
	}
	
	public CmlAlphabet getSynchronousEvents()
	{
		return new CmlAlphabet(synchronisingEvents, new HashSet<CmlSpecialEvent>());
	}
	

	@Override
	public int hashCode() {
		return synchronisingEvents.hashCode();
	}

	@Override
	public String toString() {
		return synchronisingEvents.iterator().next().toString();
	}

	@Override
	public boolean equals(Object obj) {
		
		if((obj instanceof SynchronisationEvent))
		{
			SynchronisationEvent other = (SynchronisationEvent)obj;
			return other.getChannel().equals(getChannel()) && 
					synchronisingEvents.equals(other.synchronisingEvents);
		}
		else
			return false;
	}

	@Override
	public void handleChannelEventRegistration(ChannelObserver observer) {
		
		synchronisingEvents.iterator().next().getChannel().onChannelSignal().registerObserver(observer);
	}
	
	@Override
	public void handleChannelEventUnregistration(ChannelObserver observer) {
		synchronisingEvents.iterator().next().getChannel().onChannelSignal().unregisterObserver(observer);
	}

	/**
	 * There is no reference event for a synchronisation event
	 */
	@Override
	public ObservableEvent getReferenceEvent() {
		return synchronisingEvents.iterator().next().getReferenceEvent();
	}

}
