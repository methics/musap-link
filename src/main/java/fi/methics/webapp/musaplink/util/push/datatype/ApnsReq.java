//
//(c) Copyright 2003-2021 Methics Technologies Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util.push.datatype;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;


/**
 * AFE side of  HMSSP&rarr;AFE for <i>Applet Push Notification Service</i> calls. 
 */
public class ApnsReq extends GsonMessage {

    @SerializedName("payload")
    public String payload;
    @SerializedName("token")
    public String token;
    @SerializedName("topic")
    public String topic;
    @SerializedName("collapseId")
    public String collapseId;
    @SerializedName("expiration")
    public long expiration;
    @SerializedName("priority")
    public String priority;
    @SerializedName("pushType")
    public String pushType;
    @SerializedName("uuid")
    public String uuid;

    public static ApnsReq from(String json)
        throws JsonSyntaxException
    {
        return GSON.fromJson(json, ApnsReq.class);
    }

    public static ApnsReq from(byte[] json)
            throws JsonSyntaxException
    {
        return fromBytes(json, ApnsReq.class);
    }
}
