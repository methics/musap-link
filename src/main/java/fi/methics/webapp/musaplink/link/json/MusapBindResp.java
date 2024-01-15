package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapBindResp extends MusapResp  {

    @SerializedName("keyid")
    public String keyid;

    @SerializedName("publickey")
    public MusapPublicKey publickey;

    @SerializedName("signature")
    public MusapSignature signature;
    
}
