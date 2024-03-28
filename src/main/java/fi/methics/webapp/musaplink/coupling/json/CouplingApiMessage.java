package fi.methics.webapp.musaplink.coupling.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import fi.methics.webapp.musaplink.util.GsonMessage;
import fi.methics.webapp.musaplink.util.HexUtil;
import fi.methics.webapp.musaplink.util.MusapRandom;
import fi.methics.webapp.musaplink.util.MusapTransportEncryption;

/**
 * MUSAP Coupling API message.
 * The message consists of the following parts:
 * <ul>
 * <li>type    - Message type (e.g. "enrolldata")
 * <li>payload - Message payload (base64 encoded)
 * <li>transid - Transaction ID unique to this transaction
 * </ul>
 */
public class CouplingApiMessage extends GsonMessage {

    private static final Log log = LogFactory.getLog(CouplingApiMessage.class);

    public static final String TYPE_ENROLLDATA         = "enrolldata";
    public static final String TYPE_UPDATEDATA         = "updatedata";
    public static final String TYPE_LINKACCOUNT        = "linkaccount";
    public static final String TYPE_GETDATA            = "getdata";
    public static final String TYPE_ERROR              = "error";
    public static final String TYPE_EXTERNAL_SIGREQ    = "externalsignature";
    public static final String TYPE_SIGNATURE_REQ      = "signature";
    public static final String TYPE_SIGNATURE_CALLBACK = "signaturecallback";
    public static final String TYPE_GENKEY_CALLBACK    = "generatekeycallback";

    public static final List<String> MESSAGE_TYPES;
    
    static {
        MESSAGE_TYPES = getKnownTypes();
    }
    
    private static final String MAC_ALGORITHM = "HmacSHA256";
    
    // Req
    @SerializedName("payload")
    public String payload;
    @SerializedName("type")
    public String type;
    @SerializedName("musapid")
    public String musapid;
    @SerializedName("transid")
    public String transid;
    @SerializedName("mac")
    public String mac;
    @SerializedName("iv")
    public String iv;

    private transient boolean isError;
    private transient boolean isMt;
    private transient boolean isEncrypted;
    
    
    /**
     * Private constructor
     */
    private CouplingApiMessage() {
        // Do nothing
    }
    /**
     * Create a generic error response from an Exception
     * @param e       Exception
     * @param transid APP txnid (optional)
     * @return error message
     */
    public static CouplingApiMessage createError(final Exception e, final String transid) {
        MusapErrorMsg err = new MusapErrorMsg(e);
        return createRequest(TYPE_ERROR, transid, err);
    }
    
    /**
     * Create a generic error response from an errorcode
     * @param errorcode APP errorcode
     * @param transid   APP txnid (optional)
     * @return error message
     */
    public static CouplingApiMessage createError(final int errorcode) {
        MusapErrorMsg err = new MusapErrorMsg(errorcode);
        return createRequest(TYPE_ERROR, null, err);
    }

    
    /**
     * Create a new request without payload
     * @param type    Message type
     * @param transId Transaction ID
     */
    public static CouplingApiMessage createRequest(final String type,
                                                 final String transId) {
        final CouplingApiMessage req = new CouplingApiMessage();
        req.type    = type;
        req.transid = transId;
        req.isMt    = true; // all requests created on MSSP are assumed to be MT
        return req;
    }
    
    /**
     * Create a new request
     * @param type    Message type
     * @param transId Transaction ID
     * @param payload JSON to be used as payload
     */
    public static CouplingApiMessage createRequest(final String type,
                                                final String transId,
                                                final CouplingApiPayload payload) {
        final CouplingApiMessage req = new CouplingApiMessage();
        req.type    = type;
        req.transid = transId;
        req.setPayload(payload);
        req.isMt    = true; // all requests created on MSSP are assumed to be MT
        return req;
    }
    
    /**
     * Parse MusapAppMessage from byte[]. Uses {@link String#String(byte[])}.
     * @param bytes byte[] to parse
     * @return new MusapAppMessage or null if given byte[] is null
     * @throws JsonSyntaxException if JSON syntax is not valid
     */
    public static CouplingApiMessage fromBytes(final byte[] bytes) throws JsonSyntaxException {
        if (bytes == null)  return null;
        CouplingApiMessage msg = fromBytes(bytes, CouplingApiMessage.class);
        msg.isEncrypted = (msg.iv != null);  // Assume MO messages are encrypted
        msg.isMt        = false; // All requests parsed from String are assumed to be MO
        return msg;
    }

