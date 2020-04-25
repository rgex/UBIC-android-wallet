package network.ubic.ubic.AsyncTasks;

import android.os.AsyncTask;

public class PrivateKeyPopulate extends AsyncTask<Void, Void, Void> {

    private byte[] privateKey;
    private String address;
    private String privateKeyString;
    private OnPrivateKeyFragmentPopulateCompleted listener;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public PrivateKeyPopulate(OnPrivateKeyFragmentPopulateCompleted listener, byte[] privateKey, String address) {
        this.listener = listener;
        this.privateKey = privateKey;
        this.address = address;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        privateKeyString = bytesToHex(privateKey);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.onPrivateKeyFragmentPopulateCompleted(privateKeyString, this.address);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
