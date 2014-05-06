package eu.compassresearch.core.analysis.c2c.utility;

import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;

import eu.compassresearch.core.analysis.c2c.ICircusList;
import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.visitors.CircusGenerator;

public class C2CPluginUtil {
	
	public static ICircusList generateCircus(List<INode> ast) throws AnalysisException {
		CircusList r = new CircusList();
		CircusGenerator gen = new CircusGenerator();
		for (INode node : ast) {
			ICircusList aux = node.apply(gen);
			r.addAll(aux);
		}
		return r;
	}
	

}
