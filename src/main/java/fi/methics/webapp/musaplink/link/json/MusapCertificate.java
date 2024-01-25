package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapCertificate {

    @SerializedName("pem")
    public String pem;

    @SerializedName("subject")
    public String subject;
    
}
