//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.util;

import java.util.UUID;

public class IdGenerator {

    public static String generateTxnId() {
        return UUID.randomUUID().toString();
    }
    
}
