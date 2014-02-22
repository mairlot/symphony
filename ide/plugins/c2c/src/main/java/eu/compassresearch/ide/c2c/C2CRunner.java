package eu.compassresearch.ide.c2c;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.overture.ast.node.INode;

import eu.compassresearch.ide.core.resources.ICmlModel;
import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.ui.utility.CmlProjectUtil;
import eu.compassresearch.core.analysis.c2c.ICircusList;
import eu.compassresearch.core.analysis.c2c.CircusList;
import eu.compassresearch.core.analysis.c2c.utility.C2CPluginUtil;

public class C2CRunner {
	private IWorkbenchWindow window;
	private IWorkbenchSite site;
	private ICmlProject cmlProj;
	
	public void runC2C(){
		
		if (!CmlProjectUtil.typeCheck(this.window.getShell(), cmlProj)) {
			C2CPluginUtils.popErrorMessage(window, "Errors in model.");
		}
		
		final ICmlModel model = cmlProj.getModel();
		
		List<INode> ast = model.getAst();
	
		//Class<?> c = ast.getClass();
		
		/*try{
			PrintWriter out = new PrintWriter("astlog.txt");
			
			for(int i = 0; i <= ast.size(); i++){
				out.print("Number " + i + ": \n" + ast.get(i).toString() + "\n");
			}
			
			out.print("BREAK - BREAK");
			out.print("Length of AST:");
			out.print(ast.size());
			C2CPluginUtils.popErrorMessage(window, String.valueOf(ast.size()));
			
			out.close();
		} catch(FileNotFoundException e){
			C2CPluginUtils.popErrorMessage(window, "astlog.txt not found.");
		} catch(ArrayIndexOutOfBoundsException e){
		}
		*/
		
	
		ICircusList circList = genCircus(model);	
		
		if(!circList.isEmpty()){
			createCircusFile(circList);
		}
	}
	
	public ICircusList genCircus(ICmlModel model){
		ICircusList circList = new CircusList();
		
		try{
			circList = C2CPluginUtil.generateCircus(model.getAst());
			return circList;
		} catch (Exception e){
			C2CPluginUtils.popErrorMessage(window, "Internal C2C error.");
			e.printStackTrace();
			return circList;
		}
	}
	
	public void createCircusFile(ICircusList cList){
		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		File circus = new File(workspacePath.concat("circusfinal.txt"));
		try {
			circus.createNewFile();
		} catch (IOException e) {
			C2CPluginUtils.popErrorMessage(window,"Error: The Circus file could not be created.");
			e.printStackTrace();
		}
		if(!circus.canWrite()){
			C2CPluginUtils.popErrorMessage(window,"Error: The Circus file cannot be written to.");
			circus.setWritable(true);	
		} else {
			FileWriter writer;
			try {
				writer = new FileWriter(circus);
				int i = 0;
				while(i < cList.size()){
					writer.write(cList.get(i));
					i++;
				}
			} catch (IOException e) {
				C2CPluginUtils.popErrorMessage(window,  "Error: The Circus FileWriter has malfunctioned.");
				e.printStackTrace();
			}
			
		}
		
	}
		
	
	
	public C2CRunner(IWorkbenchWindow window, IWorkbenchSite site,
			ICmlProject cmlproj) {
		this.cmlProj = cmlproj;
		this.window = window;
		this.site = window.getActivePage().getActivePart().getSite();
	}

}
