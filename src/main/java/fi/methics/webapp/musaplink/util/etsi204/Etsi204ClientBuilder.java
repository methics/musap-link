//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util.etsi204;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;

import fi.laverca.etsi.EtsiClient;

/**
 * ETSI TS 102 204 Client builder 
 */
public class Etsi204ClientBuilder {

    private String apid;
    private String appwd;
    private String signatureurl;
    private String statusurl;
    private String receipturl;
    private String profileurl;
    
    private String keystoreFile;
    private String keystorePwd;
    private String keystoreType;
    
    private String clientid;
    private String sscdtype;
    
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

        // Setup SSL
        SSLSocketFactory ssf = null;
        System.out.println("Setting up ssl");
        ssf = fi.laverca.mss.MssClient.createSSLFactory(this.keystoreFile,
                                                        this.keystorePwd,
                                                        this.keystoreType);

        // Create client
        EtsiClient client = new EtsiClient(this.apid,           
                                           this.appwd,          
                                           this.signatureurl,   
                                           this.statusurl,
                                           this.receipturl,
                                           null,
                                           this.profileurl,
                                           null);
        
        client.setSSLSocketFactory(ssf);
        return new Etsi204Client(client, this.clientid, this.sscdtype);
    }
    
    public Etsi204ClientBuilder withApid(String apid) {
        this.apid = apid;
        return this;
    }

    public Etsi204ClientBuilder withApPwd(String appwd) {
        this.appwd = appwd;
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

}
