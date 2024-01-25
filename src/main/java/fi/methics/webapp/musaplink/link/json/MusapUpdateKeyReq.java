package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapUpdateKeyReq extends MusapReq {

    @SerializedName("keyid")
    public String keyid;

    @SerializedName("oldkeyname")
    public String oldKeyname;
    
    @SerializedName("linkid")
    public String linkid;

    @SerializedName(value="newkeyname", alternate="keyname")
    public String newkeyname;


}