    /**
     * Parse MusapAppMessage from String.
     * @param str String to parse
     * @return new MusapAppMessage
     * @throws JsonSyntaxException if JSON syntax is not valid
     */
    public static CouplingApiMessage fromJson(final String str) throws JsonSyntaxException {
        CouplingApiMessage msg = fromJson(str, CouplingApiMessage.class);
        if (msg == null) {
            return null;
        }
        msg.isEncrypted = (msg.iv != null);  // Assume MO messages are encrypted
        msg.isMt        = false; // All requests parsed from String are assumed to be MO
        return msg;
    }
    
    /**
     * Get a list of known message types
     * <p>This is called once. Use the constant {@link MESSAGE_TYPES} to get the full list.
     * @return message types
     */
    private static List<String> getKnownTypes() {
        List<String> types = new ArrayList<>();
        for (Field field : CouplingApiMessage.class.getFields()) {
            if (field.getName().startsWith("TYPE")) {
                try {
                    types.add((String)field.get(null));
                } catch (Exception e) {
                    log.trace("Failed to read field " + field.getName(), e);
                }
            }
        }
        return types;
    }

    /**
     * Calculates the mac value and stores it to {@link #mac}
     * @param macKey
     * @return Mac as hex bytes
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws Base64.InvalidInputDataException if {@link #iv} is not valid base64
     */
    public String calculateMac(final byte[] macKey) throws GeneralSecurityException, IOException {
        if (macKey == null) return null;
        if (this.iv == null) {
            if (this.isMT()) {
                this.generateIV();
            } else {
                throw new IOException("MO message is missing IV");
            }
        }
        if (this.transid == null && this.musapid == null) {
            throw new IOException("Message is missing transid and uuid");
        }
        if (this.transid != null && this.musapid != null) {
            log.trace("UUID and TransID not allowed in same message. UUID=" + this.musapid + ", TransID=" + this.transid);
            throw new IOException("Message has both transid and uuid");
        }
        String msgid   = this.transid != null ? this.transid : this.musapid;
        byte[] message = (msgid + this.type + this.iv + this.payload).getBytes(StandardCharsets.UTF_8);
        if (log.isTraceEnabled()) {
            log.trace("Calculating mac from " + msgid+this.type+this.iv+this.payload);
        }
        SecretKey key = new SecretKeySpec(macKey, 0, macKey.length, MAC_ALGORITHM);
        Mac m = Mac.getInstance(MAC_ALGORITHM);
        m.init(key);
        return HexUtil.hexLine(m.doFinal(message));
    }
    
    /**
     * Create a new error response
     * @param e Exception to be converted to an error code
     */
    public CouplingApiMessage createErrorResponse(final Exception e) {
        return this.createErrorResponse(this.createErrorPayload(e));
    }

    /**
     * Create a new error response
     * @param errorcode Error code as int (one of {@link MusapErrorMsg}.ERROR_XX)
     */
    public CouplingApiMessage createErrorResponse(final int errorcode) {
        return this.createErrorResponse(this.createErrorPayload(errorcode));
    }

    /**
     * Create a new error response
     * 
     * @param msg JSON to be used as payload
     */
    public CouplingApiMessage createErrorResponse(MusapErrorMsg msg) {
        final CouplingApiMessage resp = newCouplingResponse(msg);
        resp.type    = TYPE_ERROR; // Change type!
        resp.isError = true;
        return resp;
        
    }
    
    /**
     * Create a new response (generic case)
     * 
     * @param resp JSON to be used as payload
     */
    public CouplingApiMessage createResponse(final CouplingApiPayload resp) {
        return newCouplingResponse(resp);
    }
    
    /**
     * Create a new success response
     * <p>Adds "status": "success" to the payload.
     */
    public CouplingApiMessage createSuccessResponse() {
        final CouplingApiPayload resp = new CouplingApiPayload();
        resp.status = "success";
        return newCouplingResponse(resp);
    }
    
