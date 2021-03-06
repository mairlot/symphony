package eu.compassresearch.ide.core;

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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.overture.ide.core.resources.VdmProjectNature;

public class CmlProjectNature extends VdmProjectNature
{

	protected IProject project = null;

	public IProject getProject()
	{
		return project;
	}

	public void setProject(IProject project)
	{
		this.project = project;
	}

	@Override
	public void configure() throws CoreException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deconfigure() throws CoreException
	{
		// TODO Auto-generated method stub

	}

}