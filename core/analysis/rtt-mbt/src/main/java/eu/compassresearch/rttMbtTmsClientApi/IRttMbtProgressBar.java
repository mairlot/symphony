package eu.compassresearch.rttMbtTmsClientApi;

public interface IRttMbtProgressBar {
	public enum Tasks {
		Global,
		Goal,
	    TC_COV,
	    TR_COV,
	    BCS_COV,
	    MCDC_COV,
	    ALL
	}
	public void setProgress(Tasks task, int percent);
	public int getProgress(Tasks task);
}
