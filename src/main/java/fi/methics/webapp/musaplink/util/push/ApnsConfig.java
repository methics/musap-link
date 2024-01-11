//
//(c) Copyright 2003-2021 Methics Technologies Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eatthepath.pushy.apns.auth.ApnsSigningKey;

public class ApnsConfig {

    private static final Log log = LogFactory.getLog(ApnsConfig.class);

    public final static String TYPE = "APNS";
    
    private final String  teamId;
    private final String  keyId;

    private final boolean debugEnable;
    private final boolean traceEnable;
    private final boolean prodEnable;
    
    private final String  signingkey;

    private String topic;
    private String overridePayload;

    private KeyStore       clientKeystore;
    private ApnsSigningKey clientSigningKey;
    
    private String clientKeystoreFile;
    private String clientKeystorePass;
    
    private int maxconnections = 5;
    
    private ApnsClient client;
    
    /**
     * Parse APNs client configuration.
     * Note that this is always called even when no configuration for it exist, and it shall
     * then return at least a semi-sensible configuration.
     * 
     * @param prefix
     * @param p
     */
    public ApnsConfig(final Properties p, final String prefix) {
        
        this.teamId          = p.getProperty(prefix + "apns.teamid");
        this.keyId           = p.getProperty(prefix + "apns.keyid");
        this.signingkey      = p.getProperty(prefix + "apns.signingkey");
        this.overridePayload = p.getProperty(prefix + "apns.override.payload");
        this.topic           = p.getProperty(prefix + "apns.default.topic", 
                               p.getProperty(prefix + "apns.topic", "fi.methics.Alauda.PBY"));
        
        this.debugEnable  = Boolean.getBoolean(p.getProperty(prefix + "apns.debug"));  
        this.traceEnable  = Boolean.getBoolean(p.getProperty(prefix + "apns.trace"));
        this.prodEnable   = Boolean.getBoolean(p.getProperty(prefix + "apns.production"));
        
        this.clientKeystoreFile = p.getProperty(prefix + "apns.keystore.file");
        this.clientKeystorePass = p.getProperty(prefix + "apns.keystore.pwd");
        String keystoreType = p.getProperty(prefix + "apns.keystore.type");
        
        if (this.clientKeystoreFile != null) {
            try (InputStream kis = new FileInputStream(this.clientKeystoreFile)) {
                this.clientKeystore = KeyStore.getInstance(keystoreType);
                this.clientKeystore.load(kis, this.clientKeystorePass.toCharArray());
            } catch (Exception e) {
                log.error("Failed to load APNs client keystore", e);
            }
        }
        
        if (this.signingkey != null) {
            try {
                this.clientSigningKey = ApnsSigningKey.loadFromPkcs8File(new File(this.signingkey), this.getTeamId(), this.getKeyId());
            } catch (Exception e) {
                log.warn("Could not load APNS signing key", e);
            }
        }
    }
    
    public ApnsClient getClient() {
        if (this.client == null) {
            this.client = new ApnsClient(this);
        }
        return this.client;
    }
    /**
     * Check if this client is enabled?
     * @return true if enabled
     */
    public boolean isEnabled() {
        return this.teamId != null;
    }
    
    /**
     * Get APNS signing key used to authenticate to APNS.
     * This is alternative to {@link #getKeystore()}.
     * @return SigningKey or null
     */
    public ApnsSigningKey getSigningKey() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return this.clientSigningKey;
    }

    /**
     * Get topic that will be used if HMSSP does not provide a topic
     * <p>This should match the APP's BundleID!
     * @return topic
     */
    public String getTopic() {
        return this.topic;
    }
    
    /**
     * Get APNS payload to use over the provided one
     * @return APNS payload
     */
    public String getOverridePayload() {
        return this.overridePayload;
    }
    
    /**
     * Get the amount of desired maximum concurrent connections to APNS
     * @return max connections
     */
    public int getMaxConnections() {
        return this.maxconnections;
    }
    
    /**
     * Get the Key ID
     * @return Key ID
     */
    public String getKeyId() {
        return this.keyId;
    }
    
    /**
     * Get Keystore containing client certificate used to authenticate to APNS
     * This is alternative to {@link #getSigningKey()}.
     * @return Keystore configuration or null
     */
    public KeyStore getKeystore() {
        return this.clientKeystore;
    }
    
    public String getKeystoreFile() {
        return this.clientKeystoreFile;
    }
    
    public String getKeystorePwd() {
        return this.clientKeystorePass;
    }
    
    /**
     * Get the Team ID
     * @return Team ID
     */
    public String getTeamId() {
        return this.teamId;
    }
    
    public boolean isDebugEnabled() {
        return this.debugEnable;
    }
    
    public boolean isTraceEnabled() {
        return this.traceEnable;
    }
    
    public boolean isProductionEnabled() {
        return this.prodEnable;
    }
    
}
