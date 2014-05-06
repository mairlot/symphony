package eu.compassresearch.core.analysis.c2c.visitors;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.statements.PStm;

import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;
import eu.compassresearch.ast.definitions.AActionClassDefinition;
import eu.compassresearch.ast.definitions.AChannelDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.c2c.CircusList;


public class CircusGenerator extends AnswerCMLAdaptor<CircusList> {
private final static String ANALYSIS_NAME = "Circus Generator";
	
	//Duplicated main overture handlers. Necessary for now since we don't want to switch visitor context at the root level
	public CircusGenerator(){
		super();
	}

//	@Override
//	public CircusList defaultPSingleDeclaration(PSingleDeclaration node) throws AnalysisException{
//		return node.apply(this);
//	}

	public CircusList caseAModuleModules(AModuleModules node,
			String question) throws AnalysisException
	{
		CircusList list = new CircusList();
		for (PDefinition def: node.getDefs()) {
			list.addAll(def.apply(this));
		}
		return list;
	}
	
//	@Override
//	public CircusList defaultPProcess(PProcess node) throws AnalysisException{
//		return node.apply(this);
//	}
//	
//	@Override
//	public CircusList defaultPAction(PAction node) throws AnalysisException{
//		return node.apply(this);
//	}
	
	
	@Override
	public CircusList defaultSClassDefinition(SClassDefinition node) throws AnalysisException {
		return new CircusList();
	}

	@Override
	public  CircusList defaultPStm(PStm node) throws AnalysisException{
		return new CircusList();//return node.apply(this);
	}
	
	@Override
	public CircusList defaultPExp(PExp node) throws AnalysisException{
		return new CircusList();//return node.apply(this);
	}
	
	@Override
	public CircusList defaultPDefinition(PDefinition node) throws AnalysisException
	{
		CircusList pol = new CircusList();
		pol.addAll(node.getClassDefinition().apply(this));
		return pol;
	}
	
	@Override
	public CircusList caseAProcessDefinition(AProcessDefinition node) throws AnalysisException{
		CircusList c = new CircusList();
		c.add("\\circprocess\\ " + node.getName() + " \\circdef \\circbegin \\\\ ");
		
		PProcess  process = node.getProcess();
		c.addAll(process.apply(this));
		
		c.add("\\circend ");
		return c;
	}
	
	@Override
	public CircusList caseAActionProcess(AActionProcess node) throws AnalysisException{
		CircusList c = new CircusList();
		//get subparts
		AActionClassDefinition scd = (AActionClassDefinition)node.getActionDefinition();
		LinkedList<PDefinition> pdef = scd.getDefinitions();
		c.add(pdef.toString() + "\\\\");
		PAction action = node.getAction();
		c.addAll(action.apply(this));
		c.add("\\circspot " + scd.getName() + "\\\\");
		return c;
	}
	
	@Override
	public CircusList caseASkipAction(ASkipAction node) throws AnalysisException{
		CircusList c = new CircusList();
		//c.add(node.toString());
		c.add("\\Skip");
		return c;
	}
	
	/**
	 * CML Channel Definition
	 */
	@Override
	public CircusList caseAChannelDefinition(AChannelDefinition node) throws AnalysisException{
		CircusList c = new CircusList();
		return c;
	}
	
	/**
	 * CML Chanset definition
	 */
	@Override
	public CircusList caseAChansetDefinition(AChansetDefinition node) throws AnalysisException {
		CircusList c = new CircusList();
		return c;
	}

	
	
	// ---------------------------------------------
	// -- Public API to CML C2C
	// ---------------------------------------------
	// Taken from Type Checker code
	// ---------------------------------------------
	/**
	 * This method is invoked by the command line tool when pretty printing the analysis name.
	 * 
	 * @return Pretty short name for this analysis.
	 */
	public String getAnalysisName()
	{
		return ANALYSIS_NAME;
	}

	@Override
	public CircusList createNewReturnValue(INode arg0)
			throws AnalysisException {
		return null;
	}

	@Override
	public CircusList createNewReturnValue(Object arg0)
			throws AnalysisException {
		return null;
	}
}
