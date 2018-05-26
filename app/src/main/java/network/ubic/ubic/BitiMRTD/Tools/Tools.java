package network.ubic.ubic.BitiMRTD.Tools;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class Tools {

    public byte[] unpadData(byte[] data)
    {
        int pos = 1;
        while(data[data.length - pos] != (byte) 0x80) {
            if(data.length <= pos) {
                return null;
            }
            pos++;
        }
        return Arrays.copyOfRange(data, 0, data.length - pos);
    }

    public byte[] doXor(byte[] input1, byte[] input2)
    {
        if(input1.length != input2.length) {
            System.out.println("Can not do XOR: input1 and input2 length mismatch");
            System.out.println("Input1 length : ".concat(String.valueOf(input1.length)));
            System.out.println("Input2 length : ".concat(String.valueOf(input2.length)));
            return null;
        }

        byte[] output = new byte[input1.length];

        for(int i = 0; i < input1.length; i++) {
            output[i] = (byte)((int)input1[i] ^ (int)input2[i]);
        }

        return output;
    }

    public byte[] adjustParityBits(byte[] input)
    {
        for (int i = 0; i < input.length; i++) {
            int b = input[i];
            input[i] = (byte) ((b & 0xfe) | ((((b >> 1) ^ (b >> 2) ^ (b >> 3) ^ (b >> 4) ^ (b >> 5) ^ (b >> 6) ^ (b >> 7)) ^ 0x01) & 0x01));
        }
        return input;
    }

    public String bytesToString(byte[] input)
    {
        if(input == null) {
            return "";
        }

        String output = "";
        for (byte b : input) {
            output = output.concat(String.format("%02x", b));
        }

        return output;
    }

    public byte[] concatByteArrays(byte[] input1, byte[] input2)
    {
        if(input1 == null) {
            return input2;
        }
        if(input2 == null) {
            return input1;
        }

        byte[] output = new byte[input1.length + input2.length];

        for(int i = 0; i < input1.length + input2.length; i++) {
            if(i < input1.length) {
                output[i] = input1[i];
            }
            else {
                output[i] = input2[i - input1.length];
            }
        }

        return output;
    }

    public byte[] byteToBytes(byte input)
    {
        byte[] output = {input};
        return output;
    }

    public byte[] incrementBytesArray(byte[] input, int index)
    {
        if(index >= input.length) {
            return this.concatByteArrays(
                    this.byteToBytes((byte)0x01),
                    input
            );
        }
        input[index]++;
        if(input[index] == (byte)0x00) {
            return this.incrementBytesArray(input, index+1);
        }
        return input;
    }

    public byte[] incrementBytesArray(byte[] input)
    {
        return this.incrementBytesArray(input, input.length - 1);
    }

    public byte[] calculateAsn1Length(byte[] data)
    {
        if(data.length <= 0x7F) {
            return this.byteToBytes((byte) data.length);
        }
        else if(data.length >= 0x7F && data.length <= 0xFF) {
            return this.concatByteArrays(
                    this.byteToBytes((byte)0x81),
                    this.byteToBytes((byte)data.length)
            );
        }
        else if(data.length >= 0x0100 && data.length <= 0xFFFF) {
            byte[] length = this.concatByteArrays(
                    this.byteToBytes((byte)(data.length >> 8)),
                    this.byteToBytes((byte)data.length)
            );
            return this.concatByteArrays(
                    this.byteToBytes((byte)0x82),
                    length
            );
        }

        System.out.println("Error: length is too big");
        return null;
    }

    public int getAsn1HeaderLength(byte[] asn1)
    {
        if(asn1 == null) {
            System.out.println("asn1 is null");
            return 0;
        }

        if((asn1[0] <= (byte)0x7F && asn1[0] >= (byte)0x00)) {
            return 1;
        }
        else if(asn1[0] == (byte)0x81) {
            return 2;
        }
        else if(asn1[0] == (byte)0x82) {
            return 3;
        }

        return 0;
    }

    public int getIntFrom16bits(byte[] input)
    {
        return ((input[0] << 8) & 0x0000ff00) | (input[1] & 0x000000ff);
    }

    public int getLengthFromAsn1(byte[] asn1)
    {

        if(asn1 == null) {
            System.out.println("asn1 is null");
            return 0;
        }

        if((asn1[0] <= (byte)0x7F && asn1[0] >= (byte)0x00)) {
            return asn1[0] & 0xFF;
        }
        else if(asn1[0] == (byte)0x81) {
            return asn1[1] & 0xFF;
        }
        else if(asn1[0] == (byte)0x82) {
            return ((asn1[1] << 8) & 0x0000ff00) | (asn1[2] & 0x000000ff);
        }
        return 0;
    }

    public int getLengthFromFileHeader(byte[] fileHeader)
    {
        if(fileHeader.length != 4) {
            System.out.println("Expected file header to be 4 bytes long");
            return 0;
        }

        return this.getLengthFromAsn1(Arrays.copyOfRange(fileHeader, 1, 4));
    }

    public byte[] calculate2bytesInt(int value)
    {
        if(value <= 0xFF) {
            return this.concatByteArrays(
                    this.byteToBytes((byte) 0x00),
                    this.byteToBytes((byte) value)
            );
        }
        else if(value <= 0xFFFF) {
            return this.concatByteArrays(
                    this.byteToBytes((byte) (value >> 8)),
                    this.byteToBytes((byte) value)
            );
        }

        System.out.println("Error: value is too big");
        return null;
    }

    /**
     * calculates checksum in accordance to the mrp standard
     * http://www.highprogrammer.com/alan/numbers/mrp.html
     * @param input
     * @return
     */
    public int calculateMrzCheckDigit(String input)
    {
        char chr;
        int value;
        int weight = 0;
        int sum = 0;

        input = input.toUpperCase();

        for(int i = 0; i < input.length(); i++) {
            chr = input.charAt(i);

            value = 0;

            if(chr > 64 && chr < 91) {
                value = (int)chr - 65 + 10;
            }

            if(chr > 47 && chr < 58) {
                value = (int)chr - 48;
            }

            switch (i%3) {
                case 0:
                    weight = 7;
                    break;
                case 1:
                    weight = 3;
                    break;
                case 2:
                    weight = 1;
                    break;
            }

            sum += weight * value;
        }
        return (sum % 10);
    }

    public byte[] inputStreamToByteArray(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return buffer.toByteArray();
        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * In order to endian conversion
     * @param bytes
     * @return
     */
    public byte[] invertBytes(byte[] bytes)
    {
        byte[] invertedBytes = new byte[bytes.length];
        for(int i=0; i < bytes.length; i++) {
            invertedBytes[i] = bytes[bytes.length - 1 - i];
        }
        return invertedBytes;
    }
}
