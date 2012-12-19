package eu.compassresearch.core.interpreter.runtime;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.LexNameToken;
import org.overture.interpreter.runtime.Context;

import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.AExternalChoiceAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AInterleavingParallelAction;
import eu.compassresearch.ast.actions.AReferenceAction;
import eu.compassresearch.ast.actions.ASequentialCompositionAction;
import eu.compassresearch.ast.actions.ASingleGeneralAssignmentStatementAction;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.actions.SParallelAction;
import eu.compassresearch.core.interpreter.api.InterpretationErrorMessages;
import eu.compassresearch.core.interpreter.api.InterpreterRuntimeException;
import eu.compassresearch.core.interpreter.cml.CmlAlphabet;
import eu.compassresearch.core.interpreter.cml.CmlBehaviourSignal;
import eu.compassresearch.core.interpreter.cml.CmlProcess;
import eu.compassresearch.core.interpreter.cml.CmlProcessState;
import eu.compassresearch.core.interpreter.cml.CmlSupervisorEnvironment;
import eu.compassresearch.core.interpreter.cml.events.ObservableEvent;
import eu.compassresearch.core.interpreter.cml.events.SynchronisationEvent;
import eu.compassresearch.core.interpreter.eval.AlphabetInspectionVisitor;
import eu.compassresearch.core.interpreter.eval.CmlOpsToString;
import eu.compassresearch.core.interpreter.events.CmlProcessStateEvent;
import eu.compassresearch.core.interpreter.events.CmlProcessStateObserver;
import eu.compassresearch.core.interpreter.events.CmlProcessTraceObserver;
import eu.compassresearch.core.interpreter.events.TraceEvent;
import eu.compassresearch.core.interpreter.util.CmlActionAssistant;
import eu.compassresearch.core.interpreter.util.CmlProcessUtil;
import eu.compassresearch.core.interpreter.util.Pair;

/**
 *  This class represents a running CML Action. It represents a specific node as specified in D23.2 section 7.4.2,
 *  where a node is specified as a tuple (w,s,a) where w is the set of variables, s is the state values and a is the 
 *  current action.
 *  w and s are stored in the current Context object and a is represented by storing the next action AST node to be executed.
 * 
 * 	The possible transitions are handled in the visitor case methods.
 * 
 *  Therefore this Class should be fully consistent with the operational semantics described in D23.2 chapter 7.
 * 
 * @author akm
 *
 */
public class CmlActionInstance extends AbstractInstance<PAction> implements CmlProcessStateObserver , CmlProcessTraceObserver{

	private LexNameToken name;
	private AlphabetInspectionVisitor alphabetInspectionVisitor = new AlphabetInspectionVisitor(this); 
	
	public CmlActionInstance(PAction action,Context context, LexNameToken name)
	{
		super(null);
		this.name = name;
		pushNext(action, context);
	}
	
	public CmlActionInstance(PAction action,Context context, LexNameToken name, CmlProcess parent)
	{
		super(parent);
		this.name = name;
		pushNext(action, context);
	}
	
	@Override
	public void start(CmlSupervisorEnvironment env) {
		
		this.env = env; 
		
		if(parent() != null)
			supervisor().addPupil(this);
		
		setState(CmlProcessState.RUNNABLE);
	}

	@Override
	public CmlAlphabet inspect()
	{
		try
		{
			if(hasNext())
			{
				Pair<PAction,Context> next = nextState();
				return next.first.apply(alphabetInspectionVisitor,next.second);
			}
			//if the process is done we return the empty alphabet
			else
			{
				return new CmlAlphabet();
			}
		}
		catch(AnalysisException ex)
		{
			CmlRuntime.logger.throwing(this.toString(),"inspect()", ex);
			throw new InterpreterRuntimeException(InterpretationErrorMessages.FATAL_ERROR.customizeMessage(),ex);
		}
	}

	@Override
	public LexNameToken name() {
		return this.name;
	}
	
	@Override
	public String nextStepToString() {
		
		if(hasNext())
		{
			if(hasChildren())
			{
				CmlProcess leftChild = children().get(0);
				CmlProcess rightChild = children().get(1);
				
				return "(" + leftChild.nextStepToString() + ")" + CmlOpsToString.toString(nextState().first) + "(" + rightChild.nextStepToString()+")";
			}
			else{
				return nextState().first.toString();
			}
		}
		else
			return "Finished";
	}
	
