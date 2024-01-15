package fi.methics.webapp.musaplink;

import java.util.HashSet;
import java.util.Set;

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
    
}
