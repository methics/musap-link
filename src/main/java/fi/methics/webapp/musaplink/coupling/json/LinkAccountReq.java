//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Link Account request payload
 */
public class LinkAccountReq extends CouplingApiPayload {

    @SerializedName("couplingcode")
    public String couplingcode;
    
    @SerializedName("musapid")
    public String musapid;
   
    public static LinkAccountReq fromJson(final String str) {
        return GSON.fromJson(str, LinkAccountReq.class);
    }

}
