package eu.compassresearch.core.analysis.c2c.visitors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.expressions.PExp;
import org.overture.ast.node.INode;

import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.ICircusList;
import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;

public class C2CExpressionVisitor extends AnswerCMLAdaptor<CircusList> {
	
	private CircusGenerator parentC2C;
	
	//default
	@Override
	public CircusList defaultPExp(PExp node) throws AnalysisException{
		CircusList c = new CircusList();
		c.addAll(node.apply(parentC2C));
		
		return c;
	}
	
	//Call the main c2c when it's not an expression
	@Override
	public CircusList defaultINode(INode node) throws AnalysisException {
		CircusList c = new CircusList();
		c.addAll(node.apply(parentC2C));
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
