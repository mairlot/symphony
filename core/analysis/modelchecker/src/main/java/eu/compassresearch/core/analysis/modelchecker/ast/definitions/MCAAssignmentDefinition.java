package eu.compassresearch.core.analysis.modelchecker.ast.definitions;

import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCPCMLType;

public class MCAAssignmentDefinition implements MCPCMLDefinition {

	private String name;
	private MCPCMLExp expression;
	private MCPCMLType expType;
	
	
	public MCAAssignmentDefinition(String name, MCPCMLExp expression,
			MCPCMLType expType) {
		super();
		this.name = name;
		this.expression = expression;
		this.expType = expType;
	}


	@Override
	public String toFormula(String option) {
		StringBuilder result = new StringBuilder();
		result.append("var(\""+getName()+"\",\"");
		result.append(getExpType().toFormula(option));
		result.append("\",");
		return result.toString();
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public MCPCMLExp getExpression() {
		return expression;
	}


	public void setExpression(MCPCMLExp expression) {
		this.expression = expression;
	}


	public MCPCMLType getExpType() {
		return expType;
	}


	public void setExpType(MCPCMLType expType) {
		this.expType = expType;
	}

	
}
