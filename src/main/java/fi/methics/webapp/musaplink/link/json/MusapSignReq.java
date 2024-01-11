//
//  (c) Copyright 2003-2020 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.coupling.json.SignatureReq;
import fi.methics.webapp.musaplink.coupling.json.SignatureReq.Key;

/**
 * MUSAP Link API /sign request
 */
public class MusapSignReq extends MusapReq {

    @SerializedName("data")
    public String data;

    @SerializedName("display")
    public String display = "Sign with MUSAP";
    
    @SerializedName("linkid")
    public String linkid;
    
    @SerializedName("generatenew")
    public Boolean generatenew;
    
    @SerializedName("key")
    public Key key;

    public transient String transid;
    
    /**
     * Convert this MUSAP Link API request to a Coupling API request.
     * This can be then picked up by MUSAP via the GetData call.
     * @return Coupling API request
     */
    public SignatureReq toCouplingRequest() {
        SignatureReq req = new SignatureReq();
        
        req.data    = this.data;
        req.display = this.display;
        req.linkid  = this.linkid;
        req.transid = this.transid;

        if (this.key != null) {
            req.key = new SignatureReq.Key();
            req.key.keyid     = this.key.keyid;
            req.key.keyname   = this.key.keyname;
            req.key.keyusage  = this.key.keyusage;
            req.key.publickeyhash = this.key.publickeyhash;
        }
        
        if (this.generatenew != null && this.generatenew.booleanValue()) {
            req.mode = SignatureReq.MODE_GENSIGN;
        } else {
            req.mode = SignatureReq.MODE_SIGN;
        }
        return req;
    }

    public static class Key {
        
        @SerializedName("keyid")
        public String keyid;

        @SerializedName("keyalias")
        public String keyalias;
        
        @SerializedName("publickeyhash")
        public String publickeyhash;

        @SerializedName("keyname")
        public String keyname;

        @SerializedName("keyusage")
        public String keyusage;
        
    }
    
}
