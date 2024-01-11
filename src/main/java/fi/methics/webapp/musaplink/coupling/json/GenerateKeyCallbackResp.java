//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.link.json.MusapPublicKey;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.link.json.MusapSignature;

/**
 * MUSAP GenerateKey Callback
 */
public class GenerateKeyCallbackResp extends CouplingApiPayload {

    @SerializedName("publickey")
    public String publickey;
    
    @SerializedName("keyname")
    public String keyname;
    
    @SerializedName("keyuri")
    public String keyuri;

    @SerializedName("keyid")
    public String keyid;
    
    @SerializedName("linkid")
    public String linkid;
    
    public static GenerateKeyCallbackResp fromJson(final String str) {
        return GSON.fromJson(str, GenerateKeyCallbackResp.class);
    }
    
    public MusapSignResp toSignResp() {
        MusapSignResp resp = new MusapSignResp();
        
        resp.publickey = new MusapPublicKey();
        resp.signature = new MusapSignature();
        
        resp.publickey.pem      = this.publickey;
        resp.publickey.keyuri   = this.keyuri;
        resp.publickey.keyid    = this.keyid;
        resp.linkid = this.linkid;
        
        return resp;
    }

}
