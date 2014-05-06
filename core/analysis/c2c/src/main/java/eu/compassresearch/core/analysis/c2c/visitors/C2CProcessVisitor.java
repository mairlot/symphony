package eu.compassresearch.core.analysis.c2c.visitors;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.node.INode;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.c2c.CircusList;

public class C2CProcessVisitor extends AnswerCMLAdaptor<CircusList> {
	
	private CircusGenerator parentC2C;
	
	public C2CProcessVisitor(CircusGenerator parent){
		this.parentC2C = parent;
	}

	@Override
	public CircusList defaultPProcess(PProcess node) throws AnalysisException{
		CircusList  c = new CircusList();
		return c;
	}
	
	@Override
	public CircusList caseAActionProcess(AActionProcess node) throws AnalysisException{
		CircusList c = new CircusList();
		
		//get subparts
		PAction action = node.getAction();
		c.addAll(action.apply(parentC2C));
		c.add("\\circspot " + node.getActionDefinition().getName().toString() + " \\\\");
		return c;
	}
	
	@Override
	public CircusList createNewReturnValue(INode arg0)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CircusList createNewReturnValue(Object arg0)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}

}
