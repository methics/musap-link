//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.link.json;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.coupling.json.CouplingApiPayload;

public class MusapBindReq extends CouplingApiPayload {

    @SerializedName("linkid")
    public String linkid;

    @SerializedName("key_requirements")
    public KeyRequirements keyRequirements;
    
    public static class KeyRequirements {

        @SerializedName("requirenew") // Which is better? Spec says "requirenew"
        public boolean generate;

        // TODO: These must be changed to match MUSAP Library SignatureReq better
        @SerializedName("algorithm")
        public String algorithm;

        @SerializedName("scheme")
        public String scheme;

        @SerializedName("curve")
        public String curve;
        
    }
    
}
