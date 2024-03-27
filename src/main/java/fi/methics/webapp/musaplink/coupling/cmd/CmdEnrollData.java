package fi.methics.webapp.musaplink.coupling.cmd;

import java.io.IOException;
import java.util.UUID;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import fi.methics.util.Base64;
import fi.methics.webapp.musaplink.AccountStorage;
import fi.methics.webapp.musaplink.MusapLinkAccount;
import fi.methics.webapp.musaplink.coupling.CouplingCommand;
import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;
import fi.methics.webapp.musaplink.coupling.json.EnrollDataReq;
import fi.methics.webapp.musaplink.coupling.json.EnrollDataResp;

/**
 * Coupling API command for enrolling MUSAP to this MUSAP Link.
 * 
 * <p>This initializes transport security and push notification tokens.
 */
public class CmdEnrollData extends CouplingCommand {

    public CmdEnrollData(CouplingApiMessage req) {
        super(req, EnrollDataReq.class);
    }

    @Override
    public CouplingApiMessage execute() throws IOException {
        final CouplingApiMessage req = this.getRequest();

        log.debug("Calling activation");

        final EnrollDataReq payload = this.getRequestPayload();

        MusapLinkAccount account = new MusapLinkAccount();
        account.apnsToken = payload.apnstoken;
        account.fcmToken  = payload.fcmtoken;
        account.musapid   = UUID.randomUUID().toString();
    
        String sharedSecret = payload.getSharedSecret();
        if (sharedSecret != null && sharedSecret.length() > 0) {
            byte[] ss = Base64.decodeBase64(sharedSecret);
            byte[][] keypair_mac_aes = this.deriveKeys(ss);
            account.macKey = keypair_mac_aes[0];
            account.aesKey = keypair_mac_aes[1];
        }
        
        log.debug("Storing account with MusapID " + account.musapid);
        AccountStorage.storeAccount(account);
        
        EnrollDataResp respPayload = new EnrollDataResp();
        respPayload.musapid = account.musapid;
        
        CouplingApiMessage resp = req.createResponse(respPayload);
        if (account.aesKey != null) {
            try {
                resp.encrypt(account.aesKey);
            } catch (Exception e) {
                log.warn("Failed to encrypt response", e);
            }
        }
        return resp;
    }
    
    /**
     * Calculates MAC and AES keys and returns them in a small array in that order (indexes 0, 1) 
     * @param secret Shared secret
     */
    public byte[][] deriveKeys(final byte[] secret) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(secret, null, null));
        byte[] macKey = new byte[32];
        byte[] aesKey = new byte[16];
        byte[] output = new byte[macKey.length+aesKey.length];

        hkdf.generateBytes(output, 0, output.length);
        System.arraycopy(output, 0, macKey, 0, macKey.length);
        System.arraycopy(output, macKey.length, aesKey, 0, aesKey.length);
        return new byte[][] {macKey, aesKey};
    }
    
}
