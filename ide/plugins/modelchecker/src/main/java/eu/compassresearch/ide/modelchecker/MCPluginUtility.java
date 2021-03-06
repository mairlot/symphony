package eu.compassresearch.ide.modelchecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class MCPluginUtility {
	
	private IWorkbenchSite site;
	
	public MCPluginUtility(IWorkbenchSite s) {
		this.site = s;
	}
	
	public void openMcviewPerspective()
	{
		try
		{
			PlatformUI.getWorkbench().showPerspective(MCConstants.MC_PERSPECTIVE_ID, site.getWorkbenchWindow());
		} catch (WorkbenchException e)
		{

			e.printStackTrace();
		}
	}

	public static ArrayList<IResource> getAllCFilesInProject(IProject project) {
		ArrayList<IResource> allCFiles = new ArrayList<IResource>();
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
				.getRoot();

		IPath path = project.getLocation();

		recursiveFindCMLFiles(allCFiles, path, myWorkspaceRoot);
		return allCFiles;
	}
	

	private static void recursiveFindCMLFiles(ArrayList<IResource> allCMLFiles,
			IPath path, IWorkspaceRoot myWorkspaceRoot) {
		IContainer container = myWorkspaceRoot.getContainerForLocation(path);

		try {
			IResource[] iResources;
			iResources = container.members();
			for (IResource iR : iResources) {
				// for c files
				if ("cml".equalsIgnoreCase(iR.getFileExtension()))
					allCMLFiles.add(iR);
				if (iR.getType() == IResource.FOLDER) {
					IPath tempPath = iR.getLocation();
					recursiveFindCMLFiles(allCMLFiles, tempPath,
							myWorkspaceRoot);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static IProject getCurrentlySelectedProject() {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (window != null) {
			IStructuredSelection selection = (IStructuredSelection) window
					.getSelectionService().getSelection(
							"eu.compassresearch.ide.ui.CmlNavigator");
			IResource res = extractSelection(selection);
			if (res != null ) {
				IProject project = res.getProject();
				return project;
			}
		}
		return null;
	}

	static IResource extractSelection(ISelection sel) {
		if (!(sel instanceof IStructuredSelection))
			return null;
		IStructuredSelection ss = (IStructuredSelection) sel;
		Object element = ss.getFirstElement();
		if (element instanceof IResource)
			return (IResource) element;
		if (!(element instanceof IAdaptable))
			return null;
		IAdaptable adaptable = (IAdaptable) element;
		Object adapter = adaptable.getAdapter(IResource.class);
		return (IResource) adapter;
	}

	public static void writeToFile(String filePath, StringBuilder content) throws IOException {
		 File realFile = new File(filePath);
		 boolean created = false;
		
		 if(!realFile.exists()){
			 created = realFile.createNewFile();
		 }
		 FileWriter fw = new FileWriter(realFile);
		 BufferedWriter bw = new BufferedWriter(fw);
		 bw.write(content.toString());
		 bw.flush();
		 fw.close();
		 bw.close();
	 }
	public static void writeToFile(File file, StringBuilder content) throws IOException {
		 File parentFolder = file.getParentFile();
		 if(!parentFolder.exists()){
			 parentFolder.mkdir();
		 }
		 if(!file.exists()){
			 file.createNewFile();
		 }
		 FileWriter fw = new FileWriter(file);
		 BufferedWriter bw = new BufferedWriter(fw);
		 bw.write(content.toString());
		 bw.flush();
		 fw.close();
		 bw.close();
	 }
}
