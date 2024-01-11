//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.util.push.datatype;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

public class FcmReq extends GsonMessage {

    @SerializedName("message")
    public Message message;
    
    public String getToken() {
        if (this.message == null) return null;
        return this.message.token;
    }
    
    /**
     * Create an FCM Push Notification request
     * @param token FCM token
     * @param title Notification title
     * @param body  Notification body
     * @return
     */
    public static FcmReq makeNotificationMessage(String token, String title) {
        
        FcmReq msg = new FcmReq();
        msg.message = new Message();
        msg.message.token = token;
        msg.message.notification = new Notification();
        msg.message.notification.title = title;
        return msg;
    }
    
    
    public static class Message {
        
        @SerializedName("token")
        public String token;
        
        @SerializedName("notification")
        public Notification notification;
        
        public Map<String, String> data;
        
    }
    
    public static class Notification {
        
        @SerializedName("title")
        public String title;

        @SerializedName("body")
        public String body;
    }
    
}
