package fi.methics.webapp.musaplink.link.cmd;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyReq;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.db.AccountStorage;

/**
 * Link API command for updating key data.
 * 
 * <p>The key data is kept local on MUSAP Link.
 */
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
        
        MusapLinkAccount account = AccountStorage.findAccountByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }
        
        log.debug("Updating keyname for " + account + " to " + jReq.newkeyname);
        
        MusapKey key;
        if (jReq.oldKeyname != null) {
            key = AccountStorage.findKeyDetailsByKeyname(account, jReq.oldKeyname);
        } else {
            key = AccountStorage.findKeyDetailsByKeyID(account, jReq.newkeyname);
        }
        
        if (key == null) {
            throw new MusapException(MusapResp.ERROR_UNKNOWN_KEY);
        }
        
        if (jReq.newkeyname != null) {
            key.keyname = jReq.newkeyname;
        }
        
        AccountStorage.upsertKeyDetails(account, key);
        return jResp;
    }
    
}
