package fi.methics.webapp.musaplink.link.cmd;

import java.util.UUID;

import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapLinkReq;
import fi.methics.webapp.musaplink.link.json.MusapLinkResp;
import fi.methics.webapp.musaplink.util.CouplingCode;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.db.CouplingStorage;


/**
 * Link API command for requesting new coupling code and linkid
 */
public class CmdLink extends LinkCommand<MusapLinkReq, MusapLinkResp> {

    public CmdLink(MusapLinkReq req) {
        super(req);
    }
    
    @Override
    public MusapLinkResp execute() throws MusapException {
        MusapLinkResp jResp = new MusapLinkResp();
        
        String     linkid = newLinkId();
        CouplingCode code = CouplingStorage.newCouplingCode(linkid);
        
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
