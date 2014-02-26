package eu.compassresearch.core.analysis.c2c.visitors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;

import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.PAction;
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
