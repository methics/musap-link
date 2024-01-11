//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.coupling.json;

import fi.methics.webapp.musaplink.util.GsonMessage;

/**
 * Generic base for MUSAP Coupling API MT/MO payloads
 */
public class CouplingApiPayload extends GsonMessage {

    // General values expected in MO messages
    public String os;
    public String version;
    public String osversion;
    public String model;
    
    // General values expected in MT messages
    public String status;

    /**
     * Validate the payload 
     * @throws Exception if payload is not valid for any reason.
     */
    public void validate() throws Exception {
        // Default implementation is empty
    }

}
