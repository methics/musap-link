//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * MUSAP Link API response base
 */
public class MusapResp extends GsonMessage {

    public static final int ERROR_WRONG_PARAM        = 101;
    public static final int ERROR_MISSING_PARAM      = 102;
    public static final int ERROR_INVALID_ALGORITHM  = 103;
    public static final int ERROR_UNKNOWN_KEY        = 105;
    public static final int ERROR_UNKNOWN_USER       = 106;
    public static final int ERROR_UNSUPPORTED_DATA   = 107;
    public static final int ERROR_KEYGEN_UNSUPPORTED = 108;
    public static final int ERROR_BIND_UNSUPPORTED   = 109;
    public static final int ERROR_TIMED_OUT          = 208;
    public static final int ERROR_USER_CANCEL        = 401;
    public static final int ERROR_KEY_BLOCKED        = 402;
    public static final int ERROR_SSCD_BLOCKED       = 403;
    public static final int ERROR_SSCD_UNREACHABLE   = 404;
    public static final int ERROR_COUPLING_ERROR     = 405;
    public static final int ERROR_INTERNAL           = 900;
    
    @SerializedName("errorcode")
    public Integer errorcode;

    @SerializedName("errorname")
    public String errorname;
    
    public MusapResp() {
        
    }
    
    public static MusapResp createError(int errorcode) {
        MusapResp resp = new MusapResp();
        resp.errorcode = errorcode;
        resp.errorname = resp.getErrorName(errorcode);
        return resp;
    }
    
    public static Response createErrorResponse(int errorcode) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(MusapResp.createError(errorcode)).build();
    }
    
    private String getErrorName(int errorcode) {
        switch (errorcode) {
            case ERROR_WRONG_PARAM:        return "wrong_param";
            case ERROR_MISSING_PARAM:      return "missing_param";
            case ERROR_INVALID_ALGORITHM:  return "invalid_algorithm";
            case ERROR_UNKNOWN_KEY:        return "unknown_key";
            case ERROR_UNKNOWN_USER:       return "unknown_user";
            case ERROR_UNSUPPORTED_DATA:   return "unsupported_data";
            case ERROR_KEYGEN_UNSUPPORTED: return "keygen_unsupported";
            case ERROR_TIMED_OUT:          return "timed_out";
            case ERROR_USER_CANCEL:        return "user_cancel";
            case ERROR_KEY_BLOCKED:        return "key_blocked";
            case ERROR_SSCD_BLOCKED:       return "sscd_blocked";
            case ERROR_SSCD_UNREACHABLE:   return "sscd_unreachable";
            case ERROR_COUPLING_ERROR:     return "coupling_error";
            case ERROR_INTERNAL: default:  return "internal_error";
        }
    }
    
}
