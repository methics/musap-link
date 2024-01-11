//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.json;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP External signature request.
 * This is used by MUSAP to request signatures through MUSAP Link.
 */
public class ExternalSignatureReq extends CouplingApiPayload {

    @SerializedName("clientid")
    public String clientid;

    @SerializedName("sscdname")
    public String sscdname;

    @SerializedName("sscdtype")
    public String sscdtype;

    @SerializedName("data")
    public String data;

    @SerializedName("display")
    public String display;

    @SerializedName("format")
    public String format;

    @SerializedName("publickey")
    public String publickey;

    @SerializedName("timeout")
    public String timeout;
    
    @SerializedName("attributes")
    public Map<String, String> attributes = new HashMap<>();
    
    @SerializedName("transid")
    public String transid;
    
    public static ExternalSignatureReq fromJson(final String str) {
        return GSON.fromJson(str, ExternalSignatureReq.class);
    }
    
    /**
     * Get the DTBS as byte[]
     * @return DTBS
     */
    public byte[] getDtbs() {
        return Base64.getDecoder().decode(this.data);
    }
    
    /**
     * Get a transaction id assigned to this request by MUSAP Link
     * @return Transaction ID
     */
    public String getTransId() {
        return this.transid;
    }

}