    /**
     * Get base {@link CouplingApiPayload payload} containing e.g.
     * <ul>
     * <li>os        - Operating system (ios/android)
     * <li>osversion - Operating system version
     * <li>version   - App version
     * </ul>
     * @return base payload or null if no payload in message
     */
    public CouplingApiPayload getBasePayload() {
        return getPayload(CouplingApiPayload.class);
    }
    
    /**
     * Get the payload as error
     * @return payload as error
     * @throws JsonSyntaxException if payload cannot be parsed as error
     */
    public MusapErrorMsg getErrorPayload() {
        return getPayload(MusapErrorMsg.class);
    }
    
    /**
     * Get payload as raw byte[]
     * @return Decoded payload
     */
    public byte[] getPayload() {
        if (this.payload == null) return null;
        try {
            return Base64.getDecoder().decode(this.payload);
        } catch (Exception e) {
            log.error("Failed to decode payload:", e);
            return null;
        }
    }
    
    /**
     * Get typed payload
     * 
     * @param <T> Type of the payload
     * @param clazz class instance
     * @return instantiated payloa
     */
    public <T extends CouplingApiPayload> T getPayload(Class<T> clazz) {
        if (this.payload == null) return null;
        try {
            T payload = GSON.fromJson(this.getPayloadJson(), clazz);
            if (payload != null) {
                payload.validate();
            }
            return payload;
        } catch (Exception e) {
            log.error("Could not parse payload", e);
            return null;
        }
    }

    /**
     * Get payload as a JSON encoded String
     * @return payload as JSON (String) or null
     */
    public String getPayloadJson() {
        byte[] p = this.getPayload();
        if (p == null) return null;
        if (this.isEncrypted) return null;
        return new String(p, StandardCharsets.UTF_8);
    }

    /**
     * Get payload as key/value map
     * @return payload as key/value (String/String) map
     */
    public Map<String, String> getPayloadMap() {
        String json = this.getPayloadJson();
        if (json == null) {
            return Collections.emptyMap();
        }
        Type t = new TypeToken<Map<String, String>>(){}.getType();
        return GSON.fromJson(json, t);
    }
    
    /**
     * Get message type
     * @return type
     */
    public String getType() {
        if (this.type == null) return this.type;
        return this.type.toLowerCase();
    }
    
    /**
     * Is this message encrypted?
     * @return true if encrypted
     */
    public boolean isEncrypted() {
        return this.isEncrypted;
    }
    
    /**
     * Is this an error message?
     * @return true if message type is "error"
     */
    public boolean isError() {
        return this.isError || this.type.equals(TYPE_ERROR);
    }
    
