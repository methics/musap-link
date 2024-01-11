//
//  (c) Copyright 2003-2023 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import fi.methics.webapp.musaplink.link.json.MusapResp;
import fi.methics.webapp.musaplink.link.json.MusapSignResp;

public class SignatureCallback {

    private String transid;
    private Semaphore semaphore;
    
    private MusapSignResp sigResp;
    private MusapResp     error;
    
    public SignatureCallback(String transid) {
        this.semaphore = new Semaphore(0);
        this.transid   = transid;
    }
    
    public String getTransId() {
        return this.transid;
    }
    
    public void tryAcquire() throws InterruptedException {
        this.semaphore.tryAcquire(180, TimeUnit.SECONDS);
    }
    
    public void release() {
        this.semaphore.release();
    }
    
    public void setError(MusapResp error) {
        this.error = error;
    }
    
    public void setResponse(MusapSignResp resp) {
        this.sigResp = resp;
    }
    
    public boolean isError() {
        return this.error != null;
    }
    
    public MusapSignResp getResponse() {
        return this.sigResp;
    }
    
    public MusapResp getError() {
        return this.error;
    }
    
}
