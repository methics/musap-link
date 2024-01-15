package fi.methics.webapp.musaplink.util;

import java.util.UUID;

public class IdGenerator {

    public static String generateTxnId() {
        return UUID.randomUUID().toString();
    }
    
}
