package network.ubic.ubic.BitiAndroid;

import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.util.Log;

public class TagProvider
{

    private static IsoDep tag = null;
    private static int timeout = 1500;
    private static boolean tagIsLost = false;

    public static byte[] transceive(byte[] payload)
    {
        if(getTag() != null) {
            try {
                return getTag().transceive(payload);
            }
            catch (Exception e) {
                if(e instanceof TagLostException) {
                    TagProvider.setTagIsLost();
                }
                return null;
            }
        }
        return null;
    }

    public static IsoDep getTag()
    {
        if(tagIsLost) {
            return null;
        }
        connectTag();
        return tag;
    }

    public static void setTag(IsoDep nTag)
    {
        tagIsLost = false;
        connectTag();
        if(nTag != null) {
            try {
                nTag.setTimeout(timeout);
            }
            catch (Exception e) {
                //@TODO
            }
        }
        tag = nTag;
    }

    private static void connectTag() {
        if(tag != null) {
            if (!tag.isConnected()) {
                try {
                    tag.connect();
                } catch (Exception e) {
                    if(e.getMessage() != null) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println(Log.getStackTraceString(e));
                }
            }
        }
    }

    public static void closeTag() {
        if(tag != null) {
            try {
                tag.close();
            } catch (Exception e) {
                if(e.getMessage() != null) {
                    System.out.println(e.getMessage());
                }
                System.out.println(Log.getStackTraceString(e));
            }
        }
    }

    public static boolean isTagReady() {
        if(tag != null) {
            connectTag();
            if(tag.isConnected()) {
                return true;
            }
        }

        return false;
    }

    public static void setTimeout(int time)
    {
        timeout = time;
        if(getTag() != null) {
            getTag().setTimeout(time);
        }
    }

    public static int getTimeout()
    {
        if(getTag() != null) {
            return getTag().getTimeout();
        }
        return 0;
    }

    public static int getMaxTransceiveLength()
    {
        if(getTag() != null) {
            return getTag().getMaxTransceiveLength();
        }
        return 0;
    }

    public static void setTagIsLost()
    {
        tagIsLost = true;
    }

    public static boolean getTagIsLost()
    {
        return tagIsLost;
    }
}
