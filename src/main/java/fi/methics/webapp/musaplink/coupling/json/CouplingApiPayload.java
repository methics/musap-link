package fi.methics.webapp.musaplink.coupling.json;

import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * Generic base for MUSAP Coupling API MT/MO payloads
 */
public class CouplingApiPayload extends GsonMessage {

    protected static final Log log = LogFactory.getLog(CouplingApiPayload.class);
    
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

    @SerializedName("timestamp")
    public String timestamp;
    
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
    
    /**
     * Get the message timestamp as a Java {@link Instant}
     * @return timestamp or null if unavailable or unparseable
     */
    public Instant getTimestamp() {
        if (this.timestamp == null) return null;
        try {
            return Instant.parse(this.timestamp);
        } catch (Exception e) {
            log.error("Failed to parse timestamp " + this.timestamp, e);
            return null;
        }
    }

}
