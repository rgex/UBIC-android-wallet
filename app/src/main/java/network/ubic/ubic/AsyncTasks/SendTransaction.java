package network.ubic.ubic.AsyncTasks;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import network.ubic.ubic.HttpHandler;
import network.ubic.ubic.MainActivity;

public class SendTransaction extends AsyncTask<Void, Void, Void> {

    private String overWriteUrl = "";
    //private String baseUrl = "http://192.168.178.35:8888/ubic.network/api/send";

    private String TAG = MainActivity.class.getSimpleName();
    private OnSendTransactionCompleted listener;
    private String response;
    private int responseCode;
    String transaction;

    public SendTransaction(OnSendTransactionCompleted listener, String transaction) {
        this.listener = listener;
        this.transaction = transaction;
    }

    public void setOverwriteUrl(String overWriteUrl) {
        this.overWriteUrl = overWriteUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();

        String data = "{\"base64\":\"" + this.transaction + "\"}"; //data to post

        OutputStream out;
        try {

            URL url = new URL(APIServerSelector.getBestServer() + "/api/send");
            if(!this.overWriteUrl.isEmpty()) {
                url = new URL(overWriteUrl);
                this.overWriteUrl = "";
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();
            urlConnection.connect();
            responseCode = urlConnection.getResponseCode();

            StringBuffer sb = new StringBuffer();
            InputStream is = null;
            try {
                is = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                response = sb.toString();
            }
            catch (Exception e) {
                response = null;
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        System.out.println("Error closing InputStream");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = "exception";
            responseCode = 0;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.onSendTransactionCompleted(responseCode, response);
    }
}
