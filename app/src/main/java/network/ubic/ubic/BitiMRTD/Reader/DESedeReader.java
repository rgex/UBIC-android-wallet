package network.ubic.ubic.BitiMRTD.Reader;

import java.util.Arrays;

public class DESedeReader extends AbstractReader
{

    /**
     * return 24 bytes 3DES key
     * @return
     */
    public byte[] calculateEncryptionKey(byte[] seed)
    {
        return this.crypto.calculate3DESEncryptionKey(seed);
    }

    /**
     * 3DES
     * @param key
     * @param payload
     * @return
     */
    public byte[] encrypt(byte[] key, byte[] payload)
    {
        return this.crypto.encrypt3DES(key, payload);
    }

    /**
     * 3DES
     * @param key
     * @param payload
     * @return
     */
    public byte[] decrypt(byte[] key, byte[] payload)
    {
        return this.crypto.decrypt3DES(key, payload);
    }

    public byte[] calculateMac(byte[] macKey, byte[] message)
    {
        return this.calculateMac(macKey, message, true);
    }

    /**
     * MAC algorithm 3 of ISO/IEC 9797-1
     * @param macKey
     * @param message
     * @return
     */
    public byte[] calculateMac(byte[] macKey, byte[] message, boolean addPad)
    {
        return this.crypto.calculate3DESMac(macKey, message, addPad);
    }

    public byte[] calculateSequenceCounter(byte[] payload)
    {
        byte[] sequenceCounter = this.tools.concatByteArrays(Arrays.copyOfRange(payload, 4, 8), Arrays.copyOfRange(payload, 12, 16));
        System.out.println("Sequence counter: ".concat(this.tools.bytesToString(sequenceCounter)));
        return sequenceCounter;
    }

    public byte[] padData(byte[] data)
    {
        return this.crypto.padData(data, 8);
    }
}
