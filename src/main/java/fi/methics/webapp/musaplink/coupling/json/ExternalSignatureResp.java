package fi.methics.webapp.musaplink.coupling.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP External signature response.
 */
public class ExternalSignatureResp extends CouplingApiPayload {

    @SerializedName("transid")
    public String transid;
    
    @SerializedName("signature")
    public String signature;

    @SerializedName("publickey")
    public String publickey;

    @SerializedName("certificate")
    public String certificate;

    @SerializedName("attributes")
    public Map<String, String> attributes = new HashMap<>();
    
    public static ExternalSignatureResp fromJson(final String str) {
        return GSON.fromJson(str, ExternalSignatureResp.class);
    }

}
