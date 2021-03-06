package eu.compassresearch.core.analysis.theoremprover.visitors.deps;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.node.INode;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.definitions.AActionClassDefinition;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.AAlphabetisedParallelismProcess;
import eu.compassresearch.ast.process.AAlphabetisedParallelismReplicatedProcess;
import eu.compassresearch.ast.process.AChannelRenamingProcess;
import eu.compassresearch.ast.process.AEndDeadlineProcess;
import eu.compassresearch.ast.process.AExternalChoiceProcess;
import eu.compassresearch.ast.process.AExternalChoiceReplicatedProcess;
import eu.compassresearch.ast.process.AGeneralisedParallelismProcess;
import eu.compassresearch.ast.process.AGeneralisedParallelismReplicatedProcess;
import eu.compassresearch.ast.process.AHidingProcess;
import eu.compassresearch.ast.process.AInstantiationProcess;
import eu.compassresearch.ast.process.AInterleavingProcess;
import eu.compassresearch.ast.process.AInterleavingReplicatedProcess;
import eu.compassresearch.ast.process.AInternalChoiceProcess;
import eu.compassresearch.ast.process.AInternalChoiceReplicatedProcess;
import eu.compassresearch.ast.process.AInterruptProcess;
import eu.compassresearch.ast.process.AReferenceProcess;
import eu.compassresearch.ast.process.ASequentialCompositionProcess;
import eu.compassresearch.ast.process.ASequentialCompositionReplicatedProcess;
import eu.compassresearch.ast.process.AStartDeadlineProcess;
import eu.compassresearch.ast.process.ATimedInterruptProcess;
import eu.compassresearch.ast.process.ATimeoutProcess;
import eu.compassresearch.ast.process.AUntimedTimeoutProcess;
import eu.compassresearch.core.analysis.theoremprover.thms.NodeNameList;
import eu.compassresearch.core.analysis.theoremprover.thms.ThmNodeList;
import eu.compassresearch.core.analysis.theoremprover.utils.ThmProcessUtil;
import eu.compassresearch.core.analysis.theoremprover.visitors.TPVisitor;
import eu.compassresearch.core.analysis.theoremprover.visitors.string.ThmVarsContext;

public class ThmProcessDepVisitor  extends
QuestionAnswerCMLAdaptor<NodeNameList, NodeNameList>{
	
	final private QuestionAnswerCMLAdaptor<NodeNameList, NodeNameList> thmDepVisitor;
	
	public ThmProcessDepVisitor(ThmDepVisitor thmDepVisitor) {
		this.thmDepVisitor = thmDepVisitor;
	}
	
	/**
	 * NEED TO GET VISITORS WORKING HERE MORE...
	 * 
	 * Return the ThmNode for a Action Process - this is more complex than most other
	 * Node utils, due to the internal scoping etc required in a process
	 * @param procName the process name
	 * @param act the action process of the owning process
	 * @return the ThmNode object for this process
	 * @throws AnalysisException 
	 */
	public NodeNameList caseAActionProcess(AActionProcess act, NodeNameList bvars) throws AnalysisException
	{
		NodeNameList nodeDeps = new NodeNameList();		

		//Require a list of all names used within a process, so to ensure the dependency 
		//relationships within and outside the process can be dealt with.
		NodeNameList procNodeNames = ThmProcessUtil.getProcessNames(act);
		NodeNameList svars = ThmProcessUtil.getProcessStatementNames(act);
		AActionClassDefinition actdef = (AActionClassDefinition) act.getActionDefinition();	
						
		//if there are state variables
		if (!svars.isEmpty())
		{
			//next generate nodes for the state variables, and add their initialised 
			//assignments to a collection for initialisation in main action
			//Also generate the invariant functions...
			NodeNameList initExprNodeDeps = new NodeNameList();
			for (PDefinition pdef : actdef.getDefinitions())
			{
				if (pdef instanceof AInstanceVariableDefinition)
				{					
					AInstanceVariableDefinition sdef = (AInstanceVariableDefinition) pdef;
					//if the variable is initialised straight away, get the dependencies
					initExprNodeDeps.addAll(sdef.getExpression().apply(thmDepVisitor, new NodeNameList()));//(ThmExprUtil.getIsabelleExprDeps(new NodeNameList(),  st.getExpression()));
					//Add all dependencies to the processes dependencies
					nodeDeps.addAll(initExprNodeDeps);
				}
			}
		}

		for (PDefinition pdef : actdef.getDefinitions())
		{
			ThmNodeList defNodes = new ThmNodeList();
			
			defNodes.addAll(pdef.apply(new TPVisitor(), new ThmVarsContext(svars, new NodeNameList())));//(ThmProcessUtil.getAExplicitFunctionDefinition(f));

			//Add all dependencies to the list of process dependencies
			nodeDeps.addAll(defNodes.getAllNodeDeps());
		}
		
		//Remove all inner dependencies from the process dependency list. We only care about the 
		//things external to the process that we depend upon.
		nodeDeps = nodeDeps.removeDeps(procNodeNames);
				
		//Finally construct the node to represent the process
		return nodeDeps;
	}
	
	
	public NodeNameList caseAAlphabetisedParallelismProcess(AAlphabetisedParallelismProcess p, NodeNameList bvars) throws AnalysisException{
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}

	public NodeNameList caseAAlphabetisedParallelismReplicatedProcess(AAlphabetisedParallelismReplicatedProcess p, NodeNameList bvars) throws AnalysisException{
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}

	public NodeNameList caseAChannelRenamingProcess(AChannelRenamingProcess node, NodeNameList bvars) throws AnalysisException{	
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}
	
	public NodeNameList caseAEndDeadlineProcess(AEndDeadlineProcess p, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}

	public NodeNameList caseAExternalChoiceProcess(AExternalChoiceProcess p, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAExternalChoiceReplicatedProcess(
			AExternalChoiceReplicatedProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAGeneralisedParallelismProcess(
			AGeneralisedParallelismProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAGeneralisedParallelismReplicatedProcess(
			AGeneralisedParallelismReplicatedProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAHidingProcess(AHidingProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInstantiationProcess(AInstantiationProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInterleavingProcess(AInterleavingProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInterleavingReplicatedProcess(
			AInterleavingReplicatedProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInternalChoiceProcess(AInternalChoiceProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInternalChoiceReplicatedProcess(
			AInternalChoiceReplicatedProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAInterruptProcess(AInterruptProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAReferenceProcess(AReferenceProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseASequentialCompositionProcess(
			ASequentialCompositionProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseASequentialCompositionReplicatedProcess(
			ASequentialCompositionReplicatedProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAStartDeadlineProcess(AStartDeadlineProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseATimedInterruptProcess(ATimedInterruptProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseATimeoutProcess(ATimeoutProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}


	public NodeNameList caseAUntimedTimeoutProcess(AUntimedTimeoutProcess node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		return nodeDeps;
	}

	
	
//	
//	public NodeNameList caseAReferenceProcess(AReferenceProcess p, NodeNameList bvars)
//			throws AnalysisException {
//		NodeNameList nodeDeps = new NodeNameList();
//
//		LinkedList<PExp> args = p.getArgs();
//		nodeDeps.add(p.getProcessName());
//		if (args.size() != 0)
//		{
//			for (PExp e: p.getArgs()) 
//			{
//				nodeDeps.addAll(e.apply(thmDepVisitor, bvars));	
//			}
//		}
//	
//		return nodeDeps;
//	}

	@Override
	public NodeNameList createNewReturnValue(INode arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeNameList createNewReturnValue(Object arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}
}
