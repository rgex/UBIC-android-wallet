package network.ubic.ubic.BitiMRTD.Tools;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{

    protected byte[] ivValue = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

    protected byte[] C1 = {
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0x01
    };

    protected byte[] C2 = {
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,
            (byte)0x02
    };

    protected Tools tools;

    public Crypto()
    {
        this.tools = new Tools();
    }

    public byte[] generateRandomBytes(int size)
    {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * returns Kseed 16 bytes
     * @return
     */
    public byte[] calculateSeed(String passportNbr, String dateOfBirth, String dateOfExpiration)
    {
        //@TODO make sure passportNbr, DOB, DOE aren't null
        String mrz = "";

        mrz = mrz.concat(passportNbr);
        mrz = mrz.concat(String.valueOf(this.tools.calculateMrzCheckDigit(passportNbr)));

        mrz = mrz.concat(dateOfBirth);
        mrz = mrz.concat(String.valueOf(this.tools.calculateMrzCheckDigit(dateOfBirth)));

        mrz = mrz.concat(dateOfExpiration);
        mrz = mrz.concat(String.valueOf(this.tools.calculateMrzCheckDigit(dateOfExpiration)));

        //System.out.println("mrz:");
        //System.out.println(mrz);

        return Arrays.copyOfRange(sha1(mrz.getBytes()), 0, 16);
    }

    /**
     * return 16 bytes Mac key
     * @return
     */
    public byte[] calculateMacKey(byte[] seed)
    {
        byte[] macKey = this.tools.adjustParityBits(
                Arrays.copyOfRange(
                        sha1(this.tools.concatByteArrays(seed, C2)),
                        0,
                        16
                )
        );

        System.out.println("MacKey : ".concat(this.tools.bytesToString(macKey)));

        return macKey;
    }

    public byte[] sha1(byte[] input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input);
            return md.digest();
        } catch (Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }

    public byte[] padData(byte[] data, int padSize)
    {
        byte[] result = this.tools.concatByteArrays(
                data,
                this.tools.byteToBytes((byte) 0x80)
        );

        while((result.length % padSize) != 0) {
            result = this.tools.concatByteArrays(
                    result,
                    this.tools.byteToBytes((byte) 0x00)
            );
        }

        return result;
    }

    public byte[] calculate3DESEncryptionKey(byte[] seed)
    {
        byte[] encKey16Bytes = this.tools.adjustParityBits(
                Arrays.copyOfRange(
                        sha1(this.tools.concatByteArrays(seed, C1)),
                        0,
                        16
                )
        );

        byte[] encKey24Bytes = this.tools.concatByteArrays(
                encKey16Bytes,
                Arrays.copyOfRange(encKey16Bytes, 0, 8) // again bytes 0 to 8,
        );                                              // 3DES = DES encryption using bytes 0,8
        // decryption using bytes 8,16
        // encryption using bytes 16,24


        System.out.println("EncKey 16 Bytes : ".concat(this.tools.bytesToString(encKey16Bytes)));
        System.out.println("EncKey 24 Bytes : ".concat(this.tools.bytesToString(encKey24Bytes)));

        return encKey24Bytes;
    }

    public byte[] calculateAESEncryptionKey(byte[] seed)
    {
        return this.tools.adjustParityBits(
                Arrays.copyOfRange(
                        sha1(this.tools.concatByteArrays(seed, C1)),
                        0,
                        16
                )
        );
    }

    public byte[] encryptUsingDES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "DES");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
            return cipher.doFinal(payload);
        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }

    public byte[] decryptUsingDES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "DES");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv);

            return cipher.doFinal(payload);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    /**
     * MAC algorithm 3 of ISO/IEC 9797-1
     * @param macKey
     * @param message
     * @return
     */
    public byte[] calculate3DESMac(byte[] macKey, byte[] message, boolean addPad)
    {
        byte[] key1 = Arrays.copyOfRange(macKey, 0, 8);
        byte[] key2 = Arrays.copyOfRange(macKey, 8, 16);

        if(addPad) {
            //Step 1. Byte padding. first byte 0x80 then 0x00 until mod n reached
            message = this.padData(message, 8);
        }

        //Step 2. Initial transformation n°1
        byte[] hq = this.encryptUsingDES(key1, Arrays.copyOfRange(message, 0, 8));

        //Step 3. Iteration
        for(int i = 8; i + 8 <= message.length; i+=8) {
            hq = this.tools.doXor(hq, Arrays.copyOfRange(message, i, i + 8));
            hq = this.encryptUsingDES(key1, hq);
        }

        //Step 4. Output transformation n°3
        return this.encryptUsingDES(
                key1,
                this.decryptUsingDES(key2, hq)
        );
    }

    public byte[] calculateAESMac(byte[] macKey, byte[] message, boolean addPad)
    {
        return Arrays.copyOfRange(AESCMAC.get(macKey, message), 0, 8);
    }

    public byte[] encrypt3DES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "DESede");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);

            return cipher.doFinal(payload);
        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }

    public byte[] encryptAES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);

            return cipher.doFinal(payload);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * 3DES
     * @param key
     * @param payload
     * @return
     */
    public byte[] decrypt3DES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "DESede");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv);

            return cipher.doFinal(payload);
        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            return null;
        }
    }

    public byte[] decryptAES(byte[] key, byte[] payload)
    {
        try {
            SecretKey sKey = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(this.ivValue);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv);

            return cipher.doFinal(payload);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
