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

@Stateless
@Path("ocn-collection")
public class OcnCollectionBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcnCollectionBean.class);

    @GET
    @Path("{ocn}")
    @Produces({ MediaType.APPLICATION_XML })
    public PidList getPidList(@Context UriInfo uriInfo, @PathParam("ocn") String ocn) {
        final ObjectFactory objectFactory = new ObjectFactory();
        final Pid pid = objectFactory.createPid();
        pid.setFormat("katalog");
        pid.setLibraryNumber("1");
        pid.setIdNumber("42");
        final PidList pidList = objectFactory.createPidList();
        pidList.setResource(uriInfo.getRequestUri().toString());
        pidList.getPid().add(pid);
        return pidList;
    }
}
