package eu.compassresearch.core.analysis.c2c.visitors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.node.INode;
import org.overture.ast.statements.PStm;

import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.ICircusList;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.AnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.PSingleDeclaration;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.ast.program.AFileSource;
import eu.compassresearch.ast.program.AInputStreamSource;
import eu.compassresearch.ast.program.PSource;


public class CircusGenerator extends AnswerCMLAdaptor<CircusList> {
	
	private final static String ANALYSIS_NAME = "Circus Generator";
	
	//subvisitors
	private C2CExpressionVisitor expressionVisitor;
	private C2CStatementVisitor statementVisitor;
	private C2CProcessVisitor processVisitor;
	private C2CDeclAndDefVisitor declAndDefVisitor;
	private C2CActionVisitor actionVisitor;
	
	private void initialize(){
		expressionVisitor = new C2CExpressionVisitor();
		statementVisitor = new C2CStatementVisitor();
		processVisitor = new C2CProcessVisitor(this);
		declAndDefVisitor = new C2CDeclAndDefVisitor(this);
		actionVisitor = new C2CActionVisitor(this);
	}
	
	//Duplicated main overture handlers. Necessary for now since we don't want to switch visitor context at the root level
	public CircusGenerator(){
		this.initialize();
	}
	
	@Override
	public CircusList defaultPDefinition(PDefinition node) throws AnalysisException {
		return node.apply(this.declAndDefVisitor);
	}
	
	@Override
	public CircusList defaultPSingleDeclaration(PSingleDeclaration node) throws AnalysisException{
		return node.apply(this.declAndDefVisitor);
	}
	
	@Override
	public CircusList defaultPProcess(PProcess node) throws AnalysisException{
		return node.apply(this.processVisitor);
	}
	
	@Override
	public CircusList defaultPAction(PAction node) throws AnalysisException{
		return node.apply(this.actionVisitor);
	}
	
	@Override
	public  CircusList defaultPStm(PStm node) throws AnalysisException{
		return node.apply(this.statementVisitor);
	}
	
	@Override
	public CircusList defaultPExp(PExp node) throws AnalysisException{
		return node.apply(this.expressionVisitor);
	}
	
	
	
	// ---------------------------------------------
	// -- Public API to CML C2C
	// ---------------------------------------------
	// Taken from Type Checker code
	// ---------------------------------------------
	/**
	 * This method is invoked by the command line tool when pretty printing the analysis name.
	 * 
	 * @return Pretty short name for this analysis.
	 */
	public String getAnalysisName()
	{
		return ANALYSIS_NAME;
	}
	
//	/**
//	 * Run the Circus generator. The Circus code is placed in the return value but we may eventually want to switch
//	 * them over to the registry
//	 * 
//	 * @param sources
//	 *            The list of definition to generate obligations for
//	 * @return - Returns CMLProofObligation list. This may need to change.
//	 */
//	public CircusList generateCircusCode(List<PDefinition> sources) throws AnalysisException{
//		this.initialize();
//		CircusList circus = new CircusList();
//		
//		//for each CML paragraph
//		for (PDefinition paragraph : sources){
//			try{
//				circus.addAll(paragraph.apply(this));
//			} catch(AnalysisException ae){
//				//unexpected c2c crash
//				throw ae;
//			}
//		}
//		return circus;
//	}
//	
//	// ---------------------------------------------
//	// Static stuff for running the C2C from Eclipse
//	// ---------------------------------------------
//	// Taken from Type Checker code
//	// ---------------------------------------------
//
//	// setting the file on AFileSource allows the POG to interact with it
//	// TODO this method is a duplicate( from VanillaTypeChecker). Should be
//	// placed in a common utils lib
//	private static PSource prepareSource(File f)
//	{
//		if (f == null)
//		{
//			AInputStreamSource iss = new AInputStreamSource();
//			iss.setStream(System.in);
//			iss.setOrigin("stdin");
//			return iss;
//		} else
//		{
//			AFileSource fs = new AFileSource();
//			fs.setName(f.getName());
//			fs.setFile(f);
//			return fs;
//		}
//	}
//	
//	/**
//	 * This method runs the Circus generator on a given file. The method invokes methods to generate POs.
//	 * 
//	 * @param f
//	 *            - The file to generate POs
//	 */
//	// TODO this method is a duplicate( from VanillaTypeChecker). Should be
//	// placed in a common utils lib
//	private static void runOnFile(File f) throws IOException
//	{
//		// set file name
//		PSource source = prepareSource(f);
//
//		// generate POs
//		CircusGenerator cmlC2C = new CircusGenerator();
//		try
//		{
//			cmlC2C.generateCircusCode(source.getParagraphs());
//		} catch (AnalysisException e)
//		{
//			System.out.println("The Symphony Circus Generator failed on this cml-source.\n");
//			e.printStackTrace();
//		}
//
//		// Report success
//		System.out.println("Circus Generation is complete for the given CML Program");
//	}
//	
//	/**
//	 * Main method for the class. Current test class takes a set of cml examples and generates Circus code for each
//	 */
//	// TODO the body of this method is a duplicate (From VanillaTypeChecker)
//	public static void main(String[] args) throws IOException{
//		File cml_examples = new File("../../docs/cml-examples");
//		int failures = 0;
//		int successes = 0;
//		// runOnFile(null);
//
//		if (cml_examples.isDirectory())
//		{
//			for (File example : cml_examples.listFiles())
//			{
//				System.out.print("Generating Circus for example: "
//						+ example.getName() + " \t\t...: ");
//				System.out.flush();
//				try
//				{
//					runOnFile(example);
//					System.out.println("done");
//					successes++;
//				} catch (Exception e)
//				{
//					System.out.println("exception");
//					failures++;
//				}
//			}
//		}	
//	}

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
