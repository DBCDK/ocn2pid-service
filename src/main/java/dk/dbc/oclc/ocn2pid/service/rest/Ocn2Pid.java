package dk.dbc.oclc.ocn2pid.service.rest;

import dk.dbc.oclc.ocn2pid.service.ejb.OcnCollectionBean;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 */
@ApplicationPath("/")
public class Ocn2Pid extends Application {

    private static final Set<Class<?>> CLASSES = Set.of(
            OcnCollectionBean.class,
            HealthChecks.class,
            JaxbMessageBodyWriter.class
    );

    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }
}
