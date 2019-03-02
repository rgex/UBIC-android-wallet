package network.ubic.ubic.BitiAndroid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class AbstractNfcActivity extends AppCompatActivity
{
    protected NfcAdapter mNfcAdapter;
    protected PendingIntent pendingIntent;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
            System.out.println("failed to get NFC adapter, NFC disabled?");
        }
        else {
            pendingIntent = PendingIntent.getActivity(
                    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
    }


    public void onPause()
    {
        super.onPause();
        if(mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void onResume()
    {
        super.onResume();
        try {
        if(mNfcAdapter != null && pendingIntent != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        }catch (Exception e) {
            System.out.println("onResume error");
        }
    }

    public void onNewIntent(Intent intent)
    {
        try {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tagFromIntent != null) {
                TagProvider.setTag(IsoDep.get(tagFromIntent));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Got new intent!");
    }
}
