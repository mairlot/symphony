package eu.compassresearch.ide.pog;

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
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligationList;

import eu.compassresearch.core.analysis.pog.obligations.CmlPOContextStack;
import eu.compassresearch.core.analysis.pog.visitors.ProofObligationGenerator;
import eu.compassresearch.ide.pog.view.PoListView;

public abstract class PogPluginUtils {

	public static IProofObligationList generateProofObligations(List<INode> ast)
			throws AnalysisException {
		IProofObligationList r = new ProofObligationList();
		for (INode node : ast) {
			r.addAll(node.apply(new ProofObligationGenerator(),
					new CmlPOContextStack()));
		}
		r.renumber();
		return r;
	}

	public static void popErrorMessage(IWorkbenchWindow window, String message) {
		MessageDialog.openError(window.getShell(), "Symphony POG",
				"Could not generate Proof Obligations.\n\n" + message);
	}

	public static void openPoviewPerspective(IWorkbenchSite site) {
		try {
			PlatformUI.getWorkbench().showPerspective(
					POConstants.PO_PERSPECTIVE_ID, site.getWorkbenchWindow());
		} catch (WorkbenchException e) {

			e.printStackTrace();
		}
	}

	public static PoListView getMainView() throws PartInitException {
		PoListView view = (PoListView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.showView(POConstants.PO_OVERVIEW_TABLE);
		return view;
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
