package fi.methics.webapp.musaplink.coupling.json;

import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Exception;

/**
 * MUSAP Coupling API Error payload
 */
public class MusapErrorMsg extends CouplingApiPayload {

    // Error codes
    public static final int ERROR_INVALID_REQ         = 101;
    public static final int ERROR_UNAUTHORIZED        = 104;
    public static final int ERROR_UNKNOWN_CLIENT      = 105;
    public static final int ERROR_USER_CANCEL         = 401;
    public static final int ERROR_PIN_BLOCKED         = 402;
    public static final int ERROR_ALREADY_IN_PROGRESS = 406;
    public static final int ERROR_ALREADY_HANDLED     = 407;
    public static final int ERROR_WRONG_PIN           = 411;
    public static final int ERROR_UNABLE_TO_PROVIDE   = 780;
    public static final int ERROR_INTERNAL            = 900;
    public static final int ERROR_TIMED_OUT           = 998;

    // Error response contents
    public String error;
    public String errorcode;
    
    public static MusapErrorMsg fromJson(final String str) {
        return GSON.fromJson(str, MusapErrorMsg.class);
    }
    
    public MusapErrorMsg() {
        // Do nothing
    }
    
    /**
     * Create an error payload from an error code
     * @param errorcode Error code
     */
    public MusapErrorMsg(final int errorcode) {
        this.errorcode = String.valueOf(errorcode);
        this.status    = "failed";
    }
    
    /**
     * Create an error payload from an exception
     * @param e Exception
     */
    public MusapErrorMsg(final Exception e) {
        
        if (e instanceof Etsi204Exception) {
            this.errorcode = ((Etsi204Exception)e).toMusapErrorMsg().errorcode;
        } else {
            this.errorcode = String.valueOf(MusapErrorMsg.ERROR_INTERNAL);
        }
        
        this.status = "failed";
    }
    
    public MusapResp toMusapResp() {
        if (this.errorcode == null) {
            return MusapResp.createError(MusapResp.ERROR_INTERNAL);
        }
        return MusapResp.createError(Integer.parseInt(this.errorcode));
    }

    
}
