package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.Set;

/**
 * This Enterprise Java Bean (EJB) class acts as a JAX-RS root resource
 * exposed by the /ocn-collection entry point
 */

@Stateless
@Path("ocn-collection")
public class OcnCollectionBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcnCollectionBean.class);

    @EJB
    OcnResolverBean ocnResolver;

    /**
     * Resolves all local PIDs associated with given OCN identifier
     * @param uriInfo application and request URI information
     * @param ocn OCN identifier
     * @param libraryNumberFilter set of library numbers to be included in the response,
     * if empty all library numbers will be included
     * @return PidList (i.e. a HTTP 200 OK response with PidList entity)
     * @throws EJBException on internal server error
     */
    @GET
    @Path("{ocn}")
    @Produces({ MediaType.APPLICATION_XML })
    public PidList getPidListByOcn(@Context UriInfo uriInfo, @PathParam("ocn") String ocn,
            @QueryParam("libraryNumberFilter") Set<String> libraryNumberFilter) throws EJBException {
        LOGGER.debug("Called");
        final String resource = uriInfo.getRequestUri().toString();
        LOGGER.trace("Resource: {}", resource);

        sanitizeFilter(libraryNumberFilter);

        final PidList pidList = ocnResolver.getPidListByOcn(ocn, libraryNumberFilter);

        pidList.setResource(resource);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Exit with {}", jaxbObjectToString(PidList.class, pidList));
        }

        return pidList;
    }

    private void sanitizeFilter(Set<String> filter) {
        for (final String member : filter) {
            if ("". equals(member.trim())) {
                filter.remove(member);
            }
        }
    }

    private <T> String jaxbObjectToString(Class<T> tClass, T object) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(tClass);
            final StringWriter writer = new StringWriter();
            jaxbContext.createMarshaller().marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }
}
