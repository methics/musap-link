package fi.methics.webapp.musaplink.coupling.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * Generic base for MUSAP Coupling API MT/MO payloads
 */
public class CouplingApiPayload extends GsonMessage {

    // General values expected in MO messages
    @SerializedName("os")
    public String os;
    
    @SerializedName("version")
    public String version;

    @SerializedName("osversion")
    public String osversion;
    
    @SerializedName("model")
    public String model;
    
    @SerializedName("nonce")
    public String nonce;
    
    // General values expected in MT messages
    @SerializedName("status")
    public String status;

    /**
     * Validate the payload 
     * @throws Exception if payload is not valid for any reason.
     */
    public void validate() throws Exception {
        // Default implementation is empty
    }

}
