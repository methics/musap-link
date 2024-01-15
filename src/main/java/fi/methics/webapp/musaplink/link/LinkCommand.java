package fi.methics.webapp.musaplink.link;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

import fi.methics.webapp.musaplink.link.json.MusapReq;
import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.util.MusapLinkConf;

/**
 * MUSAP Link API command. These are called from {@link MusapLinkServlet}.
 * Each MUSAP Link API message type should have a corresponding {@link LinkCommand} implementation.
 */
public abstract class LinkCommand <REQ extends MusapReq, RESP extends MusapResp> {

    protected static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    protected static final Gson                GSON = new Gson();
    
    protected static final Log log = LogFactory.getLog(LinkCommand.class);

    private REQ req;
    
    public LinkCommand(REQ req) {
        this.req = req;
    }
    
    /**
     * Execute the command
     * @return 
     * @throws Exception
     */
    public abstract RESP execute() throws Exception;
    
    /**
     * Get the original MUSAP Link API request
     * @return request
     */
    public REQ getRequest() {
        return this.req;
    }
    
    /**
     * Get MUSAP Link configuration
     * @return configuration
     */
    public MusapLinkConf getConfig() {
        return MusapLinkConf.getInstance();
    }
    
}
