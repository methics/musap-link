//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.link.cmd;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyReq;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyResp;
import fi.methics.webapp.musaplink.util.MusapException;

public class CmdUpdateKey extends LinkCommand<MusapUpdateKeyReq, MusapUpdateKeyResp> {

    public CmdUpdateKey(MusapUpdateKeyReq req) {
        super(req);
    }
    
    @Override
    public MusapUpdateKeyResp execute() throws MusapException {
        
        MusapUpdateKeyReq  jReq  = this.getRequest();
        MusapUpdateKeyResp jResp = new MusapUpdateKeyResp();

        if (jReq == null) throw new MusapException(MusapResp.ERROR_WRONG_PARAM);
        String linkid = jReq.linkid;
        
        MusapLinkAccount account = AccountStorage.findByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }
        
        log.debug("Updating keyname for " + account);
        AccountStorage.upsertKeyDetails(account, jReq.keyname, jReq.keyid);
        return jResp;
    }
    
}
