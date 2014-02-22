package eu.compassresearch.core.analysis.c2c.visitors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.ast.statements.PStm;

import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;
import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.ICircusList;

public class C2CStatementVisitor extends AnswerCMLAdaptor<CircusList> {
	
	private CircusGenerator parentC2C;
	
	//default
	@Override
	public CircusList defaultPStm(PStm node) throws AnalysisException {
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
