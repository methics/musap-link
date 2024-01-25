package fi.methics.webapp.musaplink.util.etsi204;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fi.laverca.etsi.EtsiClient;
import fi.laverca.etsi.EtsiRequest;
import fi.laverca.ficom.FiComAdditionalServices;
import fi.laverca.jaxb.mss.AdditionalServiceType;
import fi.laverca.jaxb.mss.MessagingModeType;
import fi.laverca.util.DTBS;

/**
 * SOAP ETSI TS 102 204 signature client
 */
public class Etsi204SoapClient extends Etsi204Client {
    
    private EtsiClient client; // Laverca client
    
    protected Etsi204SoapClient(EtsiClient client, String sscdid, String sscdtype) {
        super(sscdid, sscdtype);
        this.client   = client;
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
        
        try {
            
            List<AdditionalServiceType> additionalServices = new ArrayList<>();
            if (this.enableEventId) {
                additionalServices.add(FiComAdditionalServices.createEventIdService(transid));
            }
            if (this.enableNoSpamCode) {
                additionalServices.add(FiComAdditionalServices.createNoSpamService(null, false));
            }
            
            String    mimeType  = "application/octet-stream";
            String    apTransId = "A" + UUID.randomUUID().toString();
            EtsiRequest soapReq = this.client.createRequest(apTransId, 
                                                            msisdn,
                                                            new DTBS(dtbs, "BASE64", mimeType),
                                                            dtbd,
                                                            additionalServices,
                                                            this.getSignatureProfile(), 
                                                            null,
                                                            MessagingModeType.SYNCH);
            return new Etsi204Response(this.client.send(soapReq));
        } catch (Exception e) {
            throw new Etsi204Exception(e);
        }
    }
    
}