	@Override
	public String toString() {
	
		return name.toString();
	}
	
	/**
	 * Process state methods 
	 */
	@Override
	public CmlProcessState getState() {
		return state;
	}

	@Override
	protected void setState(CmlProcessState state) {
		
		if(getState() != state)
		{
			CmlProcessStateEvent ev = new CmlProcessStateEvent(this, this.state, state);
			this.state = state;
			notifyOnStateChange(ev);
		}
	}
	
	/**
	 * CmlProcessStateObserver interface 
	 */
	
	@Override
	public void onStateChange(CmlProcessStateEvent stateEvent) {

		switch(stateEvent.getTo())
		{
		case RUNNING:
			setState(CmlProcessState.WAIT_CHILD);
			break;
		case WAIT_EVENT:
			//if at least one child are waiting for an event this process must invoke either Parallel Non-sync or sync
			if(CmlProcessUtil.isAtLeastOneChildWaitingForEvent(this))
				setState(CmlProcessState.RUNNABLE);
			break;
		case FINISHED:
			stateEvent.getSource().onStateChanged().unregisterObserver(this);
			
			//if all the children are finished this process can continue and evolve into skip
			if(CmlProcessUtil.isAllChildrenFinished(this))
				setState(CmlProcessState.RUNNABLE);
			
			break;
		default:
			break;
		}
	}

	/**
	 * CmlProcessTraceObserver interface 
	 */
	
	/**
	 * This will provide the traces from all the child actions
	 */
	@Override
	public void onTraceChange(TraceEvent traceEvent) {
		
		this.trace.addEvent(traceEvent.getEvent());
		notifyOnTraceChange(TraceEvent.createRedirectedEvent(this, traceEvent));
	}
	
	/**
	 * Transition methods
	 */

	/**
	 * Synchronisation and Communication D23.2 7.5.2
	 * 
	 * This transition can either be
	 * Simple prefix   	: a -> A
	 * Synchronisation 	: a.1 -> A
	 * Output			: a!2 -> A
	 * Input			: a?x -> A
	 * As defined in 7.5.2 in D23.2
	 */
	@Override
	public CmlBehaviourSignal caseACommunicationAction(
			ACommunicationAction node, Context question)
			throws AnalysisException {
		
		CmlBehaviourSignal result = null;
		
		//TODO: sync/output/input is still missing
		//At this point the supervisor has already given go to the event, 
		//so we can execute it immediately. We just have figure out which kind of event it is
		if(CmlActionAssistant.isPrefixEvent(node))
			result = casePrefixEvent(node, question);
	
		//supervisor().clearSelectedCommunication();
		
		return result;
	}
	
	/**
	 * Helper methods for Synchronisation and Communication transition rules
	 */
	
	
	private CmlBehaviourSignal casePrefixEvent(ACommunicationAction node, Context question) 
			throws AnalysisException 
	{
		pushNext(node.getAction(), question); 
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	
	/**
	 * External Choice D23.2 7.5.4
	 * 
	 *  There four transition rules for external choice:
	 *  
	 *  * External Choice Begin
	 *  
	 *  * External Choice Silent
	 *  
	 *  * External Choice SKIP
	 *  
	 *  * External Choice End
	 *  
	 */
	@Override
	public CmlBehaviourSignal caseAExternalChoiceAction(
			AExternalChoiceAction node, Context question)
			throws AnalysisException {
		
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseExternalChoiceBegin(node,question);
			//We push the current state, since this process will control the child processes created by it
			pushNext(node, question);
		}
		//If this is true, the Skip rule is instantiated. This means that the entire choice evolves into Skip
		//with the state from the skip. After this all the children processes are terminated
		else if(CmlProcessUtil.existsAFinishedChild(this))
		{
			//find the finished child
			CmlProcess skipChild = findFinishedChild();
			
			//get the state replace the current state
			pushNext(new ASkipAction(), skipChild.getExecutionState().second);
			
			//mmmmuhuhuhahaha kill all the children
			killAndRemoveAllTheEvidenceOfTheChildren();
			
			result = CmlBehaviourSignal.EXEC_SUCCESS;
		}
		//if this is true, then we can resolve the choice to the event
		//of one of the children that are waiting for events
		else if(CmlProcessUtil.isAtLeastOneChildWaitingForEvent(this))
		{
			CmlProcess theChoosenOne = findTheChoosenChild(supervisor().selectedCommunication());
			
			theChoosenOne.onTraceChanged().unregisterObserver(this);
			theChoosenOne.execute(supervisor()); 
			theChoosenOne.onTraceChanged().registerObserver(this);
			
			//get the state replace the current state
			pushNext((PAction)theChoosenOne.getExecutionState().first, 
					theChoosenOne.getExecutionState().second);
			
			setState(CmlProcessState.RUNNING);
			
			//mmmmuhuhuhahaha kill all the children
			killAndRemoveAllTheEvidenceOfTheChildren();
			result = CmlBehaviourSignal.EXEC_SUCCESS;
		}
		else
			result = CmlBehaviourSignal.FATAL_ERROR;
		
		return result;
	}
	
