package eu.compassresearch.ide.c2c.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.c2c.C2CRunner;
import eu.compassresearch.ide.c2c.C2CPluginUtils;

public class GenerateCircusHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		IProject proj = C2CPluginUtils.getCurrentlySelectedProject();

		if (proj == null)
		{
			C2CPluginUtils.popErrorMessage(window,"No project selected.");
			return null;
		}
		
		ICmlProject cmlProj = (ICmlProject) proj.getAdapter(ICmlProject.class);
		
		C2CRunner doer = new C2CRunner(window, HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart().getSite(), cmlProj);
		doer.runC2C();

		return null;
	}
}
