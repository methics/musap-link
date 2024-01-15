//
//  (c) Copyright 2003-2024 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.cmd;

import java.io.IOException;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.UpdateDataReq;
import fi.methics.webapp.musaplink.coupling.json.UpdateDataResp;

public class CmdUpdateData extends CouplingCommand {

    public CmdUpdateData(CouplingApiMessage req) {
        super(req, UpdateDataReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws IOException {
        final CouplingApiMessage req = this.getRequest();

        log.debug("Calling data update");

        final UpdateDataReq payload = this.getRequestPayload();

        MusapLinkAccount account = AccountStorage.findByMusapId(req.musapid);
        account.apnsToken = payload.apnstoken;
        account.fcmToken  = payload.fcmtoken;
        
        AccountStorage.updateAccount(req.musapid, account);
        
        UpdateDataResp resp = new UpdateDataResp();
        resp.musapid = account.musapid;
        
        return req.createResponse(resp);
    }
    
}
