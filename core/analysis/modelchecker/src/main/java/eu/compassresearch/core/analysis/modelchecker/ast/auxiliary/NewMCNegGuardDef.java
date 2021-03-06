package eu.compassresearch.core.analysis.modelchecker.ast.auxiliary;

import eu.compassresearch.core.analysis.modelchecker.ast.MCNode;
import eu.compassresearch.core.analysis.modelchecker.ast.actions.MCPAction;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.ast.statements.MCPCMLStm;

public class NewMCNegGuardDef extends NewMCGuardDef{

	
	public NewMCNegGuardDef(int counterId, MCPCMLExp condition, MCPCMLStm action) {
		super(counterId, condition, action);
	}
	
	@Override
	public String toFormula(String option) {

		StringBuilder result = new StringBuilder();
		result.append("  guardNDef(" + this.counterId +","+ max.toFormula(option)+")");
		result.append(" :- State(" + max.toFormula(option) + "," + parentStm.toFormula(MCNode.DEFAULT) + ")");
		result.append(",");
		MCPCMLExp negCondition = ExpressionNegator.negate(condition); 
		result.append(negCondition.toFormula(option));
		result.append(".\n");
		
		return result.toString();

	}

	
}
