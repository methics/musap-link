package fi.methics.webapp.musaplink.util.etsi204;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;

import fi.laverca.etsi.EtsiResponse;
import fi.laverca.mss.MssException;

public class Etsi204Response {

    private static final Log log = LogFactory.getLog(Etsi204Response.class);
    private byte[] signature;
    private byte[] publickey;
    private byte[] certificate;
    
    private List<byte[]> certChain;
    
    /**
     * Create a response from a Laverca SOAP response
     * @param resp Laverca SOAP response
     */
    protected Etsi204Response(EtsiResponse resp) {
        this.signature = resp.getSignature().getRawSignature();
        try {
            if (resp.getPkcs7Signature() != null && resp.getPkcs7Signature().getSignerCert() != null) {
                this.certificate = resp.getPkcs7Signature().getSignerCert().getEncoded();
                this.publickey   = resp.getPkcs7Signature().getSignerCert().getPublicKey().getEncoded();
                this.certChain   = this.getCertificateChain(resp);
            }
        } catch (Exception e) {
            log.error("Failed to parse response details", e);
        }
    }
    
    /**
     * Create a response from a raw CMS signature
     * @param signature CMS signature
     */
    protected Etsi204Response(byte[] signature) {
        this.signature = signature;
        try {
            ContentInfo   ci = ContentInfo.getInstance(this.signature);
            CMSSignedData cd = new CMSSignedData(ci);
            
            Collection<X509CertificateHolder> certs = cd.getCertificates().getMatches(null);
            if (!certs.isEmpty()) {
                X509CertificateHolder signerCert = certs.stream().findFirst().get();
                this.certificate = signerCert.getEncoded();
                this.publickey   = signerCert.getSubjectPublicKeyInfo().getEncoded();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Get the raw signature response
     * @return Signature
     */
    public byte[] getSignature() {
        return this.signature;
    }
    
    /**
     * Get the certificate chain
     * @return certificate chain
     */
    public List<byte[]> getCertificateChain() {
        return this.certChain;
    }
    
    /**
     * Get the signature as base64 String
     * @return signature base64
     */
    public String getSignatureB64() {
        byte[] sig = this.getSignature();
        if (sig == null) return null;
        return Base64.getEncoder().encodeToString(sig);
    }
    
    /**
     * Get the raw certificate 
     * @return raw certificate
     * @throws MssException 
     * @throws CertificateEncodingException 
     */
    public byte[] getCertificate() {
        return this.certificate;
    }

    /**
     * Get public key
     * @return public key as byte[]
     * @throws MssException
     */
    public byte[] getPublicKey() {
        return this.publickey;
    }
    
    /**
     * Get public key as base64
     * @return public key baes64
     * @throws MssException
     */
    public String getPublicKeyB64() {
        byte[] pubkey = this.getPublicKey();
        return Base64.getEncoder().encodeToString(pubkey);
    }

    /**
     * Get certificate as base64 String
     * @return certificate base64
     * @throws CertificateEncodingException
     * @throws MssException
     */
    public String getCertificateB64() {
        byte[] cert = this.getCertificate();
        return Base64.getEncoder().encodeToString(cert);
    }
    
    /**
     * Get the certificate chain
     * @param resp ETSI TS 102 204 response
     * @return Certificate chain
     */
    private List<byte[]> getCertificateChain(EtsiResponse resp) {
        return resp.getPkcs7Signature().getCertificateChain().stream().map(c -> {
            try {
                return c.getEncoded();
            } catch (CertificateEncodingException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
}
