package fi.methics.webapp.musaplink.util.etsi204;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;

import fi.laverca.etsi.EtsiResponse;
import fi.laverca.mss.MssException;

public class Etsi204Response {

    private EtsiResponse resp;
    
    protected Etsi204Response(EtsiResponse resp) {
        this.resp = resp;
    }
    
    /**
     * Get the raw signature response
     * @return Signature
     */
    public byte[] getSignature() {
        if (this.resp == null) return null;
        if (this.resp.getSignature() == null) return null;
        return this.resp.getSignature().getRawSignature();
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
    public byte[] getCertificate() throws CertificateEncodingException, MssException {
        if (this.resp == null) return null;
        if (this.resp.getSignature() == null) return null;
        if (this.resp.getPkcs7Signature().getSignerCert() == null) return null;
        return this.resp.getPkcs7Signature().getSignerCert().getEncoded();
    }
    
}
