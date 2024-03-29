package fi.methics.webapp.musaplink.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fi.methics.webapp.musaplink.coupling.json.MusapErrorMsg;
import fi.methics.webapp.musaplink.link.json.MusapResp;
public class MusapException extends WebApplicationException {

    private static final long serialVersionUID = 1L;
    private int errorcode;
    
    public MusapException(final Exception e) {
        super(e);
        this.errorcode = MusapErrorMsg.ERROR_INTERNAL;
    }

    public MusapException(String msg) {
        super(msg);
        this.errorcode = MusapErrorMsg.ERROR_INTERNAL;
    }
    
    public MusapException(MusapResp error) {
        if (error.errorcode != null) {
            this.errorcode = error.errorcode;
        } else {
            this.errorcode = MusapErrorMsg.ERROR_INTERNAL;
        }
    }
    
    public MusapException(int errorcode) {
        this.errorcode = errorcode;
    }
    
    public MusapException(int errorcode, String msg) {
        super(msg);
        this.errorcode = errorcode;
    }
    
    public int getErrorCode() {
        return this.errorcode;
    }
    
    @Override
    public Response getResponse() {
        return Response.serverError().entity(MusapResp.createError(this.getErrorCode(), this.getMessage()).toJson()).build();
    }
    
}
