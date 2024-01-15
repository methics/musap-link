package fi.methics.webapp.musaplink.util.etsi204;

import java.util.Collections;
import java.util.UUID;

import fi.laverca.etsi.EtsiClient;
import fi.laverca.etsi.EtsiRequest;
import fi.laverca.jaxb.mss.MessagingModeType;
import fi.laverca.util.DTBS;

/**
 * ETSI TS 102 204 signature client
 */
public class Etsi204Client {
    
    private EtsiClient client; // Laverca client
    private String sscdid;
    private String sscdtype;

    protected Etsi204Client(EtsiClient client, String sscdid, String sscdtype) {
        this.client   = client;
        this.sscdid   = sscdid;
        this.sscdtype = sscdtype;
    }
    
    /**
     * Send a SignatureRequest to MSSP
     * @param msisdn  MSISDN
     * @param dtbd DTBS
     * @return Signature response
     * @throws Etsi204Exception
     */
    public Etsi204Response sign(final String msisdn, 
                                final String dtbd,
                                final byte[] dtbs) 
        throws Etsi204Exception
    {
        
        try {
            String    apTransId = "A" + UUID.randomUUID().toString();
            EtsiRequest soapReq = this.client.createRequest(apTransId, 
                                                            msisdn,
                                                            new DTBS(dtbs, "BASE64", "application/octet-stream"),
                                                            null,
                                                            Collections.emptyList(),
                                                            "http://alauda.mobi/digitalSignature", 
                                                            null,
                                                            MessagingModeType.SYNCH);
            return new Etsi204Response(this.client.send(soapReq));
        } catch (Exception e) {
            throw new Etsi204Exception(e);
        }
    }
    
    public String getClientId() {
        return this.sscdid;
    }
    
    public String getSscdType() {
        return this.sscdtype;
    }
    
}
