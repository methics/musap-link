package fi.methics.webapp.musaplink.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of helper methods for commonplace X509 tasks.
 */
public class X509Util {
    private static final Log log = LogFactory.getLog(X509Util.class);

    /**
     * Convert a DER certificate to X509Certificate
     * @param der Certificate to convert
     * @return Converted certificate as X509Certificate. Returns null if the conversion failed or input is null. 
     */
    public static X509Certificate DERtoX509Certificate(final byte[] der) {
        if (der == null) {
            log.error("Trying to convert a null DER to X509Certificate.");
            return null;
        }
        
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(der);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate)cf.generateCertificate(bis);
        } catch (CertificateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Convert an X509 certificate to byte[] DER
     * @param cert Certificate to convert
     * @return Converted certificate as byte[] DER or null if the conversion failed
     */
    public static byte[] X509CertificateToDER(final X509Certificate cert) {
        if (cert == null) {
            log.error("Trying to convert null X509Cert to DER.");
            return null;
        }
        
        try {
            return cert.getEncoded();
        } catch (CertificateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    
    /** 
     * Calculates a SHA-1 hash of the given byte[] certificate
     * @param cert Certificate as byte[]
     * @return SHA-1 hash of the cert  or null if the calculation failed
     */
    public static byte[] certHash(final byte[] cert) {
        if(cert == null) {
            return null;
        }
        
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            hash = md.digest(cert);
        } catch (Throwable t) {
            // never happens
        }
        
        return hash;
    }
    
    /**
     * Parse the CN part from the given certificate's Subject
     * @param cert Certificate
     * @return CN as String
     */
    public static String parseSubjectCn(final X509Certificate cert) {
        return parseSubjectName(cert, "CN");
    }

    /**
     * Parse the given RND type from the given certificate's subject
     * @param cert Certificate
     * @param rdnType RND type
     * @return parsed value as String
     */
    public static String parseSubjectName(final X509Certificate cert, final String rdnType) {
        String dn = cert.getSubjectX500Principal().getName();
    
        String name = null;
        try {
            LdapName ldapDn = new LdapName(dn);
            List<Rdn> rdns = ldapDn.getRdns();
            for(Rdn r : rdns) {
                if(rdnType.equals(r.getType())) {
                    name = r.getValue().toString();
                }
            }
        } catch(InvalidNameException e) {
            log.error(e);
        }
        
        return name;
    }
    
    /**
     * Convert the X509Certificate {@code boolean[]} to {@code List<String>}
     * 
     * The following list shows the String vs the corresponding boolean[] index:
     * <pre>
     *     digitalSignature        (0),
     *     nonRepudiation          (1),
     *     keyEncipherment         (2),
     *     dataEncipherment        (3),
     *     keyAgreement            (4),
     *     keyCertSign             (5),
     *     cRLSign                 (6),
     *     encipherOnly            (7),
     *     decipherOnly            (8)
     * </pre>
     * @param keyUsage KeyUsage {@code boolean[]}
     * @return KeyUsage {@code List<String>}
     */
   public static List<String> keyUsageToString(final boolean[] keyUsage) {
       
       List<String> str = new ArrayList<>();
       
       if (keyUsage != null && keyUsage.length >= 9) {
           if (keyUsage[0]) str.add("digitalSignature");
           if (keyUsage[1]) str.add("nonRepudiation");
           if (keyUsage[2]) str.add("keyEncipherment");
           if (keyUsage[3]) str.add("dataEncipherment");
           if (keyUsage[4]) str.add("keyAgreement");
           if (keyUsage[5]) str.add("keyCertSign");
           if (keyUsage[6]) str.add("cRLSign");
           if (keyUsage[7]) str.add("encipherOnly");
           if (keyUsage[8]) str.add("decipherOnly");
       }
       
       return str;
   }
   
   private static final String BEGIN_CERTIFICATE = "-----BEGIN .*?-----";
   private static final String BEGIN_tag         = "-----BEGIN ";
   private static final String END_CERTIFICATE   = "-----END .*?-----";

   /**
    * The input may be PEM encoded cert or something.  If so, decode it to DER form.
    * If it does not look like PEM, return the input data unmodified. 
    */
   public static byte[] maybeDecodePEM(final byte[] pemin) {
       // If the data is null, too short, or starts with DER SEQUENCE tag, return it as is.
       if (pemin == null || pemin.length < 1 || pemin[0] == 0x30) {
           return pemin;
       }
       try {
           String pem = new String(pemin,"ISO-8859-1");
           return maybeDecodePEM(pem);
       } catch (Exception e) {
           // ignore
       }
       return pemin;
   }

   /**
    * The input may be PEM encoded cert or something.  If so, decode it to DER form.
    * If it does not look like PEM, return the input data unmodified.
    * If it fails Base64 decode, throw IOException.
    */
   public static byte[] maybeDecodePEM(final String pemin) throws IOException {
       try {
           String pem = pemin;
           if (pem.startsWith(BEGIN_tag)) {
               pem = pem.replaceAll(BEGIN_CERTIFICATE, "");
               pem = pem.replaceAll(END_CERTIFICATE, "");
           }
           return Base64.getDecoder().decode(pem);
       } catch (Exception e) {
           // ignore
       }
       throw new IOException("The input value is not Base64 encoded data.");
   }


}
