package fi.methics.webapp.musaplink.coupling;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.cmd.CmdEnrollData;
import fi.methics.webapp.musaplink.coupling.cmd.CmdExternalSignature;
import fi.methics.webapp.musaplink.coupling.cmd.CmdGenerateKeyCallback;
import fi.methics.webapp.musaplink.coupling.cmd.CmdGetData;
import fi.methics.webapp.musaplink.coupling.cmd.CmdLinkAccount;
import fi.methics.webapp.musaplink.coupling.cmd.CmdSignatureCallback;
import fi.methics.webapp.musaplink.coupling.cmd.CmdUpdateData;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapLinkConf;
import fi.methics.webapp.musaplink.util.MusapTransportEncryption;
import fi.methics.webapp.musaplink.util.db.AccountStorage;

/**
 * Servlet for communication between MUSAP and MUSAP Link.
 * Handles the Coupling API.
 */
@Path("/")
public class MusapCouplingServlet {

    private static final Log log = LogFactory.getLog(MusapCouplingServlet.class);
    
    private static MusapLinkConf conf;
    private static MusapTransportEncryption enc;
    
    /**
     * Initialize the servlet
     */
    public static void init() {
        // Read conf first
        conf = MusapLinkConf.getInstance();
        enc  = new MusapTransportEncryption(conf);
        if (conf == null || !conf.isInitialized()) {
            log.fatal("Cannot read configuration file " + conf.getConfFilePath());
            System.out.println("Cannot read configuration file " + conf.getConfFilePath());
            throw new RuntimeException("Cannot read configuration file");
        }
        log.info("MUSAP Servlet initialized");
    }
    
    @POST
    @Path("/musap")
    public Response musapEndpoint(String body) {
        
        CouplingApiMessage jReq  = CouplingApiMessage.fromJson(body);
        CouplingApiMessage jResp = null;

        if (jReq == null) {
            log.debug("No request body");
            return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Missing request body");
        }
        
        MusapLinkAccount account = null;
        
        boolean isEncrypted     = jReq.isEncrypted();
        boolean shouldDecrypt   = MusapTransportEncryption.shouldDecrypt(jReq);
        boolean encryptRequired = conf.isTransportEncryptionRequired();

        if (!isEncrypted && shouldDecrypt && encryptRequired) {
            // Request is encrypted even when it should be
            return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Missing transport encryption");
        }

        try {
            if (isEncrypted) {
                // Fetch transport encryption keys and decrypt if needed
                account = AccountStorage.findAccountByMusapId(jReq.musapid);
                
                if (account == null) {
                    log.debug("Could not find account with MUSAP ID " + jReq.musapid);
                    return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Failed to decrypt the request: Could not find account with MUSAP ID " + jReq.musapid);
                }
                if (account.getTransportKeys() == null) {
                    log.debug("Could not find transport encryption key");
                    return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Failed to decrypt the request: Missing transport key");
                }
                try {
                    enc.decrypt(jReq, account.getTransportKeys());
                } catch (Exception e) {
                    log.error("Failed to decrypt message", e);
                    return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Failed to decrypt the request: " + e.getMessage());
                }
                if (!enc.isNonceValid(jReq)) {
                    log.error("NONCE check failed. Returning error.");
                    return MusapResp.createErrorResponse(MusapResp.ERROR_INTERNAL, "Replay-attack detection failed. Invalid NONCE.");
                }
            }
    
            log.debug("Request Payload: " + jReq.getPayloadJson());

            switch (jReq.type) {
                case CouplingApiMessage.TYPE_ENROLLDATA: {
                    log.debug("Enrolling data");
                    jResp = new CmdEnrollData(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_UPDATEDATA: {
                    log.debug("Updating data");
                    jResp = new CmdUpdateData(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_ERROR: {
                    log.debug("Got error");
                    break;
                }
                case CouplingApiMessage.TYPE_LINKACCOUNT: {
                    log.debug("Linking MUSAP");
                    jResp = new CmdLinkAccount(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_GETDATA: {
                    log.debug("Get data");
                    jResp = new CmdGetData(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_SIGNATURE_CALLBACK: {
                    log.debug("Got signature response");
                    jResp = new CmdSignatureCallback(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_GENKEY_CALLBACK: {
                    log.debug("Got keygen response");
                    jResp = new CmdGenerateKeyCallback(jReq).execute();
                    break;
                }
                case CouplingApiMessage.TYPE_EXTERNAL_SIGREQ: {
                    log.debug("Got external signature request");
                    jResp = new CmdExternalSignature(jReq).execute();
                    break;
                }
                default: {
                    log.debug("Unknown request type " + jReq.type);
                    return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM, "Unknown request type " + jReq.type);
                }
            }
        } catch (MusapException e) {
            log.error(jReq.type + " failed", e);
            throw e;
        } catch (Exception e) {
            log.error(jReq.type + " failed", e);
            return MusapResp.createErrorResponse(MusapResp.ERROR_INTERNAL, e.getMessage());
        }
        
        if (jResp == null) {
            log.debug("Returning empty response");
            return Response.ok().build();
        } else {
            log.debug("Response Payload: " + jReq.getPayloadJson());
            
            try {
                if (MusapTransportEncryption.shouldEncrypt(jResp)) {
                    if (account == null) {
                        account = AccountStorage.findAccountByLinkId(body);
                    }
                    if (account != null) {
                        enc.encrypt(jResp, account.getTransportKeys());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to encrypt the response", e);
                return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM,  "Failed to encrypt the response: " + e.getMessage());
            }
            
            return Response.ok(jResp.toJson()).build();
        }
    }

}
