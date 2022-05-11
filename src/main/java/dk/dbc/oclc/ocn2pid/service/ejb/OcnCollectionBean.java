package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.commons.jdbc.util.CursoredResultSet;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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

    /**
     * Gets an ocn by pid
     * @param pid the pid to look up
     * @return an ocn or empty response if no ocn found
     */
    @GET
    @Path("ocn-by-pid/{pid}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOcnByPid(@PathParam("pid") String pid) {
        final Optional<String> ocn = ocnResolver.getOcnByPid(pid);
        if(ocn.isPresent()) return Response.ok().entity(ocn.get()).build();
        return Response.noContent().build();
    }

    /**
     * Gets pids of records with lhr
     *
     * @return stream of pids
     */
    @GET
    @Path("pid/lhr")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response getLhrPidStream() {
        final CursoredResultSet<WorldCatEntity> entitiesWithLHR =
            ocnResolver.getEntitiesWithLHR();
        final StreamingOutput stream = os -> {
            for(WorldCatEntity entity : entitiesWithLHR) {
                os.write(String.format("%s\n", entity.getPid()).getBytes(
                    StandardCharsets.UTF_8));
            }
        };
        return Response.ok(stream).build();
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
