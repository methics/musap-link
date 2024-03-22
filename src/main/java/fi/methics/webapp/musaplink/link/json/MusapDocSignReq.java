package fi.methics.webapp.musaplink.link.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.coupling.json.SignatureReq;
import fi.methics.webapp.musaplink.link.json.MusapSignReq.Key;

/**
 * MUSAP Link API /docsign request
 */
public class MusapDocSignReq extends MusapReq {

    @SerializedName("datachoice")
    public List<DTBS> datachoice;
    
    @SerializedName("display")
    public String display = "Sign with MUSAP";
    
    @SerializedName("linkid")
    public String linkid;
    
    public transient String transid;
    
    /**
     * Convert this MUSAP Link API request to a Coupling API request.
     * This can be then picked up by MUSAP via the GetData call.
     * @return Coupling API request
     */
    public SignatureReq toCouplingRequest() {
        SignatureReq req = new SignatureReq();
        
        req.display = this.display;
        req.linkid  = this.linkid;
        req.transid = this.transid;

        if (this.datachoice != null) {
            for (DTBS dtbs : this.datachoice) {
                if (dtbs == null) continue;
                if (req.datachoice == null) {
                    req.datachoice = new ArrayList<>();
                }
                fi.methics.webapp.musaplink.coupling.json.SignatureReq.DTBS couplingDtbs = new fi.methics.webapp.musaplink.coupling.json.SignatureReq.DTBS();
                couplingDtbs.data = dtbs.data;
                couplingDtbs.mimetype = dtbs.mimetype;
                couplingDtbs.key  = new SignatureReq.Key();
                couplingDtbs.key.keyid         = dtbs.key.keyid;
                couplingDtbs.key.keyname       = dtbs.key.keyname;
                couplingDtbs.key.keyusage      = dtbs.key.keyusage;
                couplingDtbs.key.publickeyhash = dtbs.key.publickeyhash;
                couplingDtbs.key.algorithm     = dtbs.key.algorithm;
                req.datachoice.add(couplingDtbs);
            }
        }
        
        req.mode = SignatureReq.MODE_SIGNCHOICE;
        return req;
    }

    public static class DTBS {
        
        @SerializedName("data")
        public String data;
        
        @SerializedName("key")
        public Key key;

        @SerializedName("mimetype")
        public String mimetype;
        
    }
    
}
