package fi.methics.webapp.musaplink.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiPayload;
import fi.methics.webapp.musaplink.util.db.AccountStorage;

public class MusapTransportEncryption {

    private static final Log log = LogFactory.getLog(MusapTransportEncryption.class);

    // List of message types that should not be encrypted or decrypted
    private static final List<String> NO_ENCRYPT_TYPES = Arrays.asList(new String[]{ });
    private static final List<String> NO_DECRYPT_TYPES = Arrays.asList(new String[]{ "enrolldata" });

    private static final ExpirableMap<String, TransportKeys> EXP_MAP = new ExpirableMap<>(Interval.ofMinutes(10).toMillis());
    private static final Map<String, TransportKeys>          CACHE   = Collections.synchronizedMap(EXP_MAP);

    private static final ExpirableSet<String> NONCE_SET = new ExpirableSet<>(Interval.ofMinutes(60).toMillis());

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
            log.debug("Message encryption failed for " + tkeys, e);
            throw new IOException("Transport security error", e);
        }
    }

    /**
     * Decrypt a message
     * <p>Also calculates and verifies MAC
     * @param msg Message to encrypt
     * @param tkeys Transport Keys
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
     * Clear transport encryption key cache for the given MUSAP ID
     * @param musapid MUSAP ID
     */
    public void clearCache(final String musapid) {
        CACHE.remove(musapid);
    }

    /**
     * Resolve encryption key for given MUSAP ID
     * @param musapid MUSAP ID
     * @return Encryption keys (contains null keys if not found)
     */
    public TransportKeys resolveKeys(final String musapid) {
        if (musapid == null) {
            return null;
        }
        log.trace("Resolving transport keys for MUSAP ID " + musapid);

        final TransportKeys cached = CACHE.get(musapid);
        if (cached != null) {
            log.trace("Resolved transport keys from cache");
            return cached;
        }
        try {
            MusapLinkAccount account = AccountStorage.findAccountByMusapId(musapid);
            return account.getTransportKeys();
        } catch (Exception e) {
            log.debug("Failed to fetch transport keys", e);
            return null;
        }
    }
    
    /**
     * Check if the nonce in the given message is acceptable.
     * This compares the nonce to a used nonce list, and verifies that the message
     * timestamp is not too old.
     * @param msg Message to check
     * @return true if nonce is acceptable
     */
    public boolean isNonceValid(CouplingApiMessage msg) {
        
        if (msg == null) return true;
        if (msg.getBasePayload() == null) return true;
        
        CouplingApiPayload payload = msg.getBasePayload();
        String nonce = payload.nonce;
        
        if (nonce == null) return true;
        if (NONCE_SET.containsKey(nonce)) {
            log.warn("Potential replay attack: NONCE already used");
            return false;
        }
        NONCE_SET.add(nonce);
        
        if (payload.getTimestamp() == null) {
            log.warn("Potential replay attack: No timestamp");
            return false;
        }
        
        // Check if the timestamp is within an hour
        if (Instant.now().minus(Duration.ofHours(1)).isBefore(payload.getTimestamp())) {
            return true;
        } else {
            log.warn("Potential replay attack: Timestamp too old (" + payload.getTimestamp() + ")");
            return false;
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
    
    /**
     * Class that contains MUSAP transport encryption keys
     */
    public static class TransportKeys {
        public String musapid;
        public byte[] enc;
        public byte[] mac;
        public TransportKeys(final String musapid, final byte[] enc, final byte[] mac) {
            this.musapid = musapid;
            this.enc = enc;
            this.mac = mac;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = this.musapid != null ? this.musapid.hashCode() : 0;
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
            if (this.musapid != null) {
                if (!this.musapid.equals(other.musapid)) {
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
            final StringBuilder sb = new StringBuilder("TKey{musapid=");
            sb.append(this.musapid);
            sb.append(", enc");
            sb.append(", mac");
            sb.append("}");
            return sb.toString();
        }
    }
}
