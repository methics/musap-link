package fi.methics.webapp.musaplink.coupling.cmd;

import java.io.IOException;

import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.LinkAccountReq;
import fi.methics.webapp.musaplink.coupling.json.LinkAccountResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapRandom;
import fi.methics.webapp.musaplink.util.db.AccountStorage;
import fi.methics.webapp.musaplink.util.db.CouplingStorage;

/**
 * Coupling API command for requesting MUSAP to link with an RP
 * 
 * <p>This should be called after the user has entered a Coupling Code or scanned a QR.
 */
public class CmdLinkAccount extends CouplingCommand {

    public CmdLinkAccount(CouplingApiMessage req) {
        super(req, LinkAccountReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws IOException {
        final CouplingApiMessage req = this.getRequest();

        log.debug("Calling link account");

        final LinkAccountReq payload = this.getRequestPayload();
        
        String couplingcode = payload.couplingcode;
        if (couplingcode == null) {
            throw new MusapException(MusapResp.ERROR_COUPLING_ERROR, "Null coupling code");

        }
        couplingcode = couplingcode.replace(" ", "");
        couplingcode = couplingcode.replace("-", "");
        
        if (this.isSimulated(couplingcode)) {
            String linkid = AccountStorage.SIMULATED_LINKID + "-" + MusapRandom.getShortUUID(); 
            AccountStorage.addLinkId(payload.musapid, linkid);
            return req.createResponse(new LinkAccountResp(linkid, null));
        }
        
        final String linkid = CouplingStorage.findLinkId(couplingcode);
        
        if (linkid != null) {
            AccountStorage.addLinkId(payload.musapid, linkid);
            return req.createResponse(new LinkAccountResp(linkid, null));
        } else {
            throw new MusapException(MusapResp.ERROR_COUPLING_ERROR, "Unknown coupling code");
        }
        
    }
    
    /**
     * Check if couplingcode is simulated
     * @param couplingcode Coupling Code entered by user
     * @return true if simulated
     */
    private boolean isSimulated(String couplingcode) {
        return "55555".equals(couplingcode) || "555555".equals(couplingcode);
    }
    
}
