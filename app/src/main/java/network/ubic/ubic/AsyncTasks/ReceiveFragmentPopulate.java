package network.ubic.ubic.AsyncTasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import network.ubic.ubic.HttpHandler;
import network.ubic.ubic.MainActivity;

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

    public static interface OnGetBalanceCompleted {

        void OnGetBalanceCompleted(HashMap<Integer, BigInteger> balanceMap, HashMap<String, HashMap<Integer, BigInteger>> transactions, boolean isReceivingUBI);
    }

    public static class GetBalance extends AsyncTask<Void, Void, Void> {
        private String baseUrl = "https://ubic.network";

        private String TAG = MainActivity.class.getSimpleName();
        private HashMap<Integer, BigInteger> balanceMap = new HashMap<Integer, BigInteger>();
        HashMap<String, HashMap<Integer, BigInteger>> transactions = new HashMap<String, HashMap<Integer, BigInteger>>();
        boolean isReceivingUBI;
        private OnGetBalanceCompleted listener;

        public GetBalance(OnGetBalanceCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = baseUrl + "/api/addresses/qWeMZA73N7HYiUDqvbZuXfJV5ornPW7PQ/";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // BALANCE
                    JSONObject amounts = jsonObj.getJSONObject("amount");
                    Iterator<String> keys = amounts.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = amounts.getString(key);
                        balanceMap.put(Integer.parseInt(key), new BigInteger(value));
                    }

                    // IS RECEIVING UBI
                    isReceivingUBI = jsonObj.getBoolean("is_receiving_ubi");

                    // BALANCE
                    JSONArray transactionsArray = jsonObj.getJSONArray("last_transactions");
                    System.out.println("transactionsArray.length():" + transactionsArray.length());
                    for (int i=0; i < transactionsArray.length(); i++) {

                        String transactionID = transactionsArray.getJSONObject(i).getString("transaction_id");

                        System.out.println(transactionsArray.getJSONObject(i).toString());

                        try {
                            amounts = transactionsArray.getJSONObject(i).getJSONObject("amount");
                            keys = amounts.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = amounts.getString(key);
                                HashMap<Integer, BigInteger> map2 = new HashMap<Integer, BigInteger>();
                                map2.put(Integer.parseInt(key), new BigInteger(value));
                                transactions.put(transactionID, map2);
                            }
                        } catch (Exception e) {
                            // Amount is probably empty
                        }
                    }

                } catch (final JSONException e) {
                    System.out.println("JSONException");
                    e.printStackTrace();
                }

            } else {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            listener.OnGetBalanceCompleted(balanceMap, transactions, isReceivingUBI);
        }
    }
}
