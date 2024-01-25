package fi.methics.webapp.musaplink.util.etsi204;

/**
 * ETSI TS 102 204 signature client
 */
public abstract class Etsi204Client {
    
    public static final String SIGPROF_ALAUDA_AUTHN = "http://alauda.mobi/digitalSignature";
    public static final String SIGPROF_ALAUDA_SIGN  = "http://alauda.mobi/nonRepudiation";
    
    protected String sscdid;
    protected String sscdtype;
    
    protected boolean enableNoSpamCode;
    protected boolean enableEventId;
    
    protected String signatureProfile;
    
    protected Etsi204Client(String sscdid, String sscdtype) {
        this.sscdid   = sscdid;
        this.sscdtype = sscdtype;
    }
    
    public void setEventIdEnabled(boolean enabled) {
        this.enableEventId = enabled;
    }

    public void setNospamCodeEnabled(boolean enabled) {
        this.enableNoSpamCode = enabled;
    }
    
    /**
     * Get the SignatureProfile to be used
     * @return SignatureProfile. Default is {@link #SIGPROF_ALAUDA_SIGN}.
     */
    public String getSignatureProfile() {
        return this.signatureProfile;
    }
    
    /**
     * Set the default SignatureProfile to be used
     * @param sigprof Signature Profile
     */
    public void setSignatureProfile(String sigprof) {
        this.signatureProfile = sigprof;
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
    public abstract Etsi204Response sign(String msisdn, 
                                         String dtbd,
                                         byte[] dtbs,
                                         String transid) 
        throws Etsi204Exception;
    
    public String getClientId() {
        return this.sscdid;
    }
    
    public String getSscdType() {
        return this.sscdtype;
    }
    
    public static enum ClientType {
        REST("rest"),
        SOAP("soap");
        String type;
        private ClientType(String type) {
            this.type = type;
        }
        
        public static ClientType fromString(String type) {
            if (type == null) return SOAP;
            switch (type.toLowerCase()) {
            case "rest": return REST;
            default: return SOAP;
            }
        }
    }
    
}
