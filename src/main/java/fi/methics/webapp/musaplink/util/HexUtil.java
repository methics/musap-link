package fi.methics.webapp.musaplink.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Produce and parse hexadecimal presentation of bytes.
 */
public class HexUtil {

    /** Hex digits in lower case */
    private static final char[] hexDigits = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };
    /** Hex digits in upper case */
    private static final char[] hexDigitsUp = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    /**
     * Parse hex String to a byte
     * @param hexByte String containing at least one byte in hex
     * @return 1st byte of the String
     */
    public static byte hexByte(final String hexByte) {
        return parseHex(hexByte)[0];
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * NOTE: DON't change this method, because it's not just for logging. 
     *
     * @param buf the buffer to dump.
     * @param spaces include spaces between numbers or not.
     * @param toupper use upper case letters for hex digits A-F.
     * @return the hexadecimal string.
     */
    public static String hexLine(final byte [] buf, 
                                 final boolean spaces, 
                                 final boolean toupper) 
    {
        if (buf != null) {
            int l = buf.length*2; // length estimator
            if (spaces)
                l += buf.length;
            final StringBuilder sb = new StringBuilder(l);
            hexLine(sb, buf, spaces, toupper);
            return sb.toString();
        }
        return "";
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * NOTE: DON't change this method, because it's not just for logging. 
     *
     * @param buf the buffer to dump.
     * @param offset start offset within buffer.
     * @param len length of subfield.
     * @return the hexadecimal string.
     */
    public static String hexLine(final byte [] buf, 
                                 final int offset, 
                                 final int len) 
    {
        if (buf != null) {
            final StringBuilder sb = new StringBuilder(len+len);
            final int end = offset+len;
            for (int i = offset; i < buf.length && i < end; ++i) {
                final byte b = buf[i];
                sb.append(hexDigits[(b >> 4) & 0x0F]);
                sb.append(hexDigits[b & 0x0F]);
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * NOTE: DON't change this method, because it's not just for logging. 
     *
     * @param buf the buffer to dump.
     * @param offset start offset within buffer.
     * @param len length of subfield.
     * @return the hexadecimal string.
     */
    public static String hexLine(final byte [] buf, 
                                 final int offset, 
                                 final int len,
                                 final String tail) 
    {
        if (buf != null) {
            final StringBuilder sb = new StringBuilder(len+len);
            final int end = offset+len;
            for (int i = offset; i < buf.length && i < end; ++i) {
                final byte b = buf[i];
                sb.append(hexDigits[(b >> 4) & 0x0F]);
                sb.append(hexDigits[b & 0x0F]);
            }
            sb.append(tail);
            return sb.toString();
        }
        return "";
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * NOTE: DON't change this method, because it's not just for logging. 
     *
     * @param sb Output the text to this StringBuilder.
     * @param buf the buffer to dump.
     * @param spaces include spaces between numbers or not.
     * @param toupper use upper case letters for hex digits A-F.
     */
    public static void hexLine(final StringBuilder sb,
                               final byte[] buf,
                               final boolean spaces,
                               final boolean toupper)
    {
        for (int i = 0; buf != null && i < buf.length; ++i) {
            final byte b = buf[i];
            if (spaces && i > 0)
                sb.append(' ');
            if (toupper) {
                sb.append(hexDigitsUp[(b >> 4) & 0x0F]);
                sb.append(hexDigitsUp[b & 0x0F]);
            } else {
                sb.append(hexDigits[(b >> 4) & 0x0F]);
                sb.append(hexDigits[b & 0x0F]);
            }
        }
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * NOTE: DON't change this method, because it's not just for logging. 
     *
     * @param out Output the text to this StringBuilder.
     * @param buf the buffer to dump.
     * @param spaces include spaces between numbers or not.
     * @param toupper use upper case letters for hex digits A-F.
     * @throws IOException Error from the output writer
     */
    public static void hexLine(final Writer out,
                               final byte[] buf,
                               final boolean spaces,
                               final boolean toupper)
        throws IOException
    {
        for (int i = 0; buf != null && i < buf.length; ++i) {
            final byte b = buf[i];
            if (spaces && i > 0)
                out.write(' ');
            if (toupper) {
                out.write(hexDigitsUp[(b >> 4) & 0x0F]);
                out.write(hexDigitsUp[b & 0x0F]);
            } else {
                out.write(hexDigits[(b >> 4) & 0x0F]);
                out.write(hexDigits[b & 0x0F]);
            }
        }
    }
    

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * This outputs hex bytes with controlled enable of spaces in between the bytes.
     * The hex digits A-F are in lower case.
     * @see #hexLine(byte[],boolean,boolean)
     * 
     * @param buf the buffer to dump.
     * @param spaces include spaces between numbers or not.
     * @return the hexadecimal string.
     */
    public static String hexLine(final byte [] buf, 
                                 final boolean spaces) 
    {
        return hexLine(buf, spaces, false);
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * The hex digits A-F are in lower case, and there are no spaces in between bytes.
     * @see #hexLine(byte[],boolean,boolean)
     * 
     * @param buf the buffer to dump.
     * @return the hexadecimal string.
     */
    public static String hexLine(final byte... buf) {
        return hexLine(buf, false, false);
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * The hex digits A-F are in lower case, and there are no spaces in between bytes.
     * @see #hexLine(byte[])
     * 
     * @param buf the buffer to dump.
     * @return the hexadecimal string.
     */
    public static String hexLine(final Short buf) {
        if (buf == null) return null;
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(buf);
        bb.rewind();
        byte byte1 = bb.get();
        byte byte2 = bb.get();
        return hexLine(new byte[] {byte1, byte2});
    }

    /**
     * Constructs a hexadecimal string representation of the buffer.
     * The hex digits A-F are in lower case, and there are no spaces in between bytes.
     * @see #hexLine(byte[],boolean,boolean)
     * 
     * @param buf the buffer to dump.
     * @return the hexadecimal string.
     */
    public static String hexLine(final BigInteger buf) {
        if (buf == null) return "";
        final byte[] tmp = buf.toByteArray();
        if (tmp.length > 0) {
            if (tmp[0] == (byte)0) {
                return hexLine(tmp, 1, tmp.length-1);
            }
        }
        return hexLine(buf.toByteArray(), false, false);
    }
    

    /**
     * Constructs a hexadecimal string representation of the ByteBuffer from current position to limit.
     * The hex digits A-F are in lower case, and there are no spaces in between bytes.

     * @param bb the buffer to dump.
     * @return the hexadecimal string.
     */
    public static String hexLine(final ByteBuffer bb) {
        return hexLine(bb.array(), bb.position(), bb.remaining());
    }

    /**
     * Constructs a hexadecimal string representation of the ByteBuffer from current position to limit.
     * The hex digits A-F are in lower case, and there are no spaces in between bytes.

     * @param bb the buffer to dump.
     * @param lengthLimit max length that will be rendered
     * @return the hexadecimal string.
     */
    public static String hexLine(final ByteBuffer bb, final int lengthLimit) {
        if (bb.remaining() > lengthLimit) {
            return hexLine(bb.array(), bb.position(), lengthLimit, "...");
        }
        return hexLine(bb.array(), bb.position(), bb.remaining());
    }

    
    /**
     * Convert incoming ETSI format 10 byte packed ICCID to string.
     * <p>
     * Digit values of 'F' are stripped.
     * <p>
     *  
     * @param iccid Binary ICCID from SIM card in card format.
     * @return Text presentation of the ICCID value. 
     * 
     */
    public static String iccidToGSMString(byte[] iccid) {
        if (iccid == null) {
            throw new IllegalArgumentException();
        }
        // Low nybble first, then high nybble
        // (big-endian nybble order -- cf. ETSI protocols use big-endian style,
        // even when packing 4 bit fields on bytes.)
        final StringBuilder sb = new StringBuilder(20);
        for (int j = 0; j < iccid.length; j++) {
            if ((iccid[j] & 0x0f) != 0x0f)
                sb.append(hexDigits[iccid[j] & 0x0f]);
            if (((iccid[j] >> 4) & 0x0f) != 0x0f)
                sb.append(hexDigits[(iccid[j] >> 4) & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Convert incoming ETSI format 8 byte packed IMSI to string.
     * <p>
     * Digit values of 'F' are stripped.
     * <p>
     * First semi-octet is parity indication, and not part of the textual IMSI value!
     *  
     * @param imsi Binary IMSI from SIM card in card format.
     * @return Text presentation of the IMSI value. 
     * 
     */
    public static String imsiToGSMString(byte[] imsi) {
        if (imsi == null) {
            throw new IllegalArgumentException();
        }
        // Low nybble first, then high nybble
        // (big-endian nybble order -- cf. ETSI protocols use big-endian style,
        // even when packing 4 bit fields on bytes.)
        final StringBuilder sb = new StringBuilder(20);
        for (int j = 0; j < imsi.length; j++) {
            if (j > 0) {
                if ((imsi[j] & 0x0f) != 0x0f)
                    sb.append(hexDigits[imsi[j] & 0x0f]);
            }
            if (((imsi[j] >> 4) & 0x0f) != 0x0f)
                sb.append(hexDigits[(imsi[j] >> 4) & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Parse HEX encoded string to bytes.
     * <p>
     * Input must consist of even number of hex digits without any whitespace/punctuation fillers.
     * Digits A-F can be upper or lower case.
     * <p>
     * @param hex input text 
     * @return parse result
     * @throws IllegalArgumentException for bad length, or bad input digits. 
     */
    public static byte[] parseHex(final CharSequence hex)
        throws IllegalArgumentException
    {
        if (hex == null) return null; // bad parse..

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int l = hex.length();
        if ((l % 2) != 0) {
            throw new IllegalArgumentException("Input data length is no multiple of two chars");
        }
        for (int i = 0;i < l; i += 2) {
            char c1 = hex.charAt(i);
            if ('0' <= c1 && c1 <= '9') {
                c1 -= '0';
            } else if ('A' <= c1 && c1 <= 'F') {
                c1 -= ('A' - 10);
            } else if ('a' <= c1 && c1 <= 'f') {
                c1 -= ('a' - 10);
            } else {
                // bad input..
                throw new IllegalArgumentException("Invalid input char ("+c1+") on hex digit at offset "+i);
            }
            char c2 = hex.charAt(i+1);
            if ('0' <= c2 && c2 <= '9') {
                c2 -= '0';
            } else if ('A' <= c2 && c2 <= 'F') {
                c2 -= ('A' - 10);
            } else if ('a' <= c2 && c2 <= 'f') {
                c2 -= ('a' - 10);
            } else {
                // bad input..
                throw new IllegalArgumentException("Invalid input char ("+c2+") on hex digit at offset "+(i+1));
            }
            baos.write(((c1 & 0x0F) << 4) | (c2 & 0x0F));
        }
        return baos.toByteArray();
    }
    
    

    /**
     * Convert a byte array to a pretty hex String
     * <p>
     * NOTE: This is used purely for logging.
     * 
     * @param bytes byte array to convert
     * @return Hex String representation of the byte array 
     */
    public static String bytesToHex(byte... bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    /**
     * Convert a byte array to a pretty hex String
     * <p>
     * NOTE: This is used purely for logging.
     * 
     * @param bytes byte array to convert
     * @param startOffs start offset
     * @param len length
     * @return Hex String representation of the byte array
     */
    public static String bytesToHex(byte[] bytes, int startOffs, int len) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        if (bytes != null) { 
            // Prevent IndexOutOfBoundsException
            if (startOffs + len >= bytes.length) {
                len = bytes.length - startOffs;
            }
            
            for (int i = 0; i < len; ++i) {
                final byte b = bytes[startOffs+i];
                sb.append(String.format(" 0x%02x", Byte.valueOf(b)));                
                if (i < len-1) sb.append(",");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * @param w a Writer receiving the byte text
     * @param b the byte value
     */
    public static void writeByte(final Writer w, int b)
        throws IOException
    {
        w.write(hexDigitsUp[ (b >> 4) & 0x0F]);
        w.write(hexDigitsUp[ (b) & 0x0F]);
    }
    
    public static void putByte(final ByteBuffer bb, int b)
        throws BufferOverflowException
    {
        bb.put((byte) hexDigitsUp[ (b >> 4) & 0x0F]);
        bb.put((byte) hexDigitsUp[ (b) & 0x0F]);
    }
    
    public static void putByte(final StringBuilder sb, int b)
        throws BufferOverflowException
    {
        sb.append(hexDigitsUp[ (b >> 4) & 0x0F]);
        sb.append(hexDigitsUp[ (b) & 0x0F]);
    }
}
