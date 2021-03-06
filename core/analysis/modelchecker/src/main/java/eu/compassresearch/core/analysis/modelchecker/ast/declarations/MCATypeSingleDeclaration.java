package eu.compassresearch.core.analysis.modelchecker.ast.declarations;

import eu.compassresearch.core.analysis.modelchecker.ast.types.MCPCMLType;

public class MCATypeSingleDeclaration implements MCPSingleDeclaration {

	private String identifier;
	private MCPCMLType type;
	
	
	public MCATypeSingleDeclaration(String identifier,
			MCPCMLType type) {
		this.identifier = identifier;
		this.type = type;
	}


	@Override
	public String toFormula(String option) {
		StringBuilder result = new StringBuilder();
		result.append("Int(");
		result.append(this.getIdentifier());
		result.append(")");

		return result.toString();
	}


	

	public String getIdentifier() {
		return identifier;
	}


	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	public MCPCMLType getType() {
		return type;
	}


	public void setType(MCPCMLType type) {
		this.type = type;
	}

	
}
