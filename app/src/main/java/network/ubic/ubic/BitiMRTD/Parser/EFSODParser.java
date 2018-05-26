package network.ubic.ubic.BitiMRTD.Parser;

import android.util.Log;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.icao.LDSSecurityObject;
import org.spongycastle.asn1.util.ASN1Dump;
import org.spongycastle.asn1.x500.AttributeTypeAndValue;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cms.CMSSignedDataParser;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;
import org.spongycastle.util.Store;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.spongycastle.asn1.x500.RDN;

import network.ubic.ubic.BitiMRTD.Converter.ASN1Converter;
import network.ubic.ubic.BitiMRTD.Tools.Tools;

public class EFSODParser
{

    private Tools tools;

    private String ldsVersion;
    private String ldsHashAlgorithmOID;

    private String issuerCertificationAuthority;
    private String issuerCountry;
    private String issuerOrganization;
    private String issuerOrganizationalUnit;
    private String signatureAlgorithmOID;
    private Date validFrom;
    private Date validUntil;


    public EFSODParser(byte[] efsod)
    {

        this.tools = new Tools();
        TagParser tagParser = new TagParser(efsod);
        byte[] tag77 = tagParser.geTag("77").getBytes();


        try {

            CMSSignedDataParser cmsSignedDataParser = new CMSSignedDataParser(new BcDigestCalculatorProvider(), tag77);
            System.out.println("Version : ".concat(String.valueOf(cmsSignedDataParser.getVersion())));

            byte[] cmsContent = this.tools.inputStreamToByteArray(
                    cmsSignedDataParser.getSignedContent().getContentStream()
            );

            System.out.println("cmsContent Type : ".concat(cmsSignedDataParser.getSignedContentTypeOID().toString()));
            System.out.println("cmsContent : ".concat(cmsSignedDataParser.getSignedContent().toString()));
            System.out.println("cmsContent size : ".concat(String.valueOf(cmsContent.length)));

            LDSSecurityObject lds = LDSSecurityObject.getInstance(cmsContent);
            System.out.println("LDS version : ".concat(String.valueOf(lds.getVersion())));
            System.out.println("LDS hash algorithm : ".concat(
                    ASN1Converter.getInstance().translateAsn1Oid(
                            lds.getDigestAlgorithmIdentifier().getAlgorithm().toString()
                    )
            ));
            lds.getEncoded();

            this.ldsHashAlgorithmOID = lds.getDigestAlgorithmIdentifier().getAlgorithm().toString();

            //lds.getDatagroupHash();

            cmsSignedDataParser.getSignedContent().drain();

            Store certStore = cmsSignedDataParser.getCertificates();
            Collection signers = cmsSignedDataParser.getSignerInfos().getSigners();
            Iterator it = signers.iterator();

            while(it.hasNext()) {
                SignerInformation signer = (SignerInformation) it.next();
                Collection certCollection = certStore.getMatches(null);

                Iterator certIt = certCollection.iterator();
                while(certIt.hasNext()) {
                    X509CertificateHolder cert = (X509CertificateHolder) certIt.next();

                    System.out.println("Issuer : ".concat(cert.getIssuer().toString()));

                    for (RDN rdn:cert.getIssuer().getRDNs()) {
                        for(AttributeTypeAndValue attr : rdn.getTypesAndValues()) {
                            System.out.println("type : ".concat(attr.getType().toString()));
                            System.out.println("value : ".concat(attr.getValue().toString()));

                            if(attr.getType().toString().equals("2.5.4.3")) {
                                this.issuerCertificationAuthority = attr.getValue().toString();
                            }
                            if(attr.getType().toString().equals("2.5.4.6")) {
                                this.issuerCountry = attr.getValue().toString();
                            }
                            if(attr.getType().toString().equals("2.5.4.10")) {
                                this.issuerOrganization = attr.getValue().toString();
                            }
                            if(attr.getType().toString().equals("2.5.4.11")) {
                                this.issuerOrganizationalUnit = attr.getValue().toString();
                            }
                        }

                    }

                    System.out.println("Signature algorithm : ".concat(
                            ASN1Converter.getInstance().translateAsn1Oid(
                                    cert.getSignatureAlgorithm().getAlgorithm().toString()
                            )
                    ));
                    this.signatureAlgorithmOID = cert.getSignatureAlgorithm().getAlgorithm().toString();

                    System.out.println("Valid from : ".concat(
                            SimpleDateFormat.getDateInstance().format(cert.getNotBefore())
                    ));
                    this.validFrom = cert.getNotBefore();

                    System.out.println("Valid until : ".concat(
                            SimpleDateFormat.getDateInstance().format(cert.getNotAfter())
                    ));
                    this.validUntil = cert.getNotAfter();

                    /*
                    if(signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert)))
                        System.out.println("Signature verification: SUCCESS");
                    else
                        System.out.println("Signature verification: FAILED");*/

                    //@TODO what if several certificates???
                }
            }

        }
        catch(Exception e) {
            if(e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
            System.out.println(Log.getStackTraceString(e));
        }
    }


    public String getLdsVersion()
    {
        return this.ldsVersion;
    }

    public String getLdsHashAlgorithmOID()
    {
        return this.ldsHashAlgorithmOID;
    }

    public String getLdsHashAlgorithm()
    {
        return ASN1Converter.getInstance().translateAsn1Oid(this.ldsHashAlgorithmOID);
    }

    public String getIssuerCountry()
    {
        return this.issuerCountry;
    }

    public String getIssuerCertificationAuthority()
    {
        return this.issuerCertificationAuthority;
    }

    public String getIssuerOrganization()
    {
        return this.issuerOrganization;
    }

    public String getIssuerOrganizationalUnit()
    {
        return this.issuerOrganizationalUnit;
    }

    public String getSignatureAlgorithmOID()
    {
        return this.signatureAlgorithmOID;
    }

    public String getSignatureAlgorithm()
    {
        return ASN1Converter.getInstance().translateAsn1Oid(this.signatureAlgorithmOID);
    }

    public Date getValidFrom()
    {
        return this.validFrom;
    }

    public String getValidFromString()
    {
        if(this.validFrom == null) {
            return "";
        }
        return SimpleDateFormat.getDateInstance().format(this.validFrom);
    }

    public Date getValidUntil()
    {
        return this.validUntil;
    }

    public String getValidUntilString()
    {
        if(this.validUntil == null) {
            return "";
        }
        return SimpleDateFormat.getDateInstance().format(this.validUntil);
    }

}
