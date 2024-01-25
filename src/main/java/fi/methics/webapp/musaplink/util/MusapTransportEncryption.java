package fi.methics.webapp.musaplink.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;

public class MusapTransportEncryption {

    private static final Log log = LogFactory.getLog(MusapTransportEncryption.class);

    // List of message types that should not be encrypted or decrypted
    private static final List<String> NO_ENCRYPT_TYPES = Arrays.asList(new String[]{ });
    private static final List<String> NO_DECRYPT_TYPES = Arrays.asList(new String[]{ "enrolldata" });

    private static final ExpirableMap<String, TransportKeys> EXP_MAP = new ExpirableMap<>(Interval.ofMinutes(10).toMillis());
    private static final Map<String, TransportKeys>          CACHE   = Collections.synchronizedMap(EXP_MAP);

    private MusapLinkConf config;
    
    /**
     * Check if the given message is one that should be encrypted
     * @param msg {@link CouplingApiMessage} message
     * @return true if message should be encrypted
     */
    public static boolean shouldEncrypt(final CouplingApiMessage msg) {
        if (msg == null) return false;
        if (msg.isEncrypted()) return false;
        if (NO_ENCRYPT_TYPES.contains(msg.type)) return false;
        return true;
    }
    
    /**
     * Check if the given message is one that should be decrypted
     * @param msg {@link CouplingApiMessage} message
     * @return true if message should be decrypted
     */
    public static boolean shouldDecrypt(final CouplingApiMessage msg) {
        if (msg == null) return false;
        // Ignore msg.isEncrypted() as it only checks if iv is present
        if (NO_DECRYPT_TYPES.contains(msg.type)) return false;
        return true;
    }

    public MusapTransportEncryption(final MusapLinkConf config) {
        this.config = config;
        EXP_MAP.setDefaultTime(Interval.ofMinutes(10).toMillis());
    }

    /**
     * Encrypt a message
     * <p>Also calculates and sets MAC
     * @param msg Message to encrypt
     * @param tkeys Transport keys
     * @return Encrypted message with MAC set
     * @throws IOException if encryption or MAC calculation fails
     */
    public CouplingApiMessage encrypt(final CouplingApiMessage msg, final TransportKeys tkeys)
        throws IOException
    {
        if (msg == null) return msg;
        if (msg.isEncrypted()) {
            log.trace("Request is already encrypted (iv is present)");
            return msg;
        }
        if (NO_ENCRYPT_TYPES.contains(msg.type)) {
            log.trace("Skipping encrypt of " + msg.type + " message");
            return msg;
        }
        
        if (tkeys == null) {
            log.info("No encryption keys available");
            throw new IOException("Transport security error");
        }
        
        try {
            msg.encrypt(tkeys.enc);
            msg.setMac(this.calculateMac(tkeys, msg));
            return msg;
        } catch (Exception e) {
            log.debug("Message encryption failed for MSISDN " + tkeys, e);
            throw new IOException("Transport security error", e);
        }
    }

    /**
     * Decrypt a message
     * <p>Also calculates and verifies MAC
     * @param msg Message to encrypt
     * @param tkeys UUID
     * @return Decrypted message
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
    public CouplingApiMessage decrypt(final CouplingApiMessage msg, final TransportKeys tkeys)
        throws GeneralSecurityException, IOException
    {
        if (msg == null) return msg;
        if (NO_DECRYPT_TYPES.contains(msg.type)) {
            log.trace("Skipping decrypt of " + msg.type + " message");
            msg.iv  = null;
            msg.mac = null;
            return msg;
        }

        if (tkeys == null) {
            log.info("No decryption keys available");
            throw new IOException("Transport security error");
        }
        
        // Verify mac
        final String respMac = this.calculateMac(tkeys, msg);
        log.trace("Message    MAC: " + msg.mac);
        log.trace("Calculated MAC: " + respMac);
        if (!(Objects.equals(respMac, msg.mac) && respMac != null)) {
            // respMac and msg.mac being null is not acceptable..
            log.debug("Message MAC comparison failed");
            throw new IOException("Transport security error");
        }
        
        msg.decrypt(tkeys.enc);
        msg.iv  = null;
        msg.mac = null;
        return msg;
    }

    /**
     * Clear transport encryption key cache for the given MSISDN
     * @param uuid
     */
    public void clearCache(final String uuid) {
        CACHE.remove(uuid);
    }

    /**
     * Resolve encryption key for given MSISDN
     * @param uuid UUID
     * @return Encryption keys (contains null keys if not found)
     */
    public TransportKeys resolveKeys(final String uuid) {
        if (uuid == null) {
            return null;
        }
        log.trace("Resolving transport keys for UUID " + uuid);

        final TransportKeys cached = CACHE.get(uuid);
        if (cached != null) {
            log.trace("Resolved transport keys from cache");
            return cached;
        }
        try {
            //final AppKeys          keys = this.config.getAppKeys();
            //try (Connection conn = keys.getConnectionRO("AppTransportEncryption.FETCH")) {
            //    final List<AppKeys.Row> keyRows = keys.fetchKeysByUUID(conn, uuid);
            //    if (keyRows.size() > 0) {
            //        AppKeys.Row enc = null;
            //        AppKeys.Row mac = null;
            //        for (AppKeys.Row k : keyRows) {
            //            if (k.keytype == AppKeys.KEYTYPE_AES) enc = k;
            //            if (k.keytype == AppKeys.KEYTYPE_MAC) mac = k;
            //        }
            //        if (enc != null && mac != null) {
            //            final TransportKeys txnKeys = new TransportKeys(uuid, enc.key, mac.key);
            //            if (this.config.isTransportEncryptionCacheEnabled()) {
            //                CACHE.put(uuid, txnKeys);
            //            }
            //            return txnKeys;
            //        }
            //    }
            //    log.debug("No transport keys found");
            //    return null;
            //}
            return null;

        } catch (Exception e) {
            log.debug("Failed to fetch transport keys", e);
            return null;
        }
    }
    
    /**
     * Calculate MAC for a message
     * @param keys  Key set of user
     * @param msg   Message
     * @return MAC
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
    public String calculateMac(final TransportKeys keys, final CouplingApiMessage msg)
        throws GeneralSecurityException, IOException
    {
        return msg.calculateMac(keys.mac);
    }
    
    public static class TransportKeys {
        public String uuid;
        public byte[] enc;
        public byte[] mac;
        public TransportKeys(final String uuid, final byte[] enc, final byte[] mac) {
            this.uuid = uuid;
            this.enc = enc;
            this.mac = mac;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = this.uuid != null ? this.uuid.hashCode() : 0;
            if (this.enc != null) result = prime * result + Arrays.hashCode(this.enc);
            if (this.mac != null) result = prime * result + Arrays.hashCode(this.mac);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TransportKeys other = (TransportKeys) obj;
            if (this.uuid != null) {
                // MSISDN mismatch
                if (!this.uuid.equals(other.uuid)) {
                    return false;
                }
            }
            if (!Arrays.equals(this.enc, other.enc)) {
                // ENC key mismatch
                return false;
            }
            if (!Arrays.equals(this.mac, other.mac)) {
                // MAC key mismatch
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TKey{uuid=");
            sb.append(this.uuid);
            sb.append(", enc");
            sb.append(", mac");
            sb.append("}");
            return sb.toString();
        }
    }
}
