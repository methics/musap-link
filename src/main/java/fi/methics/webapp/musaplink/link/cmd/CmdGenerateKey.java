package fi.methics.webapp.musaplink.link.cmd;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.TxnStorage;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapGenerateKeyReq;
import fi.methics.webapp.musaplink.link.json.MusapGenerateKeyResp;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.SignatureCallback;
import fi.methics.webapp.musaplink.util.push.PushClient;

/**
 * Link API command for requesting new key generation from MUSAP
 */
public class CmdGenerateKey extends LinkCommand<MusapGenerateKeyReq, MusapGenerateKeyResp> {

    public CmdGenerateKey(MusapGenerateKeyReq req) {
        super(req);
    }
    
    @Override
    public MusapGenerateKeyResp execute() throws MusapException {
        MusapGenerateKeyReq jReq = this.getRequest();
        if (jReq == null) throw new MusapException(MusapResp.ERROR_WRONG_PARAM);

        String linkid = jReq.linkid;
        MusapLinkAccount account = AccountStorage.findByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }

        log.info("Got /generatekey request for MUSAP with linkid " + linkid);

        String keyname = jReq.key != null ? jReq.key.keyname : null;

        if (keyname != null) {
            if (AccountStorage.findKeyDetailsByKeyname(account, keyname) != null) {
                throw new MusapException(MusapResp.ERROR_WRONG_PARAM, "Key with name " + keyname + " already exists");
            }
        }
        
        SignatureCallback callback = TxnStorage.storeRequest(jReq.linkid, jReq.toSignReq());

        String transid = callback.getTransId();
        String message = jReq.display;

        PushClient.sendPushNotification(account, message);

        Callable<MusapSignResp> c = () -> {
            callback.tryAcquire();
            log.info("Got /generatekey response");

            if (callback.isError()) {
                return null;
            } else {
                return callback.getResponse();
            }
        };

        Future<MusapSignResp> future = EXECUTOR.submit(c);
        try {
            MusapSignResp signResp = future.get(120, TimeUnit.SECONDS);
            TxnStorage.deleteTransaction(transid);

            
            if (signResp == null) {
                throw new MusapException(callback.getError());
            } else {
                MusapGenerateKeyResp resp = new MusapGenerateKeyResp();
                resp.publickey = signResp.publickey;
                resp.linkid    = signResp.linkid;
                
                if (keyname != null) {
                    log.debug("Updating keyname to " + keyname);
                    AccountStorage.upsertKeyDetails(account, new MusapKey(signResp.keyid, keyname));
                }
                
                log.info("Returning /generatekey response " + resp.toJson());
                return resp;
            }
        } catch (TimeoutException e) {
            log.warn("Failed to generate key. Timed out.", e);
            throw new MusapException(MusapResp.ERROR_TIMED_OUT);
        } catch (Exception e) {
            log.warn("Failed to generate key.", e);
            throw new MusapException(MusapResp.ERROR_INTERNAL);
        } finally {
            future.cancel(true);
        }
    }
    
}
