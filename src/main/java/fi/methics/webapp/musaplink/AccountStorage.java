package fi.methics.webapp.musaplink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.link.MusapLinkServlet;
import fi.methics.webapp.musaplink.util.CouplingCode;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapLinkConf;
import fi.methics.webapp.musaplink.util.db.MusapDb;

/**
 * Database class for MUSAP Link Account Storage.
 * This stores MUSAP Link Accounts and Coupling Codes used during account enrollment.
 */
public class AccountStorage extends MusapDb {
    
    private static final Log log = LogFactory.getLog(MusapLinkServlet.class);

    private static final String INSERT_COUPLING_CODE = "INSERT INTO coupling_codes (couplingcode, linkid, created_dt) VALUES (?,?, ?)";
    private static final String SELECT_COUPLING_CODE = "SELECT linkid, couplingcode FROM coupling_codes WHERE couplingcode=?";
    private static final String DELETE_OLD_COUPLING_CODES = "DELETE FROM coupling_codes WHERE created_dt<?";

    private static final String INSERT_ACCOUNT       = "INSERT INTO musap_accounts (musapid, fcmtoken, apnstoken, created_dt) VALUES (?,?,?,?)";
    private static final String INSERT_LINKID        = "INSERT INTO link_ids (musapid, linkid, name) VALUES (?,?,?)";
    private static final String INSERT_KEYS          = "INSERT INTO transport_keys (musapid, mackey, enckey) VALUES (?,?,?)";
    
    private static final String UPDATE_ACCOUNT       = "UPDATE musap_accounts SET fcmtoken=?, apnstoken=? WHERE musapid=?";
    private static final String SELECT_ACCOUNT       = "SELECT musapid, fcmtoken, apnstoken FROM musap_accounts WHERE musapid=?";
    private static final String LIST_ACCOUNTS        = "SELECT musapid, fcmtoken, apnstoken FROM musap_accounts";
    
    private static final String INSERT_KEYDETAILS    = "INSERT INTO key_details (musapid, keyid, keyname) VALUES (?,?,?)";
    private static final String UPDATE_KEYDETAILS    = "UPDATE key_details SET (keyname) VALUES (?,?,?) WHERE musapid=? AND keyid=?";
    private static final String SELECT_KEYDETAILS    = "SELECT keyid FROM key_details WHERE musapid=? AND keyname=?";
    
    private static final String SELECT_MUSAPID_BY_LINKID   = "SELECT musapid FROM link_ids WHERE linkid=?";
    private static final String SELECT_LINKIDS_BY_MUSAPID  = "SELECT linkid  FROM link_ids WHERE musapid=?";

