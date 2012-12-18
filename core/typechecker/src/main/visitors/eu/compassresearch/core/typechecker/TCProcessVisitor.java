package eu.compassresearch.core.typechecker;

import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.ALocalDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.lex.LexIdentifierToken;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;

import sun.reflect.generics.tree.ShortSignature;

import eu.compassresearch.ast.actions.ATimedInterruptAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.ATypeSingleDeclaration;
import eu.compassresearch.ast.declarations.SSingleDeclaration;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.definitions.AProcessParagraphDefinition;
import eu.compassresearch.ast.expressions.SRenameChannelExp;
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
import eu.compassresearch.ast.process.AInternalChoiceReplicatedProcess;
import eu.compassresearch.ast.process.AInterruptProcess;
import eu.compassresearch.ast.process.AReferenceProcess;
import eu.compassresearch.ast.process.ASequentialCompositionProcess;
import eu.compassresearch.ast.process.ASequentialCompositionReplicatedProcess;
import eu.compassresearch.ast.process.AStartDeadlineProcess;
import eu.compassresearch.ast.process.AStateProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismReplicatedProcess;
import eu.compassresearch.ast.process.ATimedInterruptProcess;
import eu.compassresearch.ast.process.ATimeoutProcess;
import eu.compassresearch.ast.process.AUntimedTimeoutProcess;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.ast.types.AActionType;
import eu.compassresearch.ast.types.AProcessType;
import eu.compassresearch.core.typechecker.api.TypeComparator;
import eu.compassresearch.core.typechecker.api.TypeErrorMessages;
import eu.compassresearch.core.typechecker.api.TypeIssueHandler;
import eu.compassresearch.core.typechecker.api.TypeWarningMessages;

