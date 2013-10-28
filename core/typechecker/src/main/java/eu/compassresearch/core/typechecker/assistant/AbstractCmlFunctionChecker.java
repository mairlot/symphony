package eu.compassresearch.core.typechecker.assistant;

import java.io.File;
import java.io.InputStream;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.statements.AActionStm;
import org.overture.ast.statements.AAltNonDeterministicStm;
import org.overture.ast.statements.ADoNonDeterministicStm;
import org.overture.ast.statements.AIfNonDeterministicStm;
import org.overture.ast.statements.ANewStm;
import org.overture.ast.statements.AUnresolvedStateDesignator;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.utilities.FunctionChecker;

import eu.compassresearch.ast.actions.AAlphabetisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AAlphabetisedParallelismReplicatedAction;
import eu.compassresearch.ast.actions.AChannelRenamingAction;
import eu.compassresearch.ast.actions.AChaosAction;
import eu.compassresearch.ast.actions.ACommonInterleavingReplicatedAction;
import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.ADeclarationInstantiatedAction;
import eu.compassresearch.ast.actions.ADivAction;
import eu.compassresearch.ast.actions.AEndDeadlineAction;
import eu.compassresearch.ast.actions.AExternalChoiceAction;
import eu.compassresearch.ast.actions.AExternalChoiceReplicatedAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismReplicatedAction;
import eu.compassresearch.ast.actions.AGuardedAction;
import eu.compassresearch.ast.actions.AHidingAction;
import eu.compassresearch.ast.actions.AInterleavingParallelAction;
import eu.compassresearch.ast.actions.AInterleavingReplicatedAction;
import eu.compassresearch.ast.actions.AInternalChoiceAction;
import eu.compassresearch.ast.actions.AInternalChoiceReplicatedAction;
import eu.compassresearch.ast.actions.AInterruptAction;
import eu.compassresearch.ast.actions.AMuAction;
import eu.compassresearch.ast.actions.AParametrisedAction;
import eu.compassresearch.ast.actions.AParametrisedInstantiatedAction;
import eu.compassresearch.ast.actions.AReadCommunicationParameter;
import eu.compassresearch.ast.actions.AReferenceAction;
import eu.compassresearch.ast.actions.AResParametrisation;
import eu.compassresearch.ast.actions.ASequentialCompositionAction;
import eu.compassresearch.ast.actions.ASequentialCompositionReplicatedAction;
import eu.compassresearch.ast.actions.ASignalCommunicationParameter;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.AStartDeadlineAction;
import eu.compassresearch.ast.actions.AStmAction;
import eu.compassresearch.ast.actions.AStopAction;
import eu.compassresearch.ast.actions.ASynchronousParallelismParallelAction;
import eu.compassresearch.ast.actions.ASynchronousParallelismReplicatedAction;
import eu.compassresearch.ast.actions.ATimedInterruptAction;
import eu.compassresearch.ast.actions.ATimeoutAction;
import eu.compassresearch.ast.actions.AUntimedTimeoutAction;
import eu.compassresearch.ast.actions.AValParametrisation;
import eu.compassresearch.ast.actions.AVresParametrisation;
import eu.compassresearch.ast.actions.AWaitAction;
import eu.compassresearch.ast.actions.AWriteCommunicationParameter;
import eu.compassresearch.ast.analysis.intf.ICMLAnswer;
import eu.compassresearch.ast.declarations.AExpressionSingleDeclaration;
import eu.compassresearch.ast.declarations.ATypeSingleDeclaration;
import eu.compassresearch.ast.definitions.AActionClassDefinition;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.definitions.AActionsDefinition;
import eu.compassresearch.ast.definitions.AChannelDefinition;
import eu.compassresearch.ast.definitions.AChannelsDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AChansetsDefinition;
import eu.compassresearch.ast.definitions.AFunctionsDefinition;
import eu.compassresearch.ast.definitions.AInitialDefinition;
import eu.compassresearch.ast.definitions.ALogicalAccess;
import eu.compassresearch.ast.definitions.ANamesetDefinition;
import eu.compassresearch.ast.definitions.ANamesetsDefinition;
import eu.compassresearch.ast.definitions.AOperationsDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.definitions.ATypesDefinition;
import eu.compassresearch.ast.definitions.AValuesDefinition;
import eu.compassresearch.ast.expressions.ABracketedExp;
import eu.compassresearch.ast.expressions.ACompVarsetExpression;
import eu.compassresearch.ast.expressions.AComprehensionRenameChannelExp;
import eu.compassresearch.ast.expressions.AEnumVarsetExpression;
import eu.compassresearch.ast.expressions.AEnumerationRenameChannelExp;
import eu.compassresearch.ast.expressions.AFatCompVarsetExpression;
import eu.compassresearch.ast.expressions.AFatEnumVarsetExpression;
import eu.compassresearch.ast.expressions.AIdentifierVarsetExpression;
import eu.compassresearch.ast.expressions.AInterVOpVarsetExpression;
import eu.compassresearch.ast.expressions.ANameChannelExp;
import eu.compassresearch.ast.expressions.ASubVOpVarsetExpression;
import eu.compassresearch.ast.expressions.ATupleSelectExp;
import eu.compassresearch.ast.expressions.AUnionVOpVarsetExpression;
import eu.compassresearch.ast.expressions.AUnresolvedPathExp;
import eu.compassresearch.ast.patterns.ARenamePair;
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
import eu.compassresearch.ast.process.ASkipProcess;
import eu.compassresearch.ast.process.AStartDeadlineProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismReplicatedProcess;
import eu.compassresearch.ast.process.ATimedInterruptProcess;
import eu.compassresearch.ast.process.ATimeoutProcess;
import eu.compassresearch.ast.process.AUntimedTimeoutProcess;
import eu.compassresearch.ast.program.AFileSource;
import eu.compassresearch.ast.program.AInputStreamSource;
import eu.compassresearch.ast.program.ATcpStreamSource;
import eu.compassresearch.ast.types.AActionParagraphType;
import eu.compassresearch.ast.types.AActionType;
import eu.compassresearch.ast.types.ABindType;
import eu.compassresearch.ast.types.AChannelType;
import eu.compassresearch.ast.types.AChannelsParagraphType;
import eu.compassresearch.ast.types.AChansetParagraphType;
import eu.compassresearch.ast.types.AChansetType;
import eu.compassresearch.ast.types.AErrorType;
import eu.compassresearch.ast.types.AFunctionParagraphType;
import eu.compassresearch.ast.types.AInitialParagraphType;
import eu.compassresearch.ast.types.ANamesetType;
import eu.compassresearch.ast.types.ANamesetsType;
import eu.compassresearch.ast.types.AOperationParagraphType;
import eu.compassresearch.ast.types.AParagraphType;
import eu.compassresearch.ast.types.AProcessParagraphType;
import eu.compassresearch.ast.types.AProcessType;
import eu.compassresearch.ast.types.ASourceType;
import eu.compassresearch.ast.types.AStateParagraphType;
import eu.compassresearch.ast.types.AStatementType;
import eu.compassresearch.ast.types.ATypeParagraphType;
import eu.compassresearch.ast.types.AValueParagraphType;
import eu.compassresearch.ast.types.AVarsetExpressionType;

