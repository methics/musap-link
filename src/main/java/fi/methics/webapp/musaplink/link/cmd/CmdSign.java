package fi.methics.webapp.musaplink.link.cmd;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.TxnStorage;
import fi.methics.webapp.musaplink.link.LinkCommand;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignReq;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.SignatureCallback;
import fi.methics.webapp.musaplink.util.push.PushClient;

/**
 * Link API command for a signature from MUSAP
 */
public class CmdSign extends LinkCommand<MusapSignReq, MusapSignResp> {

    public CmdSign(MusapSignReq req) {
        super(req);
    }
    
    @Override
    public MusapSignResp execute() throws MusapException {
        MusapSignReq jReq = this.getRequest();
        if (jReq == null) throw new MusapException(MusapResp.ERROR_WRONG_PARAM);

        String linkid = jReq.linkid;
        MusapLinkAccount account = AccountStorage.findByLinkId(linkid);
        if (account == null) {
            log.error("No account found with linkid " + linkid);
            throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);
        }

        log.info("Got /sign request for MUSAP with linkid " + linkid);

        // Resolve KeyID from keyname
        if (jReq.key != null && jReq.key.keyname != null) {
            String keyname = jReq.key.keyname;
            String keyid   = AccountStorage.getKeyId(account, keyname);
            if (keyid != null) {
                log.debug("Resolved keyid: " + keyid + " from keyname: " + keyname);
                jReq.key.keyid = keyid;
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
