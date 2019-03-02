package network.ubic.ubic.BitiMRTD.Converter;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class ASN1Converter {

    JSONObject asn1List;
    static private ASN1Converter asn1Converter = null;

    public static ASN1Converter getInstance()
    {
        if(asn1Converter == null) {
            asn1Converter = new ASN1Converter();
        }
        return asn1Converter;
    }

    private ASN1Converter()
    {
        String asn1json = "{\n" +
                "\t\"1.2.840.113549.1.1.5\" : \"sha1RSA\",\n" +
                "\t\"1.2.840.113549.1.1.4\" : \"md5RSA\",\n" +
                "\t\"1.2.840.10040.4.3\" : \"sha1DSA\",\n" +
                "\t\"1.3.14.3.2.29\" : \"sha1RSA\",\n" +
                "\t\"1.3.14.3.2.15\" : \"shaRSA\",\n" +
                "\t\"1.3.14.3.2.3\" : \"md5RSA\",\n" +
                "\t\"1.2.840.113549.1.1.2\" : \"md2RSA\",\n" +
                "\t\"1.2.840.113549.1.1.3\" : \"md4RSA\",\n" +
                "\t\"1.3.14.3.2.2\" : \"md4RSA\",\n" +
                "\t\"1.3.14.3.2.4\" : \"md4RSA\",\n" +
                "\t\"1.3.14.7.2.3.1\" : \"md2RSA\",\n" +
                "\t\"1.3.14.3.2.13\" : \"sha1DSA\",\n" +
                "\t\"1.3.14.3.2.27\" : \"dsaSHA1\",\n" +
                "\t\"2.16.840.1.101.2.1.1.19\" : \"mosaicUpdatedSig\",\n" +
                "\t\"1.3.14.3.2.26\" : \"sha1NoSign\",\n" +
                "\t\"1.2.840.113549.2.5\" : \"md5NoSign\",\n" +
                "\t\"2.16.840.1.101.3.4.2.1\" : \"sha256NoSign\",\n" +
                "\t\"2.16.840.1.101.3.4.2.2\" : \"sha384NoSign\",\n" +
                "\t\"2.16.840.1.101.3.4.2.3\" : \"sha512NoSign\",\n" +
                "\t\"1.2.840.113549.1.1.11\" : \"sha256RSA\",\n" +
                "\t\"1.2.840.113549.1.1.12\" : \"sha384RSA\",\n" +
                "\t\"1.2.840.113549.1.1.13\" : \"sha512RSA\",\n" +
                "\t\"1.2.840.113549.1.1.10\" : \"RSASSA-PSS\",\n" +
                "\t\"1.2.840.10045.4.1\" : \"sha1ECDSA\",\n" +
                "\t\"1.2.840.10045.4.3.2\" : \"sha256ECDSA\",\n" +
                "\t\"1.2.840.10045.4.3.3\" : \"sha384ECDSA\",\n" +
                "\t\"1.2.840.10045.4.3.4\" : \"sha512ECDSA\",\n" +
                "\t\"1.2.840.10045.4.3\" : \"specifiedECDSA\",\n" +
                "\n" +
                "\n" +
                "\t\"1.2.840.113549.1.1.1\" : \"RSA\",\n" +
                "\t\"1.2.840.10040.4.1\" : \"DSA\",\n" +
                "\t\"1.2.840.10046.2.1\" : \"DH\",\n" +
                "\t\"1.2.840.113549.1.1.10\" : \"RSASSA-PSS\",\n" +
                "\t\"1.3.14.3.2.12\" : \"DSA\",\n" +
                "\t\"1.2.840.113549.1.3.1\" : \"DH\",\n" +
                "\t\"1.3.14.3.2.22\" : \"RSA_KEYX\",\n" +
                "\t\"2.16.840.1.101.2.1.1.20\" : \"mosaicKMandUpdSig\",\n" +
                "\t\"1.2.840.113549.1.9.16.3.5\" : \"ESDH\",\n" +
                "\t\"1.3.6.1.5.5.7.6.2\" : \"NO_SIGN\",\n" +
                "\t\"1.2.840.10045.2.1\" : \"ECC\",\n" +
                "\t\"1.2.840.10045.3.1.7\" : \"ECDSA_P256\",\n" +
                "\t\"1.3.132.0.34\" : \"ECDSA_P384\",\n" +
                "\t\"1.3.132.0.35\" : \"ECDSA_P521\",\n" +
                "\t\"1.2.840.113549.1.1.7\" : \"RSAES_OAEP\",\n" +
                "\t\"1.3.133.16.840.63.0.2\" : \"ECDH_STD_SHA1_KDF\"\n" +
                "}";

        try {
            this.asn1List = new JSONObject(asn1json);
        } catch (Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
        }
    }

    public String translateAsn1Oid(String asn1oid)
    {
        if(this.asn1List != null) {
            try {
                String asn1name = this.asn1List.get(asn1oid).toString();
                if(asn1name != null) {
                    return asn1name;
                }
            }
            catch (Exception e) {
                if(e.getMessage() != null) {
                    System.out.println(e.getMessage());
                }
                System.out.println(Log.getStackTraceString(e));
            }
        }

        return "unkown";
    }
}
