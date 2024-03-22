package fi.methics.webapp.musaplink.util.push;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


/**
 * 
 * Example:
 * <pre>
 * webapp.musaplink.fcm.projectid   = 1000707872362
 * webapp.musaplink.fcm.projectname = test-app
 * webapp.musaplink.fcm.debug       = true
 * webapp.musaplink.google.services.file = ...
 * </pre>
 */
public class FcmConfig  {

    private static final Log log = LogFactory.getLog(FcmConfig.class);
    
    public final static String TYPE = "FCM";
    
    private final boolean fcmDebugEnable;
    private final boolean fcmProdEnable;
    private final String  fcmProjectID;
    private final String  fcmProjectName;
    
    private final String  googleServicesJsonFile;
    
    private final Properties properties;
    
    private FcmClient client;

    public FcmConfig(final Properties p, final String prefix)
    {
        this.properties     = p;
        this.fcmProdEnable  = Boolean.parseBoolean(p.getProperty(prefix  + "fcm.production"));
        this.fcmDebugEnable = Boolean.parseBoolean(p.getProperty(prefix  + "fcm.debug"));
        this.fcmProjectID   = p.getProperty(prefix + "fcm.projectid");
        this.fcmProjectName = p.getProperty(prefix + "fcm.projectname", this.fcmProjectID);
        this.googleServicesJsonFile = p.getProperty(prefix + "google.services.file", "conf/google-services.json");
        
        if (this.isEnabled()) {
            log.info("FCM Push Notifications are enabled");
        } else {
            log.debug("FCM Push Notifications are disabled");
        }
    }
    
    public FcmClient getClient() {
        if (this.client == null) {
            this.client = new FcmClient(this);
        }
        return this.client;
    }

    public boolean isEnabled() {
        return this.fcmProjectID != null;
    }
    
    public String getFcmProjectId() {
        return this.fcmProjectID;
    }

    public String getFcmProjectName() {
        return this.fcmProjectName;
    }
    
    public boolean isFcmDebugEnabled() {
        return this.fcmDebugEnable;
    }
    
    public boolean isFcmProdEnabled() {
        return this.fcmProdEnable;
    }

    public Properties getProperties() {
        return this.properties;
    }
    
    /**
     * Get a HTTP client for FCM HTTP
     * @return HTTP client
     */
    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    public InputStream getGoogleServicesJson() throws FileNotFoundException {
        return new FileInputStream(this.googleServicesJsonFile);
    }

}
