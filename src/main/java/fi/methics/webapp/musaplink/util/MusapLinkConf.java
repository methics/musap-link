package fi.methics.webapp.musaplink.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.util.etsi204.Etsi204Client;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204Client.ClientType;
import fi.methics.webapp.musaplink.util.etsi204.Etsi204ClientBuilder;
import fi.methics.webapp.musaplink.util.push.ApnsConfig;
import fi.methics.webapp.musaplink.util.push.FcmConfig;

public class MusapLinkConf {
    
    private static final Log log = LogFactory.getLog(MusapLinkConf.class);

    private static final String PREFIX = "musaplink.";
    
    private static MusapLinkConf instance;
    private boolean initialized;
    private String filename;

    private Properties properties;
    private String     home;
    
    private FcmConfig  fcmConfig;
    private ApnsConfig apnsConfig;
    
    private MusapLinkConf(String filename) {
        this.home = System.getProperty("CATALINA_HOME");
        if (this.home == null) this.home = "";
        
        this.filename    = filename;
        this.initialized = true;
        this.properties  = readProperties(this.filename);
        this.fcmConfig   = new FcmConfig(this.properties, PREFIX);
        this.apnsConfig  = new ApnsConfig(this.properties, PREFIX);
    }
    
    public static MusapLinkConf getInstance() {
        if (instance == null) {
            instance = new MusapLinkConf("conf/musaplink.conf");
        }
        return instance;
    }

    /**
     * Get the DB JDBC URL
     * @return JDBC URL
     */
    public String getDbUrl() {
        return this.properties.getProperty(PREFIX + "db.url");
    }

    /**
     * Get the DB username
     * @return DB username
     */
    public String getDbUsername() {
        return this.properties.getProperty(PREFIX + "db.username");
    }

    /**
     * Get the DB password
     * @return Db password
     */
    public String getDbPassword() {
        return this.properties.getProperty(PREFIX + "db.password");
    }

    /**
     * Get the DB Driver class
     * @return DB Driver class. Default is org.sqlite.JDBC if not configured.
     */
    public String getDriverClass() {
        return this.properties.getProperty(PREFIX + "db.driver.class", "org.sqlite.JDBC");
    }

    /**
     * Get the FCM push notification configuration
     * @return FCM config
     */
    public FcmConfig getFcmConfig() {
        return this.fcmConfig;
    }

    /**
     * Get the FCM push notification configuration
     * @return FCM config
     */
    public ApnsConfig getApnsConfig() {
        return this.apnsConfig;
    }
    
    /**
     * Check if the configuration has been initialized
     * @return true if initialized
     */
    public boolean isInitialized() {
        return this.initialized;
    }
    
    /**
     * Get the configuration file path
     * @return conf file path
     */
    public String getConfFilePath() {
        if ("".equals(this.home)) return this.filename;
        return this.home + "/" + this.filename;
    }
    
