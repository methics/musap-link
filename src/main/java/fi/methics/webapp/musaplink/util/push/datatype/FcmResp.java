//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.util.push.datatype;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

public class FcmResp extends GsonMessage {

    @SerializedName(value="message_id", alternate="name")
    public String messageid;

    @SerializedName("error")
    public FcmError error;
    
    public boolean isError() {
        return this.error != null;
    }

    public boolean isSuccessful() {
        return !this.isError();
    }

    public static FcmResp from(String json) {
        return GsonMessage.fromJson(json, FcmResp.class);
    }

    public String getError() {
        if (this.error == null) return null;
        return this.error.status;
    }
    
    public static class FcmError {
        
        @SerializedName("code")
        public int code;
        
        @SerializedName("message")
        public String message;

        @SerializedName("status")
        public String status;

    }
    
}
