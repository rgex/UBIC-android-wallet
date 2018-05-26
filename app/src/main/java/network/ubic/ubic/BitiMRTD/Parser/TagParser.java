package network.ubic.ubic.BitiMRTD.Parser;

import network.ubic.ubic.BitiMRTD.Tools.Tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TagParser
{

    private Tools tools;
    private byte[] data;
    private Map<String, byte[]> tags = new HashMap<String, byte[]>();

    public TagParser(byte[] data)
    {
        if(data == null) {
            return;
        }

        this.tools = new Tools();
        this.data = data;
        //System.out.println("Build TagParser with : ".concat(this.tools.bytesToString(data)));
    }

    public void parseElement(byte[] element)
    {
        try {
            int cursor = 0;
            while (cursor < element.length) {

                byte[] tag = this.getTagFromElement(Arrays.copyOfRange(element, cursor, cursor + 2));
                String sTag = this.tools.bytesToString(tag).toLowerCase();
                cursor += tag.length;

                byte[] bytesToRead = Arrays.copyOfRange(element, cursor, element.length);

                int lengthOfAsn1Header = this.tools.getAsn1HeaderLength(bytesToRead);
                int asn1Length = this.tools.getLengthFromAsn1(bytesToRead);

                System.out.println("Found tag : ".concat(sTag).concat(", length : ").concat(String.valueOf(asn1Length)));

                cursor += lengthOfAsn1Header;
                byte[] tagBody = Arrays.copyOfRange(element, cursor, cursor + asn1Length);
                cursor += asn1Length;

                this.tags.put(sTag, tagBody);
            }
        } catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
        }
    }

    private byte[] getTagFromElement(byte[] element)
    {
        if(element[0] == (byte)0x7F || element[0] == (byte)0x5F) {
            //tag on 2 bytes
            return Arrays.copyOfRange(element, 0, 2);
        }

        return this.tools.byteToBytes(element[0]);
    }


    public TagParser geTag(String tag)
    {
        this.parseElement(this.data);
        System.out.println("Getting tag : ".concat(tag));
        tag = tag.toLowerCase();

        return new TagParser(this.tags.get(tag));
    }

    public byte[] getBytes()
    {
        return this.data;
    }

}
