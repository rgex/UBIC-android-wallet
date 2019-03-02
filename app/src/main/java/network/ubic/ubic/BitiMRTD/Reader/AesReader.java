package network.ubic.ubic.BitiMRTD.Reader;

import java.util.Arrays;

public class AesReader extends AbstractReader {

    /**
     * return 16 bytes key
     * @return
     */
    public byte[] calculateEncryptionKey(byte[] seed)
    {
        return this.crypto.calculateAESEncryptionKey(seed);
    }

    public byte[] encrypt(byte[] key, byte[] payload)
    {
        return this.crypto.encryptAES(key, payload);
    }

    public byte[] decrypt(byte[] key, byte[] payload)
    {
        return this.crypto.decryptAES(key, payload);
    }

    public byte[] calculateMac(byte[] macKey, byte[] message)
    {
        return this.calculateMac(macKey, message, true);
    }

    public byte[] calculateMac(byte[] macKey, byte[] message, boolean addPad)
    {
        return crypto.calculateAESMac(macKey, message, addPad);
    }

    public byte[] calculateSequenceCounter(byte[] payload)
    {
        byte[] sequenceCounter = Arrays.copyOfRange(payload, 0, 16);

        System.out.println("Sequence counter: ".concat(this.tools.bytesToString(sequenceCounter)));

        return sequenceCounter;
    }

    public byte[] padData(byte[] data)
    {
        return this.crypto.padData(data, 16);
    }
}
