package eu.compassresearch.core.interpreter.api.transitions;

import java.util.HashSet;
import java.util.Set;

import eu.compassresearch.core.interpreter.api.behaviour.CmlAlphabet;
import eu.compassresearch.core.interpreter.api.behaviour.CmlBehaviour;

public class CmlTock extends AbstractCmlTransition implements ObservableEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5584770741085270746L;
	private final long limit;
	
	public CmlTock(CmlBehaviour eventSource, long limit) {
		super(eventSource);
		this.limit = limit;
	}
	
	public CmlTock(CmlBehaviour eventSource) {
		this(eventSource,0);
	}
	
	public CmlTock() {
		super(new HashSet<CmlBehaviour>());
		limit = 0;
	}
	
	protected CmlTock(Set<CmlBehaviour> eventSources) {
		super(eventSources);
		limit = 0;
	}

	@Override
	public ObservableEvent synchronizeWith(ObservableEvent syncEvent) {
		
//		if(!isComparable(other))
//			throw new NotComparableException();
		
		Set<CmlBehaviour> sources = new HashSet<CmlBehaviour>();
		sources.addAll(this.getEventSources());
		sources.addAll(syncEvent.getEventSources());
		
		return new CmlTock(sources);
	}

	@Override
	public boolean isComparable(ObservableEvent other) {

		return equals(other);
	}
	
	public long getLimit()
	{
		return limit;
	}
	
	public boolean hasLimit()
	{
		return limit != 0;
	}

	@Override
	public CmlAlphabet getAsAlphabet() {

		Set<CmlTransition> events = new HashSet<CmlTransition>();
		events.add(this);
		return new CmlAlphabet(events);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof CmlTock))
			return false;
		
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return "tock";
	}
	
	@Override
	public int hashCode() {
		return "tock".hashCode();
	}
	
}
