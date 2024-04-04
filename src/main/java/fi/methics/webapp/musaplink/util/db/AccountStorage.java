package fi.methics.webapp.musaplink.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;
import fi.methics.webapp.musaplink.util.MusapException;
import fi.methics.webapp.musaplink.util.MusapLinkConf;

/**
 * Database class for MUSAP Link Account Storage.
 * This stores MUSAP Link Accounts and Coupling Codes used during account enrollment.
 */
public class AccountStorage extends MusapDb {
    
    private static final Log log = LogFactory.getLog(AccountStorage.class);

    private static final String INSERT_ACCOUNT       = "INSERT INTO musap_accounts (musapid, fcmtoken, apnstoken, created_dt) VALUES (?,?,?,?)";
    private static final String INSERT_LINKID        = "INSERT INTO link_ids (musapid, linkid, name) VALUES (?,?,?)";
    private static final String INSERT_KEYS          = "INSERT INTO transport_keys (musapid, mackey, enckey) VALUES (?,?,?)";
    private static final String SELECT_KEYS          = "SELECT mackey, enckey FROM transport_keys WHERE musapid=?";
    
    private static final String UPDATE_ACCOUNT       = "UPDATE musap_accounts SET fcmtoken=?, apnstoken=? WHERE musapid=?";
    private static final String SELECT_ACCOUNT       = "SELECT musapid, fcmtoken, apnstoken FROM musap_accounts WHERE musapid=?";
    private static final String LIST_ACCOUNTS        = "SELECT musapid, fcmtoken, apnstoken FROM musap_accounts";
    
    private static final String INSERT_KEYDETAILS         = "INSERT INTO key_details (musapid, keyid, keyname, certificate, publickey) VALUES (?,?,?,?,?)";
    private static final String UPDATE_KEYDETAILS         = "UPDATE key_details SET keyname=?, certificate=?, publickey=?, modified_dt=? WHERE musapid=? AND keyid=?";
    private static final String SELECT_KEYDETAILS         = "SELECT keyid, keyname, certificate, publickey FROM key_details WHERE musapid=? AND keyname=?";
    private static final String SELECT_KEYDETAILS_BY_ID   = "SELECT keyid, keyname, certificate, publickey FROM key_details WHERE musapid=? AND keyid=?";
    private static final String LIST_KEYDETAILS           = "SELECT keyid, keyname, certificate, publickey FROM key_details WHERE musapid=?";

    private static final String SELECT_MUSAPID_BY_LINKID   = "SELECT musapid FROM link_ids WHERE linkid=?";
    private static final String SELECT_LINKIDS_BY_MUSAPID  = "SELECT linkid  FROM link_ids WHERE musapid=?";

    public static final String SIMULATED_LINKID = "SIMULATED-LINKID";

    /**
     * Add a linkid to an existing MUSAP account
     * @param musapid
     * @param linkid
     */
    public static void addLinkId(String musapid, String linkid) {
        MusapLinkAccount account = findAccountByMusapId(musapid);
        if (account == null) {
            log.warn("No account found with musapid " + musapid + ". Not linking.");
            return;
        }
        
        String name = null;
        
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
     * Find a MUSAP account by Link ID
     * @param linkid Link ID
     * @return MusapAccount or null
     */
    public static MusapLinkAccount findAccountByLinkId(String linkid) {
        if (linkid == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_MUSAPID_BY_LINKID))
        {
            ps.setString(1, linkid);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    return findAccountByMusapId(result.getString(1));
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
    public static MusapLinkAccount findAccountByMusapId(String musapid) {
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
                    account.linkids = new HashSet<>(listLinkIds(musapid));
                    fillTransportKeys(conn, account);
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
     * Get KeyID based on the given keyname
     * @param account MUSAP account
     * @param keyid Key name
     * @return KeyID or null if not found
     */
    public static MusapKey findKeyDetailsByKeyID(MusapLinkAccount account, String keyid) {
        if (account == null) return null;
        if (keyid   == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_KEYDETAILS_BY_ID))
        {
            ps.setString(1, account.musapid);
            ps.setString(2, keyid);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    MusapKey key = new MusapKey();
                    key.keyid       = result.getString(1);
                    key.keyname     = result.getString(2);
                    key.certificate = result.getBytes(3);
                    key.publickey   = result.getBytes(4);
                    return key;
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP account", e);
            throw new MusapException(e);
        }
        return null;
    }

    /**
     * Get KeyID based on the given keyname
     * @param account MUSAP account
     * @param keyname Key name
     * @return KeyID or null if not found
     */
    public static MusapKey findKeyDetailsByKeyname(MusapLinkAccount account, String keyname) {
        if (account == null) return null;
        if (keyname == null) return null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_KEYDETAILS))
        {
            ps.setString(1, account.musapid);
            ps.setString(2, keyname);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    MusapKey key = new MusapKey();
                    key.keyid       = result.getString(1);
                    key.keyname     = result.getString(2);
                    key.certificate = result.getBytes(3);
                    key.publickey   = result.getBytes(4);
                    return key;
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP account", e);
            throw new MusapException(e);
        }
        return null;
    }
    
