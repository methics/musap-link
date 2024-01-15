package fi.methics.webapp.musaplink;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.coupling.json.SignatureReq;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;
import fi.methics.webapp.musaplink.util.IdGenerator;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapLinkConf;
import fi.methics.webapp.musaplink.util.SignatureCallback;
import fi.methics.webapp.musaplink.util.db.MusapDb;

/**
 * Database class for MUSAP Link transaction storage.
 */
public class TxnStorage extends MusapDb {

    private static final Log log = LogFactory.getLog(TxnStorage.class);

    private static final Map<String, SignatureCallback> SIGNATURE_CALLBACKS = new HashMap<>();
    
    private static final String INSERT_SIGN_REQ    = "INSERT INTO transactions (transid, linkid, request, created_dt) VALUES (?,?,?,?)";
    private static final String SELECT_SIGN_REQ    = "SELECT request, transid FROM transactions WHERE linkid=? AND response IS NULL AND created_dt >= CURRENT_TIMESTAMP - interval '2 minutes' ORDER BY created_dt DESC LIMIT 1";

    private static final String DELETE_TXN         = "DELETE FROM transactions WHERE transid=?";
    private static final String DELETE_OLD_TXNS    = "DELETE FROM transactions WHERE created_dt<?";
    private static final String COUNT_TRANSACTIONS = "SELECT COUNT(*) FROM transactions";
    
    /**
     * Store a signature request and assign txnid to it
     * @param linkid Link ID the request is sent to
     * @param req    The signature request
     * @return SignatureCallback
     */
    public static SignatureCallback storeRequest(String linkid, SignatureReq req) {
        String transid = IdGenerator.generateTxnId();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SIGN_REQ))
        {
            ps.setString(1, transid);
            ps.setString(2, linkid);
            ps.setString(3, req.toJson());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed insert transaction", e);
            throw new MusapException(e);
        }
        SignatureCallback callback = new SignatureCallback(transid);
        SIGNATURE_CALLBACKS.put(transid, callback);
        return callback;
    }
    
    /**
     * Store a signature response
     * @param transid Transaction ID
     * @param linkid  MUSAP Link Account ID
     * @param resp    Signature response
     */
    public static void storeResponse(String transid, MusapSignResp resp) {
        log.info("Storing a signature response with transid=" + transid + ")");

        SignatureCallback callback = SIGNATURE_CALLBACKS.get(transid);
        if (callback != null) {
            callback.setResponse(resp);
            callback.release();
        } else {
            log.debug("Found no callback");
        }
    }
    
    /**
     * Delete a transaction that has been handled
     * @param transid Transaction ID
     * @param linkid  MUSAP Link Account ID
     */
    public static void deleteTransaction(String transid) {
        log.info("Deleting handled transaction (transid=" + transid + ")");
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_TXN))
        {
            ps.setString(1, transid);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed delete transaction", e);
        }
    }
    
    /**
     * Get a Signature Response
     * @param transid Transaction ID
     * @param linkid  MUSAP Link Account ID
     * @return Signature Response or null if not found
     */
    public static MusapSignResp getSignResp(String transid) {
        SignatureCallback callback = SIGNATURE_CALLBACKS.get(transid);
        if (callback != null) {
            return callback.getResponse();
        }
        return null;
    }
    
    /**
     * Get a Signature Request from the DB
     * @param linkid  MUSAP Link Account ID
     * @return Signature Request or null if not found
     */
    public static SignatureReq getSignReq(String linkid) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SIGN_REQ))
        {
            ps.setString(1, linkid);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    SignatureReq req = SignatureReq.fromJson(result.getString(1), SignatureReq.class);
                    req.transid = result.getString(2);
                    return req;
                }
            }
        } catch (SQLException e) {
            log.error("Failed get transaction", e);
            throw new MusapException(e);
        }
        return null;
    }
    
    /**
     * Count total amount of transactions currently being processed
     * @return transaction count
     */
    public static int countTransactions() {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(COUNT_TRANSACTIONS))
        {
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed count transactions", e);
            throw new MusapException(e);
        }
        return 0;
    }

    
    /**
     * Clean old transactions
     */
    public static void cleanTransactions() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_OLD_TXNS))
        {
            int cutoff = MusapLinkConf.getInstance().getTxnLifetime();
            int cutoffMs = cutoff * 1000;
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - cutoffMs));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed clean transactiosn", e);
        }
    }

    /**
     * Schedule a transaction cleanup task
     * @param interval Task run interval (milliseconds)
     * @return a handle to the timer
     */
    public static Timer scheduleCleaner(long interval) {

        Timer timer = new Timer();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                TxnStorage.cleanTransactions();
            }
        }, interval, interval);
        return timer;
    }
    
}
