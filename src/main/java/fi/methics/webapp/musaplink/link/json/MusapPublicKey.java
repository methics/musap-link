//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapPublicKey {

    @SerializedName("pem")
    public String pem;

    @SerializedName("keyuri")
    public String keyuri;

    @SerializedName("keyid")
    public String keyid;
    
}
