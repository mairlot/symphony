package eu.compassresearch.ide.cml.interpreter.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import eu.compassresearch.core.interpreter.api.CmlProcessInfo;
import eu.compassresearch.core.interpreter.api.behaviour.CmlBehaviorState;

public class CmlThread extends CmlDebugElement implements IThread {

	private CmlProcessInfo cmlProcessInfo;
	private CmlStackFrame cmlStackFrame = null;
	
	public CmlThread(CmlDebugTarget debugTarget, CmlProcessInfo cmlProcessInfo)
	{
		super(debugTarget);
		this.cmlProcessInfo = cmlProcessInfo;
		cmlStackFrame = new CmlStackFrame(debugTarget, this,cmlProcessInfo.getLocation());
	}

	@Override
	public boolean canResume() {
		return isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended();
	}

	@Override
	public boolean isSuspended() {
		return getDebugTarget().isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		getDebugTarget().resume();

	}

	@Override
	public void suspend() throws DebugException {
		getDebugTarget().suspend();
	}

	@Override
	public boolean canStepInto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stepInto() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub

	}

	/**
	 * It should not be posible to terminate the thread from the debugger
	 */
	@Override
	public boolean canTerminate() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return this.cmlProcessInfo.getState() == CmlBehaviorState.FINISHED || getDebugTarget().isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		return new CmlStackFrame[]{cmlStackFrame};
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		return true;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		return cmlStackFrame;
	}
	
	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	

	@Override
	public String getName() throws DebugException {
		return cmlProcessInfo.getName();
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}

}