package fi.methics.webapp.musaplink.coupling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiPayload;
import fi.methics.webapp.musaplink.util.MusapLinkConf;
import fi.methics.webapp.musaplink.util.MusapTransportEncryption;

/**
 * Coupling API command. These are called from {@link MusapCouplingServlet}.
 * Each Coupling API message type should have a corresponding {@link CouplingCommand} implementation.
 */
public abstract class CouplingCommand {

    protected static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    protected static final Log log = LogFactory.getLog(CouplingCommand.class);
    
    private CouplingApiMessage req;
    private Class<? extends CouplingApiPayload> payloadClass;
    
    public CouplingCommand(CouplingApiMessage req, Class<? extends CouplingApiPayload> payloadClass) {
        this.req = req;
        this.payloadClass = payloadClass;
    }
    
    /**
     * Execute the command
     * @return 
     * @throws Exception
     */
    public abstract CouplingApiMessage execute() throws Exception;
    
    /**
     * Get the original Coupling API request
     * @return request
     */
    public CouplingApiMessage getRequest() {
        return this.req;
    }
    
    /**
     * Get the original Coupling API payload
     * @param <T> payload type
     * @return payload (or null if not available)
     */
    @SuppressWarnings("unchecked")
    public <T extends CouplingApiPayload> T getRequestPayload() {
        CouplingApiMessage req = this.getRequest();
        if (req == null) return null;
        T payload = (T) req.getPayload(this.payloadClass);
        return payload;
    }
    
    /**
     * Get a handle to the transport encryption handler
     * @return Transport Encryption handler
     */
    public MusapTransportEncryption getTransportEncryption() {
        return null;
    }
    
    /**
     * Get MUSAP Link configuration
     * @return configuration
     */
    public MusapLinkConf getConfig() {
        return MusapLinkConf.getInstance();
    }
    
}
