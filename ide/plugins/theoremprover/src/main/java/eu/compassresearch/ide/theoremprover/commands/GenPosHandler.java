package eu.compassresearch.ide.theoremprover.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.pog.POConstants;
import eu.compassresearch.ide.pog.PogPluginUtils;
import eu.compassresearch.ide.pog.view.PoListView;
import eu.compassresearch.ide.theoremprover.ProofSess;
import eu.compassresearch.ide.theoremprover.TPConstants;
import eu.compassresearch.ide.theoremprover.TPPluginDoStuff;
import eu.compassresearch.ide.theoremprover.TPPluginUtils;

public class GenPosHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICmlProject proj = null;

		String needs = event.getParameter(TPConstants.NEEDS_PROJECT_PARAM_ID);
		if (needs.equals("yes")) {
			IProject proj_ = TPPluginUtils.getCurrentlySelectedProject();
			proj = (ICmlProject) proj_.getAdapter(ICmlProject.class);

		} else {

			try {
				PoListView view = PogPluginUtils.getMainView();
				proj = view.getProject();
			} catch (PartInitException e) {
				MessageDialog.openInformation(HandlerUtil
						.getActiveWorkbenchWindow(event).getShell(),
						"Symphony Internal Error",
						"Could not load the POG View.");
				e.printStackTrace();
			}
		}

		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage();
		TPPluginDoStuff doer = new TPPluginDoStuff(
				HandlerUtil.getActiveWorkbenchWindow(event), page
						.getActivePart().getSite());
		doer.dischargeClassic(proj);
		

		// open Isabelle perspective
		try {
			PlatformUI.getWorkbench().showPerspective(
					TPConstants.ISABELLE_PERSPECTIVE_ID, HandlerUtil.getActiveWorkbenchWindow(event));
		} catch (WorkbenchException e) {

			e.printStackTrace();
		}
		
		return null;
	}
}
