package fi.methics.webapp.musaplink.util.jaxrs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.io.CharStreams;

@Provider
public class RequestLoggerFilter implements ContainerRequestFilter {

    private static final Log log = LogFactory.getLog(RequestLoggerFilter.class);
    
    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        // The payload may be empty, but it is never null
        String method  = req.getMethod();
        String url     = req.getUriInfo().getPath();
        String query   = getQueryString(req);
        
        if (url.contains("sim/upload") || url.contains("test/sign")) {
            log.info("Got " + method + " request to " + url + query);
            return;
        }
        
        BufferedInputStream stream = new BufferedInputStream(req.getEntityStream());        
        String             payload = CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));

        if (payload.isEmpty()) {
            log.info("Got " + method + " request to " + url + query);
        } else {
            log.info("Got " + method + " request to " + url + query + ": " + payload);
        }
        
        // Return stream to request
        InputStream targetStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        req.setEntityStream(targetStream);
    }
    
    /**
     * Fetch the HTTP query string
     * @param req Query Parameters returned by {@code req.getUriInfo().getQueryParamters()}
     * @return Query String (may be empty)
     */
    public static String getQueryString(ContainerRequestContext req) {
        MultivaluedMap<String, String> queryParams = req.getUriInfo().getQueryParameters();
        UriBuilder uriBuilder = UriBuilder.fromPath("/");
        for (String paramName : queryParams.keySet()) {
            List<String> paramValues = queryParams.get(paramName);
            for (String paramValue : paramValues) {
                uriBuilder.queryParam(paramName, paramValue);
            }
        }
        URI uri = uriBuilder.build();
        String queryString = uri.getQuery();
        
        if (queryString != null && !queryString.isEmpty()) {
            return "?" + queryString;
        } else {
            return "";
        }
    }
    
}