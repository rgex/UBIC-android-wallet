package network.ubic.ubic.BitiMRTD.Reader;

public interface ProgressListenerInterface
{
    public void updateProgress(int progress);

    public boolean isCanceled();

    public void cancel();
}
