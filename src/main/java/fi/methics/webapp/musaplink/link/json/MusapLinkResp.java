package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapLinkResp extends MusapResp  {

    @SerializedName("linkid")
    public String linkid;

    @SerializedName("couplingcode")
    public String couplingcode;

    @SerializedName("qrcode")
    public String qrcode;    
    
}
