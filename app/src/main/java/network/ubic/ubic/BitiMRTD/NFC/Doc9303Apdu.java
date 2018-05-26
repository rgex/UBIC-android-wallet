package network.ubic.ubic.BitiMRTD.NFC;

import network.ubic.ubic.BitiMRTD.Tools.Tools;

public class Doc9303Apdu
{
    protected byte[] AID = {
            (byte) 0x00,
            (byte) 0xA4,
            (byte) 0x04,
            (byte) 0x0C,
            (byte) 0x07,
            (byte) 0xA0,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x02,
            (byte) 0x47,
            (byte) 0x10,
            (byte) 0x01
    };

    protected Tools tools;

    public Doc9303Apdu()
    {
        this.tools = new Tools();
    }

    public byte[] getAID()
    {
        return this.AID;
    }

    public byte[] buildDO87(byte[] data)
    {
        data = this.tools.concatByteArrays(this.tools.byteToBytes((byte)0x01), data);
        byte[] asn1Length = this.tools.calculateAsn1Length(
                data
        );
        byte[] result = this.tools.concatByteArrays(asn1Length, data);
        result = this.tools.concatByteArrays(this.tools.byteToBytes((byte)0x87), result);
        return result;
    }

    public byte[] buildDO97(int length)
    {
        byte[] header = {(byte) 0x97, (byte) 0x01};
        return this.tools.concatByteArrays(header, this.tools.byteToBytes((byte) length));
    }

    public byte[] buildDO8E(byte[] data)
    {
        byte[] asn1Length = this.tools.calculateAsn1Length(
                data
        );
        byte[] result = this.tools.concatByteArrays(asn1Length, data);
        result = this.tools.concatByteArrays(this.tools.byteToBytes((byte)0x8e), result);
        return result;
    }

}
