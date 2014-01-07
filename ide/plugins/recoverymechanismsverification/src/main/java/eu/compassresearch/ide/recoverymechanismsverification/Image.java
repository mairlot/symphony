/**
 * 
 */
package eu.compassresearch.ide.recoverymechanismsverification;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;

/**
 * @author Andr&eacute; Didier (<a href=
 *         "mailto:alrd@cin.ufpe.br?Subject=Package eu.compassresearch.ide.recoverymechanismsverification, class Image"
 *         >alrd@cin.ufpe.br</a>)
 * 
 */
public enum Image {
	RELOAD;

	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(RMVActivator.getDefault()
				.getImageRegistry().get(name()));
	}

	public void updateImageDescriptor(ImageRegistry reg, String iconFileName) {
		Bundle bundle = RMVActivator.getDefault().getBundle();
		IPath path = new Path("icons/" + iconFileName);
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		reg.put(name(), desc);
	}
}
