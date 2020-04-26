package network.ubic.ubic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class PrivateKeyStoreOld {

    KeyStore keyStore;

    // this is actually a private key seed
    public byte[] getPrivateKey(Context context) {
        System.out.println("called getPrivateKey()");
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            String alias = "ubickey";

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

            SharedPreferences sharedPref = context.getSharedPreferences(
                    context.getResources().getString(
                            R.string.private_key_shared_pref),
                    Context.MODE_PRIVATE
            );

            String privateKey = sharedPref.getString(
                    context.getResources().getString(R.string.private_key_shared_pref),
                    ""
            );

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(alias, null);

            System.out.println("secretKeyEntry.toString():");
            System.out.println(secretKeyEntry.toString());


            Cipher inCipher = Cipher.getInstance("AES/GCM/NoPadding");
            inCipher.init(Cipher.ENCRYPT_MODE, secretKeyEntry.getSecretKey());
            byte[] iv;

            if (privateKey.isEmpty()) {
                System.out.println("privateKey.isEmpty");
                try {
                    iv = inCipher.getIV();
                    byte[] newKey = getNewKey();

                    System.out.println("ivLength:" + iv.length);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    String privateKeyEncrypted = Base64.encodeToString(inCipher.doFinal(newKey), Base64.DEFAULT);
                    System.out.println("outCipher.doFinal():");
                    System.out.println(privateKeyEncrypted);
                    editor.putString(
                            context.getResources().getString(R.string.private_key_shared_pref),
                            privateKeyEncrypted
                    );

                    String iv64 = Base64.encodeToString(iv, Base64.DEFAULT);
                    System.out.println("iv64Length:" + iv64.getBytes().length);
                    editor.putString(
                            context.getResources().getString(R.string.iv_key_shared_pref),
                            iv64
                    );
                    editor.commit();

                    return newKey;
                } catch (Exception e) {
                    System.out.println("Exception PrivateKeyStore0;");
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println("Exception PrivateKeyStore0;");
                    return null;
                }
            } else {
                iv = Base64.decode(sharedPref.getString(
                        context.getResources().getString(R.string.iv_key_shared_pref),
                        ""
                ), Base64.DEFAULT);
            }


            System.out.println("iv final Length:" + iv.length);

            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            Cipher outCipher = Cipher.getInstance("AES/GCM/NoPadding");
            outCipher.init(Cipher.DECRYPT_MODE, secretKeyEntry.getSecretKey(), spec);

            return outCipher.doFinal(Base64.decode(privateKey,Base64.DEFAULT));
        } catch (Exception e) {

            System.out.println("Exception PrivateKeyStore;");
            e.printStackTrace();
            System.out.print(e.getMessage());
            return null;
        }
    }

    private byte[] getNewKey() {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(20);
    }
}