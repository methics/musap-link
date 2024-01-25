package fi.methics.webapp.musaplink.coupling.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.X509Util;

/**
 * MUSAP External signature response.
 */
public class ExternalSignatureResp extends CouplingApiPayload {

    @SerializedName("transid")
    public String transid;

    @SerializedName("keyid")
    public String keyid;
    
    @SerializedName("signature")
    public String signature;

    @SerializedName("publickey")
    public String publickey;

    @SerializedName("certificate")
    public String certificate;

    @SerializedName("attributes")
    public Map<String, String> attributes = new HashMap<>();
    
    public static ExternalSignatureResp fromJson(final String str) {
        return GSON.fromJson(str, ExternalSignatureResp.class);
    }
    
    /**
     * Get the public key as raw byte[]
     * @return public key
     */
    public byte[] getPublicKey() {
        if (this.publickey == null) return null;
        try {
            return X509Util.maybeDecodePEM(this.publickey);
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
        try {
            return X509Util.maybeDecodePEM(this.certificate);
        } catch (Exception e) {
            return null;
        }
    }
    
}
