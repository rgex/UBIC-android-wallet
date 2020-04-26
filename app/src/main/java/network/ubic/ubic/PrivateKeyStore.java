package network.ubic.ubic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class PrivateKeyStore {

    KeyStore keyStore;

    private KeyStore.SecretKeyEntry getEncryptionKey(Context context) {
        String alias = "ubickey_new";
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Create the keys if necessary
            if (!keyStore.containsAlias(alias)) {
                try {
                    KeyGenerator keyGenerator;

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        // Below Android M, use the KeyPairGeneratorSpec.Builder.

                        keyGenerator = KeyGenerator
                                .getInstance("AES", "AndroidKeyStore");
                        KeyPairGeneratorSpec keyGenParameterSpec = new KeyPairGeneratorSpec.Builder(context)
                                .setAlias(alias).build();
                        keyGenerator.init(keyGenParameterSpec);

                    } else {
                        // On Android M or above, use the KeyGenparameterSpec.Builder and specify permitted
                        // properties  and restrictions of the key.
                        keyGenerator = KeyGenerator
                                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                .build();
                        keyGenerator.init(keyGenParameterSpec);
                    }

                    SecretKey secretKey = keyGenerator.generateKey();
                } catch (Exception e) {
                    System.out.println("Exception PrivateKeyStore3;");
                    e.printStackTrace();
                    System.out.print(e.getMessage());
                }
            }

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(alias, null);
            return secretKeyEntry;
        } catch (Exception e) {

            System.out.println("Exception PrivateKeyStore;");
            e.printStackTrace();
            System.out.print(e.getMessage());
            return null;
        }
    }

    private boolean insertWallet(Context context, JSONObject jsonWallet) {

        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getResources().getString(
                            R.string.private_key_shared_pref_new),
                    Context.MODE_PRIVATE
            );

            String wallet = sharedPref.getString(
                    context.getResources().getString(R.string.private_key_shared_pref_new),
                    ""
            );

            String jsonWalletString = jsonWallet.toString();

            SharedPreferences.Editor editor = sharedPref.edit();

            KeyStore.SecretKeyEntry secretKeyEntry = getEncryptionKey(context);
            Cipher inCipher = Cipher.getInstance("AES/GCM/NoPadding");
            inCipher.init(Cipher.ENCRYPT_MODE, secretKeyEntry.getSecretKey());

            String walletEncrypted = Base64.encodeToString(inCipher.doFinal(jsonWalletString.getBytes()), Base64.DEFAULT);

            editor.putString(
                    context.getResources().getString(R.string.private_key_shared_pref_new),
                    walletEncrypted
            );

            byte[] iv;
            iv = inCipher.getIV();
            String iv64 = Base64.encodeToString(iv, Base64.DEFAULT);
            System.out.println("iv64Length:" + iv64.getBytes().length);
            editor.putString(
                    context.getResources().getString(R.string.iv_key_shared_pref_new),
                    iv64
            );
            editor.commit();
        } catch (Exception e) {
            System.out.println("");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public JSONObject getWallet(Context context) {

        JSONObject wallet = new JSONObject();

        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getResources().getString(
                            R.string.private_key_shared_pref_new),
                    Context.MODE_PRIVATE
            );

            String walletEncrypted = sharedPref.getString(
                    context.getResources().getString(R.string.private_key_shared_pref_new),
                    ""
            );

            if (walletEncrypted.isEmpty()) {
                return wallet;
            }

            byte[] iv = Base64.decode(sharedPref.getString(
                    context.getResources().getString(R.string.iv_key_shared_pref_new),
                    ""
            ), Base64.DEFAULT);

            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            Cipher outCipher = Cipher.getInstance("AES/GCM/NoPadding");
            KeyStore.SecretKeyEntry secretKeyEntry = getEncryptionKey(context);
            outCipher.init(Cipher.DECRYPT_MODE, secretKeyEntry.getSecretKey(), spec);

            byte[] rawWallet = outCipher.doFinal(Base64.decode(walletEncrypted, Base64.DEFAULT));

            wallet = new JSONObject(new String(rawWallet));
            return wallet;
        } catch (Exception e) {
            System.out.println("Failed to get the wallet");
            e.printStackTrace();
            return wallet;
        }
    }

    private boolean createNewWallet(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getResources().getString(
                        R.string.private_key_shared_pref_new),
                Context.MODE_PRIVATE
        );

        String wallet = sharedPref.getString(
                context.getResources().getString(R.string.private_key_shared_pref_new),
                ""
        );

        try {
            // Check again that the shared pref is really empty!
            if (wallet.isEmpty()) {
                PrivateKeyStoreOld privateKeyStoreOld = new PrivateKeyStoreOld();
                byte[] firstPrivateKey = privateKeyStoreOld.getPrivateKey(context);

                System.out.println("privateKey.isEmpty");
                try {
                    String privateKeyString = new String(Hex.encode(firstPrivateKey));

                    JSONObject jsonWallet = new JSONObject();
                    JSONArray allPrivKeyList = new JSONArray();

                    jsonWallet.put("current", privateKeyString);
                    jsonWallet.put("all", (Object)allPrivKeyList);
                    insertWallet(context, jsonWallet);
                } catch (Exception e) {
                    System.out.println("Exception PrivateKeyStore0;");
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println("Exception PrivateKeyStore0;");
                    return false;
                }

                return true;
            }
        } catch (Exception e) {
            System.out.println("failed to generate new wallet");
            e.printStackTrace();
            return false;
        }

        return false;
    }

    // this is actually a private key seed
    public byte[] getPrivateKey(Context context) {
        System.out.println("called getPrivateKey()");
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getResources().getString(
                            R.string.private_key_shared_pref_new),
                    Context.MODE_PRIVATE
            );

            String walletEncrypted = sharedPref.getString(
                    context.getResources().getString(R.string.private_key_shared_pref_new),
                    ""
            );

            if (walletEncrypted.isEmpty()) {
                createNewWallet(context);
            }

            JSONObject wallet = getWallet(context);
            byte[] currentPrivateKey = Hex.decode(wallet.getString("current").getBytes());

            return currentPrivateKey;

        } catch (Exception e) {

            System.out.println("Exception PrivateKeyStore;");
            e.printStackTrace();
            System.out.print(e.getMessage());
            return null;
        }
    }

    public boolean addPrivateKey(Context context, byte[] newKey) {
        try {
            String privateKeyString = new String(Hex.encode(newKey));
            JSONObject wallet = getWallet(context);
            wallet.getJSONArray("all").put(privateKeyString);
            insertWallet(context, wallet);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setAsDefault(Context context, String newDefault) {
        try {
            JSONObject wallet = getWallet(context);
            String oldCurrent = wallet.getString("current");
            wallet.put("current", newDefault);

            // replace new default in array list

            JSONArray allList = wallet.getJSONArray("all");

            boolean foundOldKey = false;
            for (int i = 0; i < allList.length(); i++) {
                String privKey = allList.getString(i);
                if (privKey.equals(newDefault)) {
                    allList.put(i, oldCurrent); // place the old primary key back in the pool where the new current key was
                    foundOldKey = true;
                    break;
                }
            }
            if (!foundOldKey) {
                return false;
            }

            wallet.put("", allList);

            return  insertWallet(context, wallet);
        } catch (Exception e) {
            return false;
        }
    }


    public byte[] generateNewKey() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(20);
    }
}