    /**
     * Is this an error message with the given ErrorCode
     * @param errorCode ErrorCode
     * @return true if message type is "error" and ErrorCode matches
     */
    public boolean isError(final int errorCode) {
        try {
            return this.isError() && String.valueOf(errorCode).equals(this.getErrorPayload().errorcode);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if this message is of given type
     * @param type Operation type
     * @return true if operation types match
     */
    public boolean isType(final String type) {
        return this.type != null && this.type.equalsIgnoreCase(type);
    }
    
    /**
     * Is this a GETDATA request?
     * @return true for GETDATA
     */
    public boolean isGetData() {
        return TYPE_GETDATA.equals(this.type);
    }
    
    /**
     * Check if the message is an internal operation only sent between HMSSP and SAM.
     * These should never be encrypted.
     * @return true if this is an internal operation
     */
    public boolean isInternal() {
        if (this.getType() == null) return false;
        if (this.getType().endsWith("_b")) return true;
        if (this.getType().endsWith("b"))  return true;
        if (this.getType().equals("enrollsrp6a")) return true;
        return false;
    }
    
    /**
     * Is this an Mobile Originated message (App -> MSSP)
     * @return true if message is MO
     */
    public boolean isMO() {
        return !this.isMt;
    }
    
    /**
     * Is this an Mobile Terminated message (MSSP -> App)
     * @return true if message is MT
     */
    public boolean isMT() {
        return this.isMt;
    }
    
    /**
     * Quick check if the response is for my request.
     * 
     * @param resp Received response
     * @return true when the result is not for my request (maybe an error?)
     */
    public boolean notMyAnswer(final CouplingApiMessage resp) {
        if (resp == null) return true;
        if (resp.type == null) return true;
        if (resp.type.equals(this.type)) return false;
        return true;
    }
    
    /**
     * Set calculated mac
     * @param mac
     */
    public void setMac(final String mac) {
        if (mac != null) {
            this.mac = mac;
            log.trace("Set MAC to " + this.mac);
        }
    }
    
    /**
     * Force the message MT/MO information.
     * <p>This information is normally automatically set by the message construction methods.
     * @param isMt Is the message MT? true for MT message, false for MO message.
     */
    public void setMT(final boolean isMt) {
        this.isMt = isMt;
    }
    
    /**
     * Set the payload 
     * @param payload Payload to set
     */
    public void setPayload(final CouplingApiPayload payload) {
        if (payload == null) return;
        this.payload = Base64.getEncoder().encodeToString(payload.getBytes());
    }
    
    /**
     * Set the Transaction ID for this message
     * <p>Also resets UUID if one exists.
     * @param transid Transaction ID
     */
    public void setTransId(final String transid) {
        this.transid = transid;
        this.musapid = null;
    }
    
    /**
     * Create a new error response
     * 
     * @param errorcode Error code as int (one of {@link MusapErrorMsg}.ERROR_XX)
     */
    private MusapErrorMsg createErrorPayload(final Exception e) {
        return new MusapErrorMsg(e);
    }
    
    /**
     * Create a new error response
     * 
     * @param errorcode Error code as int (one of {@link MusapErrorMsg}.ERROR_XX)
     */
    private MusapErrorMsg createErrorPayload(final int errorcode) {
        return new MusapErrorMsg(errorcode);
    }

    /**
     * Generate a random IV
     * <p>Also sets the value to {@link #iv} as base64 string
     * @return IV
     * @throws NoSuchAlgorithmException 
     * @throws UnsupportedEncodingException 
     * @throws Base64.InvalidInputDataException 
     */
    private byte[] generateIV() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (this.iv != null) {
            return Base64.getDecoder().decode(this.iv);
        }
        byte[] _iv = MusapRandom.getRandomBytes(16);
        this.iv = Base64.getEncoder().encodeToString(_iv);
        return _iv;
    }

    /**
     * Initialize Cipher
     * @param mode    Mode (encrypt or decrypt)
     * @param aesKey  AES key (byte[])
     * @return Cipher
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private Cipher initCipher(int mode, byte[] aesKey) throws GeneralSecurityException, IOException {
        try {
            byte[]     _iv = this.generateIV();
            SecretKey key = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(mode, key, new IvParameterSpec(_iv));
            return cipher;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private CouplingApiMessage newCouplingResponse(CouplingApiPayload msg) {
        final CouplingApiMessage resp = new CouplingApiMessage();
        resp.type    = this.type;
        resp.transid = this.transid;
        resp.musapid = this.musapid;
        resp.isMt    = true;
        
        resp.setPayload(msg);
        
        return resp;
    }
    
    /**
     * Decrypt this message with given AES key
     * @param aesKey AES key
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void decrypt(final byte[] aesKey) throws GeneralSecurityException, IOException {
        
        if (this.iv == null) {
            log.debug("Cannot decrypt a message without iv");
            throw new IOException("Unable to decrypt message. Message is not encrypted.");
        }
        
        if (aesKey == null) {
            log.debug("No key to decrypt with.");
            throw new IOException("Decryption failed. Missing decryption key.");
        }
        log.debug("Decrypting payload: " + this.payload);
        Cipher cipher    = this.initCipher(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(this.getPayload());
        this.payload = Base64.getEncoder().encodeToString(decrypted);
        log.debug("Decrypted payload: " + this.payload);
        this.isEncrypted = false;
    }
    
    /**
     * Encrypt this message with given AES key
     * @param aesKey AES key
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void encrypt(final byte[] aesKey) throws GeneralSecurityException, IOException {
        
        if (!MusapTransportEncryption.shouldEncrypt(this)) {
            return;
        }
        
        if (aesKey == null) {
            log.debug("No key to encrypt with.");
            throw new IOException("Encryption failed. Missing encryption key.");
        }
        if (log.isTraceEnabled()) {
            log.trace("Encrypting payload: " + this.payload);
        }
        Cipher cipher    = this.initCipher(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(this.getPayload());
        this.payload = Base64.getEncoder().encodeToString(encrypted);
        this.isEncrypted = true;
        
        if (log.isTraceEnabled()) {
            log.trace("Encrypted payload: " + this.payload);
        }
    }
    
}
