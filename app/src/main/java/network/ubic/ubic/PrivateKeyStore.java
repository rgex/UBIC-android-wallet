package network.ubic.ubic;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.KeyStore;
import java.security.SecureRandom;

public class PrivateKeyStore {

    KeyStore keyStore;

    public byte[] getPrivateKey(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getResources().getString(
                        R.string.private_key_shared_pref),
                Context.MODE_PRIVATE
        );

        String privateKey = sharedPref.getString(
                context.getResources().getString(R.string.private_key_shared_pref),
                ""
        );

        if(privateKey.isEmpty()) {
            try {
                SharedPreferences.Editor editor = sharedPref.edit();
                String privateKeyStr = new String(getNewKey(), "ASCII");
                editor.putString(
                        context.getResources().getString(R.string.private_key_shared_pref),
                        privateKeyStr
                );
                editor.commit();
            } catch (Exception e) {
                return null;
            }
        }

        return privateKey.getBytes();
    }

    private byte[] getNewKey() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(20);
    }
}
