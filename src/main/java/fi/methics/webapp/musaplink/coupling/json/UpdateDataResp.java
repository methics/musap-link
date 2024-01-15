//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Update Data response payload
 */
public class UpdateDataResp extends CouplingApiPayload {

    @SerializedName("musapid")
    public String musapid;
    
    public UpdateDataResp() { 
        this.status = "success";
    }
    public static UpdateDataResp fromJson(final String str) {
        return GSON.fromJson(str, UpdateDataResp.class);
    }
}
