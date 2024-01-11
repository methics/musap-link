//
//  (c) Copyright 2003-2022 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.cmd;

import java.io.IOException;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.LinkAccountReq;
import fi.methics.webapp.musaplink.coupling.json.LinkAccountResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapException;

public class CmdLinkAccount extends CouplingCommand {

    public CmdLinkAccount(CouplingApiMessage req) {
        super(req, LinkAccountReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws IOException {
        final CouplingApiMessage req = this.getRequest();

        log.debug("Calling link account");

        final LinkAccountReq payload = this.getRequestPayload();
        final String          linkid = AccountStorage.findLinkId(payload.couplingcode);
        
        if (linkid != null) {
            AccountStorage.addLinkId(payload.musapid, linkid);
            return req.createResponse(new LinkAccountResp());
        } else {
            throw new MusapException(MusapResp.ERROR_COUPLING_ERROR);
        }
        
    }
    
}
