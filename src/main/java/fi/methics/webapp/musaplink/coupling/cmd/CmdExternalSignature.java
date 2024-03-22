package fi.methics.webapp.musaplink.coupling.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiPayload;
import fi.methics.webapp.musaplink.coupling.json.ExternalSignatureReq;
import fi.methics.webapp.musaplink.coupling.json.ExternalSignatureResp;
import fi.methics.webapp.musaplink.coupling.json.MusapErrorMsg;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.IdGenerator;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Client;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Exception;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Response;

/**
 * Coupling API command for requesting a signature from an external service.
 * 
 * <p>Currently only supports ETSI TS 102 204 via SOAP.
 * <br>TODO: Implement an interface to allow new services.
 */
public class CmdExternalSignature extends CouplingCommand {

    private static final Map<String, CouplingApiPayload> RESPONSES = new HashMap<>();
    
    public CmdExternalSignature(CouplingApiMessage req) {
        super(req, ExternalSignatureReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws Exception {
        
        CouplingApiMessage req = this.getRequest();
        String musapid = req.musapid;
        
        MusapLinkAccount account = AccountStorage.findByMusapId(musapid);
        if (account == null) throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);

        ExternalSignatureReq sigReq = this.getRequestPayload();
        
        // Check if this was a poll request
        if (sigReq.transid != null) {
            return this.poll(req, sigReq.transid);
        } 
        
        // Generate new transid for future use
        String transid = IdGenerator.generateTxnId();
        log.info("Got external signature request from MUSAP ID " + musapid);
        log.debug("Created a new transaction with transid " + transid);
        Etsi204Client client = this.getConfig().getClient(sigReq.clientid);
        
        if (client == null) {
            log.debug("No client found with ClientID " + sigReq.clientid);
            log.debug("Found clients: " + this.getConfig().getClients().stream().map(c -> c.getClientId()).collect(Collectors.toList()));
            throw new MusapException(MusapResp.ERROR_INTERNAL);
        }
        
        this.sendRequest(account, client, req, sigReq, transid);
        ExternalSignatureResp statusResp = new ExternalSignatureResp();
        statusResp.transid = transid;
        statusResp.status  = "pending";
        return req.createResponse(statusResp);
    }
    
    /**
     * Poll for a response for given transaction
     * @param req     Coupling API request
     * @param transid Signature transaction id
     * @return Signature Request or null
     */
    private CouplingApiMessage poll(CouplingApiMessage req, String transid) {
        log.info("Got an external signature poll request from MUSAP ID " + req.musapid);
        log.debug("Searching for a transaction with transid " + transid);
        CouplingApiPayload payload = RESPONSES.get(transid);
        
        if (payload == null) {
            log.info("No transaction found with transid " + transid);
            ExternalSignatureResp statusResp = new ExternalSignatureResp();
            statusResp.transid = transid;
            statusResp.status  = "pending";
            return req.createResponse(statusResp);
        }
        
        log.info("Found transaction with payload " + payload);
        return req.createResponse(payload);
    }
    
    /**
     * Send a signature request to the external service asynchronously
     * @param client Client to send request with
     * @param req     Raw MUSAP Coupling API request
     * @param sigReq  Raw External Signature request
     * @param transid Transaction ID
     * @throws Etsi204Exception
     */
    private void sendRequest(MusapLinkAccount account,
                             Etsi204Client client, 
                             CouplingApiMessage req, 
                             ExternalSignatureReq sigReq, 
                             String transid) 
        throws Etsi204Exception 
    {
        
        if (transid == null) {
            log.error("Not storing empty transid");
            return;
        }
        
        EXECUTOR.execute(() -> {
            String msisdn  = sigReq.attributes.get("msisdn");
            String dtbd    = sigReq.display;
            
            log.debug("Sending signature request to clientid=" + sigReq.clientid + ", msisdn=" + msisdn);
            try {
                Etsi204Response resp = client.sign(msisdn, dtbd, sigReq.getDtbs(), transid, sigReq.attributes);
                
                ExternalSignatureResp sigResp = new ExternalSignatureResp();
                sigResp.signature   = resp.getSignatureB64();
                try {
                    sigResp.publickey   = resp.getPublicKeyB64();
                    sigResp.certificate = resp.getCertificateB64();
                } catch (Exception e) {
                    log.warn("Failed to parse certificate from response", e);
                }
                sigResp.status  = "success";
                sigResp.transid = transid;
                sigResp.keyid   = sigReq.keyid;
                AccountStorage.upsertKeyDetails(account, new MusapKey(sigResp));
                
                log.debug("Got signature response for " + sigResp.transid);
                RESPONSES.put(transid, sigResp);
            } catch (Etsi204Exception e) {
                MusapErrorMsg err = new MusapErrorMsg(e);
                log.error("Signature request failed", e);
                log.error("Storing error payload " + err);
                RESPONSES.put(transid, err);
            }
        });

    }

}
