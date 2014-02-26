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
		
		File circusFile = createCircusFile();
		
		final ICmlModel model = cmlProj.getModel();
		
		//List<INode> ast = model.getAst();
	
		//Class<?> c = ast.getClass();
		
		//C2CPluginUtils.popErrorMessage(window, "Error: " + System.getProperty("java.class.path"));
		
	
		ICircusList circList = genCircus(model);	
		
		if(!circList.isEmpty()){
			C2CPluginUtils.popInformationMessage(window, "About to create Circus file.");
			writeToCircusFile(circList,circusFile);
		}
		
		endCircusFile(circusFile);
		C2CPluginUtils.popInformationMessage(window, "Plugin execution completed.");
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
	
	public void writeToCircusFile(ICircusList cList, File file){
		try {
			FileWriter writer = new FileWriter(file);
			int i = 0;
			while(i < cList.size()){
				writer.write(cList.get(i));
				writer.write("\n");
				i++;
			}
			writer.close();
		} catch (IOException e) {
			C2CPluginUtils.popErrorMessage(window,  "Error: The Circus FileWriter has malfunctioned.");
			e.printStackTrace();
		}
	}
	
	public File createCircusFile(){
		C2CPluginUtils.popInformationMessage(window, "Creating file...");
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
			try{
				FileWriter creator = new FileWriter(circus);
				creator.write("\\begin{circus} \n");
				creator.close();
			} catch(IOException e){
				C2CPluginUtils.popErrorMessage(window, "Unable to beign Circus File.");
			}
			
			
		}
		C2CPluginUtils.popInformationMessage(window, "File completed.");
		return circus;
	}
		
	public void endCircusFile(File file){
		try{
			FileWriter writer = new FileWriter(file);
			writer.write("\\end{circus}");
			writer.close();
		} catch(IOException e){
			C2CPluginUtils.popErrorMessage(window, "Unable to end Circus File.");
		}
	}
	
	public C2CRunner(IWorkbenchWindow window, IWorkbenchSite site,
			ICmlProject cmlproj) {
		this.cmlProj = cmlproj;
		this.window = window;
		this.site = window.getActivePage().getActivePart().getSite();
	}

}