public abstract class AbstractCmlFunctionChecker extends FunctionChecker
		implements ICMLAnswer<Boolean>
{

	public AbstractCmlFunctionChecker(ITypeCheckerAssistantFactory af)
	{
		super(af);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1470058386572243235L;

	@Override
	public Boolean caseFile(File node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseInputStream(InputStream node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAFileSource(AFileSource node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATcpStreamSource(ATcpStreamSource node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInputStreamSource(AInputStreamSource node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATypeSingleDeclaration(ATypeSingleDeclaration node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAExpressionSingleDeclaration(
			AExpressionSingleDeclaration node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionDefinition(AActionDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChansetDefinition(AChansetDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseANamesetDefinition(ANamesetDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAProcessDefinition(AProcessDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelDefinition(AChannelDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelsDefinition(AChannelsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChansetsDefinition(AChansetsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseANamesetsDefinition(ANamesetsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionsDefinition(AActionsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATypesDefinition(ATypesDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAOperationsDefinition(AOperationsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAFunctionsDefinition(AFunctionsDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAValuesDefinition(AValuesDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInitialDefinition(AInitialDefinition node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseABracketedExp(ABracketedExp node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATupleSelectExp(ATupleSelectExp node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAUnresolvedPathExp(AUnresolvedPathExp node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseANameChannelExp(ANameChannelExp node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAComprehensionRenameChannelExp(
			AComprehensionRenameChannelExp node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAEnumerationRenameChannelExp(
			AEnumerationRenameChannelExp node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAIdentifierVarsetExpression(
			AIdentifierVarsetExpression node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAEnumVarsetExpression(AEnumVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseACompVarsetExpression(ACompVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAFatEnumVarsetExpression(AFatEnumVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAFatCompVarsetExpression(AFatCompVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAUnionVOpVarsetExpression(AUnionVOpVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterVOpVarsetExpression(AInterVOpVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASubVOpVarsetExpression(ASubVOpVarsetExpression node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAStatementType(AStatementType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAProcessType(AProcessType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAErrorType(AErrorType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseABindType(ABindType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAProcessParagraphType(AProcessParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChansetParagraphType(AChansetParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelsParagraphType(AChannelsParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionParagraphType(AActionParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAValueParagraphType(AValueParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAFunctionParagraphType(AFunctionParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATypeParagraphType(ATypeParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAOperationParagraphType(AOperationParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAStateParagraphType(AStateParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASourceType(ASourceType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelType(AChannelType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChansetType(AChansetType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseANamesetType(ANamesetType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseANamesetsType(ANamesetsType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInitialParagraphType(AInitialParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionType(AActionType node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAVarsetExpressionType(AVarsetExpressionType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAParagraphType(AParagraphType node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseALogicalAccess(ALogicalAccess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseARenamePair(ARenamePair node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionProcess(AActionProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASequentialCompositionProcess(
			ASequentialCompositionProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAExternalChoiceProcess(AExternalChoiceProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInternalChoiceProcess(AInternalChoiceProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAGeneralisedParallelismProcess(
			AGeneralisedParallelismProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAAlphabetisedParallelismProcess(
			AAlphabetisedParallelismProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASynchronousParallelismProcess(
			ASynchronousParallelismProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterleavingProcess(AInterleavingProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterruptProcess(AInterruptProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATimedInterruptProcess(ATimedInterruptProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAUntimedTimeoutProcess(AUntimedTimeoutProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATimeoutProcess(ATimeoutProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAHidingProcess(AHidingProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASkipProcess(ASkipProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAStartDeadlineProcess(AStartDeadlineProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAEndDeadlineProcess(AEndDeadlineProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInstantiationProcess(AInstantiationProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAReferenceProcess(AReferenceProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelRenamingProcess(AChannelRenamingProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASequentialCompositionReplicatedProcess(
			ASequentialCompositionReplicatedProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAExternalChoiceReplicatedProcess(
			AExternalChoiceReplicatedProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInternalChoiceReplicatedProcess(
			AInternalChoiceReplicatedProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAGeneralisedParallelismReplicatedProcess(
			AGeneralisedParallelismReplicatedProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAAlphabetisedParallelismReplicatedProcess(
			AAlphabetisedParallelismReplicatedProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASynchronousParallelismReplicatedProcess(
			ASynchronousParallelismReplicatedProcess node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterleavingReplicatedProcess(
			AInterleavingReplicatedProcess node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASkipAction(ASkipAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAStopAction(AStopAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChaosAction(AChaosAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseADivAction(ADivAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAWaitAction(AWaitAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseACommunicationAction(ACommunicationAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAGuardedAction(AGuardedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASequentialCompositionAction(
			ASequentialCompositionAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAExternalChoiceAction(AExternalChoiceAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInternalChoiceAction(AInternalChoiceAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterruptAction(AInterruptAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATimedInterruptAction(ATimedInterruptAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAUntimedTimeoutAction(AUntimedTimeoutAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseATimeoutAction(ATimeoutAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAHidingAction(AHidingAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAStartDeadlineAction(AStartDeadlineAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAEndDeadlineAction(AEndDeadlineAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAChannelRenamingAction(AChannelRenamingAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAMuAction(AMuAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAParametrisedAction(AParametrisedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAReferenceAction(AReferenceAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterleavingParallelAction(
			AInterleavingParallelAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAGeneralisedParallelismParallelAction(
			AGeneralisedParallelismParallelAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAAlphabetisedParallelismParallelAction(
			AAlphabetisedParallelismParallelAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASynchronousParallelismParallelAction(
			ASynchronousParallelismParallelAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASequentialCompositionReplicatedAction(
			ASequentialCompositionReplicatedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAExternalChoiceReplicatedAction(
			AExternalChoiceReplicatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInternalChoiceReplicatedAction(
			AInternalChoiceReplicatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseACommonInterleavingReplicatedAction(
			ACommonInterleavingReplicatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAInterleavingReplicatedAction(
			AInterleavingReplicatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAGeneralisedParallelismReplicatedAction(
			AGeneralisedParallelismReplicatedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAAlphabetisedParallelismReplicatedAction(
			AAlphabetisedParallelismReplicatedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASynchronousParallelismReplicatedAction(
			ASynchronousParallelismReplicatedAction node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseADeclarationInstantiatedAction(
			ADeclarationInstantiatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAParametrisedInstantiatedAction(
			AParametrisedInstantiatedAction node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAReadCommunicationParameter(
			AReadCommunicationParameter node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAWriteCommunicationParameter(
			AWriteCommunicationParameter node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseASignalCommunicationParameter(
			ASignalCommunicationParameter node) throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAValParametrisation(AValParametrisation node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAResParametrisation(AResParametrisation node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAVresParametrisation(AVresParametrisation node)
			throws AnalysisException
	{

		return false;
	}

	@Override
	public Boolean caseAActionStm(AActionStm node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseAStmAction(AStmAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseAUnresolvedStateDesignator(
			AUnresolvedStateDesignator node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseANewStm(ANewStm node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseAIfNonDeterministicStm(AIfNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseAAltNonDeterministicStm(AAltNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseADoNonDeterministicStm(ADoNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean caseAActionClassDefinition(AActionClassDefinition node)
			throws AnalysisException
	{
		return false;
	}

}