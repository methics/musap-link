//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.coupling.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
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

public class CmdExternalSignature extends CouplingCommand {

    private static final Map<String, CouplingApiPayload> RESPONSES = new HashMap<>();
    
    public CmdExternalSignature(CouplingApiMessage req) {
        super(req, ExternalSignatureReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws Exception {
        
        CouplingApiMessage req = this.getRequest();
        String  musapid = req.musapid;
        
        MusapLinkAccount account = AccountStorage.findByMusapId(musapid);
        if (account == null) throw new MusapException(MusapResp.ERROR_UNKNOWN_USER);

        ExternalSignatureReq sigReq = this.getRequestPayload();
        
        // Check if this was a poll request
        if (sigReq.transid != null) {
            log.info("Got an external signature poll request from MUSAP ID " + musapid);
            log.debug("Searching for a transaction with transid " + sigReq.transid);
            CouplingApiPayload payload = this.pollSignature(sigReq.transid);
            
            if (payload == null) {
                log.error("No transaction found with transid " + sigReq.transid);
                return req.createErrorResponse(MusapErrorMsg.ERROR_INVALID_REQ);
            }
            log.info("Found transaction with payload " + payload);
            return req.createResponse(payload);
        } else {
            log.info("Got external signature request from MUSAP ID " + musapid);
        }
        
        // Generate new transid for future use
        sigReq.transid = IdGenerator.generateTxnId();
        log.debug("Created a new transaction with transid " + sigReq.transid);
        Etsi204Client client = this.getConfig().getClient(sigReq.clientid);
        
        if (client == null) {
            log.debug("No client found with ClientID " + sigReq.clientid);
            log.debug("Found clients: " + this.getConfig().getClients().stream().map(c -> c.getClientId()).collect(Collectors.toList()));
            throw new MusapException(MusapResp.ERROR_INTERNAL);
        }
        
        this.sendRequest(client, req, sigReq);
        
        ExternalSignatureResp statusResp = new ExternalSignatureResp();
        statusResp.transid = sigReq.transid;
        statusResp.status  = "pending";
        return req.createResponse(statusResp);
    }
    
    /**
     * Send a signature request to the external service asynchronously
     * @param client Client to send request with
     * @param req    Raw MUSAP Coupling API request
     * @param sigReq Raw External Signature request
     * @throws Etsi204Exception
     */
    private void sendRequest(Etsi204Client client, CouplingApiMessage req, ExternalSignatureReq sigReq) throws Etsi204Exception {
        
        String transid = sigReq.transid;
        if (transid == null) {
            log.error("Not storing empty transid");
            return;
        }
        
        EXECUTOR.execute(() -> {
            String msisdn  = sigReq.attributes.get("msisdn");
            String dtbd    = sigReq.display;
            
            log.debug("Sending signature request to clientid=" + sigReq.clientid + ", msisdn=" + msisdn);
            try {
                Etsi204Response resp = client.sign(msisdn, dtbd, sigReq.getDtbs());
                
                ExternalSignatureResp sigResp = new ExternalSignatureResp();
                sigResp.signature = resp.getSignatureB64();
                sigResp.status    = "success";
                sigResp.transid   = transid;
                
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

    private CouplingApiPayload pollSignature(String transid) {
        return RESPONSES.get(transid);
    }
    
}
