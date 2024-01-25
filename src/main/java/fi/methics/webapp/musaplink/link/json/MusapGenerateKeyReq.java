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
        
        if (this.key != null) {
            req.key = new SignatureReq.Key();
            req.key.keyname   = this.key.keyname;
            req.key.keyusage  = this.key.keyusage;
            req.key.algorithm = this.key.algorithm;
        }
        return req;
    }
    public static class Key {
        
        @SerializedName(value="algorithm", alternate="keyalgorithm")
        public String algorithm;
        
        @SerializedName("keyname")
        public String keyname;

        @SerializedName("keyusage")
        public String keyusage;
        
    }
    
    
}
