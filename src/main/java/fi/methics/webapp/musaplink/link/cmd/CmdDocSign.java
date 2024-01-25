package fi.methics.webapp.musaplink.link.cmd;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.TxnStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapDocSignReq;
import fi.methics.webapp.musaplink.link.json.MusapDocSignReq.DTBS;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignReq;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.SignatureCallback;
import fi.methics.webapp.musaplink.util.push.PushClient;

/**
 * Link API command for a document signature from MUSAP
 */
public class CmdDocSign extends LinkCommand<MusapDocSignReq, MusapSignResp> {

    public CmdDocSign(MusapDocSignReq req) {
        super(req);
    }
    
    @Override
    public MusapSignResp execute() throws MusapException {
        MusapDocSignReq jReq = this.getRequest();
        if (jReq == null) throw new MusapException(MusapResp.ERROR_WRONG_PARAM);

        String linkid = jReq.linkid;
        MusapLinkAccount account = AccountStorage.findByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }

        log.info("Got /sign request for MUSAP with linkid " + linkid);

        // Resolve KeyID from keyname
        if (jReq.datachoice == null) {
            throw new MusapException(MusapResp.ERROR_MISSING_PARAM);
        }
        for (DTBS dtbs : jReq.datachoice) {
            if (dtbs.key != null && dtbs.key.keyname != null) {
                String keyname = dtbs.key.keyname;
                MusapKey key   = AccountStorage.findKeyDetailsByKeyname(account, keyname);
                if (key != null && key.keyid != null) {
                    log.debug("Resolved keyid: " + key.keyid + " from keyname: " + keyname);
                    dtbs.key.keyid = key.keyid;
                }
            }
        }
        
        SignatureCallback callback = TxnStorage.storeRequest(jReq.linkid, jReq.toCouplingRequest());
        String transid = callback.getTransId();
        String message = jReq.display;

        PushClient.sendPushNotification(account, message);

        Callable<MusapSignResp> c = () -> {
            callback.tryAcquire();
            log.info("Found /sign response");

            if (callback.isError()) {
                return null;
            } else {
                return callback.getResponse();
            }
        };

        Future<MusapSignResp> future = EXECUTOR.submit(c);
        try {
            MusapSignResp resp = future.get(120, TimeUnit.SECONDS);
            AccountStorage.upsertKeyDetails(account, new MusapKey(resp));
            TxnStorage.deleteTransaction(transid);

            if (resp == null) {
                throw new MusapException(callback.getError());
            } else {
                log.info("Returning /sign response " + resp.toJson());
                return resp;
            }
        } catch (TimeoutException e) {
            log.warn("Failed to get signature. Timed out.", e);
            throw new MusapException(MusapResp.ERROR_TIMED_OUT);
        } catch (Exception e) {
            log.warn("Failed to get signature.", e);
            throw new MusapException(MusapResp.ERROR_INTERNAL);
        } finally {
            future.cancel(true);
        }
    }
    
}
