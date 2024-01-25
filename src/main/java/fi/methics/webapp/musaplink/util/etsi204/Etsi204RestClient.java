package fi.methics.webapp.musaplink.util.etsi204;

import fi.methics.laverca.rest.MssClient;
import fi.methics.laverca.rest.util.SignatureProfile;

/**
 * REST ETSI TS 102 204 signature client
 */
public class Etsi204RestClient extends Etsi204Client {
    
    private MssClient client;
    
    protected Etsi204RestClient(MssClient client, String sscdid, String sscdtype) {
        super(sscdid, sscdtype);
        this.sscdid   = sscdid;
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
                                final String transid) 
        throws Etsi204Exception
    {
        String mimeType  = "application/octet-stream";
        byte[] signature = this.client.sign(msisdn, dtbd, dtbs, mimeType, SignatureProfile.of(this.getSignatureProfile()));
        
        return new Etsi204Response(signature);
    }
    
}
