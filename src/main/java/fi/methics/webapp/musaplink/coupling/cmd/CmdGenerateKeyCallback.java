//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.cmd;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.TxnStorage;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.GenerateKeyCallbackResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.MusapException;

public class CmdGenerateKeyCallback extends CouplingCommand {

    public CmdGenerateKeyCallback(CouplingApiMessage req) {
        super(req, null);
    }

    @Override
    public CouplingApiMessage execute() throws Exception {
        
        CouplingApiMessage req = this.getRequest();
        String      musapid = req.musapid;
        String      transid = req.transid;
       
        log.info("Got generated key for MUSAP ID " + musapid);
        MusapLinkAccount account = AccountStorage.findByMusapId(musapid);
        if (account == null) throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);

        GenerateKeyCallbackResp callback = req.getPayload(GenerateKeyCallbackResp.class);
        
        MusapSignResp response = callback.toSignResp();
        TxnStorage.storeResponse(transid, response);
        return req.createSuccessResponse();
    }

}
