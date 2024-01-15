//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Update Data request payload
 */
public class UpdateDataReq extends CouplingApiPayload {

    @SerializedName("fcmtoken")
    public String fcmtoken;
    
    @SerializedName("apnstoken")
    public String apnstoken;
    
    public static UpdateDataReq fromJson(final String str) {
        return GSON.fromJson(str, UpdateDataReq.class);
    }

}
