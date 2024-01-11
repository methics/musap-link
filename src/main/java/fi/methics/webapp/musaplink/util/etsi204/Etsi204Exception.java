//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.util.etsi204;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;

import fi.methics.webapp.musaplink.coupling.json.MusapErrorMsg;

public class Etsi204Exception extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    private String errorMsg;
    private String errorDetail;

    public Etsi204Exception(Exception e) {
        super(e);
        if (e instanceof AxisFault) {
            AxisFault af = (AxisFault)e;
            if (af.getFaultSubCodes() != null) {
                for (QName subcode : af.getFaultSubCodes()) {
                    this.errorCode = subcode.getLocalPart();
                }
            } else {
                this.errorCode = "900";
            }
            this.errorMsg    = af.getFaultReasonText("en");
            this.errorDetail = e.getMessage();
        } else {
            this.errorCode = "900";
            this.errorMsg  = "INTERNAL_ERROR";
            this.errorDetail = e.getMessage();
        }
    }
    
    public String getErrorCode() {
        return this.errorCode;
    }
    
    public String getErrorMsg() {
        return this.errorMsg;
    }
    
    public String getErrorDetail() {
        return this.errorDetail;
    }
    
    public MusapErrorMsg toMusapErrorMsg() {

        MusapErrorMsg error = new MusapErrorMsg();
        if (this.errorCode == null) {
            error.errorcode = String.valueOf(MusapErrorMsg.ERROR_INTERNAL);
        } else {
            switch (this.errorCode.replace("_", "")) {
                case "101": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_INVALID_REQ); break;
                case "104": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_UNAUTHORIZED); break;
                case "105": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_UNKNOWN_CLIENT); break;
                case "208": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_TIMED_OUT); break;
                case "402": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_PIN_BLOCKED); break;
                case "780": error.errorcode = String.valueOf(MusapErrorMsg.ERROR_UNABLE_TO_PROVIDE); break;
                default: error.errorcode = String.valueOf(MusapErrorMsg.ERROR_INTERNAL); break;
            }
        }
        return error;
    }

}
