package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.ocn2pid.service.dto.ObjectFactory;
import dk.dbc.oclc.ocn2pid.service.dto.Pid;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Enterprise Java Bean (EJB) handles database lookup and response formatting of
 * ocn-to-pid resolve operations.
 */
@Stateless
public class OcnResolverBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcnResolverBean.class);

    private static final Pattern PID_PATTERN = Pattern.compile("^(.+?)-(.+?):(.+)$");

    @EJB
    OcnStoreConnectorBean ocnStoreConnector;

    /**
     * Resolves all local PIDs associated with given OCN identifier
     * @param ocn OCN identifier
     * @param libraryNumberFilter set of library numbers to be included in the response,
     * if null or empty all library numbers will be included
     * @return resolved PIDs as PidList
     * @throws EJBException on datasource communication failure, or if lookup returns a PID
     * not adhering to the PID formatting rules
     */
    public PidList getPidListByOcn(String ocn, Set<String> libraryNumberFilter) throws EJBException {
        try {
            final List<String> localIds = ocnStoreConnector.getLocalIdsMappedToOcn(ocn);
            final ObjectFactory objectFactory = new ObjectFactory();
            final PidList pidList = objectFactory.createPidList();
            for (String id : localIds) {
                final Pid pid = buildPid(objectFactory, id);
                if (libraryNumberFilter == null
                        || libraryNumberFilter.isEmpty()
                        || libraryNumberFilter.contains(pid.getLibraryNumber())) {
                    pidList.getPid().add(pid);
                }
            }
            return pidList;
        } catch (SQLException e) {
            LOGGER.error("Unable to access OCN store", e);
            throw new EJBException(e);
        }
    }

    private Pid buildPid(ObjectFactory objectFactory, String id) throws EJBException {
        final Pid pid = objectFactory.createPid();
        Matcher matcher = PID_PATTERN.matcher(id);
        if (matcher.matches()) {
            pid.setLibraryNumber(matcher.group(1));
            pid.setFormat(matcher.group(2));
            pid.setIdNumber(matcher.group(3));
        } else {
            final String errMsg = String.format("ID %s does not match PID pattern %s",
                    id, PID_PATTERN.pattern());
            LOGGER.error(errMsg);
            throw new EJBException(errMsg);
        }
        pid.setValue(id);
        return pid;
    }
}