    /**
     * Get a list of signature clients
     * @return Signature clients
     */
    public List<Etsi204Client> getClients() {
        List<Etsi204Client> clients = new ArrayList<>();
        
        // Find SSCDs with suffix 1-10
        for (int i = 1; i <= 10; i++) {
            try {
                String clientid = this.properties.getProperty(PREFIX + "client.clientid."  + i);
                String sscdtype = this.properties.getProperty(PREFIX + "client.sscdtype." + i);
                
                if (clientid == null) continue;
                
                String apid         = this.properties.getProperty(PREFIX + "client.apid."  + i);
                String appwd        = this.properties.getProperty(PREFIX + "client.appwd." + i);
                String restPassword = this.properties.getProperty(PREFIX + "client.restpassword." + i);
                String restApiKey   = this.properties.getProperty(PREFIX + "client.restapikey." + i);
                
                String signatureurl = this.properties.getProperty(PREFIX + "client.signatureurl." + i);
                String statusurl    = this.properties.getProperty(PREFIX + "client.statusurl." + i);
                String receipturl   = this.properties.getProperty(PREFIX + "client.receipturl." + i);
                String profileurl   = this.properties.getProperty(PREFIX + "client.profileurl." + i);
                String resturl      = this.properties.getProperty(PREFIX + "client.resturl." + i);
                
                String keystoreFile = this.properties.getProperty(PREFIX + "client.keystore." + i);
                String keystorePwd  = this.properties.getProperty(PREFIX + "client.keystore.pwd." + i);
                String keystoreType = this.properties.getProperty(PREFIX + "client.keystore.type." + i);
                String sigProfile   = this.properties.getProperty(PREFIX + "client.signature.profile." + i, Etsi204Client.SIGPROF_ALAUDA_SIGN);
                
                boolean enableNospam  = Boolean.valueOf(this.properties.getProperty(PREFIX + "client.nospam.enabled" + i));
                boolean enableEventid = Boolean.valueOf(this.properties.getProperty(PREFIX + "client.eventid.enabled." + i));

                ClientType clientType = ClientType.fromString(this.properties.getProperty(PREFIX + "client.type." + i));
                
                log.debug("Configuring client " + clientid + " with AP ID " + apid);
                
                clients.add(new Etsi204ClientBuilder(clientid, sscdtype)
                        .withApid(apid)
                        .withApPwd(appwd)
                        .withRestPassword(restPassword)
                        .withRestApiKey(restApiKey)
                        .withSignatureUrl(signatureurl)
                        .withStatusUrl(statusurl)
                        .withReceiptUrl(receipturl)
                        .withProfileUrl(profileurl)
                        .withRestUrl(resturl)
                        .withSignatureProfile(sigProfile)
                        .withKeystoreFile(keystoreFile)
                        .withKeystorePwd(keystorePwd)
                        .withKeystoreType(keystoreType)
                        .withEventIdEnabled(enableEventid)
                        .withNospamEnabled(enableNospam)
                        .withClientType(clientType)
                        .build());
            } catch (Exception e) {
                log.error("Failed to initialize client", e);
            }
            
        }
        return clients;
    }
    
    /**
     * Get a client for an external SSCD with given SSCD ID
     * @param clientid Client ID
     * @return Client if found, null otherwise
     */
    public Etsi204Client getClient(String clientid) {
        if (clientid == null) return null;
        for (Etsi204Client client : this.getClients()) {
            if (client == null) continue;
            if (clientid.equals(client.getClientId())) {
                return client;
            }
        }
        return null;
    }
    
    /**
     * Read Java properties from given file
     * @param filename Name of the file
     * @return Java properties
     */
    private Properties readProperties(String filename) {
        Properties properties = null;
        try {
            log.trace("Reading configuration file " + this.getConfFilePath());
            File f = new File(this.getConfFilePath());
            if (f.exists()) {
                try {
                    properties = new Properties();

                    InputStreamReader is = null;
                    FileInputStream  fis = null;
                    try {
                        fis = new FileInputStream(this.getConfFilePath());
                        is  = new InputStreamReader(fis, "UTF-8");
                        properties.load( is );
                    } finally {
                        if (is  != null) is.close();
                        if (fis != null) fis.close();
                    }
                    log.trace("Done reading");
                } catch (FileNotFoundException e) {
                    log.error(this.getConfFilePath() + " not found.");
                } catch (IOException e) {
                    log.error("Problem loading " + this.getConfFilePath());
                }
            } else {
                log.error(this.getConfFilePath() + " does not exist.");
            }
        } catch (Throwable t) {
            log.fatal("Failed to load configuration");
            log.fatal("", t);
        }
        return properties;
    }

    /**
     * Get the coupling lifetime in seconds
     * @return coupling lifetime (default 600)
     */
    public int getCouplingLifetime() {
        try {
            return Integer.parseInt(this.properties.getProperty(PREFIX + "coupling.lifetime", "600"));
        } catch (NumberFormatException e) {
            return 600;
        }
    }
    
    /**
     * Get the transaction lifetime in seconds
     * @return transaction lifetime (default 600)
     */
    public int getTxnLifetime() {
        try {
            return Integer.parseInt(this.properties.getProperty(PREFIX + "txn.lifetime", "600"));
        } catch (NumberFormatException e) {
            return 600;
        }
    }

    /**
     * Is the ListKeys MUSAP Link operation enabled? Default is false.
     * @return true if ListKeys is enabled.
     */
    public boolean isListKeysEnabled() {
        return Boolean.valueOf(this.properties.getProperty(PREFIX + "listkeys.enabled", "false"));
    }
    
}
