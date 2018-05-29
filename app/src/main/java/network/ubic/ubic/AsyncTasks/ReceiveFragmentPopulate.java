package network.ubic.ubic.AsyncTasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class ReceiveFragmentPopulate extends AsyncTask<Void, Void, Void> {

    private byte[] privateKey;
    private OnReceiveFragmentPopulateCompleted listener;
    private Bitmap qrCode;
    private String address;

    public ReceiveFragmentPopulate(OnReceiveFragmentPopulateCompleted listener, byte[] privateKey) {
        this.listener = listener;
        this.privateKey = privateKey;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        address = getAddress(privateKey);
        try {
            encodeAsBitmap(address);
        }  catch (Exception e) {
            System.out.println("err1");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.onReceiveFragmentPopulateCompleted(qrCode, address);
    }


    private void encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 350, 350, null);
        } catch (Exception e) {
            System.out.println("err2");
            e.printStackTrace();
            return;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 350, 0, 0, w, h);

        this.qrCode = bitmap;
    }


    public native String getAddress(byte[]  seed);
}
