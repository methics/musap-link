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

    @SerializedName("keyname")
    public String keyname;

    @SerializedName("keyusage")
    public String keyusage;

    @SerializedName("keyalgorithm")
    public String keyalgorithm;
    
    public transient String transid;
    
    public SignatureReq toSignReq() {
        SignatureReq req = new SignatureReq();
        req.mode    = SignatureReq.MODE_GENONLY;
        req.display = this.display;
        req.linkid  = this.linkid;
        req.key = new SignatureReq.Key();
        req.key.keyname   = this.keyname;
        req.key.keyusage  = this.keyusage;
        req.key.algorithm = this.keyalgorithm;
        return req;
    }
    
}
