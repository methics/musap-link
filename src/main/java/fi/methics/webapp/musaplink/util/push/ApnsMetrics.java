package fi.methics.webapp.musaplink.util.push;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientMetricsListener;

/**
 * Simple metrics class that just prints to log
 */
public class ApnsMetrics implements ApnsClientMetricsListener {

    private static final Log log = LogFactory.getLog(ApnsMetrics.class);
    
    private AtomicInteger writeFailures         = new AtomicInteger();
    private AtomicInteger notificationsSent     = new AtomicInteger();
    private AtomicInteger notificationsAccepted = new AtomicInteger();
    private AtomicInteger notificationsRejected = new AtomicInteger();
    private AtomicInteger connectionsAdded      = new AtomicInteger();
    private AtomicInteger connectionsRemoved    = new AtomicInteger();
    
    @Override
    public void handleWriteFailure(ApnsClient apnsClient, long notificationId) {
        log.debug("APNS write failed. ID=" + notificationId + ", Total Count=" + this.writeFailures.incrementAndGet());
    }

    @Override
    public void handleNotificationSent(ApnsClient apnsClient, long notificationId) {
        log.debug("APNS notification sent. ID=" + notificationId+ ", Total Count=" + this.notificationsSent.incrementAndGet());
    }

    @Override
    public void handleNotificationAccepted(ApnsClient apnsClient, long notificationId) {
        log.debug("APNS notification accepted. ID=" + notificationId+ ", Total Count=" + this.notificationsAccepted.incrementAndGet());
    }

    @Override
    public void handleNotificationRejected(ApnsClient apnsClient, long notificationId) {
        log.debug("APNS notification rejected. ID=" + notificationId+ ", Total Count=" + this.notificationsRejected.incrementAndGet());
    }

    @Override
    public void handleConnectionAdded(ApnsClient apnsClient) {
        log.debug("APNS connection added. Total Count=" + this.connectionsAdded.incrementAndGet());
    }

    @Override
    public void handleConnectionRemoved(ApnsClient apnsClient) {
        log.debug("APNS connection removed. Total Count=" + this.connectionsRemoved.incrementAndGet());
    }

    @Override
    public void handleConnectionCreationFailed(ApnsClient apnsClient) {
        log.debug("APNS connection creation failed.");
    }

}
