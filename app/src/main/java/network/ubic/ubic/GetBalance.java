package network.ubic.ubic;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

public class GetBalance extends AsyncTask<Void, Void, Void> {
    private String baseUrl = "https://ubic.network";

    private String TAG = MainActivity.class.getSimpleName();
    private HashMap<Integer, BigInteger> balanceMap = new HashMap<Integer, BigInteger>();
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
        String url = baseUrl + "/api/addresses/xx/";
        String jsonStr = sh.makeServiceCall(url);

        Log.e(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting JSON Array node
                JSONObject amounts = jsonObj.getJSONObject("amount");

                Iterator<String> keys = amounts.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = amounts.getString(key);
                    balanceMap.put(Integer.parseInt(key), new BigInteger(value));

                }
            } catch (final JSONException e) {


            }

        } else {

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.OnGetBalanceCompleted(balanceMap);
    }
}
