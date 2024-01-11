//
//  (c) Copyright 2003-2022 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.cmd;

import java.io.IOException;
import java.util.UUID;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.EnrollDataReq;
import fi.methics.webapp.musaplink.coupling.json.EnrollDataResp;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;

public class CmdEnrollData extends CouplingCommand {

    public CmdEnrollData(CouplingApiMessage req) {
        super(req, EnrollDataReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws IOException {
        final CouplingApiMessage req = this.getRequest();

        log.debug("Calling activation");

        final EnrollDataReq payload = this.getRequestPayload();

        MusapLinkAccount account = new MusapLinkAccount();
        account.apnsToken = payload.apnstoken;
        account.fcmToken  = payload.fcmtoken;
        account.musapid   = UUID.randomUUID().toString();
        
        log.debug("Storing account with MusapID " + account.musapid);
        AccountStorage.storeAccount(account);
        
        EnrollDataResp resp = new EnrollDataResp();
        resp.musapid = account.musapid;
        
        return req.createResponse(resp);
    }
    
}
