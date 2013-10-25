package eu.compassresearch.core.analysis.theoremprover.visitors.deps;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.node.INode;
import org.overture.ast.types.PType;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.core.analysis.theoremprover.thms.NodeNameList;

@SuppressWarnings("serial")
public class ThmValueDepVisitor  extends
QuestionAnswerCMLAdaptor<NodeNameList, NodeNameList>{
	
	final private QuestionAnswerCMLAdaptor<NodeNameList, NodeNameList> thmDepVisitor;
	
	public ThmValueDepVisitor(ThmDepVisitor thmDepVisitor) {
		this.thmDepVisitor = thmDepVisitor;
	}
	
	/**
	 * Return the dependencies of a value AST node
	 * @param node - the value definition
	 * @return the list of dependencies
	 */
	public NodeNameList caseAValueDefinition(AValueDefinition node, NodeNameList bvars)
			throws AnalysisException {
		NodeNameList nodeDeps = new NodeNameList();
		
		PType valTp = node.getType();
		nodeDeps.addAll(valTp.apply(thmDepVisitor, bvars));

		PExp valExp = node.getExpression();
		nodeDeps.addAll(valExp.apply(thmDepVisitor, bvars));

		return nodeDeps;
	}

	@Override
	public NodeNameList createNewReturnValue(INode arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeNameList createNewReturnValue(Object arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}
}
