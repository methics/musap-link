//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

public class MusapUpdateKeyReq extends MusapReq {

    @SerializedName("linkid")
    public String linkid;
    
    @SerializedName("keyid")
    public String keyid;
    
    @SerializedName("keyname")
    public String keyname;

}
