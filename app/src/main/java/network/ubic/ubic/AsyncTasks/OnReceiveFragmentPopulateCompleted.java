package network.ubic.ubic.AsyncTasks;

import android.graphics.Bitmap;

public interface OnReceiveFragmentPopulateCompleted {
    void onReceiveFragmentPopulateCompleted(Bitmap qrCode, String address);
}
