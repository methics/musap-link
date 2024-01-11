//
//  (c) Copyright 2003-2020 Methics Oy. All rights reserved. 
//

package fi.methics.webapp.musaplink.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * AppActivation Servlet utility methods
 */
public class Util {

    
    /** 
     * Utility method that reads request body content from InputStream.
     * (Reader API does not glob BOM markers, InputStreamReader on UTF-8
     * charset seem to glob them.)
     * 
     * @param is InputStream containing request body
     * @return request body as String
     * @throws IOException 
     */
    public static String readRequestBody(final InputStream is)
        throws IOException
    {
        InputStreamReader isr = null;
        try {
            final String charset = "UTF-8";
            isr = new InputStreamReader(is, charset);
            final StringBuilder sb = new StringBuilder();

            char cbuf[] = new char[1024];
            int rc;

            while ((rc = isr.read(cbuf, 0, cbuf.length)) > 0) {
                sb.append(cbuf, 0, rc);
            }

            return sb.toString();

        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

}
