package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.ocn2pid.service.dto.ObjectFactory;
import dk.dbc.oclc.ocn2pid.service.dto.Pid;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;

@Stateless
@Path("ocn-collection")
public class OcnCollectionBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcnCollectionBean.class);

    @GET
    @Path("{ocn}")
    @Produces({ MediaType.APPLICATION_XML })
    public PidList getPidList(@Context UriInfo uriInfo, @PathParam("ocn") String ocn) {
        LOGGER.debug("Called");
        final String resource = uriInfo.getRequestUri().toString();
        LOGGER.trace("Resource: {}", resource);

        final ObjectFactory objectFactory = new ObjectFactory();
        final Pid pid = objectFactory.createPid();
        pid.setFormat("katalog");
        pid.setLibraryNumber("1");
        pid.setIdNumber("42");
        final PidList pidList = objectFactory.createPidList();
        pidList.setResource(resource);
        pidList.getPid().add(pid);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Exit with {}", jaxbObjectToString(PidList.class, pidList));
        }

        return pidList;
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
