package fi.methics.webapp.musaplink.coupling.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * MUSAP Coupling API Signature Request.
 * <p>This request is returned by the GetData operation.
 * <p>The request supports three operatin modes:
 * <ol>
 * <li>sign          - Request only signature with specified keyid or publickey
 * <li>generate-sign - Request generating a new key, and signing with it
 * <li>generate-only - Request only generating a new key and returning it
 * </ol>
 */
public class SignatureReq extends CouplingApiPayload {

    public static final String MODE_SIGN       = "sign";
    public static final String MODE_SIGNCHOICE = "sign-choice";
    public static final String MODE_GENSIGN    = "generate-sign";
    public static final String MODE_GENONLY    = "generate-only";
    
    @SerializedName("mode")
    public String mode = MODE_SIGN;

    @SerializedName("data")
    public String data;

    @SerializedName("display")
    public String display = "Sign with MUSAP";
    
    @SerializedName("linkid")
    public String linkid;

    @SerializedName("key")
    public Key key;
    
    // For mode sign-choice
    @SerializedName("datachoice")
    public List<DTBS> datachoice;
    
    public transient String transid;
    
    public static class Key extends GsonMessage {
        
        @SerializedName("keyid")
        public String keyid;

        @SerializedName("keyname")
        public String keyname;

        @SerializedName("keyusage")
        public String keyusage;
        
        @SerializedName("publickeyhash")
        public String publickeyhash;

        @SerializedName("algorithm")
        public String algorithm;
    }
    
    public static class DTBS {
        
        @SerializedName("data")
        public String data;
        
        @SerializedName("key")
        public Key key;

        @SerializedName("mimetype")
        public String mimetype;
        
    }
    
}
