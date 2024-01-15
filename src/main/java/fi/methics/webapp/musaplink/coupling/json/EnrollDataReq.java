package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * MUSAP Enroll Data request payload
 */
public class EnrollDataReq extends CouplingApiPayload {

    @SerializedName("fcmtoken")
    public String fcmtoken;
    
    @SerializedName("apnstoken")
    public String apnstoken;
    
    // This is always encrypted
    @SerializedName("tokendata")
    public String tokendata;
   
    public static EnrollDataReq fromJson(final String str) {
        return GSON.fromJson(str, EnrollDataReq.class);
    }

    /**
     * Security related tokens & shared secret.
     * These can be encrypted by the app and decrypted on SAM
     */
    public static class TokenData extends GsonMessage {
        @SerializedName("secret")
        public String secret;
        public static EnrollDataReq.TokenData fromJson(String json) {
            return GsonMessage.fromJson(json, EnrollDataReq.TokenData.class);
        }
        public static EnrollDataReq.TokenData fromBase64(String b64) {
            return GsonMessage.fromBase64(b64, EnrollDataReq.TokenData.class);
        }
    }
    
}
