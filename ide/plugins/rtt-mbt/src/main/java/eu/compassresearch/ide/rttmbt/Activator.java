package eu.compassresearch.ide.rttmbt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.compassresearch.rttMbtTmsClientApi.IRttMbtProgressBar;
import eu.compassresearch.rttMbtTmsClientApi.RttMbtClient;

public class Activator implements BundleActivator {

	// RTT-MBT client attribute
	static private RttMbtClient client;
	static private RttMbtProgressMonitor progressMonitor;

	// currently selected element in the projectExplorer
	static TreeSelection projectExplorerSelectedElement = null;

	static public RttMbtClient getClient() {
		return client;
	}

	static public TreeSelection getCurrentProjectExplorerItem() {
		return projectExplorerSelectedElement;
	}

	static public void setCurrentProjectExplorerItem(TreeSelection selection) {
		projectExplorerSelectedElement = selection;
	}

	// progress bars
	static RttMbtProgressBar progressBars;
	
	static public void setConsole(org.eclipse.swt.widgets.Text t) {
		if (client != null) {
			RttMbtConsoleLogger consoleLogger = new RttMbtConsoleLogger();
	    	consoleLogger.setConsole(t);
	    	client.setLoggingFacility(client.getWorkspaceProjectName(), consoleLogger);
		}
	}
	
	static public void addProgressBar(IRttMbtProgressBar.Tasks task, org.eclipse.swt.widgets.ProgressBar bar) {
		if (client != null) {
			if (progressBars == null) {
				progressBars = new RttMbtProgressBar();
			}
			progressBars.addProgressBar(task, bar);
	    	client.setProgressBar(progressBars);
		}
	}

	static public void setMonitor(IProgressMonitor m) {
		if (progressMonitor != null) {
			progressMonitor.setMonitor(m);
		}
	}

	public Activator() {
	}

    @Override
    public void start(BundleContext context) throws Exception
      {
    	// create client
		@SuppressWarnings("deprecation")
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    	client = new RttMbtClient(store.getString("RttMbtServer"),
    							  store.getInt("RttMbtServerPort"),
    							  store.getString("RttMbtUserName"),
    							  store.getString("RttMbtUserId"));
    	if ((store.getString("RttMbtRttTprocPrefix") != null) &&
    		(store.getString("RttMbtRttTprocPrefix").compareTo("") != 0)) {
    		client.setRttMbtTestProcFolderName(store.getString("RttMbtRttTprocPrefix"));
    	}
    	if ((store.getString("RttMbtTProcGenCtx") != null) &&
    		(store.getString("RttMbtTProcGenCtx").compareTo("") != 0)) {
    		client.setRttMbtTProcGenCtxFolderName(store.getString("RttMbtTProcGenCtx"));
        }
    	client.setMode(store.getString("ClientMode"));
    	client.setVerboseLogging(store.getBoolean("RttMbtLogVerbose"));
    	client.setExtraFiles(store.getBoolean("RttMbtExtraFiles"));
    	progressMonitor = new RttMbtProgressMonitor();
    	client.setProgressMonitor(progressMonitor);

    	// Add the listener once the workbench is fully started
    	Display.getDefault().asyncExec(new Runnable() {
    	    @Override
    	    public void run() {
    	        // wait until the workbench has been initialized
    	        if (PlatformUI.getWorkbench().isStarting()) {
        	        // if the workbench is still starting, reschedule the execution
    	            Display.getDefault().timerExec(1000, this);
    	        } else { 
    	            // the workbench finished the initialization process 
    	            IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	            RttMbtPerspectiveAdapter perspectiveListener = new RttMbtPerspectiveAdapter();
    	            workbenchWindow.addPerspectiveListener(perspectiveListener);
    	 
    	            // update the PerspectiveListener with the current perspective
    	            perspectiveListener.perspectiveActivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
    	                                                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective());
    	        }
    	    }
    	});
    }
    
    @Override
    public void stop(BundleContext arg0) throws Exception
      {

    	// store settings
    	
      }

	public static IWorkbench getDefault() {
		return PlatformUI.getWorkbench();
	}

	public static void updatePreferences() {
		System.out.println("Activator.updatePreferences");
		if (client != null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			client.setRttMbtServer(store.getString("RttMbtServer"));
			client.setRttMbtPort(store.getInt("RttMbtServerPort"));
			client.setUserName(store.getString("RttMbtUserName"));
			client.setUserId(store.getString("RttMbtUserId"));
			if ((store.getString("RttMbtRttTprocPrefix") != null) &&
				(store.getString("RttMbtRttTprocPrefix").compareTo("") != 0)) {
				client.setRttMbtTestProcFolderName(store.getString("RttMbtRttTprocPrefix"));
			}
			if ((store.getString("RttMbtTProcGenCtx") != null) &&
				(store.getString("RttMbtTProcGenCtx").compareTo("") != 0)) {
				client.setRttMbtTProcGenCtxFolderName(store.getString("RttMbtTProcGenCtx"));
			}
	    	client.setMode(store.getString("ClientMode"));
	    	client.setVerboseLogging(store.getBoolean("RttMbtLogVerbose"));
	    	client.setExtraFiles(store.getBoolean("RttMbtExtraFiles"));
		}
	}

	public static String getPreferenceValue(String key) {
		String value = null;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		value = store.getString(key);
		return value;
	}
  }
