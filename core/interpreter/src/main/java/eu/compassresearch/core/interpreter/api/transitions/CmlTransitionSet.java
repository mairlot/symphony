package eu.compassresearch.core.interpreter.api.transitions;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.overture.interpreter.values.Value;

import eu.compassresearch.core.interpreter.api.values.ChannelNameSetValue;
import eu.compassresearch.core.interpreter.api.values.ChannelNameValue;

/**
 * This represents a CML alphabet containing both silent and observable events
 * @author akm
 *
 */
public class CmlTransitionSet extends Value {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5192258370825756900L;
	
	//This contains the observable events
	private final Set<ObservableTransition> _observableEvents;
	//This contains all the special events like tau
	private final Set<AbstractSilentTransition> silentEvents;
	
	public CmlTransitionSet()
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>();
		this._observableEvents = new LinkedHashSet<ObservableTransition>();
	}
	
	public CmlTransitionSet(ObservableTransition obsEvent)
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>();
		this._observableEvents = new LinkedHashSet<ObservableTransition>();
		this._observableEvents.add(obsEvent);
	}
	
	public CmlTransitionSet(AbstractSilentTransition tauEvent)
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>();
		this._observableEvents = new LinkedHashSet<ObservableTransition>();
		this.silentEvents.add(tauEvent);
	}
	
	public CmlTransitionSet(ObservableTransition obs, AbstractSilentTransition tauEvent)
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>();
		this.silentEvents.add(tauEvent);
		this._observableEvents = new LinkedHashSet<ObservableTransition>();
		this._observableEvents.add(obs);
	}
	
	public CmlTransitionSet(Set<ObservableTransition> comms, Set<AbstractSilentTransition> specialEvents)
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>(specialEvents);
		this._observableEvents = new LinkedHashSet<ObservableTransition>(comms);
	}
	
	public CmlTransitionSet(Set<CmlTransition> events)
	{
		this.silentEvents = new LinkedHashSet<AbstractSilentTransition>();
		this._observableEvents = new LinkedHashSet<ObservableTransition>();
		
		for(CmlTransition e : new LinkedHashSet<CmlTransition>(events))
		{
			if(e instanceof AbstractSilentTransition)
				this.silentEvents.add((AbstractSilentTransition)e);
			else if(e instanceof ObservableTransition){
				_observableEvents.add((ObservableTransition)e);
			}
		}
	}
	
	/**
	 * Returns all the observable events in the alphabet
	 * @return
	 */
	public Set<ObservableTransition> getObservableEvents()
	{
		return new LinkedHashSet<ObservableTransition>(_observableEvents);
	}
	
	public Set<ObservableTransition> getObservableChannelEvents()
	{
		Set<ObservableTransition> observableChannelEvents = new LinkedHashSet<ObservableTransition>();
		for(ObservableTransition obsEvent : _observableEvents)
			if(obsEvent instanceof LabelledTransition)
				observableChannelEvents.add(obsEvent);
		
		return observableChannelEvents;
	}
	
	/**
	 * Returns all the special events in the alphabet
	 * @return
	 */
	public Set<AbstractSilentTransition> getSilentTransitions()
	{
		return new LinkedHashSet<AbstractSilentTransition>(silentEvents);
	}
	
	/**
	 * Returns all the observable and special events in the alphabet as a set.
	 * @return all the observable and special events. 
	 */
	public Set<CmlTransition> getAllEvents()
	{
		Set<CmlTransition> allEvents = new LinkedHashSet<CmlTransition>();
		allEvents.addAll(_observableEvents);
		allEvents.addAll(silentEvents);
		
		return allEvents;
	}
	
	/**
	 * Calculate  the union of this alphabet and the given event
	 * @param event 
	 * @return The union of this alphabet and the given CmlEvent
	 */
	public CmlTransitionSet union(CmlTransitionSet other)
	{
		Set<CmlTransition> resultSet = this.getAllEvents();
		resultSet.addAll(other.getAllEvents());
		
		return new CmlTransitionSet(resultSet);
	}
	
	/**
	 * Calculates the union of this alphabet and the given event.
	 * @param event 
	 * @return The union of this alphabet and the given CmlEvent
	 */
	public CmlTransitionSet union(CmlTransition event)
	{
		Set<CmlTransition> resultSet = this.getAllEvents();
		resultSet.add(event);
		
		return new CmlTransitionSet(resultSet);
	}
	
	/**
	 * Calculates the intersection with imprecision between this and other. Meaning that
	 * two events might intersect even though their value is not equal E.g.
	 * Assume that we have the following two alphabets A = {a.3,b} and B = {a.?}
	 * then the result after invoking this function would be
	 *  A.intersectImprecise(B) == {a.3,a.?}
	 *  As a.3 and a.? intersects even though a.? can be any value of the type of 'a'. 
	 * @param other
	 * @return
	 */
	public CmlTransitionSet intersect(CmlTransitionSet other)
	{
		Set<CmlTransition> resultSet = new LinkedHashSet<CmlTransition>();
		
		for(ObservableTransition thisEvent : _observableEvents)
		{
			for(ObservableTransition otherEvent : other._observableEvents)
			{
				if(thisEvent.isComparable(otherEvent) && thisEvent.isSourcesSubset(otherEvent)){
					resultSet.add(thisEvent);
				}
			}
		}
		
		return new CmlTransitionSet(resultSet);
	}
	
	public CmlTransitionSet intersect(ObservableTransition other)
	{
		Set<CmlTransition> resultSet = new LinkedHashSet<CmlTransition>();
		
		for(ObservableTransition thisEvent : _observableEvents)
		{
			if(thisEvent.isComparable(other)  && thisEvent.isSourcesSubset(other)){
				resultSet.add(thisEvent);
			}
		}
		
		return new CmlTransitionSet(resultSet);
	}
	
	public CmlTransitionSet retainByChannelName(ChannelNameValue channelNameValue)
	{
		Set<CmlTransition> resultSet = new LinkedHashSet<CmlTransition>();

		for(ObservableTransition obsTransition : _observableEvents)
		{
			if(!(obsTransition instanceof LabelledTransition))
				continue;

			LabelledTransition obsChannelEvent = (LabelledTransition)obsTransition;   
			if(obsChannelEvent.getChannelName().isComparable(channelNameValue) && 
					channelNameValue.isGTEQPrecise(obsChannelEvent.getChannelName())){
				resultSet.add(obsTransition);
			}
		}

		return new CmlTransitionSet(resultSet);
	}
	
	public CmlTransitionSet retainByChannelNameSet(ChannelNameSetValue channelNameSetValue)
	{
		Set<CmlTransition> resultSet = new LinkedHashSet<CmlTransition>();

		for(ObservableTransition obsTransition : _observableEvents)
		{
			if(!(obsTransition instanceof LabelledTransition))
				continue;

			LabelledTransition obsChannelEvent = (LabelledTransition)obsTransition;

			for(ChannelNameValue channelNameValue : channelNameSetValue)
			{

				if(obsChannelEvent.getChannelName().isComparable(channelNameValue) && 
						channelNameValue.isGTEQPrecise(obsChannelEvent.getChannelName())){
					resultSet.add(obsTransition);
				}
			}
		}

		return new CmlTransitionSet(resultSet);
	}
	
	public CmlTransitionSet removeByChannelName(ChannelNameValue channelNameValue)
	{
		Set<ObservableTransition> resultSet = new LinkedHashSet<ObservableTransition>();
		
		for(ObservableTransition obsTransition : _observableEvents)
		{
			if(!(obsTransition instanceof LabelledTransition))
				continue;
			
			LabelledTransition obsChannelEvent = (LabelledTransition)obsTransition;   
			if(!(obsChannelEvent.getChannelName().isComparable(channelNameValue) && 
					channelNameValue.isGTEQPrecise(obsChannelEvent.getChannelName()))){
				resultSet.add(obsTransition);
			}
		}
		
		return new CmlTransitionSet(resultSet,silentEvents);
	}
	
	public CmlTransitionSet removeByChannelNameSet(ChannelNameSetValue channelNameSetValue)
	{
		Set<ObservableTransition> resultSet = new LinkedHashSet<ObservableTransition>();
		
		for(ObservableTransition obsTransition : _observableEvents)
		{
			if(!(obsTransition instanceof LabelledTransition))
				continue;
			
			boolean retaintIt = true; 
			
			for(ChannelNameValue channelNameValue : channelNameSetValue)
			{
				LabelledTransition obsChannelEvent = (LabelledTransition)obsTransition;   
				if(obsChannelEvent.getChannelName().isComparable(channelNameValue) && 
						channelNameValue.isGTEQPrecise(obsChannelEvent.getChannelName()))
				{
					retaintIt = false;
					break;
				}
					
			}
			
			if(retaintIt)
				resultSet.add(obsTransition);
		}
		
		return new CmlTransitionSet(resultSet,silentEvents);
	}
	
	public TimedTransition getTockEvent()
	{
		
		TimedTransition tock = null;
				
		for(ObservableTransition obs : _observableEvents)
			if(obs instanceof TimedTransition)
			{
				tock = (TimedTransition)obs;
				break;
			}
		
		return tock;
	}

	/**
	 * Subtract other from this
	 * @param other
	 * @return An alphabet containing all the events that are this
	 */
	public CmlTransitionSet subtract(CmlTransitionSet other)
	{
		Set<ObservableTransition> newReferenceEvents = new LinkedHashSet<ObservableTransition>();
		newReferenceEvents.addAll(_observableEvents);
		newReferenceEvents.removeAll(other.getObservableEvents());
		
		return new CmlTransitionSet(newReferenceEvents,silentEvents);
	}
	
	public CmlTransitionSet subtract(ObservableTransition other)
	{
		Set<ObservableTransition> newReferenceEvents = new LinkedHashSet<ObservableTransition>();
		newReferenceEvents.addAll(_observableEvents);
		newReferenceEvents.remove(other);
		
		return new CmlTransitionSet(newReferenceEvents,silentEvents);
	}
	
	/**
	 * Subtract other from this
	 * @param other
	 * @return An alphabet containing all the events that are this
	 */
	public CmlTransitionSet subtractImprecise(CmlTransitionSet other)
	{
		Set<ObservableTransition> newReferenceEvents = new LinkedHashSet<ObservableTransition>();
		newReferenceEvents.addAll(_observableEvents);
		newReferenceEvents.removeAll(this.intersect(other).getObservableEvents());
		
		return new CmlTransitionSet(newReferenceEvents,silentEvents);
	}
	
	/**
	 * This determines whether the alphabet contains an observable event.
	 * @return true if the observable event is contained else false
	 */
	public boolean contains(CmlTransition event)
	{
		if(event instanceof ObservableTransition && !intersect((ObservableTransition)event).isEmpty())
			return true;
		else if(event instanceof AbstractSilentTransition)
		{
			return containsSilentTransition((AbstractSilentTransition)event);
		}
		else 
			return _observableEvents.contains(event); 
	}
	
	private boolean containsSilentTransition(AbstractSilentTransition transition)
	{
		for(CmlTransition thisTransition : getAllEvents())
		{
			if(thisTransition.isSourcesSubset(transition))
				return true;
		}
		
		return false;
	}
	
	public boolean isEmpty(){
		return _observableEvents.isEmpty() && silentEvents.isEmpty();
	}
	
	@Override
	public String toString() {
		return getAllEvents().toString();
	}

	@Override
	public boolean equals(Object other) {

		if(!(other instanceof CmlTransitionSet))
			return false;
			
		return getAllEvents().equals(((CmlTransitionSet)other).getAllEvents());
	}

	@Override
	public int hashCode() {
		return getAllEvents().hashCode();
	}

	@Override
	public String kind() {
		return "CmlAlphabetValue";
	}

	/**
	 * This expands all the expandable events in the alphabet.
	 * E.g. if we have
	 * 
	 * types
	 * 	switch = <ON> | <OFF> 
	 * 
	 * channels
	 * 	a : switch
	 * 
	 * process Test = begin @ a?x -> Skip end
	 * 
	 * then the immediate alphabet would be {a.AnyValue}
	 * when expanded this will be {a.<ON> , a.<OFF>}
	 * 
	 * @return The same alphabet but with all the expandable events expanded
	 */
	public CmlTransitionSet expandAlphabet()
	{
		Set<CmlTransition> eventSet = new HashSet<CmlTransition>();
		
		for(CmlTransition ev : getAllEvents())
			if(ev instanceof LabelledTransition)
				eventSet.addAll(((LabelledTransition)ev).expand());
			else
				eventSet.add(ev);
		
		return new CmlTransitionSet(eventSet);
		
	}
	
	@Override
	public Object clone() {

		return new CmlTransitionSet(new LinkedHashSet<ObservableTransition>(_observableEvents), 
				new LinkedHashSet<AbstractSilentTransition>(silentEvents));
	}
}
