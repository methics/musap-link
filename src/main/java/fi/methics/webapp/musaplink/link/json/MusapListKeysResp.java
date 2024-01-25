package fi.methics.webapp.musaplink.link.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import fi.methics.webapp.musaplink.MusapLinkAccount.MusapKey;

public class MusapListKeysResp extends MusapResp  {

    @SerializedName("keys")
    public List<MusapKey> keys;

    public void addKeys(Collection<MusapKey> keys) {
        this.keys = new ArrayList<>(keys);
    }    
    
}
