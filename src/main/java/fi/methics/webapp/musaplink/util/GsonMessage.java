package fi.methics.webapp.musaplink.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


/**
 * Generic GSON message object.
 * Obfuscation is not done to any classes extending this.
 */
public abstract class GsonMessage {

    /**
     * Master tool
     */
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
                                                     .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                                                     .registerTypeAdapter(byte[].class, new ByteaMarshaller())
                                                     .create();

    // This can be used to override the default GSON implementation
    private transient Gson gson = null;
    
    /**
     * Parse a JSON String to given type
     * @param <T> Type instance
     * @param base64 BASE64 JSON String
     * @param t      Output class
     * @return Resulting object
     * @throws JsonParseException if JSON cannot be parsed to given type
     * @see Gson#fromJson(String, Class)
     */
    public static <T> T fromBase64(final String base64, final Class<T> t) throws JsonParseException {
        if (base64 == null) return null;
        return GSON.fromJson(new String(Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8), t);
    }
    
    /**
     * Parse a JSON byte[] to given type
     * @param <T> Type instance
     * @param bytes JSON byte[]
     * @param clazz Output class
     * @return Resulting object
     * @throws JsonParseException if JSON cannot be parsed to given type
     * @see Gson#fromJson(String, Class)
     */
    public static <T> T fromBytes(final byte[] bytes, final Class<T> clazz) throws JsonParseException {
        if (bytes == null)  return null;
        return GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), clazz);
    }

    /**
     * Parse a JSON String to given type
     * @param <T>  Type instance
     * @param json JSON String
     * @param t    Output class type
     * @return Resulting object
     * @throws JsonParseException if JSON cannot be parsed to given type
     * @see Gson#fromJson(String, Class)
     */
    public static <T> T fromJson(final String json, final Class<T> t) throws JsonParseException {
        if (json == null) return null;
        return GSON.fromJson(json, t);
    }
    
    /**
     * Parse a JSON String to given type
     * @param <T>  Type instance
     * @param json JSON String
     * @param t    Output class type
     * @return     Resulting object
     * @throws JsonParseException if JSON cannot be parsed to given type
     */
    public static <T> T fromJson(final String json, final Type t)  throws JsonParseException {
        return GSON.fromJson(json, t);
    }

    /**
     * Parse a JSON InputStream to given type
     * @param <T> Type instance
     * @param is   JSON InputStream
     * @param clazz Output class
     * @return Resulting object
     * @throws JsonParseException if JSON cannot be parsed to given type
     * @throws IOException for input stream read failure
     * @see Gson#fromJson(String, Class)
     */
    public static <T> T fromStream(final InputStream is, final Class<T> clazz) throws JsonParseException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] data = new byte[4096];
        while ((read = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, read);
        }
        return GSON.fromJson(new String(buffer.toByteArray(), StandardCharsets.UTF_8), clazz);
    }
    
    /**
     * Helper for generating Base64 encoded byte[]
     * 
     * @param data input
     * @return base64 encoded string
     */
    protected static String base64(byte[] data) {
        if (data == null) return null;
        return Base64.getEncoder().encodeToString(data);
    }
    
    /**
     * Helper for decoding Base64 encoded data
     *  
     * @param data input string
     * @return decoded byte[]
     */
    protected static byte[] base64(String data) {
        if (data == null) return null;
        return Base64.getDecoder().decode(data);
    }
    
    /**
     * Make a deep copy of this JSON object
     * @param <T>   Object type
     * @param clazz Object class (required because of Java type erasure)
     * @return Deep copy of this JSON object
     */
    public <T extends GsonMessage> T copy(final Class<T> clazz) {
        return GSON.fromJson(GSON.toJsonTree(this).deepCopy(), clazz);
    }
    
    /**
     * Make a deep copy of this JSON object
     * @param <T>   Object type
     * @param clazz Object class (required because of Java type erasure)
     * @param elements Elements to drop from the copied object
     * @return Deep copy of this JSON object
     */
    public <T extends GsonMessage> T copyWithout(final Class<T> clazz, final String ... elements) {
        JsonObject obj = GSON.toJsonTree(this).deepCopy().getAsJsonObject();
        for (String e : elements) {
            obj.remove(e);
        }
        return GSON.fromJson(obj, clazz);
    }
    
    /**
     * Get byte[] representation of this JSON, like for serializing to an output stream.
     * 
     * @return byte[]
     */
    public byte[] getBytes() {
        return this.toJson().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Convert this JSON object to a String and encode it with base64
     * <p>This is useful for example when sending JSON objects via SOAP
     * 
     * @return String JSON
     */
    public String toBase64() {
        return Base64.getEncoder().encodeToString(this.getBytes());
    }
    
    /**
     * Convert this object to a JSON String
     * @return JSON String
     */
    public String toJson() {
        return this.getGson().toJson(this);
    }

    /**
     * Convert this {@link GsonMessage} object to a {@link JsonElement}
     * @return JsonElement
     */
    public JsonElement toJsonTree() {
        return this.getGson().toJsonTree(this);
    }
    
    @Override
    public String toString() {
        return this.toJson();
    }

    /**
     * Get GSON implementation
     * @return default, or overridden implementation.
     */
    protected Gson getGson() {
        if (this.gson != null) return this.gson;
        return GSON;
    }

    /**
     * Override the default GSON implementation
     * (used by {@link #toJson()})
     * @param gson
     */
    protected void setGson(final Gson gson) {
        this.gson = gson;
    }
}
