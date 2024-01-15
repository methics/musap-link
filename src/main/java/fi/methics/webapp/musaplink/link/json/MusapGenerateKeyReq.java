//
//  (c) Copyright 2003-2020 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.coupling.json.SignatureReq;

public class MusapGenerateKeyReq extends MusapReq {

    @SerializedName("display")
    public String display = "Generate a new key";
    
    @SerializedName("linkid")
    public String linkid;

    @SerializedName("key")
    public Key key;
    
    public transient String transid;
    
    public SignatureReq toSignReq() {
        SignatureReq req = new SignatureReq();
        req.mode    = SignatureReq.MODE_GENONLY;
        req.display = this.display;
        req.linkid  = this.linkid;
        req.key = new SignatureReq.Key();
        req.key.keyname   = this.key.keyname;
        req.key.keyusage  = this.key.keyusage;
        req.key.algorithm = this.key.keyalgorithm;
        return req;
    }
    public static class Key {
        
        @SerializedName("keyalgorithm")
        public String keyalgorithm;
        
        @SerializedName("keyname")
        public String keyname;

        @SerializedName("keyusage")
        public String keyusage;
        
    }
    
    
}
