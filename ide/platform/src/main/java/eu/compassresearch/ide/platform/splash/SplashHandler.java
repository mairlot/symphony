/*******************************************************************************
 * Copyright (c) 2009, 2011 Overture Team and others.
 *
 * Overture is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Overture is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Overture.  If not, see <http://www.gnu.org/licenses/>.
 * 	
 * The Overture Tool web-site: http://overturetool.org/
 *******************************************************************************/
package eu.compassresearch.ide.platform.splash;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.BasicSplashHandler;

import eu.compassresearch.ide.platform.Activator;

public class SplashHandler extends BasicSplashHandler
{

    @Override
    public void init(Shell splash)
    {
        super.init(splash);

        int foregroundColorInteger = 0xFFFFFF;
        setForeground(new RGB((foregroundColorInteger & 0xFF0000) >> 16,
                              (foregroundColorInteger & 0xFF00) >> 8,
                              foregroundColorInteger & 0xFF));

        final Point buildIdPoint = new Point(12, 218);

        final Activator activator = Activator.getDefault();
        final String buildIdString = "Version "
        		+ Platform.getResourceString(activator.getBundle(), "%productVersion")
        		+ "\n"
        		+ Platform.getResourceString(activator.getBundle(), "%productBuild");

        getContent().addPaintListener(new PaintListener()
            {
                public void paintControl(PaintEvent e)
                {
                    e.gc.setForeground(getForeground());
                    e.gc.drawText(buildIdString, buildIdPoint.x, buildIdPoint.y, true);
                }
            });
    }
}
