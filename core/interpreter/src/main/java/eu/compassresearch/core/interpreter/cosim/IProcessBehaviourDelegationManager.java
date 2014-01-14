package eu.compassresearch.core.interpreter.cosim;

public interface IProcessBehaviourDelegationManager
{
	public void addDelegate(IProcessDelegate delegate);

	public boolean hasDelegate(String name);

	public IProcessDelegate getDelegate(String name);
}