    /**
     * Add a linkid to an existing MUSAP account
     * @param musapid
     * @param linkid
     */
    public static void addLinkId(String musapid, String linkid) {
        MusapLinkAccount account = findByMusapId(musapid);
        if (account == null) {
            log.warn("No account found with musapid " + musapid + ". Not linking.");
            return;
        }
        
        String name = null; // TODO
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_LINKID))
        {
            ps.setString(1, musapid);
            ps.setString(2, linkid);
            ps.setString(3, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed insert Link ID", e);
            throw new MusapException(e);
        }
    }
    
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
     * Find a MUSAP account by Link ID
     * @param linkid Link ID
     * @return MusapAccount or null
     */
    public static MusapLinkAccount findByLinkId(String linkid) {
        if (linkid == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_MUSAPID_BY_LINKID))
        {
            ps.setString(1, linkid);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    return findByMusapId(result.getString(1));
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP account", e);
            throw new MusapException(e);
        }
        return null;
    }
    
    /**
     * Find a MUSAP account by MUSAP ID
     * @param musapid MUSAP ID
     * @return MusapAccount or null
     */
    public static MusapLinkAccount findByMusapId(String musapid) {
        if (musapid == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNT))
        {
            ps.setString(1, musapid);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    MusapLinkAccount account = new MusapLinkAccount();
                    account.musapid = result.getString(1);
                    account.fcmToken = result.getString(2);
                    account.apnsToken = result.getString(3);
                    account.linkids = new HashSet<>(getLinkIds(musapid));
                    return account;
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP account", e);
            throw new MusapException(e);
        }
        return null;
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
     * Get KeyID based on the given keyname
     * @param account MUSAP account
     * @param keyname Key name
     * @return KeyID or null if not found
     */
    public static String getKeyId(MusapLinkAccount account, String keyname) {
        if (account == null) return null;
        if (keyname == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_KEYDETAILS))
        {
            ps.setString(1, account.musapid);
            ps.setString(2,  keyname);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    return result.getString(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP account", e);
            throw new MusapException(e);
        }
        return null;
    }
    
    public static List<String> getLinkIds(String musapid) {
        List<String> linkids = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_LINKIDS_BY_MUSAPID))
        {
            ps.setString(1, musapid);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    linkids.add(result.getString(1));
                }
            }
        } catch (SQLException e) {
            log.error("Failed get Link IDs", e);
            throw new MusapException(e);
        }
        return linkids;
    }
    
    public static Collection<MusapLinkAccount> listAccounts() {
        
        List<MusapLinkAccount> accounts = new ArrayList<>();
        
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(LIST_ACCOUNTS))
        {
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    MusapLinkAccount account = new MusapLinkAccount();
                    account.musapid = result.getString(1);
                    account.fcmToken = result.getString(2);
                    account.apnsToken = result.getString(3);
                    account.linkids = new HashSet<>(getLinkIds(account.musapid));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP ID", e);
            throw new MusapException(e);
        }
        return accounts;
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
                AccountStorage.cleanCouplingCodes();
            }
        }, interval, interval);
        return timer;
    }
    
    /**
     * Store a MUSAP account
     * @param account New account
     */
    public static void storeAccount(MusapLinkAccount account) {
        if (account == null || account.musapid == null) {
            log.error("Ignoring account with null musapid");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ACCOUNT))
        {
            ps.setString(1, account.musapid);
            ps.setString(2, account.fcmToken);
            ps.setString(3, account.apnsToken);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed insert MUSAP account", e);
            throw new MusapException(e);
        }
    }

    /**
     * Update a MUSAP account
     * @param musapid MUSAP ID
     * @param account New account data
     */
    public static void updateAccount(String musapid, MusapLinkAccount account) {
        if (account == null || account.musapid == null) {
            log.error("Ignoring account with null linkid");
            return;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ACCOUNT))
        {
            ps.setString(1, account.fcmToken);
            ps.setString(2, account.apnsToken);
            ps.setString(3, account.musapid);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed update MUSAP account", e);
            throw new MusapException(e);
        }
    }
    /**
     * Store a MUSAP account
     * @param account New account
     */
    public static void upsertKeyDetails(MusapLinkAccount account, String keyname, String keyid) {
        if (account == null || account.musapid == null) {
            log.error("Ignoring account with null musapid");
            return;
        }
        if (keyname == null || keyid == null) return;
        
        if (getKeyId(account, keyname) != null) {
            
            // Update
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_KEYDETAILS))
               {
                   ps.setString(1, keyname);
                   ps.setString(2, account.musapid);
                   ps.setString(3, keyid);
                   ps.executeUpdate();
                   log.debug("Updated keyname to " + keyname + " (keyid=" + keyid + ")");
               } catch (SQLException e) {
                   log.error("Failed insert MUSAP account", e);
                   throw new MusapException(e);
               }
        } else {
            
            // Insert
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_KEYDETAILS))
            {
                ps.setString(1, account.musapid);
                ps.setString(2, keyid);
                ps.setString(3, keyname);
                ps.executeUpdate();
                log.debug("Inserted keyname " + keyname + " for keyid " + keyid);
            } catch (SQLException e) {
                log.error("Failed insert MUSAP account", e);
                throw new MusapException(e);
            }
        }

    }
    
}
