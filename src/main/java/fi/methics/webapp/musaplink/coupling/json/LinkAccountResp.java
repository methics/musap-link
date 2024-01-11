//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

/**
 * MUSAP Link Account response payload
 */
public class LinkAccountResp extends CouplingApiPayload {

    public LinkAccountResp() { 
        this.status = "success";
    }
    public static LinkAccountResp fromJson(final String str) {
        return GSON.fromJson(str, LinkAccountResp.class);
    }

}
