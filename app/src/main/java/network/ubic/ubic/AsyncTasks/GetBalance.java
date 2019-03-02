package network.ubic.ubic.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import network.ubic.ubic.HttpHandler;
import network.ubic.ubic.MainActivity;

public class GetBalance extends AsyncTask<Void, Void, Void> {
    private String baseUrl = "https://ubic.network";
    private byte[] privateKey;

    private String TAG = MainActivity.class.getSimpleName();
    private HashMap<Integer, BigInteger> balanceMap = new HashMap<Integer, BigInteger>();
    Map<Integer, HashMap<Integer, BigInteger>> transactions = new TreeMap<Integer, HashMap<Integer, BigInteger>>();
    boolean isReceivingUBI;
    private OnGetBalanceCompleted listener;
    private boolean isEmptyAddress = false;
    private int nonce;

    public GetBalance(OnGetBalanceCompleted listener, byte[] privateKey) {
        this.listener = listener;
        this.privateKey = privateKey;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String url = baseUrl + "/api/addresses/" + getAddress(privateKey);
        //String url = baseUrl + "/api/addresses/qZwAjbfMbWmihaRTTDCtt8xc32hoX4Z9R";
        String jsonStr = sh.makeServiceCall(url);

        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                if(jsonObj.has("error")) {
                    isEmptyAddress = true;
                } else {

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

                    // NONCE
                    nonce = jsonObj.getInt("nonce");

                    // BALANCE
                    JSONArray transactionsArray = jsonObj.getJSONArray("last_transactions");
                    System.out.println("transactionsArray.length():" + transactionsArray.length());
                    for (int i = 0; i < transactionsArray.length(); i++) {

                        String transactionID = transactionsArray.getJSONObject(i).getString("transaction_id");
                        Integer timestamp = transactionsArray.getJSONObject(i).getInt("timestamp");

                        System.out.println(transactionsArray.getJSONObject(i).toString());

                        try {
                            amounts = transactionsArray.getJSONObject(i).getJSONObject("amount");
                            keys = amounts.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                String value = amounts.getString(key);
                                HashMap<Integer, BigInteger> map2 = new HashMap<Integer, BigInteger>();
                                map2.put(Integer.parseInt(key), new BigInteger(value));
                                transactions.put(timestamp, map2);
                            }
                        } catch (Exception e) {
                            // Amount is probably empty
                        }
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
        listener.OnGetBalanceCompleted(balanceMap, transactions, isReceivingUBI, isEmptyAddress, nonce);
    }

    public native String getAddress(byte[]  seed);
}
