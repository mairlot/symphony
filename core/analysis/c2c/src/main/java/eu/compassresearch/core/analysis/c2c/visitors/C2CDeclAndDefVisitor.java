package eu.compassresearch.core.analysis.c2c.visitors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;

import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;
import eu.compassresearch.ast.definitions.AChannelDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.ICircusList;

public class C2CDeclAndDefVisitor extends AnswerCMLAdaptor<CircusList> {
	
	final private AnswerCMLAdaptor<CircusList> parentC2C;
	
	public C2CDeclAndDefVisitor(AnswerCMLAdaptor<CircusList> parent){
		this.parentC2C = parent;
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
