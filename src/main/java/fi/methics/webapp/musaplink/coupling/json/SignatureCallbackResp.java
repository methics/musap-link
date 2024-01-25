package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.link.json.MusapCertificate;
import fi.methics.webapp.musaplink.link.json.MusapPublicKey;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.link.json.MusapSignature;

/**
 * MUSAP Signature Callback
 */
public class SignatureCallbackResp extends CouplingApiPayload {

    @SerializedName("signature")
    public String signature;
    
    @SerializedName("publickey")
    public String publickey;

    @SerializedName("certificate")
    public String certificate;
    
    @SerializedName("linkid")
    public String linkid;

    @SerializedName("keyid")
    public String keyid;

    @SerializedName("keyuri")
    public String keyuri;
    
    public static SignatureCallbackResp fromJson(final String str) {
        return GSON.fromJson(str, SignatureCallbackResp.class);
    }
    
    public MusapSignResp toSignResp() {
        MusapSignResp resp = new MusapSignResp();
        
        resp.publickey   = new MusapPublicKey();
        resp.signature   = new MusapSignature();
        
        resp.signature.raw    = this.signature;
        resp.publickey.pem    = this.publickey;
        resp.publickey.keyid  = this.keyid;
        resp.publickey.keyuri = this.keyuri;
        
        if (this.certificate != null) {
            resp.certificate = new MusapCertificate();
            resp.certificate.pem  = this.certificate;
        }
        resp.linkid = this.linkid;
        resp.keyid  = this.keyid;
        return resp;
    }

}
