package eu.compassresearch.ide.c2c;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.overture.ast.node.INode;

import eu.compassresearch.ide.core.resources.ICmlModel;
import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.ui.utility.CmlProjectUtil;
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
		
	
		CircusList circList = genCircus(model);	
		
		if(!circList.isEmpty()){
			C2CPluginUtils.popInformationMessage(window, "About to create Circus file.");
			circList.add("End");
			C2CPluginUtils.popInformationMessage(window, "cList contains: " + circList.get(0));
			writeToCircusFile(circList,circusFile);
		}
		else {
			C2CPluginUtils.popErrorMessage(window, "CircusList is empty! :(");
		}
		
		endCircusFile(circusFile);
		C2CPluginUtils.popInformationMessage(window, "Plugin execution completed.");
	}
	
	public CircusList genCircus(ICmlModel model){
		CircusList circList = new CircusList();
		
		try{
			circList = C2CPluginUtil.generateCircus(model.getAst());
			return circList;
		} catch (Exception e){
			C2CPluginUtils.popErrorMessage(window, "Internal C2C error.");
			e.printStackTrace();
			return circList;
		}
	}
	
	public void writeToCircusFile(CircusList cList, File file){ 
		try {
			FileWriter writer = new FileWriter(file, true);
			int i = 0;
			if(cList == null) {
				C2CPluginUtils.popErrorMessage(window, "cList is null");
			}
			while(i <= cList.size()){
				C2CPluginUtils.popInformationMessage(window, "Circus Line: \n " + cList.get(i).toString());
				writer.write(cList.get(i).toString());
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
		String projectPath = C2CPluginUtils.getCurrentlySelectedProject().getLocation().toString();
		
		IProject project = C2CPluginUtils.getCurrentlySelectedProject();
		IFolder folder = project.getFolder("gen");
		if (!folder.exists()) {
			try {
				folder.create(false,  true,  null);
			} catch (CoreException e1) {
				C2CPluginUtils.popErrorMessage(window, "Error: Folder \"translated\" could not be created.");
			}
		}
		File circus = new File(projectPath.concat("/translated/"+project.getName()+".tex"));
		if(!circus.canWrite()){
			C2CPluginUtils.popErrorMessage(window, "Error: The Circus file cannot be written to.");
			circus.setWritable(true);	
		} else {
			try{
				FileWriter creator = new FileWriter(circus);
				creator.write("\\begin{circus} \n");
				creator.close();
			} catch(IOException e){
				C2CPluginUtils.popErrorMessage(window, "Unable to begin Circus File.");
			}
	
		}
		C2CPluginUtils.popInformationMessage(window, "File creation completed.");
		return circus;
	}
		
	public void endCircusFile(File file){
		try{
			FileWriter writer = new FileWriter(file, true);
			writer.write("\n\\end{circus}");
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
