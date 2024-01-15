package fi.methics.webapp.musaplink.util.push;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.util.MusapLinkConf;
import fi.methics.webapp.musaplink.util.push.datatype.ApnsReq;
import fi.methics.webapp.musaplink.util.push.datatype.FcmReq;

/**
 * Simple class for FCM and APNs push notification sending
 */
public class PushClient {

    /**
     * Send a push notification to the user 
     * @param account User account
     * @param message Notification message to send
     */
    public static void sendPushNotification(MusapLinkAccount account, String message) {
        
        if (account == null) {
            return;
        }
        
        // First check if we should send FCM notification
        FcmClient fcmClient = MusapLinkConf.getInstance().getFcmConfig().getClient();
        if (fcmClient.isEnabled() && account.fcmToken != null) {
            FcmReq fcmReq = FcmReq.makeNotificationMessage(account.fcmToken, message);
            fcmClient.send(fcmReq);
            return;
        }
        
        // Then check APNs
        ApnsClient apnsClient = MusapLinkConf.getInstance().getApnsConfig().getClient();
        if (apnsClient.isEnabled() && account.apnsToken != null) {
            ApnsReq apnsReq = new ApnsReq();
            apnsClient.sendMessage(apnsReq);
            return;
        }
    }

}
