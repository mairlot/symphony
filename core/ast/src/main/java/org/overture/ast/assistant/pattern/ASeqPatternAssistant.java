package org.overture.ast.assistant.pattern;

import org.overture.ast.lex.LexNameList;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.PPattern;


public class ASeqPatternAssistant {

	
	public static LexNameList getAllVariableNames(ASeqPattern pattern) {
		LexNameList list = new LexNameList();

		for (PPattern p: pattern.getPlist())
		{
			list.addAll(PPatternAssistant.getAllVariableNames(p));
		}

		return list;
	}

	
}
