package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Enroll Data response payload
 */
public class EnrollDataResp extends CouplingApiPayload {


    @SerializedName("musapid")
    public String musapid;
    
    public EnrollDataResp() { 
        this.status = "success";
    }
    public static EnrollDataResp fromJson(final String str) {
        return GSON.fromJson(str, EnrollDataResp.class);
    }
}
