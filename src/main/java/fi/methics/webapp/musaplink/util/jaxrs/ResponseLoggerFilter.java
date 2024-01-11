//
//  (c) Copyright 2003-2021 Methics Oy. All rights reserved. 
//
package fi.methics.webapp.musaplink.util.jaxrs;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.methics.webapp.musaplink.coupling.json.CouplingApiMessage;

@Provider
public class ResponseLoggerFilter implements ContainerResponseFilter {

    private static final Log log = LogFactory.getLog(RequestLoggerFilter.class);
    
    @Override
    public void filter(ContainerRequestContext  req,
                       ContainerResponseContext resp) throws IOException 
    {
        Object entity = resp.getEntity();
        if (entity instanceof String) {
            log.debug("Returning: " + (String)entity);
        }
        if (entity instanceof CouplingApiMessage) {
            log.debug("Returning: " + ((CouplingApiMessage)entity).toJson());
        }
    }
}