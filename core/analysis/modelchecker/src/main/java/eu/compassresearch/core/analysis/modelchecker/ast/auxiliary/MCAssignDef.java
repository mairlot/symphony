package eu.compassresearch.core.analysis.modelchecker.ast.auxiliary;

import eu.compassresearch.core.analysis.modelchecker.ast.MCNode;
import eu.compassresearch.core.analysis.modelchecker.ast.actions.MCPAction;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.graphBuilder.binding.Binding;
import eu.compassresearch.core.analysis.modelchecker.visitors.NewCMLModelcheckerContext;

public class MCAssignDef implements MCNode {

	protected Binding max;
	protected int counterId;
	protected MCPCMLExp expression;
	private MCPCMLExp stateDesignator;
	protected MCPAction parentAction;
	
	
	public MCAssignDef(int counterId, MCPCMLExp expression, MCPCMLExp stateDesignator,
			MCPAction parentAction) {
		super();
		this.max = NewCMLModelcheckerContext.getInstance().maximalBinding;
		this.counterId = counterId;
		this.expression = expression;
		this.stateDesignator = stateDesignator;
		this.parentAction = parentAction;
	}


	@Override
	public String toFormula(String option) {
		StringBuilder result = new StringBuilder();
		
		result.append("assignDef(0, "+ counterId + ", st, st_)  :- State(0,st,name,assign("+ counterId +")),");
		result.append("st = "); 
		result.append(max.toFormula(MCNode.NAMED)); //with variable names
		result.append("st_ = ");
		Binding maxCopy = max.copy();
		String newValueVarName = stateDesignator.toFormula(option) + "_";
		//maxCopy.updateBinding(variableName,newValueVarName); //this needs to be adjusted
		result.append(maxCopy.toFormula(MCNode.NAMED)); //with variable names but values changed by the assignment
		
		//THE EXPRESSION OF THE ASSIGNMENT
		result.append(",");
		result.append(newValueVarName + " = " + expression.toFormula(option)); //expression assignment
		
		
		return result.toString();
	}


	public Binding getMax() {
		return max;
	}


	public void setMax(Binding max) {
		this.max = max;
	}


	public int getCounterId() {
		return counterId;
	}


	public void setCounterId(int counterId) {
		this.counterId = counterId;
	}


	public MCPCMLExp getExpression() {
		return expression;
	}


	public void setExpression(MCPCMLExp expression) {
		this.expression = expression;
	}


	public MCPAction getParentAction() {
		return parentAction;
	}


	public void setParentAction(MCPAction parentAction) {
		this.parentAction = parentAction;
	}


	public MCPCMLExp getStateDesignator() {
		return stateDesignator;
	}


	public void setStateDesignator(MCPCMLExp stateDesignator) {
		this.stateDesignator = stateDesignator;
	}

	
	
}
