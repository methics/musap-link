//
//  (c) Copyright 2003-2020 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

/**
 * MUSAP Link API /sign response
 */
public class MusapSignResp extends MusapResp {

    @SerializedName("linkid")
    public String linkid;
    
    @SerializedName("publickey")
    public MusapPublicKey publickey;

    @SerializedName("signature")
    public MusapSignature signature;

}
