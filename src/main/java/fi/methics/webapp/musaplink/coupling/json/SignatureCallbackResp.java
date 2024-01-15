package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

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
        
        resp.publickey = new MusapPublicKey();
        resp.signature = new MusapSignature();
        
        resp.publickey.pem    = this.publickey;
        resp.signature.raw    = this.signature;
        resp.publickey.keyid  = this.keyid;
        resp.publickey.keyuri = this.keyuri;
        
        resp.linkid = this.linkid;
        return resp;
    }

}
