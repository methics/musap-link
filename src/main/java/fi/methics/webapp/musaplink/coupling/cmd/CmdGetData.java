package fi.methics.webapp.musaplink.coupling.cmd;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.SignatureReq;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.db.AccountStorage;
import fi.methics.webapp.musaplink.util.db.TxnStorage;

/**
 * Coupling API command for checking for pending signature or key generation requests.
 */
public class CmdGetData extends CouplingCommand {

    public CmdGetData(CouplingApiMessage req) {
        super(req, null);
    }

    @Override
    public CouplingApiMessage execute() throws Exception {
        
        CouplingApiMessage req = this.getRequest();
        String      musapid = req.musapid;
        
        log.info("Getting data for MUSAP ID " + musapid);
        log.debug("Total requests stored: " + TxnStorage.countTransactions());
        
        MusapLinkAccount account = AccountStorage.findAccountByMusapId(musapid);
        if (account == null) throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        
        for (String linkid : account.linkids) {
            log.debug("Checking for requests from linkid " + linkid);
            SignatureReq signReq = TxnStorage.getSignReq(linkid);
            if (signReq != null) {
                log.info("Returning sign req " + signReq.toJson());
                TxnStorage.deleteTransaction(signReq.transid);
                return CouplingApiMessage.createRequest("sign", signReq.transid, signReq);
            }
        }
        log.info("No request for " + musapid);
        return null;
    }

}
