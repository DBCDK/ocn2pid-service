package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.commons.jdbc.util.CursoredResultSet;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
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
     *
     * @param uriInfo             application and request URI information
     * @param ocn                 OCN identifier
     * @param libraryNumberFilter set of library numbers to be included in the response,
     *                            if empty all library numbers will be included
     * @return PidList (i.e. a HTTP 200 OK response with PidList entity)
     * @throws EJBException on internal server error
     */
    @GET
    @Path("{ocn}")
    @Produces({MediaType.APPLICATION_XML})
    public PidList getPidListByOcn(@Context UriInfo uriInfo, @PathParam("ocn") String ocn,
                                   @QueryParam("libraryNumberFilter") Set<String> libraryNumberFilter) throws EJBException {
        try {
            final String resource = uriInfo.getRequestUri().toString();

            sanitizeFilter(libraryNumberFilter);

            final PidList pidList = ocnResolver.getPidListByOcn(ocn, libraryNumberFilter);

            pidList.setResource(resource);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Exit with {}", jaxbObjectToString(PidList.class, pidList));
            }

            return pidList;
        } finally {
            LOGGER.info("GET /ocn-collection/{}", ocn);
        }
    }

    /**
     * Gets an ocn by pid
     *
     * @param pid the pid to look up
     * @return an ocn or empty response if no ocn found
     */
    @GET
    @Path("ocn-by-pid/{pid}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOcnByPid(@PathParam("pid") String pid) {
        try {
            final Optional<String> ocn = ocnResolver.getOcnByPid(pid);
            if (ocn.isPresent()) {
                return Response.ok().entity(ocn.get()).build();
            } else {
                return Response.noContent().build();
            }
        } finally {
            LOGGER.info("GET /ocn-collection/ocn-by-pid/{}", pid);
        }
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
        try {
            final CursoredResultSet<WorldCatEntity> entitiesWithLHR =
                    ocnResolver.getEntitiesWithLHR();
            final StreamingOutput stream = os -> {
                for (WorldCatEntity entity : entitiesWithLHR) {
                    os.write(String.format("%s%n", entity.getPid()).getBytes(
                            StandardCharsets.UTF_8));
                }
            };
            return Response.ok(stream).build();
        } finally {
            LOGGER.info("GET /ocn-collection/pid/lhr");
        }
    }

    private void sanitizeFilter(Set<String> filter) {
        filter.removeIf(member -> "".equals(member.trim()));
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
