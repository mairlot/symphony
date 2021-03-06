package eu.compassresearch.core.analysis.pog.visitors;

//POG-related imports
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.AAssignmentDefinition;
import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.ANotYetSpecifiedStm;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.PType;
import org.overture.pog.obligation.PONameContext;
import org.overture.pog.obligation.StateInvariantObligation;
import org.overture.pog.obligation.SubTypeObligation;
import org.overture.pog.pub.IPOContextStack;
import org.overture.pog.utility.POException;
import org.overture.pog.visitors.PogParamDefinitionVisitor;
import org.overture.typechecker.TypeComparator;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.PSingleDeclaration;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.definitions.AChannelDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.expressions.AUnresolvedPathExp;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.pog.obligations.CmlOperationDefinitionContext;
import eu.compassresearch.core.analysis.pog.obligations.CmlOperationPostConditionObligation;
import eu.compassresearch.core.analysis.pog.obligations.CmlParameterPatternObligation;
import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligationList;
import eu.compassresearch.core.analysis.pog.obligations.CmlSatisfiabilityObligation;
import eu.compassresearch.core.analysis.pog.obligations.CmlStateInvariantObligation;
import eu.compassresearch.core.analysis.pog.obligations.CmlSubTypeObligation;
import eu.compassresearch.core.analysis.pog.utility.ClonerProcessState;
import eu.compassresearch.core.analysis.pog.utility.MakerNameContexts;

