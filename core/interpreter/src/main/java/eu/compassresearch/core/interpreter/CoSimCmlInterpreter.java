package eu.compassresearch.core.interpreter;

import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;

import eu.compassresearch.core.interpreter.api.behaviour.CmlBehaviour;
import eu.compassresearch.core.interpreter.api.transitions.AbstractSilentTransition;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransition;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionSet;
import eu.compassresearch.core.interpreter.cosim.CoSimulationClient;
import eu.compassresearch.core.interpreter.cosim.communication.Utils;

public class CoSimCmlInterpreter extends VanillaCmlInterpreter
{

	private CoSimulationClient client;

	boolean runningInternalExecution = false;

	/**
	 * The active (currently executing) behaviour
	 */
	private CmlBehaviour activeBehaviour;

	public CoSimCmlInterpreter(List<PDefinition> definitions, Config config)
	{
		super(definitions, config);
	}

	public void setClient(CoSimulationClient client)
	{
		this.client = client;
		this.client.setInterpreter(this);
	}

	@Override
	protected void executeTopProcess(CmlBehaviour behaviour)
			throws AnalysisException, InterruptedException
	{
		this.activeBehaviour = behaviour;
		super.executeTopProcess(behaviour);
	}

	@Override
	public CmlTransition resolveChoice(CmlTransitionSet availableEvents)
	{
		CmlTransitionSet transitions = null;

		// let this simulator execute all non-observable
		transitions = extractSilentTransitions(availableEvents, transitions);

		if (transitions == null || transitions.isEmpty())
		{
			// no silent transitions are available to we must sync with the coordinator
			try
			{
				transitions = client.getAvaliableTransitions();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return super.resolveChoice(transitions);
	}

	protected synchronized CmlTransitionSet extractSilentTransitions(
			CmlTransitionSet availableEvents, CmlTransitionSet transitions)
	{
		for (AbstractSilentTransition transition : availableEvents.getSilentTransitionsAsSet())
		{
			if (transition instanceof AbstractSilentTransition)
			{
				runningInternalExecution = true;
				transitions = new CmlTransitionSet(transition);
				break;
			}
		}
		return transitions;
	}

	public CmlTransitionSet inspect() throws AnalysisException
	{
		CmlTransitionSet tmp = internalInspect();
		while (runningInternalExecution)
		{
			Utils.milliPause(10);

			tmp = internalInspect();
			if (tmp != null)
			{
				break;
			}
		}

		return tmp;

	}

	private synchronized CmlTransitionSet internalInspect()
			throws AnalysisException
	{
		if (!runningInternalExecution)
		{
			return super.inspect(this.activeBehaviour);
		}
		return null;
	}

	@Override
	protected void executeBehaviour(CmlBehaviour behaviour)
			throws AnalysisException
	{
		this.runningInternalExecution = true;
		super.executeBehaviour(behaviour);
		this.runningInternalExecution = false;
	}
}