	/**
	 * External Choice helper methods
	 */
	
	private CmlProcess findFinishedChild()
	{
		for(CmlProcess child : children())
		{
			if(child.finished())
				return child;
		}
		
		return null;
	}
	
	private CmlProcess findTheChoosenChild(ObservableEvent event)
	{
		List<CmlProcess> waitingChildren = new LinkedList<CmlProcess>();
		
		for(CmlProcess child : children())
		{
			if(child.waiting() && child.inspect().containsCommunication(event))
				return child;
		}
		
		return null;
	}
	
	private void killAndRemoveAllTheEvidenceOfTheChildren()
	{
		removeTheChildren();
	}
	
	
	private CmlBehaviourSignal caseExternalChoiceBegin(AExternalChoiceAction node,Context question)
	{
		PAction left = node.getLeft();
		PAction right = node.getRight();
		
		//TODO: create a local copy of the question state for each of the actions
		CmlActionInstance leftInstance = 
				new CmlActionInstance(left, question, 
						new LexNameToken(name.module,name.getIdentifier().getName() + "[]" ,left.getLocation()),this);
		
		CmlActionInstance rightInstance = 
				new CmlActionInstance(right, question, 
						new LexNameToken(name.module,"[]" + name.getIdentifier().getName(),right.getLocation()),this);
		
		//add the children FIXME: this should not be done directly on the children() method
		children().add(leftInstance);
		children().add(rightInstance);
		
		//Register for state change and trace change events
		leftInstance.onStateChanged().registerObserver(this);
		leftInstance.onTraceChanged().registerObserver(this);
		rightInstance.onStateChanged().registerObserver(this);
		rightInstance.onTraceChanged().registerObserver(this);
		
		//Add them to the superviser to get executed as a seperate process
		rightInstance.start(supervisor());
		leftInstance.start(supervisor());
		
		//Now let this process wait for the children to get into a waitForEvent state
		setState(CmlProcessState.WAIT_CHILD);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	
	/**
	 * This implements the 7.5.10 Action Reference transition rule in D23.2. 
	 */
	@Override
	public CmlBehaviourSignal caseAReferenceAction(AReferenceAction node,
			Context question) throws AnalysisException {
		//FIXME: the scoping is not correct, this should be done as described in the transition rule
		
		//FIXME: Consider: Instead of this might create a child process, and behave as this child until it terminates
		//CMLActionInstance refchild = new CMLActionInstance(node.getActionDefinition().getAction(), question, node.getName()); 
		
		pushNext(node.getActionDefinition().getAction(), question); 
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * This implements the 7.5.6 Sequential Composition transition rules in D23.2.
	 */
	@Override
	public CmlBehaviourSignal caseASequentialCompositionAction(
			ASequentialCompositionAction node, Context question)
			throws AnalysisException {

		//First push right and then left, so that left get executed first
		pushNext(node.getRight(), question);
		pushNext(node.getLeft(), question);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}

	/**
	 * Parallel composition D23.2 7.5.7
	 *  
	 */
	
	/**
	 * Generalised Parallelism
	 * A [| cs |] B (no state) 
	 * 
	 * or
	 * 
	 * A [| ns1 | cs | ns2 |] B 
	 */
	@Override
	public CmlBehaviourSignal caseAGeneralisedParallelismParallelAction(
			AGeneralisedParallelismParallelAction node, Context question)
			throws AnalysisException {
	
		//TODO: This only implements the "A [| cs |] B (no state)" and not "A [| ns1 | cs | ns2 |] B"
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseParallelBegin(node,question);
			//We push the current state, since this process will control the child processes created by it
			pushNext(node, question);
		}
		//At least one child is not finished and waiting for event, this will either invoke the Parallel Non-sync or Sync rule
		else if(CmlProcessUtil.isAtLeastOneChildWaitingForEvent(this))
		{
			//convert the channelset of the current node to a alphabet
			CmlAlphabet cs = CmlProcessUtil.convertChansetExpToAlphabet(this,
					node.getChanSetExpression(),question);		
			
			CmlProcess leftChild = children().get(0);
			CmlAlphabet leftChildAlpha = leftChild.inspect(); 
			CmlProcess rightChild = children().get(1);
			CmlAlphabet rightChildAlpha = rightChild.inspect();
									
			ObservableEvent selectedEvent = supervisor().selectedCommunication(); 
			
			if(selectedEvent instanceof SynchronisationEvent)
			{
				CmlAlphabet syncEventsAlpha = ((SynchronisationEvent)selectedEvent).getSynchronousEvents(); 
				CmlAlphabet leftOption = syncEventsAlpha.intersect(leftChildAlpha);
				CmlAlphabet rightOption = syncEventsAlpha.intersect(rightChildAlpha);
				
				if(!leftOption.isEmpty() &&
						!rightOption.isEmpty() )
				{
					leftChild.onTraceChanged().unregisterObserver(this);
					rightChild.onTraceChanged().unregisterObserver(this);
					supervisor().setSelectedCommunication(leftOption.getObservableEvents().iterator().next());
					leftChild.execute(supervisor());
					supervisor().setSelectedCommunication(rightOption.getObservableEvents().iterator().next());
					rightChild.execute(supervisor());

					leftChild.onTraceChanged().registerObserver(this);
					rightChild.onTraceChanged().registerObserver(this);
				}
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else if(leftChildAlpha.containsCommunication(selectedEvent) )
			{
				leftChild.onTraceChanged().unregisterObserver(this);				
				leftChild.execute(supervisor());
				leftChild.onTraceChanged().registerObserver(this);
				
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else if(rightChildAlpha.containsCommunication(selectedEvent) )
			{
				rightChild.onTraceChanged().unregisterObserver(this);
				rightChild.execute(supervisor());
				rightChild.onTraceChanged().registerObserver(this);
				
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else
			{
				result = CmlBehaviourSignal.FATAL_ERROR;
			}
			
			//We push the current state, 
			pushNext(node, question);
		}
		//The process has children and they have all evolved into Skip so now the parallel end rule will be invoked 
		else if (CmlProcessUtil.isAllChildrenFinished(this))
		{
			result = caseParallelEnd(question); 
		}
		
		return result;
	}
	
	/**
	 * Interleaving
	 * A ||| B (no state)
	 * 
	 * or 
	 * 
	 * A [|| ns1 | ns2 ||] B
	 * 
	 * This has three parts:
	 * 
	 * Parallel Begin:
	 * 	At this step the interleaving action are not yet created. So this will be a silent (tau) transition
	 * 	where the left and right actions will be created and started.
	 * 
	 * Parallel Non-sync:
	 * 	At this step the actions are each executed separately. Since no sync shall stake place this Action just wait
	 * 	for the child actions to be in the FINISHED state. 
	 * 
	 * Parallel End:
	 *  At this step both child actions are in the FINISHED state and they will be removed from the running process network
	 *  and this will make a silent transition into Skip. 
	 */
	@Override
	public CmlBehaviourSignal caseAInterleavingParallelAction(
			AInterleavingParallelAction node, Context question)
			throws AnalysisException {

		//TODO: This only implements the "A ||| B (no state)" and not "A [|| ns1 | ns2 ||] B"
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseParallelBegin(node,question);
			//We push the current state, since this process will control the child processes created by it
			pushNext(node, question);

		}
		//At least one child is not finished and waiting for event, this will invoke the Parallel Non-sync 
		else if(CmlProcessUtil.isAtLeastOneChildWaitingForEvent(this))
		{
			CmlProcess leftChild = children().get(0);
			CmlAlphabet leftChildAlpha = leftChild.inspect(); 
			CmlProcess rightChild = children().get(1);
			CmlAlphabet rightChildAlpha = rightChild.inspect();
			
			if(leftChildAlpha.containsCommunication(supervisor().selectedCommunication()) )
			{
				leftChild.onTraceChanged().unregisterObserver(this);				
				leftChild.execute(supervisor());
				leftChild.onTraceChanged().registerObserver(this);
				
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else if(rightChildAlpha.containsCommunication(supervisor().selectedCommunication()) )
			{
				rightChild.onTraceChanged().unregisterObserver(this);
				rightChild.execute(supervisor());
				rightChild.onTraceChanged().registerObserver(this);
				
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else
			{
				result = CmlBehaviourSignal.FATAL_ERROR;
			}
			
			//We push the current state, 
			pushNext(node, question);
			
		}
		//the process has children and must now handle either termination or event sync
		else if (CmlProcessUtil.isAllChildrenFinished(this))
		{
			result = caseParallelEnd(question); 
		}
		//else if ()
		
		return result;
	}
	
	/**
	 * Parallel composition Helper methods
	 */
	
	/**
	 * This method introduces a local state for each parallel action which is the source state component
	 * restricted by the nameset expressions
	 * @param question
	 * @return
	 */
	private CmlBehaviourSignal caseParallelBegin(SParallelAction node, Context question)
	{
		PAction left = node.getLeftAction();
		PAction right = node.getRightAction();
		
		//TODO: create a local copy of the question state for each of the actions
		CmlActionInstance leftInstance = 
				new CmlActionInstance(left, question, 
						new LexNameToken(name.module,name.getIdentifier().getName() + "|||" ,left.getLocation()),this);
		
		CmlActionInstance rightInstance = 
				new CmlActionInstance(right, question, 
						new LexNameToken(name.module,"|||" + name.getIdentifier().getName(),right.getLocation()),this);
		
		//add the children FIXME: this should not be done directly on the children() method
		children().add(leftInstance);
		children().add(rightInstance);
		
		//Register for state change and trace change events
		leftInstance.onStateChanged().registerObserver(this);
		leftInstance.onTraceChanged().registerObserver(this);
		rightInstance.onStateChanged().registerObserver(this);
		rightInstance.onTraceChanged().registerObserver(this);
		
		//Add them to the superviser to get executed as a seperate process
		rightInstance.start(supervisor());
		leftInstance.start(supervisor());
		
		//Now let this process wait for the children to get into a waitForEvent state
		setState(CmlProcessState.WAIT_CHILD);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	private void removeTheChildren()
	{
		for(Iterator<CmlProcess> iterator = children().iterator(); iterator.hasNext(); )
		{
			CmlProcess child = iterator.next();
			supervisor().removePupil(child);
			iterator.remove();
		}
	}
	
	private CmlBehaviourSignal caseParallelEnd(Context question)
	{
		removeTheChildren();
		
		//now this process evolves into Skip
		pushNext(new ASkipAction(), question);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	@Override
	public CmlBehaviourSignal caseASkipAction(ASkipAction node, Context question)
			throws AnalysisException {

		//if hasNext() is true then Skip is in sequential composition with next
		if(!hasNext())
			setState(CmlProcessState.FINISHED);
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * action Assignment
	 */
	@Override
	public CmlBehaviourSignal caseASingleGeneralAssignmentStatementAction(
			ASingleGeneralAssignmentStatementAction node, Context question)
					throws AnalysisException {

		//				Value expValue = node.getExpression().apply(parentInterpreter,question);
		//						//TODO Change this to deal with it in general
		//						AIdentifierStateDesignator id = (AIdentifierStateDesignator)node
		//
		//						Context nameContext = question.locate(id.getName());
		//
		//				if(nameContext == null)
		//					nameContext = new CMLContext(node.getLocation(),"caseASi
		//
		//							nameContext.put(id.getName(), expValue);
		//
		//					System.out.println( id.getName() + " := " + expValue);
		//
		//					return new ProcessValueOld(null);
		
		return null;
	}

}