public class POGDeclAndDefVisitor extends
		QuestionAnswerCMLAdaptor<IPOContextStack, CmlProofObligationList>
{

	// Errors and other things are recorded on this guy
	final private QuestionAnswerAdaptor<IPOContextStack, CmlProofObligationList> parentPOG;
	final private PogParamDefinitionVisitor<IPOContextStack, CmlProofObligationList> overtureVisitor;
	final private MakerNameContexts nameVisitor;
	final CmlPogAssistantFactory assistantFactory;

	public POGDeclAndDefVisitor(
			QuestionAnswerAdaptor<IPOContextStack, CmlProofObligationList> parent, CmlPogAssistantFactory assistantFactory)
	{
		this.parentPOG = parent;
		this.overtureVisitor = new PogParamDefinitionVisitor<IPOContextStack, CmlProofObligationList>(this, this,assistantFactory);
		this.nameVisitor = new MakerNameContexts();
		this.assistantFactory = assistantFactory;

	}

	/**
	 * CML channel definition CURRENTLY JUST PRINT TO SCREEN
	 */
	@Override
	public CmlProofObligationList caseAChannelDefinition(
			AChannelDefinition node, IPOContextStack question)
			throws AnalysisException
	{

		CmlProofObligationList pol = new CmlProofObligationList();

		// NO POs here yet.

		return pol;
	}

	/**
	 * CML chanset definition CURRENTLY JUST PRINT TO SCREEN
	 */
	@Override
	public CmlProofObligationList caseAChansetDefinition(
			AChansetDefinition node, IPOContextStack question)
			throws AnalysisException
	{

		CmlProofObligationList pol = new CmlProofObligationList();

		// NO POs here yet

		return pol;
	}

	
	
	/**
	 * CML ELEMENT - Classes
	 */
	@Override
	public CmlProofObligationList caseAClassClassDefinition(
			AClassClassDefinition node, IPOContextStack question)
			throws AnalysisException
	{

		CmlProofObligationList pol = new CmlProofObligationList();

		for (PDefinition def : node.getDefinitions())
		{
			PONameContext name = def.apply(new MakerNameContexts());
			if (name != null)
			{
				question.push(def.apply(new MakerNameContexts()));
				pol.addAll(def.apply(parentPOG, question));
				question.pop();
			} else
			{
				pol.addAll(def.apply(parentPOG, question));
			}

		}

		return pol;
	}

	@Override
	public CmlProofObligationList caseAProcessDefinition(
			AProcessDefinition node, IPOContextStack question)
			throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();


		//FIXME Process Name Context Generation is strange atm
		// OLD one was
//		LexNameList params = new LexNameList();
//		LinkedList<ATypeSingleDeclaration> ls = node.getLocalState();
//		if (ls != null)
//		{
//			for (ATypeSingleDeclaration s : ls)
//			{
//				for (PDefinition def : s.getType().getDefinitions())
//				{
//					params.add(def.getName().clone());
//				}
//			}
//		}
//
//		question.push(new PONameContext(params));

		
		
		PProcess process = node.getProcess();
		PONameContext name = process.apply(new MakerNameContexts());
		
		if (name != null)
		{
			question.push(process.apply(new MakerNameContexts()));
			pol.addAll(process.apply(parentPOG, question));
			question.pop();
		} else
		{
			pol.addAll(process.apply(parentPOG, question));
		}

		return pol;
	}

	@Override
	public CmlProofObligationList caseAStateDefinition(AStateDefinition node,
			IPOContextStack question) throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();

		for (PDefinition def : node.getStateDefs())
		{
			pol.addAll(def.apply(parentPOG, question));
		}

		return pol;
	}

	@Override
	public CmlProofObligationList caseAActionDefinition(AActionDefinition node,
			IPOContextStack question) throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();

		// TODO re-enable pog action visits. for now, not doing it.

		// PAction action = node.getAction();
		// pol.addAll(action.apply(parentPOG, question));

		return pol;
	}

	// These will involve structural changes to the Overture AST in the
	// future but for now we hack past it.
	@Override
	public CmlProofObligationList caseAUnresolvedPathExp(
			AUnresolvedPathExp node, IPOContextStack question)
			throws AnalysisException
	{
		return new CmlProofObligationList();
	}

	@Override
	public CmlProofObligationList caseAClassInvariantDefinition(
			AClassInvariantDefinition node, IPOContextStack question)
			throws AnalysisException
	{
		try
		{
			CmlProofObligationList list = new CmlProofObligationList();

			if (node.getClassDefinition() != null)
			{

				if (!node.getClassDefinition().getHasContructors())
				{
					list.add(new StateInvariantObligation(node, question));
				}
			}

			return list;
		} catch (Exception e)
		{
			throw new POException(node, e.getMessage());
		}
	}

	// default case. stuff with no pos
	@Override
	public CmlProofObligationList defaultPSingleDeclaration(
			PSingleDeclaration node, IPOContextStack question)
			throws AnalysisException
	{
		return new CmlProofObligationList();
	}

	// Call Overture for the other defs
	@Override
	public CmlProofObligationList defaultPDefinition(PDefinition node,
			IPOContextStack question) throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();
		pol.addAll(node.apply(overtureVisitor, question));
		return pol;
	}

	// Call the main pog when it's not a defintion/declaration
	@Override
	public CmlProofObligationList defaultINode(INode node,
			IPOContextStack question) throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();
		pol.addAll(node.apply(parentPOG, question));
		return pol;
	}

	/**
	 * Implicit operations - CML does not reuse Overture operations
	 */
	@Override
	public CmlProofObligationList caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node, IPOContextStack question)
			throws AnalysisException
	{
		CmlProofObligationList pol = new CmlProofObligationList();

		// Taken from Overture - Needed?
		LexNameList pids = new LexNameList();

		for (APatternListTypePair tp : node.getParameterPatterns())
			for (PPattern p : tp.getPatterns())
				for (PDefinition def : p.getDefinitions())
					pids.add(def.getName());

		// Taken from Overture - Needed?
		if (pids.hasDuplicates())
		{
			pol.add(new CmlParameterPatternObligation(assistantFactory,node, question));
		}

		// if implicit operation has a precondition, dispatch for PO checking
		if (node.getPrecondition() != null)
		{
			pol.addAll(node.getPrecondition().apply(parentPOG, question));
		}

		// if implicit operation has a precondition, dispatch for PO checking
		// and generate OperationPostConditionObligation

		if (node.getPostcondition() != null)
		{
			pol.addAll(node.getPostcondition().apply(parentPOG, question));

			PDefinition stateDef;

			AActionProcess stater = node.getAncestor(AActionProcess.class);
			if (stater != null)
			{
				List<AAssignmentDefinition> stateDefs = stater.apply(new ClonerProcessState());
				stateDefs.size();
				question.push(new CmlOperationDefinitionContext(node, false, stateDefs));
				pol.add(new CmlSatisfiabilityObligation(node, stateDefs, question));
				question.pop();
			} else
			{
				if (node.getClassDefinition() != null)
				{
					stateDef = node.getClassDefinition().clone();
					} else
				{
					stateDef = node.getStateDefinition();
				}
				question.push(new CmlOperationDefinitionContext(node, false, stateDef));
				pol.add(new CmlSatisfiabilityObligation(node, stateDef, question));
				question.pop();
			}
		}

		return pol;
	}

	//
	@Override
	public CmlProofObligationList caseAExplicitOperationDefinition(
			AExplicitOperationDefinition node, IPOContextStack question)
			throws AnalysisException
	{

		if (node.getBody() instanceof ANotYetSpecifiedStm)
		{
			return new CmlProofObligationList();
		}
		CmlProofObligationList pol = new CmlProofObligationList();

		LexNameList pids = new LexNameList();

		// add all defined names from the function parameter list
		for (PPattern p : node.getParameterPatterns())
			for (PDefinition def : p.getDefinitions())
				pids.add(def.getName());

		if (pids.hasDuplicates())
		{
			pol.add(new CmlParameterPatternObligation(assistantFactory,node, question));
		}

		// if operation has a precondition, dispatch for PO checking
		if (node.getPrecondition() != null)
		{
			pol.addAll(node.getPrecondition().apply(parentPOG, question));
		}
		// if operation has a precondition, dispatch for PO checking
		// and generate OperationPostConditionObligation
		if (node.getPostcondition() != null)
		{
			pol.addAll(node.getPostcondition().apply(parentPOG, question));
			pol.add(new CmlOperationPostConditionObligation(node, question));
		}

		// dispatch operation body for PO checking
		pol.addAll(node.getBody().apply(parentPOG, question));

		if (node.getIsConstructor() && node.getClassDefinition() != null
				&& node.getClassDefinition().getInvariant() != null)
		{
			pol.add(new CmlStateInvariantObligation(assistantFactory,node, question));
		}

		if (!node.getIsConstructor()
				&& !TypeComparator.isSubType(node.getActualResult(), ((AOperationType) node.getType()).getResult(),assistantFactory))
		{
			CmlSubTypeObligation sto = CmlSubTypeObligation.newInstance(node, node.getActualResult(), question,assistantFactory);
			if (sto != null)
			{
				pol.add(sto);
			}
		}

		return pol;
	}

	// >----------------------------------------------------------
	// >------------OVERTURE Overrides --------------------------
	// >--------------------------------------------------------

	@Override
	public CmlProofObligationList caseAAssignmentDefinition(
			AAssignmentDefinition node, IPOContextStack question)
			throws AnalysisException
	{
		CmlProofObligationList obligations = new CmlProofObligationList();

		if (node.getExpression() == null)
			return obligations;
		PExp expression = node.getExpression();
		PType type = node.getType();
		PType expType = node.getExpType();

		obligations.addAll(expression.apply(parentPOG, question));

		if (!TypeComparator.isSubType(question.checkType(expression, expType), type,assistantFactory))
		{
			SubTypeObligation sto = SubTypeObligation.newInstance(expression, type, expType, question,assistantFactory);
			if (sto != null)
			{
				obligations.add(sto);
			}
		}

		return obligations;
	}

	@Override
	public CmlProofObligationList createNewReturnValue(INode node,
			IPOContextStack question)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CmlProofObligationList createNewReturnValue(Object node,
			IPOContextStack question)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
