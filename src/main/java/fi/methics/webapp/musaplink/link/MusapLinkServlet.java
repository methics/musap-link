//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.link;

import java.time.Duration;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.TxnStorage;
import fi.methics.webapp.musaplink.link.cmd.CmdGenerateKey;
import fi.methics.webapp.musaplink.link.cmd.CmdLink;
import fi.methics.webapp.musaplink.link.cmd.CmdSign;
import fi.methics.webapp.musaplink.link.cmd.CmdUpdateKey;
import fi.methics.webapp.musaplink.link.json.MusapGenerateKeyReq;
import fi.methics.webapp.musaplink.link.json.MusapGenerateKeyResp;
import fi.methics.webapp.musaplink.link.json.MusapLinkReq;
import fi.methics.webapp.musaplink.link.json.MusapLinkResp;
import fi.methics.webapp.musaplink.link.json.MusapSignReq;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyReq;
import fi.methics.webapp.musaplink.link.json.MusapUpdateKeyResp;
import fi.methics.webapp.musaplink.util.MusapLinkConf;

/**
 * Servlet for communication between AP and MUSAP Link.
 * Handles the MUSAP Link API.
 */
@Path("/")
public class MusapLinkServlet {

    private static final Log log = LogFactory.getLog(MusapLinkServlet.class);
    
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
        log.info("MUSAP Link Servlet initialized");
        TxnStorage.scheduleCleaner(Duration.ofMinutes(1).toMillis());
        AccountStorage.scheduleCleaner(Duration.ofMinutes(1).toMillis());
    }
    
    
    @POST
    @Path("/link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response link(String body) {
        
        MusapLinkReq  jReq  = MusapLinkReq.fromJson(body, MusapLinkReq.class);
        MusapLinkResp jResp = new CmdLink(jReq).execute();
        
        return Response.ok(jResp.toJson()).build();
    }
    
    @POST
    @Path("/sign")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sign(String body) {
        
        MusapSignReq  jReq  = MusapSignReq.fromJson(body, MusapSignReq.class);
        MusapSignResp jResp = new CmdSign(jReq).execute();
        
        return Response.ok(jResp.toJson()).build();
    }
    
    @POST
    @Path("/generatekey")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateKey(String body) {
        
        MusapGenerateKeyReq  jReq  = MusapGenerateKeyReq.fromJson(body, MusapGenerateKeyReq.class);
        MusapGenerateKeyResp jResp = new CmdGenerateKey(jReq).execute();
        
        return Response.ok(jResp.toJson()).build();
    }

    @POST
    @Path("/updatekey")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateKey(String body) {
        
        MusapUpdateKeyReq  jReq  = MusapUpdateKeyReq.fromJson(body, MusapUpdateKeyReq.class);
        MusapUpdateKeyResp jResp = new CmdUpdateKey(jReq).execute();
        
        return Response.ok(jResp.toJson()).build();
    }
    
}