@SuppressWarnings("serial")
public class TCProcessVisitor extends
QuestionAnswerCMLAdaptor<org.overture.typechecker.TypeCheckInfo, PType> {

	private VanillaCmlTypeChecker parentChecker;
	private TypeIssueHandler issueHandler;
	private TypeComparator typeComparator;

	@Override
	public PType defaultPAction(PAction node, TypeCheckInfo question)
			throws AnalysisException {
		return new AActionType();
	}





	@Override
	public PType caseAUntimedTimeoutProcess(AUntimedTimeoutProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PProcess left = node.getLeft();
		PProcess right = node.getRight();
		
		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));
		
		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(right+""));
	
		return new AProcessType();
	}





	@Override
	public PType caseATimeoutProcess(ATimeoutProcess node,
			TypeCheckInfo question) throws AnalysisException {

	
		PExp timedExp = node.getTimeoutExpression();
		PProcess right = node.getRight();
		PProcess left = node.getLeft();

		
		PType timedExpType = timedExp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(timedExpType))
			return issueHandler.addTypeError(timedExp, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+timedExp));
		
		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+left));
		
		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+right));
		
		
		return new AProcessType();
	}





	@Override
	public PType caseASynchronousParallelismReplicatedProcess(
			ASynchronousParallelismReplicatedProcess node,
			TypeCheckInfo question) throws AnalysisException {
		PProcess proc = node.getReplicatedProcess();
		LinkedList<SSingleDeclaration> repdecl = node.getReplicationDeclaration();
		
		for(SSingleDeclaration decl : repdecl)
		{
			PType declType = decl.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(declType))
				return issueHandler.addTypeError(declType, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(declType+""));
		}
		

		PType procType = proc.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(procType))
			return issueHandler.addTypeError(proc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+proc));
		
		
		
		return new AProcessType();
	}





	@Override
	public PType caseASequentialCompositionReplicatedProcess(
			ASequentialCompositionReplicatedProcess node, TypeCheckInfo question)
			throws AnalysisException {

		PProcess proc = node.getReplicatedProcess();
		LinkedList<SSingleDeclaration> repdecl = node.getReplicationDeclaration();
		
		for(SSingleDeclaration decl : repdecl)
		{
			PType declType = decl.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(declType))
				return issueHandler.addTypeError(declType, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(declType+""));
		}
		

		PType procType = proc.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(procType))
			return issueHandler.addTypeError(proc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+proc));
		
		
		
		return new AProcessType();
	}





	@Override
	public PType caseAInternalChoiceReplicatedProcess(
			AInternalChoiceReplicatedProcess node, TypeCheckInfo question)
			throws AnalysisException {

		
		PProcess proc = node.getReplicatedProcess();
		LinkedList<SSingleDeclaration> repdecl = node.getReplicationDeclaration();
		
		for(SSingleDeclaration decl : repdecl)
		{
			PType declType = decl.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(declType))
				return issueHandler.addTypeError(declType, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(declType+""));
		}
		

		PType procType = proc.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(procType))
			return issueHandler.addTypeError(proc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+proc));
		
		return new AProcessType();
	}





	@Override
	public PType caseAGeneralisedParallelismReplicatedProcess(
			AGeneralisedParallelismReplicatedProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PExp csExp = node.getChansetExpression();
		PProcess repProc = node.getReplicatedProcess();
		LinkedList<SSingleDeclaration> repDecl = node.getReplicationDeclaration();
		
		PType csExpType = csExp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(csExpType))
			return issueHandler.addTypeError(csExp, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(csExp+""));
		
		PType repProcType = repProc.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(repProcType))
			return issueHandler.addTypeError(repProc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(repProc+""));
		
		for(SSingleDeclaration decl : repDecl)
		{
			PType declType = decl.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(declType))
				return issueHandler.addTypeError(decl, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+decl));
		}
		
		return new AProcessType();
	}





	@Override
	public PType caseAExternalChoiceReplicatedProcess(
			AExternalChoiceReplicatedProcess node, TypeCheckInfo question)
			throws AnalysisException {

		LinkedList<SSingleDeclaration> repDecl = node.getReplicationDeclaration();
		PProcess repProc = node.getReplicatedProcess();
		
		for(SSingleDeclaration decl : repDecl)
		{
			PType declType = decl.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(declType))
				return issueHandler.addTypeError(decl, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+decl));
			
		}
		
		PType repProcType = repProc.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(repProcType))
			return issueHandler.addTypeError(repProc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(repProc+""));
		
		
		return new AProcessType();
	}





	@Override
	public PType caseAAlphabetisedParallelismReplicatedProcess(
			AAlphabetisedParallelismReplicatedProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PExp csExp = node.getChansetExpression();
		PProcess repProcess = node.getReplicatedProcess();
		LinkedList<SSingleDeclaration> repDec = node.getReplicationDeclaration();
		
		PType csExpType = csExp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(csExpType))
			return issueHandler.addTypeError(csExp, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+csExp));
		
		for(SSingleDeclaration d : repDec)
		{
			PType dType = d.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(dType))
				return issueHandler.addTypeError(d,TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+d));
		}

		// TODO: Maybe the declarations above needs to go into the environment ?
		issueHandler.addTypeWarning(repProcess, TypeWarningMessages.INCOMPLETE_TYPE_CHECKING.customize(""+repProcess));
		PType repProcessType = repProcess.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(repProcessType))
			return issueHandler.addTypeError(repProcess, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(repProcess+""));
		
		
		return new AProcessType();
	}





	@Override
	public PType caseAInterruptProcess(AInterruptProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PProcess left = node.getLeft();
		PProcess right = node.getRight();
		
		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));
		
		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+right));
		
		
		
		return new AProcessType();
	}





	@Override
	public PType caseAInterleavingProcess(AInterleavingProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PProcess left = node.getLeft();
		PProcess right = node.getRight();
		
		
		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));
		
		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+right));
		
		
		return new AProcessType();
	}





	@Override
	public PType caseAInstantiationProcess(AInstantiationProcess node,
			TypeCheckInfo question) throws AnalysisException {

		LinkedList<PExp> args = node.getArgs();
		LinkedList<ATypeSingleDeclaration> decl = node.getDeclarations();
		PProcess proc = node.getProcess();

		CmlTypeCheckInfo cmlEnv = TCActionVisitor.getTypeCheckInfo(question);
		if (cmlEnv == null)
			return issueHandler.addTypeError(node,TypeErrorMessages.ILLEGAL_CONTEXT.customizeMessage(""+node));

		CmlTypeCheckInfo procEnv = cmlEnv.newScope();

		for(PExp arg : args)
		{
			PType argType = arg.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(argType))
			{
				return issueHandler.addTypeError(arg,TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+arg));
			}
		}

		List<PDefinition> definitions = new LinkedList<PDefinition>();
		List<LexIdentifierToken> ids = new LinkedList<LexIdentifierToken>();
		for(ATypeSingleDeclaration d : decl)
		{
			PType dType = d.apply(parentChecker,question);
			if (!TCDeclAndDefVisitor.successfulType(dType))
			{
				return issueHandler.addTypeError(dType,TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+d));
			}

			definitions.addAll(dType.getDefinitions());
		}

		if (args.size() != definitions.size())
		{
			return issueHandler.addTypeError(node, TypeErrorMessages.WRONG_NUMBER_OF_ARGUMENTS.customizeMessage(""+definitions.size(), ""+args.size()));
		}


		for(int i = 0; i < args.size(); i++)
		{
			PExp ithExp = args.get(i);
			PDefinition ithDef = definitions.get(i);
			if (!typeComparator.compatible(ithExp.getType(), ithDef.getType()))
			{
				return issueHandler.addTypeError(node,TypeErrorMessages.INCOMPATIBLE_TYPE.customizeMessage(""+ithDef.getType(), ""+ithExp.getType()));
			}
			procEnv.addVariable(ithDef.getName(), ithDef);
		}

		PType procType = proc.apply(parentChecker,procEnv);
		if (!TCDeclAndDefVisitor.successfulType(procType))
			return issueHandler.addTypeError(proc, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+proc));
		
		return new AProcessType();
	}





	@Override
	public PType caseAHidingProcess(AHidingProcess node, TypeCheckInfo question)
			throws AnalysisException {

		PProcess left = node.getLeft();
		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));

		PExp csexp = node.getChansetExpression();
		PType csexpType = csexp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(csexpType))
		{
			return issueHandler.addTypeError(csexp, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+csexp));
		}


		return new AProcessType(node.getLocation(), true);
	}





	@Override
	public PType caseAGeneralisedParallelismProcess(
			AGeneralisedParallelismProcess node, TypeCheckInfo question)
					throws AnalysisException {

		PProcess left = node.getLeft();
		PProcess right = node.getRight();
		PExp csExp = node.getChansetExpression();

		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType)){
			return issueHandler.addTypeError(left,TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));
		}

		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(right+""));

		PType csExpType = csExp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(csExpType))
		{
			return issueHandler.addTypeError(csExp, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(csExp+""));
		}

		return new AProcessType();
	}





	@Override
	public PType caseAExternalChoiceProcess(AExternalChoiceProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PProcess left = node.getLeft();
		PProcess right = node.getRight();

		PType leftType = left.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left,TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(left+""));

		PType rightType = right.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(right+""));

		return new AProcessType();
	}





	@Override
	public PType caseAChannelRenamingProcess(AChannelRenamingProcess node,
			TypeCheckInfo question) throws AnalysisException {

		PProcess process = node.getProcess();
		SRenameChannelExp renameExp = node.getRenameExpression();

		PType processType = process.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(processType))
			return issueHandler.addTypeError(process, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(process+""));

		PType renameExpType = renameExp.apply(parentChecker,question);
		if (!TCDeclAndDefVisitor.successfulType(renameExpType))
			return issueHandler.addTypeError(renameExpType, TypeErrorMessages.COULD_NOT_DETERMINE_TYPE.customizeMessage(""+renameExp));

		return new AProcessType();
	}



	@SuppressWarnings("deprecation")
	@Override
	public PType caseAAlphabetisedParallelismProcess(
			AAlphabetisedParallelismProcess node, TypeCheckInfo question)
					throws AnalysisException {

		PProcess left = node.getLeft();
		PType leftType = left.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage("" + left));

		PProcess right = node.getRight();
		PType rightType = right.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage("" + right));

		PExp leftChanSet = node.getLeftChansetExpression();
		PType leftChanSetType = leftChanSet.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(leftChanSetType))
			return issueHandler.addTypeError(leftChanSet,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage("" + leftChanSet));

		PExp rightChanSet = node.getRightChansetExpression();
		PType rightChanSetType = rightChanSet.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(rightChanSetType))
			return issueHandler.addTypeError(rightChanSet,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage("" + rightChanSet));

		return new AProcessType(node.getLocation(), true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public PType caseAStartDeadlineProcess(AStartDeadlineProcess node,
			org.overture.typechecker.TypeCheckInfo question)
					throws AnalysisException {

		PProcess left = node.getLeft();

		PExp timeExp = node.getExpression();

		PType leftType = left.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(left + ""));

		PType timeExpType = timeExp.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(timeExpType))
			return issueHandler.addTypeError(timeExp,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(timeExp + ""));

		if (!typeComparator.isSubType(timeExpType, new ANatNumericBasicType()))
			return issueHandler.addTypeError(timeExp,
					TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT
					.customizeMessage(node + "", timeExpType + ""));

		return new AProcessType(node.getLocation(), true);
	}



	@Override
	public PType caseAEndDeadlineProcess(AEndDeadlineProcess node,
			TypeCheckInfo question) throws AnalysisException {
		//TODO RWL Make this complete
		return new AProcessType(node.getLocation(), true);
	}

	@Override
	public PType caseAInterleavingReplicatedProcess(
			AInterleavingReplicatedProcess node, TypeCheckInfo question)
					throws AnalysisException {

		LinkedList<SSingleDeclaration> declarations = node
				.getReplicationDeclaration();

		for (SSingleDeclaration singleDecl : declarations) {
			PType singleDeclType = singleDecl.apply(parentChecker, question);
			if (!TCDeclAndDefVisitor.successfulType(singleDeclType))
				return issueHandler.addTypeError(singleDecl,
						TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
						.customizeMessage(singleDecl + ""));
		}

		PProcess replicatedProcess = node.getReplicatedProcess();

		PType replicatedProcessType = replicatedProcess.apply(parentChecker,
				question);
		if (!TCDeclAndDefVisitor.successfulType(replicatedProcessType))
			return issueHandler.addTypeError(replicatedProcess,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(replicatedProcess + ""));

		return new AProcessType(node.getLocation(), true);
	}

	public TCProcessVisitor(VanillaCmlTypeChecker parentChecker,
			TypeIssueHandler issueHandler, TypeComparator typeComparator) {
		this.parentChecker = parentChecker;
		this.issueHandler = issueHandler;
		this.typeComparator = typeComparator;
	}

	@Override
	public PType caseASynchronousParallelismProcess(
			ASynchronousParallelismProcess node,
			org.overture.typechecker.TypeCheckInfo question)
					throws AnalysisException {

		node.getLeft().apply(this, question);
		node.getRight().apply(this, question);

		// TODO: missing marker on processes

		return new AProcessType();
	}

	@Override
	public PType caseASequentialCompositionProcess(
			ASequentialCompositionProcess node,
			org.overture.typechecker.TypeCheckInfo question)
					throws AnalysisException {

		node.getLeft().apply(this, question);
		node.getRight().apply(this, question);

		// TODO: missing marker on processes

		return new AProcessType();
	}

	@Override
	public PType caseAReferenceProcess(AReferenceProcess node,
			org.overture.typechecker.TypeCheckInfo question)
					throws AnalysisException {
		eu.compassresearch.core.typechecker.CmlTypeCheckInfo newQ = (eu.compassresearch.core.typechecker.CmlTypeCheckInfo) question;
		PDefinition processDef = newQ.lookupVariable(node.getProcessName());

		if (processDef == null) {
			return issueHandler.addTypeError(
					node,
					TypeErrorMessages.UNDEFINED_SYMBOL.customizeMessage(node
							.getProcessName() + ""));
		}

		if (!(processDef instanceof AProcessParagraphDefinition))
			return issueHandler.addTypeError(processDef,
					TypeErrorMessages.EXPECTED_PROCESS_DEFINITION
					.customizeMessage(node.getProcessName() + ""));
		node.setProcessDefinition(((AProcessParagraphDefinition) processDef)
				.getProcessDefinition());

		return new AProcessType();
	}



	@Override
	public PType caseAStateProcess(AStateProcess node,
			org.overture.typechecker.TypeCheckInfo question)
					throws AnalysisException {


		// Set the process def for this node
		node.setProcessDefinition(node.getAncestor(AProcessDefinition.class));

		// Type check all the paragraph definitions
		for (PDefinition def : node.getDefinitionParagraphs()) {
			PType type = def.apply(this.parentChecker, question);

			if (!TCDeclAndDefVisitor.successfulType(type))
				return issueHandler.addTypeError(def,
						TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
						.customizeMessage(def.getName() + ""));
		}

		question.contextSet(eu.compassresearch.core.typechecker.CmlTypeCheckInfo.class, (eu.compassresearch.core.typechecker.CmlTypeCheckInfo)question);
		PType actionType = node.getAction().apply(this.parentChecker, question);
		question.contextRem(eu.compassresearch.core.typechecker.CmlTypeCheckInfo.class);
		if (!TCDeclAndDefVisitor.successfulType(actionType))
			return issueHandler.addTypeError(node.getAction(),
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(node.getAction() + ""));
		return new AProcessType();
	}

	@SuppressWarnings("deprecation")
	@Override
	public PType caseATimedInterruptProcess(ATimedInterruptProcess node,
			TypeCheckInfo question) throws AnalysisException {
		PProcess left = node.getLeft();
		PType leftType = left.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(leftType))
			return issueHandler.addTypeError(left,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(left + ""));

		PProcess right = node.getRight();
		PType rightType = right.apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(rightType))
			return issueHandler.addTypeError(right,
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(right + ""));

		PType expType = node.getTimeExpression().apply(parentChecker, question);
		if (!TCDeclAndDefVisitor.successfulType(expType))
			return issueHandler.addTypeError(node.getTimeExpression(),
					TypeErrorMessages.COULD_NOT_DETERMINE_TYPE
					.customizeMessage(node.getTimeExpression() + ""));
		if (!typeComparator.isSubType(expType, new ANatNumericBasicType()))
			return issueHandler.addTypeError(node.getTimeExpression(),
					TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT
					.customizeMessage(node.getTimeExpression() + ""));

		return new AProcessType(node.getLocation(), true);
	}

	@Override
	public PType caseATimedInterruptAction(ATimedInterruptAction node,
			TypeCheckInfo question) throws AnalysisException {
		// TODO Auto-generated method stub
		return super.caseATimedInterruptAction(node, question);
	}

}
