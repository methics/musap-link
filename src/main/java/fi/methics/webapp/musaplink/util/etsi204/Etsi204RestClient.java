package fi.methics.webapp.musaplink.util.etsi204;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.laverca.rest.MssClient;
import fi.methics.laverca.rest.util.MssRestException;
import fi.methics.laverca.rest.util.SignatureProfile;

/**
 * REST ETSI TS 102 204 signature client
 */
public class Etsi204RestClient extends Etsi204Client {
    
    private static final Log log = LogFactory.getLog(Etsi204RestClient.class);
    private MssClient client;
    
    protected Etsi204RestClient(MssClient client, String sscdid, String sscdtype) {
        super(sscdid, sscdtype);
        this.client   = client;
        this.clientid = sscdid;
        this.sscdtype = sscdtype;
    }
    
    /**
     * Send a SignatureRequest to MSSP
     * @param msisdn  MSISDN
     * @param dtbd DTBS
     * @param dtbs DTBD
     * @param transid Transaction ID (may be used as EventID)
     * @return Signature response
     * @throws Etsi204Exception
     */
    public Etsi204Response sign(final String msisdn, 
                                final String dtbd,
                                final byte[] dtbs,
                                final String transid,
                                final Map<String, String> attrs) 
        throws Etsi204Exception
    {
        String mimeType = attrs.get(ATTR_MIMETYPE);
        if (mimeType == null) mimeType = DEFAULT_MIMETYPE;
        
        try {
            
            SignatureProfile sigprof = SignatureProfile.of(this.getSignatureProfile());
            log.debug("Sending a signature request for MSISDN " + msisdn + " and SignatureProfile "  + sigprof.getUri());
            
            byte[] signature = this.client.sign(msisdn, dtbd, dtbs, mimeType, sigprof);
            return new Etsi204Response(signature);
        } catch (MssRestException e) {
            throw new Etsi204Exception(e);
        }
    }
    
}
