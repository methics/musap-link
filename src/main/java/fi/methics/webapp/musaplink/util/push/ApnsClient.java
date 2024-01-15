package fi.methics.webapp.musaplink.util.push;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;

import fi.methics.webapp.musaplink.util.push.datatype.ApnsReq;


public class ApnsClient {

    private static final Log log = LogFactory.getLog(ApnsClient.class);
    
    private final ApnsConfig config;
    private final com.eatthepath.pushy.apns.ApnsClient client;
    
    /**
     * Initialize new APNS client with given 
     * @param c
     */
    public ApnsClient(final ApnsConfig c) {
        try {
            this.config = c;
            
            if (this.config.isEnabled()) {
                boolean prod = this.config.isProductionEnabled();
                this.client = this.buildClient(prod);
            } else {
                this.client = null;
            }
        } catch (Exception e) {
            log.fatal("Failed to configure APNSClient: ", e);
            throw new RuntimeException(e);
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
     * Send an APNS notification
     * @param msg AFEMessage containing notification details
     */
    public void sendMessage(final ApnsReq msg) {
        
        if (!this.isEnabled()) {
            return;
        }
        
        if (msg == null) {
            log.trace("Skipping NULL APNS message");
            return;
        }
        log.info("Sending APNS notification: " + msg);
        final String topic = this.resolveTopic(msg);
        final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(msg.token, topic, msg.payload);

        if (this.config.getOverridePayload() != null) {
            msg.payload = this.config.getOverridePayload();
        }
        
        if (msg.token == null) {
            log.warn("No APNS token available for user yet");
            return;
        }
        
        try {
            this.sendNotification(msg, topic, pushNotification);
        } catch (Exception e) {
            log.error("Failed to send push notification.", e);
        }
    }
    
    /**
     * Shut down the client
     */
    public void shutdown() {
        try {
            if (this.client != null) this.client.close();
        } catch (Exception e) {
            log.warn("Failed to close APNS client", e);
        }
    }
    
    /**
     * Build an APNS client
     * @param prod Should this client connect to prod (true) or development (false)?
     * @return APNS client
     */
    private com.eatthepath.pushy.apns.ApnsClient buildClient(boolean prod) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        final ApnsClientBuilder builder = new ApnsClientBuilder();
        if (this.config.isProductionEnabled()) {
            builder.setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST);
        } else {
            builder.setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
        }

        final KeyStore       keystore   = this.config.getKeystore();
        final ApnsSigningKey signingKey = this.config.getSigningKey();
        if (keystore != null) {
            builder.setClientCredentials(new File(this.config.getKeystoreFile()), this.config.getKeystorePwd());
        } else {
            builder.setSigningKey(signingKey);
        }
        builder.setConcurrentConnections(this.config.getMaxConnections());
        
        if (this.config.isDebugEnabled()) {
            builder.setMetricsListener(new ApnsMetrics());
        }
        
        return builder.build();
    }
    
    /**
     * Handle APNS push notification response
     * @param msg        AFE message
     * @param topic      APNS topic
     * @param isFallback Is this a fallback callback? (if not, we can try to fallback)
     * @param response   APNS response
     * @param e          APNS error
     */
    private void completeCallback(final ApnsReq msg,
                                  final SimpleApnsPushNotification pushNotification,
                                  final String topic,
                                  final boolean isFallback,
                                  final PushNotificationResponse<SimpleApnsPushNotification> response,
                                  final Throwable e) {
        
        log.info("Got push notification response");
        if (response == null) {
            log.error("Failed to send push notification.", e);
            return;
        }
        
        if (response.isAccepted()) {
            log.info("Push notification accepted by APNs gateway.");
        } else {
            String reason = response.getRejectionReason();
            log.info("Notification rejected by the APNs gateway: " + reason);
            response.getTokenInvalidationTimestamp().ifPresent(timestamp -> {
                log.info("  and the token is invalid as of " + timestamp);
            });
            this.printError(reason, topic);
        }
    }
    
    /**
     * Print a pretty warning depending on the failure reason
     * @param reason Failure reason
     * @param topic  APNs topic
     */
    private void printError(String reason, String topic) {
        if (reason != null) {
            switch (reason) {
            case "BadCollapseId":
                log.warn("Collapse ID exceeds maximum size");
                break;
            case "BadDeviceToken":
                log.warn("Device token is invalid");
                break;
            case "BadMessageId":
                log.warn("MessageID is invalid");
                break;
            case "MissingDeviceToken":
                log.warn("Missing device token");
                break;
            case "TopicDisallowed":
                log.warn("Pushing to topic " + topic + " is disallowed by APNS");
                break;
            case "BadCertificate":
                log.warn("APNS certificate is invalid");
                break;
            case "PayloadTooLarge":
                log.warn("Push notification payload is too large");
                break;
            case "TooManyRequests":
                log.warn("Too many requests were made consecutively to the same device token");
                break;
            case "DeviceTokenNotForTopic":
                log.warn("The device token does not match topic " + topic);
                break;
            default:
                // Do nothing
            }
        }
    }

    /**
     * Resolve topic. If notification req contains topic, returns that.
     * Otherwise returns configured topic.
     * @param req Notification req
     * @return Topic
     */
    private String resolveTopic(final ApnsReq req) {
        String topic = this.config.getTopic();
        if (req != null && req.topic != null) {
            return topic = req.topic;
        }
        log.debug("Using APNS topic " + topic);
        return topic;
    }

    /**
     * Send a notification using {@link #client}
     * @param msg              AFE message
     * @param topic            APNS topic
     * @param pushNotification APNS push notification request
     */
    private void sendNotification(final ApnsReq msg,
                                  final String topic,
                                  final SimpleApnsPushNotification pushNotification) 
    {
        final CompletableFuture<PushNotificationResponse<SimpleApnsPushNotification>> future = this.client.sendNotification(pushNotification);
        future.whenComplete((response, e) -> this.completeCallback(msg, pushNotification, topic, false, response, e));
    }
    
}
