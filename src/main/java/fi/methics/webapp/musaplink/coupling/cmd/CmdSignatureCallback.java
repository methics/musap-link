package fi.methics.webapp.musaplink.coupling.cmd;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.SignatureCallbackResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.db.AccountStorage;
import fi.methics.webapp.musaplink.util.db.TxnStorage;

/**
 * Coupling API command for delivering a signature generation response to MUSAP Link
 */
public class CmdSignatureCallback extends CouplingCommand {

    public CmdSignatureCallback(CouplingApiMessage req) {
        super(req, null);
    }

    @Override
    public CouplingApiMessage execute() throws Exception {
        
        CouplingApiMessage req = this.getRequest();
        String      musapid = req.musapid;
        String      transid = req.transid;
       
        log.info("Got signature from MUSAP ID " + musapid);
        MusapLinkAccount account = AccountStorage.findAccountByMusapId(musapid);
        if (account == null) throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);

        SignatureCallbackResp callback = req.getPayload(SignatureCallbackResp.class);
        
        MusapSignResp response = callback.toSignResp();
        TxnStorage.storeResponse(transid, response);
        return req.createSuccessResponse();
    }

}