    /**
     * List all LinkIDs related to given MUSAP ID
     * @param musapid MUSAP ID
     * @return List of LinkIDs
     */
    public static List<String> listLinkIds(String musapid) {
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
    
    /**
     * List all stored MUSAP accounts
     * @return MUSAP accounts
     */
    public static Collection<MusapLinkAccount> listAccounts() {
        
        List<MusapLinkAccount> accounts = new ArrayList<>();
        
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(LIST_ACCOUNTS))
        {
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    MusapLinkAccount account = new MusapLinkAccount();
                    account.musapid = result.getString(1);
                    account.fcmToken = result.getString(2);
                    account.apnsToken = result.getString(3);
                    account.linkids = new HashSet<>(listLinkIds(account.musapid));
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
     * List key details for a MUSAP account. Note that this returns nothing if ListKeys is not enabled in configuration.
     * @param account MUSAP Account whose keys to list
     * @return List of key details
     */
    public static Collection<MusapKey> listKeyDetails(MusapLinkAccount account) {
        List<MusapKey> keys = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(LIST_KEYDETAILS))
        {
            ps.setString(1,  account.musapid);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    MusapKey key = new MusapKey();
                    key.keyid       = result.getString(1);
                    key.keyname     = result.getString(2);
                    key.certificate = result.getBytes(3);
                    key.publickey   = result.getBytes(4);
                    keys.add(key);
                }
            }
        } catch (SQLException e) {
            log.error("Failed get MUSAP ID", e);
            throw new MusapException(e);
        }
        return keys;
        
    }
    
    /**
     * Store a MUSAP account
     * @param account New account
     */
    public static void storeAccount(MusapLinkAccount account) {
        if (account == null || account.musapid == null) {
            log.error("Ignoring account with null MUSAP ID");
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
            
            storeTransportKeys(conn, account);
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
     * Update or insert key details
     * @param account Related MUSAP account
     * @param key Key details to update or insert
     */
    public static void upsertKeyDetails(MusapLinkAccount account, MusapKey key) {
        if (account == null || account.musapid == null) {
            log.error("Ignoring account with null musapid");
            return;
        }
        if (key == null || key.keyid == null) {
            log.debug("Skipping key update: keyid is null");
            return;
        }
        String keyname = key.keyname;
        String keyid   = key.keyid;

        if (!MusapLinkConf.getInstance().isListKeysEnabled()) {
            // Do not store cert or public key if list keys is not enabled
            key.certificate = null;
            key.publickey   = null;
        }
        
        MusapKey oldKey = findKeyDetailsByKeyname(account, keyname);
        if (oldKey == null) {
            oldKey = findKeyDetailsByKeyID(account, keyid);
        }
        
        if (oldKey != null) {
            if (key.keyname     == null) key.keyname     = oldKey.keyname;
            if (key.certificate == null) key.certificate = oldKey.certificate;
            if (key.publickey   == null) key.publickey   = oldKey.publickey;
        
            if (oldKey.equals(key)) {
                // Keys equal already
                log.debug("Keys are equal. Not updating key with keyid " + keyid);
                return;
            }
            
            // Update
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_KEYDETAILS))
               {
                   ps.setString(1, key.keyname);
                   ps.setBytes(2,  key.certificate);
                   ps.setBytes(3,  key.publickey);
                   ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                   ps.setString(5, account.musapid);
                   ps.setString(6, keyid);
                   ps.executeUpdate();
                   log.debug("Updated key details for keyid " + keyid);
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
                ps.setBytes(4, key.certificate);
                ps.setBytes(5, key.publickey);
                ps.executeUpdate();
                log.debug("Inserted key details for keyid " + keyid);
            } catch (SQLException e) {
                log.error("Failed insert MUSAP account", e);
                throw new MusapException(e);
            }
        }

    }
    

    /**
     * Store transport encryption keys.
     * Does nothing if given account object has no keys.
     * @param conn DB connection
     * @param account Account that contains the keys
     */
    private static void storeTransportKeys(Connection conn, MusapLinkAccount account) {
        if (account == null) return;
        if (account.aesKey == null) return;
        if (account.macKey == null) return;
        
        log.debug("Storing transport keys for MUSAP ID " + account.musapid);
        try (PreparedStatement ps = conn.prepareStatement(INSERT_KEYS)) {
            ps.setString(1, account.musapid);
            ps.setBytes(2,  account.macKey);
            ps.setBytes(3,  account.aesKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed insert MUSAP transport keys", e);
            throw new MusapException(e);
        }
    }
    
    /**
     * Fetch and fill transport encryption keys to given MUSAP account
     * @param conn    DB Connection
     * @param account Account that should be filled
     * @return Account with keys
     */
    private static MusapLinkAccount fillTransportKeys(Connection conn, MusapLinkAccount account) {
        
        log.debug("Looking for transport keys");
        if (account == null) return null;
        
        try (PreparedStatement ps = conn.prepareStatement(SELECT_KEYS)) {
           ps.setString(1, account.musapid);
           try (ResultSet result = ps.executeQuery()) {
               if (result.next()) {
                   account.macKey = result.getBytes(1);
                   account.aesKey = result.getBytes(2);
                   if (account.aesKey != null) log.debug("Found AES key of " + account.aesKey.length + " bytes");
                   if (account.macKey != null) log.debug("Found MAC key of " + account.macKey.length + " bytes");
               }
           }
       } catch (SQLException e) {
           log.error("Failed get transport keys", e);
           throw new MusapException(e);
       }
        return account;
    }
    
    
}
