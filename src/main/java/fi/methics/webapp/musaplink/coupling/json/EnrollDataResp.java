//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * Alauda App Enroll Data response payload
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
