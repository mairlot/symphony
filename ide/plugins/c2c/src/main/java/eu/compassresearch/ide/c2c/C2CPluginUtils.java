package eu.compassresearch.ide.c2c;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.overture.ast.node.INode;


public abstract class C2CPluginUtils {
	
	/*public static CircModel generateCircus(List<INode> ast) throws AnalysisException{
		CircModel r = new CircModel();
		for (INode node : ast) {
			r.addAll(node.apply(new CircusGenerator()));
		}
		return r;
	}*/

	public static void popErrorMessage(IWorkbenchWindow window, String message) {
		MessageDialog.openError(window.getShell(), "Symphony C2C",
				"Could not generate Circus.\n\n" + message);
	}

	public static IProject getCurrentlySelectedProject() {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (window != null) {
			IStructuredSelection selection = (IStructuredSelection) window
					.getSelectionService().getSelection(
							"eu.compassresearch.ide.ui.CmlNavigator");
			IResource res = extractSelection(selection);
			if (res != null) {
				IProject project = res.getProject();
				return project;
			}
		}
		return null;
	}

	private static IResource extractSelection(ISelection sel) {
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

}
