package eu.compassresearch.core.analysis.c2c.visitors;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.node.INode;

import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.ast.actions.AChaosAction;
import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.AStopAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.actions.PCommunicationParameter;
import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;

public class C2CActionVisitor extends AnswerCMLAdaptor<CircusList> {
	
	private CircusGenerator parentC2C;
	
	/**
	 * Constructor - Initialise parent C2C
	 * 
	 * @param parent
	 */
	public C2CActionVisitor(CircusGenerator parent){
		this.parentC2C = parent;
	}
	
	//Default action
	@Override
	public CircusList defaultPAction(PAction node) throws AnalysisException{
		CircusList c = new CircusList();
		return c;
	}
	
	//Call the main c2c when it's not a statement 
	@Override
	public CircusList defaultINode(INode node) throws AnalysisException{
		CircusList c = new CircusList();
		c.addAll(node.apply(parentC2C));
		return c;
	}
	
	@Override
	public CircusList caseASkipAction(ASkipAction node) throws AnalysisException{
		CircusList c = new CircusList();
		//c.add(node.toString());
		c.add("\\Skip");
		return c;
	}
	
	@Override
	public CircusList caseAStopAction(AStopAction node) throws AnalysisException{
		CircusList c = new CircusList();
		c.add("\\Stop");
		return c;
	}
	
	@Override
	public CircusList caseAChaosAction(AChaosAction node) throws AnalysisException{
		CircusList c = new CircusList();
		c.add("\\Chaos");
		return c;
	}
	
	@Override
	public CircusList caseACommunicationAction(ACommunicationAction node) throws AnalysisException{
		CircusList c = new CircusList();
		
		//get subparts
		PAction action = node.getAction();
		c.addAll(action.apply(parentC2C));
		LinkedList<PCommunicationParameter> comparameters = node.getCommunicationParameters();
		ILexIdentifierToken identifier = node.getIdentifier();
		c.add(node.getIdentifier().getName() + node.getCommunicationParameters() + " -> " + node.getAction());
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
