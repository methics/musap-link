//
//(c) Copyright 2003-2023 Methics Technologies Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util.push.datatype;

import java.time.Instant;

import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import fi.methics.json.GsonMessage;

/**
 * 
 * APNS response delivered from AFE to HMSSP.
 *
 */
public class ApnsResp extends GsonMessage {
    
    @SerializedName("appId")
    public String appId;
    
    @SerializedName("transId")
    public String transId;
    
    @SerializedName("messageId")
    public String messageId;
    
    @SerializedName("apnsId")
    public String apnsId;

    @SerializedName("isAccepted")
    public boolean isAccepted;

    @SerializedName("rejectionReason")
    public String rejectionReason;

    @SerializedName("tokenInvalidationTimestamp")
    public String tokenInvalidationTimestamp;

    @SerializedName("token")
    public String token;

    @SerializedName("topic")
    public String topic;

    @SerializedName("priority")
    public String priority;
    
    @SerializedName("pushType")
    public String pushType;
    
    @SerializedName("timeStamp")
    public String timeStamp;

    @SerializedName("expiration")
    public String expiration;
    
    public ApnsResp() {
        this.timeStamp = Instant.now().toString();
    }
    
    public ApnsResp(final PushNotificationResponse<SimpleApnsPushNotification> response) {
        this.timeStamp = Instant.now().toString();
        if (response == null) return;
        this.apnsId          = response.getApnsId().toString();
        this.isAccepted      = response.isAccepted();
        this.rejectionReason = response.getRejectionReason();
        this.tokenInvalidationTimestamp = response.getTokenInvalidationTimestamp().map(Instant::toString).orElse(null);
        this.readDetails(response.getPushNotification());
    }
    
    public ApnsResp(final SimpleApnsPushNotification pushNotification, final Throwable e) {
        this.timeStamp       = Instant.now().toString();
        this.isAccepted      = false;
        this.rejectionReason = e.getMessage();
        this.readDetails(pushNotification);

    }
    
    public ApnsResp(final SimpleApnsPushNotification pushNotification, final String rejectionReason) {
        this.timeStamp       = Instant.now().toString();
        this.isAccepted      = false;
        this.rejectionReason = rejectionReason;
        this.readDetails(pushNotification);
    }
    
    /**
     * Read push notification details
     * @param pushNotification
     */
    private void readDetails(final SimpleApnsPushNotification pushNotification) {
        if (pushNotification != null) {
            this.token      = pushNotification.getToken();
            this.pushType   = pushNotification.getPushType().name();
            this.topic      = pushNotification.getTopic();
            this.priority   = pushNotification.getPriority() != null ? ""+pushNotification.getPriority().name() : null;
            this.expiration = pushNotification.getExpiration() != null ? pushNotification.getExpiration().toString() : null;
        }
    }
    
    public static ApnsResp from(String json)
        throws JsonSyntaxException
    {
        return GSON.fromJson(json, ApnsResp.class);
    }

}
