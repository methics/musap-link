package fi.methics.webapp.musaplink.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.util.CouplingCode;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapLinkConf;

/**
 * Database class for MUSAP Link Coupling Codes.
 */
public class CouplingStorage extends MusapDb {
    
    private static final Log log = LogFactory.getLog(CouplingStorage.class);

    private static final String INSERT_COUPLING_CODE       = "INSERT INTO coupling_codes (couplingcode, linkid, created_dt) VALUES (?,?, ?)";
    private static final String SELECT_COUPLING_CODE       = "SELECT linkid, couplingcode FROM coupling_codes WHERE couplingcode=?";
    private static final String DELETE_OLD_COUPLING_CODES  = "DELETE FROM coupling_codes WHERE created_dt<?";

    /**
     * Clean old coupling codes
     */
    public static void cleanCouplingCodes() {
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(DELETE_OLD_COUPLING_CODES))
        {
            int cutoff = MusapLinkConf.getInstance().getCouplingLifetime();
            int cutoffMs = cutoff * 1000;
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - cutoffMs));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed clean coupling codes", e);
        }
    }
    
    /**
     * Check if a Link ID is found for the given coupling code. Removes it from the list if found.
     * @param couplingCode Coupling Code
     * @return Link ID if found. Null otherwise.
     */
    public static String findLinkId(String couplingCode) {
        
        String linkid = null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COUPLING_CODE))
        {
            ps.setString(1, couplingCode);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    linkid = result.getString(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed find Link ID", e);
            throw new MusapException(e);
        }
        return linkid;
    }
    
    /**
     * Generate a new Coupling Code for given LinkID. Store the combination.
     * @param linkid Link ID
     * @return Coupling Code
     */
    public static synchronized CouplingCode newCouplingCode(String linkid) {
        CouplingCode couplingCode = new CouplingCode();
        
        while (findLinkId(couplingCode.getCode()) != null) {
            couplingCode = new CouplingCode();
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_COUPLING_CODE))
        {
            ps.setString(1, couplingCode.getCode());
            ps.setString(2, linkid);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed insert Coupling Code", e);
            throw new MusapException(e);
        }
        return couplingCode;
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
                CouplingStorage.cleanCouplingCodes();
            }
        }, interval, interval);
        return timer;
    }

}
