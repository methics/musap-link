package fi.methics.webapp.musaplink.coupling;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

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

/**
 * Servlet for communication between MUSAP and MUSAP Link.
 * Handles the Coupling API.
 */
@Path("/")
public class MusapCouplingServlet {

    private static final Log log = LogFactory.getLog(MusapCouplingServlet.class);
    
    private static final Gson GSON = new Gson();
    
    private static MusapLinkConf conf;
    
    /**
     * Initialize the servlet
     */
    public static void init() {
        // Read conf first
        conf = MusapLinkConf.getInstance();
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
        
        CouplingApiMessage jReq  = GSON.fromJson(body, CouplingApiMessage.class);
        CouplingApiMessage jResp = null;
        
        log.debug("Got request from MUSAP: " + body);
        if (jReq == null) {
            log.debug("No request body");
            return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM);
        }
        log.debug("Request Payload: " + jReq.getPayloadJson());

        try {
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
                    return MusapResp.createErrorResponse(MusapResp.ERROR_WRONG_PARAM);
                }
            }
        } catch (MusapException e) {
            throw e;
        } catch (Exception e) {
            log.error(jReq.type + " failed", e);
            return MusapResp.createErrorResponse(MusapResp.ERROR_INTERNAL);
        }
        
        log.debug("Response Payload: " + jReq.getPayloadJson());
        return Response.ok(GSON.toJson(jResp)).build();
    }
    
}
