//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved.
//
package fi.methics.webapp.musaplink.util.push;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.auth.oauth2.GoogleCredentials;

import fi.methics.webapp.musaplink.util.push.datatype.FcmReq;
import fi.methics.webapp.musaplink.util.push.datatype.FcmResp;


/**
 * FCM client using the HTTPv1 API
 *
 * @see https://firebase.google.com/codelabs/use-the-fcm-http-v1-api-with-oauth-2-access-tokens
 */
public class FcmClient {

    private static final Log log = LogFactory.getLog(FcmClient.class);

    private static final String PROJECT_NAME = "[PROJECT_NAME]";
    private static final String FCM_URL      = "https://fcm.googleapis.com/v1/projects/[PROJECT_NAME]/messages:send";
    
    private CloseableHttpClient client;
    
    private boolean   debug;
    private String    url;
    private FcmConfig config;
    
    private String accessToken;
    
    /**
     * Create a new FCM HTTP client
     * @param config FCM configuration
     */
    public FcmClient(final FcmConfig config) {
        this.config = config;
        if (config.isEnabled()) {
            this.client = config.getHttpClient();
            this.debug  = config.isFcmDebugEnabled();
            this.url    = FCM_URL.replace(PROJECT_NAME, config.getFcmProjectName());
    
            try (InputStream is = config.getGoogleServicesJson()) {
                this.accessToken = getAccessToken(is);
            } catch (Exception e) {
                log.error("Failed to get access token", e);
            }
            
            log.debug("FcmHttpClient initialized");
        }
    }
    
    /**
     * Check if this client is enabled?
     * @return true if enabled
     */
    public boolean isEnabled() {
        return this.config.isEnabled();
    }
    
    /**
     * Send an FCM notification
     * @param fcm notification request
     */
    public void send(FcmReq fcm) {
        
        if (!this.isEnabled()) {
            return;
        }
        
        if (fcm.getToken() == null) {
            log.warn("No FCM token available for user yet");
            return;
        }
        
        this.sendMessage(fcm);
    }

    /**
     * Send an FCM notification request
     * @param fcm request
     */
    private void sendMessage(final FcmReq fcm) {
        
        HttpPost post = new HttpPost(this.url);
        post.addHeader("Authorization", "Bearer " + this.accessToken);
        post.addHeader("X-GFE-SSL", "yes");
        post.addHeader("Content-Type", "application/json");
        
        final String reqBody = fcm.toJson();
        if (!reqBody.isEmpty()) {
            post.setEntity(new StringEntity(reqBody, "UTF-8"));
        }
        
        if (this.debug) {
            log.debug("Sending FCM request: " + reqBody);
        }
        
        try (CloseableHttpResponse resp = this.client.execute(post)) {
            String bodyStr = getResponseBody(resp);
            if (this.debug) {
                log.debug("Got FCM response: " + bodyStr);
            }
            FcmResp jResp = FcmResp.from(bodyStr);
            if (jResp.isSuccessful()) {
                log.info("FCM notification request succeeded");
            } else {
                log.info("FCM notification request failed: " + jResp.getError());
            }
        } catch (Exception e) {
            log.info("FCM notification request failed", e);
        }
    }
    
    /**
     * Get response body as UTF-8 String
     * @param resp HTTP response
     * @return UTF-8 String
     * @throws ParseException
     * @throws IOException
     */
    private static String getResponseBody(final HttpResponse resp) throws ParseException, IOException {
        return resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
    }
    
    /**
     * Get the FCM access token
     * @param is InputStream of the google-services.json
     * @return access token
     * @throws IOException
     */
    private static String getAccessToken(InputStream is) throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(is).createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }
    
}
