package fi.methics.webapp.musaplink.link.cmd;

import java.util.Collection;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapListKeysReq;
import fi.methics.webapp.musaplink.link.json.MusapListKeysResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.db.AccountStorage;


/**
 * Link API command for listing keys and certificates.
 * This command must be explicitly enabled in configuration.
 */
public class CmdListKeys extends LinkCommand<MusapListKeysReq, MusapListKeysResp> {

    public CmdListKeys(MusapListKeysReq req) {
        super(req);
    }
    
    @Override
    public MusapListKeysResp execute() throws MusapException {
        
        if (!this.getConfig().isListKeysEnabled()) {
            throw new MusapException(MusapResp.ERROR_CONFIGURATION);
        }
        
        MusapListKeysReq  jReq  = this.getRequest();
        MusapListKeysResp jResp = new MusapListKeysResp();
        if (jReq == null) throw new MusapException(MusapResp.ERROR_WRONG_PARAM);
        String linkid = jReq.linkid;
        
        MusapLinkAccount account = AccountStorage.findAccountByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }
        
        log.debug("Listing keys for " + account);
        Collection<MusapKey> keys = AccountStorage.listKeyDetails(account);
        log.debug("Found " + keys.size() + " keys");

        jResp.addKeys(keys);
        return jResp;
    }
    
}
