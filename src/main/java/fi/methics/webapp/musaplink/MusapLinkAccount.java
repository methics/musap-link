package fi.methics.webapp.musaplink;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.coupling.json.ExternalSignatureResp;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;

/**
 * Class representing a single account in MUSAP Link
 */
public class MusapLinkAccount {

    public String musapid;
    public Set<String> linkids = new HashSet<>();
    public String fcmToken;
    public String apnsToken;
    
    public byte[] aesKey;
    public byte[] macKey;
    
    public MusapLinkAccount() {
        
    }
    
    @Override
    public String toString() {
        return musapid;
    }
    
    /**
     * Class containing MUSAP key details like:
     * <ul>
     * <li>Key ID
     * <li>Key Name
     * <li>Key Usage
     * <li>Public Key
     * <li>Certificate
     * </ul>
     */
    public static class MusapKey {
        
        @SerializedName("publickey")
        public byte[] publickey;
        
        @SerializedName("certificate")
        public byte[] certificate;

        @SerializedName("keyid")
        public String keyid;
        
        @SerializedName("keyname")
        public String keyname;
        
        @SerializedName("keyusages")
        public List<String> keyusages;
        
        public MusapKey() {
            
        }
        
        /**
         * Create new MusapKey with KeyID and Key Name
         * @param keyid   KeyID
         * @param keyname Key Name
         */
        public MusapKey(String keyid, String keyname) {
            this.keyid   = keyid;
            this.keyname = keyname;
        }

        /**
         * Create a MusapKey from a Link API response
         * @param resp /sign API response
         */
        public MusapKey(MusapSignResp resp) {
            this.keyid       = resp.keyid;
            this.publickey   = resp.getPublicKey();
            this.certificate = resp.getCertificate();
        }

        /**
         * Create a MusapKey from a Coupling API response
         * @param resp External Signature Response
         */
        public MusapKey(ExternalSignatureResp resp) {
            this.keyid       = resp.keyid;
            this.keyusages   = resp.keyusages;
            this.publickey   = resp.getPublicKey();
            this.certificate = resp.getCertificate();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(certificate);
            result = prime * result + Arrays.hashCode(publickey);
            result = prime * result + Objects.hash(keyid, keyname);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MusapKey)) {
                return false;
            }
            MusapKey other = (MusapKey) obj;
            return Arrays.equals(certificate, other.certificate) && 
                   Arrays.equals(publickey, other.publickey) &&
                   Objects.equals(keyid, other.keyid) && 
                   Objects.equals(keyname, other.keyname);
        }
        
    }
    
}
