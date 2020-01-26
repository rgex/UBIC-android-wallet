package network.ubic.ubic.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import network.ubic.ubic.HttpHandler;
import network.ubic.ubic.MainActivity;

public class GetBalance extends AsyncTask<Void, Void, Void> {

    private String baseUrl = "https://ubic.network";
    //private String baseUrl = "http://192.168.178.35:8888/ubic.network";

    private byte[] privateKey;

    private String TAG = MainActivity.class.getSimpleName();
    private HashMap<Integer, BigInteger> balanceMap = new HashMap<Integer, BigInteger>();
    Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> transactions = new TreeMap<Integer, Pair<String, HashMap<Integer, BigInteger>>> ();
    Map<Integer, Pair<String, HashMap<Integer, BigInteger>>> pendingTransactions = new TreeMap<Integer, Pair<String, HashMap<Integer, BigInteger>>> ();
    List<Pair<String, BigInteger>> ubiExpirationDate;
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
        //String url = baseUrl + "/api/addresses/qZAYqtWvaKcrvgsG8wY4H7Ezyg2aVWkdm";
        String jsonStr = sh.makeServiceCall(url);

        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                if(jsonObj.has("error")) {
                    isEmptyAddress = true;
                } else {

                    JSONObject amounts;
                    Iterator<String> keys;
                    // BALANCE
                    try {
                        amounts = jsonObj.getJSONObject("amount");
                        keys = amounts.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = amounts.getString(key);
                            balanceMap.put(Integer.parseInt(key), new BigInteger(value));
                        }
                    } catch (final JSONException e) {
                        System.out.println("JSONException on amounts (Not important)");
                        e.printStackTrace();
                    }

                    // IS RECEIVING UBI
                    isReceivingUBI = jsonObj.getBoolean("is_receiving_ubi");

                    // NONCE
                    nonce = jsonObj.getInt("nonce");

                    // PENDING TRANSACTIONS
                    JSONArray pendingTransactionsArray = jsonObj.getJSONArray("pending_transactions");
                    System.out.println("pendingTransactionsArray.length():" + pendingTransactionsArray.length());
                    for (int i = 0; i < pendingTransactionsArray.length(); i++) {

                        String transactionID = pendingTransactionsArray.getJSONObject(i).getString("transaction_id");
                        Integer timestamp = pendingTransactionsArray.getJSONObject(i).getInt("timestamp");
                        System.out.println("transactionID:" + transactionID);

                        System.out.println(pendingTransactionsArray.getJSONObject(i).toString());

                        if(pendingTransactionsArray.getJSONObject(i).getString("type").equals("registerPassport")) {
                            HashMap<Integer, BigInteger> hmap = new HashMap<Integer, BigInteger>();
                            Pair<String, HashMap<Integer, BigInteger>> map2 = new Pair<String, HashMap<Integer, BigInteger>>(
                                    "registerPassport",
                                    hmap
                            );
                            pendingTransactions.put(timestamp, map2);
                        } else {
                            try {
                                amounts = pendingTransactionsArray.getJSONObject(i).getJSONObject("amount");
                                keys = amounts.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    String value = amounts.getString(key);
                                    HashMap<Integer, BigInteger> hmap = new HashMap<Integer, BigInteger>();
                                    hmap.put(Integer.parseInt(key), new BigInteger(value));
                                    Pair<String, HashMap<Integer, BigInteger>> map2 = new Pair<String, HashMap<Integer, BigInteger>>(
                                            pendingTransactionsArray.getJSONObject(i).getString("type"),
                                            hmap
                                    );
                                    pendingTransactions.put(timestamp, map2);
                                }
                            } catch (Exception e) {
                                // Amount is probably empty
                            }
                        }
                    }

                    // TRANSACTIONS
                    JSONArray transactionsArray = jsonObj.getJSONArray("last_transactions");
                    System.out.println("transactionsArray.length():" + transactionsArray.length());
                    for (int i = 0; i < transactionsArray.length(); i++) {

                        String transactionID = transactionsArray.getJSONObject(i).getString("transaction_id");
                        Integer timestamp = transactionsArray.getJSONObject(i).getInt("timestamp");
                        System.out.println("transactionID:" + transactionID);

                        System.out.println(transactionsArray.getJSONObject(i).toString());

                        if(transactionsArray.getJSONObject(i).getString("type").equals("registerPassport")) {
                            HashMap<Integer, BigInteger> hmap = new HashMap<Integer, BigInteger>();
                            Pair<String, HashMap<Integer, BigInteger>> map2 = new Pair<String, HashMap<Integer, BigInteger>>(
                                    "registerPassport",
                                    hmap
                            );
                            transactions.put(timestamp, map2);
                        } else {
                            try {
                                amounts = transactionsArray.getJSONObject(i).getJSONObject("amount");
                                keys = amounts.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    String value = amounts.getString(key);
                                    HashMap<Integer, BigInteger> hmap = new HashMap<Integer, BigInteger>();
                                    hmap.put(Integer.parseInt(key), new BigInteger(value));
                                    Pair<String, HashMap<Integer, BigInteger>> map2 = new Pair<String, HashMap<Integer, BigInteger>>(
                                            transactionsArray.getJSONObject(i).getString("type"),
                                            hmap
                                    );
                                    transactions.put(timestamp, map2);
                                }
                            } catch (Exception e) {
                                // Amount is probably empty
                            }
                        }
                    }
                    System.out.println("transactions.size():" + transactions.size());

                    // DSC certs
                    JSONArray dscArray = jsonObj.getJSONArray("dsc");
                    System.out.println("dscArray.length():" + dscArray.length());
                    for (int i = 0; i < dscArray.length(); i++) {
                        String issuer = transactionsArray.getJSONObject(i).getString("issuer");
                        Integer expirationDate = transactionsArray.getJSONObject(i).getInt("expirationDate");
                        ubiExpirationDate.add(new Pair<String, BigInteger>(issuer, BigInteger.valueOf(expirationDate)));
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
        listener.OnGetBalanceCompleted(balanceMap, transactions, pendingTransactions, isReceivingUBI, ubiExpirationDate, isEmptyAddress, nonce);
    }

    public native String getAddress(byte[]  seed);
}
