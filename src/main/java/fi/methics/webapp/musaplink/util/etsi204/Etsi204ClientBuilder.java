package fi.methics.webapp.musaplink.util.etsi204;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.laverca.etsi.EtsiClient;
import fi.methics.laverca.rest.MssClient;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Client.ClientType;

/**
 * ETSI TS 102 204 Client builder.
 * This can be used to build both REST and SOAP clients.
 */
public class Etsi204ClientBuilder {

    private static final Log log = LogFactory.getLog(Etsi204ClientBuilder.class);

    private String apid;
    private String appwd;
    private String restApiKey;
    private String restPasword;
    
    private String resturl;
    private String signatureurl;
    private String statusurl;
    private String receipturl;
    private String profileurl;
    
    private String keystoreFile;
    private String keystorePwd;
    private String keystoreType;
    
    private String clientid;
    private String sscdtype;
    
    private ClientType clientType = ClientType.SOAP;
    
    private String signatureProfile;
    private boolean enableNospamCode;
    private boolean enableEventId;
    private boolean enableUserLang;

    public Etsi204ClientBuilder(String clientid, String sscdtype) {
        this.clientid = clientid;
        this.sscdtype = sscdtype;
    }
    
    /**
     * Get Laverca signature client instance
     * @return Signature client
     * @throws IOException if keystore cannot be loaded
     * @throws GeneralSecurityException if keystore cannot be loaded
     * @throws ServletException if client cannot be initialized
     */
    public Etsi204Client build() throws GeneralSecurityException, IOException {

        Etsi204Client result;
        if (this.clientType == ClientType.SOAP) {
            log.debug("Initializing SOAP client " + this.clientid);
            SSLSocketFactory ssf = fi.laverca.mss.MssClient.createSSLFactory(this.keystoreFile,
                                                                             this.keystorePwd,
                                                                             this.keystoreType);

            EtsiClient client = new EtsiClient(this.apid,           
                                               this.appwd,          
                                               this.signatureurl,   
                                               this.statusurl,
                                               this.receipturl,
                                               null,
                                               this.profileurl,
                                               null);
            
            client.setSSLSocketFactory(ssf);
            result = new Etsi204SoapClient(client, this.clientid, this.sscdtype);
        } else {
            log.debug("Initializing REST client " + this.clientid);
            MssClient.Builder builder = new MssClient.Builder();
            builder.withAppwd(this.appwd);
            builder.withRestUrl(this.resturl);
            if (this.restApiKey != null) {
                builder.withApiKey(this.apid, this.restApiKey);
            } else {
                builder.withPassword(this.apid, this.restPasword);
            }
            builder.withAppwd(this.appwd);
            result = new Etsi204RestClient(builder.build(), this.clientid, this.sscdtype);
        }
        result.setEventIdEnabled(this.enableEventId);
        result.setNospamCodeEnabled(this.enableNospamCode);
        result.setUserLangEnabled(this.enableUserLang);
        result.setSignatureProfile(this.signatureProfile);
        return result;
    }
    
    public Etsi204ClientBuilder withApid(String apid) {
        this.apid = apid;
        return this;
    }

    public Etsi204ClientBuilder withApPwd(String appwd) {
        this.appwd = appwd;
        return this;
    }

    public Etsi204ClientBuilder withRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
        return this;
    }

    public Etsi204ClientBuilder withRestPassword(String restPasword) {
        this.restPasword = restPasword;
        return this;
    }

    public Etsi204ClientBuilder withSignatureUrl(String signatureurl) {
        this.signatureurl = signatureurl;
        return this;
    }

    public Etsi204ClientBuilder withStatusUrl(String statusurl) {
        this.statusurl = statusurl;
        return this;
    }

    public Etsi204ClientBuilder withReceiptUrl(String receipturl) {
        this.receipturl = receipturl;
        return this;
    }

    public Etsi204ClientBuilder withProfileUrl(String profileurl) {
        this.profileurl = profileurl;
        return this;
    }

    public Etsi204ClientBuilder withRestUrl(String resturl) {
        this.resturl = resturl;
        return this;
    }

    public Etsi204ClientBuilder withKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
        return this;
    }

    public Etsi204ClientBuilder withKeystorePwd(String keystorePwd) {
        this.keystorePwd = keystorePwd;
        return this;
    }

    public Etsi204ClientBuilder withKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
        return this;
    }

    public Etsi204ClientBuilder withEventIdEnabled(boolean enabled) {
        this.enableEventId = enabled;
        return this;
    }

    public Etsi204ClientBuilder withNospamEnabled(boolean enabled) {
        this.enableNospamCode = enabled;
        return this;
    }

    public Etsi204ClientBuilder withUserLangEnabled(boolean enabled) {
        this.enableUserLang = enabled;
        return this;
    }
    
    public Etsi204ClientBuilder withClientType(ClientType clientType) {
        this.clientType = clientType;
        return this;
    }

    public Etsi204ClientBuilder withSignatureProfile(String sigProfile) {
        this.signatureProfile = sigProfile;
        return this;
    }
}
