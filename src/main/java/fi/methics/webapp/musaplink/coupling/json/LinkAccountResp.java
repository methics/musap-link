package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Link Account response payload
 */
public class LinkAccountResp extends CouplingApiPayload {

    @SerializedName("linkid")
    public String linkid;

    @SerializedName("name")
    public String name;
    
    public LinkAccountResp(String linkid, String name) {
        this.linkid = linkid;
        this.name   = name;
        this.status = "success";
    }
    
    public static LinkAccountResp fromJson(final String str) {
        return GSON.fromJson(str, LinkAccountResp.class);
    }

}
