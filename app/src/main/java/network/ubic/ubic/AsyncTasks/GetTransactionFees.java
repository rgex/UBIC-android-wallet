package network.ubic.ubic.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

import network.ubic.ubic.HttpHandler;
import network.ubic.ubic.MainActivity;

public class GetTransactionFees extends AsyncTask<Void, Void, Void> {

    private String baseUrl = "https://ubic.network";

    private String TAG = MainActivity.class.getSimpleName();
    private OnGetTransactionFeesCompleted listener;
    private HashMap<Integer, BigInteger> feesMap = new HashMap<Integer, BigInteger>();

    public GetTransactionFees(OnGetTransactionFeesCompleted listener) {
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
        String url = baseUrl + "/api/fees";
        String jsonStr = sh.makeServiceCall(url);

        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                JSONObject amounts = jsonObj.getJSONObject("fees");
                Iterator<String> keys = amounts.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject value = amounts.getJSONObject(key);
                    feesMap.put(Integer.parseInt(key), new BigInteger(value.getString("fee")));
                }

            } catch (final JSONException e) {
                System.out.println("JSONException");
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.OnGetTransactionFeesCompleted(feesMap);
    }
}
