//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.link.cmd;

import java.util.UUID;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapLinkReq;
import fi.methics.webapp.musaplink.link.json.MusapLinkResp;
import fi.methics.webapp.musaplink.util.CouplingCode;
import fi.methics.webapp.musaplink.util.MusapException;

public class CmdLink extends LinkCommand<MusapLinkReq, MusapLinkResp> {

    public CmdLink(MusapLinkReq req) {
        super(req);
    }
    
    @Override
    public MusapLinkResp execute() throws MusapException {
        MusapLinkResp jResp = new MusapLinkResp();
        
        String     linkid = newLinkId();
        CouplingCode code = AccountStorage.newCouplingCode(linkid);
        
        jResp.linkid       = linkid;
        jResp.couplingcode = code.getCode();
        jResp.qrcode       = code.toURL();
        return jResp;
    }

    /**
     * Generate a new Link ID
     * @return Link ID
     */
    public static String newLinkId() {
        return UUID.randomUUID().toString();
    }

    
}
