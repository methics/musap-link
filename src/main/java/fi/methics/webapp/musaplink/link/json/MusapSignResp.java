package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.X509Util;


/**
 * MUSAP Link API /sign response
 */
public class MusapSignResp extends MusapResp {

    @SerializedName("linkid")
    public String linkid;

    @SerializedName("keyid")
    public String keyid;

    @SerializedName("certificate")
    public MusapCertificate certificate;

    @SerializedName("publickey")
    public MusapPublicKey publickey;

    @SerializedName("signature")
    public MusapSignature signature;

    /**
     * Get the public key as raw byte[]
     * @return public key
     */
    public byte[] getPublicKey() {
        if (this.publickey == null) return null;
        if (this.publickey.pem == null) return null;
        try {
            return X509Util.maybeDecodePEM(this.publickey.pem);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get the X509 certificate as raw byte[]
     * @return certificate
     */
    public byte[] getCertificate() {
        if (this.certificate == null) return null;
        if (this.certificate.pem == null) return null;
        try {
            return X509Util.maybeDecodePEM(this.certificate.pem);
        } catch (Exception e) {
            return null;
        }
    }
    
}
