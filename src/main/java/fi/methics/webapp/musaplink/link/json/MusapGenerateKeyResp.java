package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapGenerateKeyResp extends MusapResp {

    @SerializedName("linkid")
    public String linkid;

    @SerializedName("keyid")
    public String keyid;

    @SerializedName("keyname")
    public String keyname;

    @SerializedName("publickey")
    public MusapPublicKey publickey;

    @SerializedName("signature")
    public MusapSignature signature;

}
