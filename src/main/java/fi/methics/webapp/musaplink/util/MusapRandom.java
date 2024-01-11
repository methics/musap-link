//
//  (c) Copyright 2003-2022 Methics Technologies Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Class supplying easy access to a system wide SecureRandom instance
 * along with helper methods for creating common random Strings.
 */
public class MusapRandom {

    protected static SecureRandom random = new SecureRandom();
    protected static Random pseudoRandom = new Random(System.nanoTime());

    /**
     * Get a random valid AP_TransID
     * @return random valid AP_TransID ("_" + 12 characters)
     */
    public static String getApTransId() {
        return getApTransId(null);
    }
    
    /**
     * Get a random valid AP_TransID
     * @param prefix Prefix for the transid (must not start with a number)
     * @return random valid AP_TransID (prefix + "-" + 12 characters)
     */
    public static String getApTransId(final String prefix) {
        if (prefix == null) {
            return RandomString.nextString(new StringBuilder("_"), 12).toString();
        }
        return RandomString.nextString(new StringBuilder(prefix).append("-"), 12).toString();
    }
    
    /**
     * Get a pre-initialized {@link Random} instance.
     * To get a cryptographically secure random, use {@link getSecureRandom()}.
     * @return {@link Random}
     * @see #getSecureRandom()
     */
    public static Random getPseudoRandom() {
        return pseudoRandom;
    }
    
    /** 
     * Get a random byte[] generated with a SecureRandom. 
     * @param length length of byte[]
     */
    public static byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        getSecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /** 
     * Get a random digit String generated with a SecureRandom.
     * <p>Output string follows the pattern [0-9].
     * @param length length of String
     * @return e.g. "5213" for length 4
     */
    public static String getRandomDigits(int length) {
        final StringBuilder sb = new StringBuilder(length);
        return RandomString.nextDigits(sb, length).toString();
    }
    
    /** 
     * Get a random alphanumeric String generated with a SecureRandom.
     * <p>Output string follows the pattern [A-Za-z0-9].
     * @param length length of String
     * @return e.g. "aB12" for length 4
     */
    public static String getRandomString(int length) {
        final StringBuilder sb = new StringBuilder(length);
        return RandomString.nextString(sb, length).toString();
    }
    
    /**
     * Get a pre-initialized {@link SecureRandom} instance.
     * @return {@link SecureRandom}
     * @see #getPseudoRandom()
     */
    public static SecureRandom getSecureRandom() {
        return random;
    }
    
    /**
     * Get an UUID without the "-" characters
     * @return short UUID
     */
    public static String getShortUUID() {
        return getUUID().replace("-", "");
    }
    
    /**
     * Get an UUID.
     * @return UUID from {@link UUID#randomUUID()}
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    // Copied and modified from https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
    private static class RandomString {
        
        private static final String ALPHANUM   = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        private static final char[] ALPHACHARS = ALPHANUM.toCharArray();
        
        private static final String DIGITS     = "0123456789";
        private static final char[] DIGITCHARS = DIGITS.toCharArray();
        
        private final int length;
        
        public RandomString(int length) {
            if (length < 1) throw new IllegalArgumentException();
            this.length = length;
        }

        /**
         * Generate random digits [0-9].
         * @return e.g. "1234" for length 4
         */
        public static StringBuilder nextDigits(StringBuilder sb, int length) {
            final int symlen = DIGITCHARS.length;
            for (int idx = 0; idx < length; ++idx) {
                sb.append(DIGITCHARS[random.nextInt(symlen)]);
            }
            return sb;
        }

        /**
         * Generate a random string [A-Za-z0-9].
         */
        public static StringBuilder nextString(StringBuilder sb, int length) {
            final int symlen = ALPHACHARS.length;
            for (int idx = 0; idx < length; ++idx) {
                sb.append(ALPHACHARS[random.nextInt(symlen)]);
            }
            return sb;
        }
        
        /**
         * Generate random digits [0-9].
         * @return e.g. "1234" for length 4
         */
        public String nextDigits() {
            final StringBuilder sb = new StringBuilder(this.length);
            final int symlen = DIGITCHARS.length;
            for (int idx = 0; idx < this.length; ++idx) {
                sb.append(DIGITCHARS[random.nextInt(symlen)]);
            }
            return sb.toString();
        }
        
        /**
         * Generate a random string [A-Za-z0-9].
         * @return e.g. "aB12" for length 4
         */
        public String nextString() {
            final StringBuilder sb = new StringBuilder(this.length);
            final int symlen = ALPHACHARS.length;
            for (int idx = 0; idx < this.length; ++idx) {
                sb.append(ALPHACHARS[random.nextInt(symlen)]);
            }
            return sb.toString();
        }
    }
}
