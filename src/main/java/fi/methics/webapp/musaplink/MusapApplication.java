package fi.methics.webapp.musaplink;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import fi.methics.webapp.musaplink.coupling.MusapCouplingServlet;
import fi.methics.webapp.musaplink.link.MusapLinkServlet;
import fi.methics.webapp.musaplink.util.jaxrs.RequestLoggerFilter;
import fi.methics.webapp.musaplink.util.jaxrs.ResponseLoggerFilter;

/**
 * JAX-RS Application endpoint
 */
@ApplicationPath("/*")
public class MusapApplication extends Application {
    
    
    /**
     * Initialize the web application
     */
    @PostConstruct
    public static void initialize() {
        MusapLinkServlet.init();
        MusapCouplingServlet.init();
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(
            Arrays.asList(
                MusapCouplingServlet.class,
                MusapLinkServlet.class,
                RequestLoggerFilter.class,
                ResponseLoggerFilter.class
        ));
    }
}
