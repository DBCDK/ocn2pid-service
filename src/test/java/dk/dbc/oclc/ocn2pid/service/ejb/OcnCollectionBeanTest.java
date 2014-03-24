package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.ocn2pid.service.dto.ObjectFactory;
import dk.dbc.oclc.ocn2pid.service.dto.Pid;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OcnCollectionBeanTest {
    private final OcnResolverBean ocnResolverBean = mock(OcnResolverBean.class);
    private final UriInfo uriInfo = mock(UriInfo.class);
    private final String ocn = "42";
    private final Set<String> libraryNumberFilter = Collections.emptySet();
    private final PidList pidListFromOcnResolver = getPidList();
    private final String requestUri = "http://host/path";

    @Before
    public void setupMocks() throws URISyntaxException {
        when(uriInfo.getRequestUri()).thenReturn(new URI(requestUri));
        when(ocnResolverBean.getPidListByOcn(ocn, libraryNumberFilter)).thenReturn(pidListFromOcnResolver);
    }

    @Test
    public void getPidListByOcn_setsRequestUriInReturnedPidList() throws URISyntaxException {
        final OcnCollectionBean ocnCollectionBean = getInitializedBean();
        final PidList pidList = ocnCollectionBean.getPidListByOcn(uriInfo, ocn, libraryNumberFilter);
        assertThat(pidList.getResource(), is(requestUri));
    }

    @Test
    public void getPidListByOcn_sanitizesLibraryNumberFilterArg() throws URISyntaxException {
        final OcnCollectionBean ocnCollectionBean = getInitializedBean();
        // If no sanitize operation occurred, method call below
        // would not be matched by our when().thenReturn() construct
        // and a NullPointerException would ensue.
        ocnCollectionBean.getPidListByOcn(uriInfo, ocn, new HashSet<>(Arrays.asList("")));
    }

    private OcnCollectionBean getInitializedBean() {
        final OcnCollectionBean ocnCollectionBean = new OcnCollectionBean();
        ocnCollectionBean.ocnResolver = ocnResolverBean;
        return ocnCollectionBean;
    }

    private PidList getPidList() {
        final String libraryNumberPid1 = "001";
        final String formatPid1 = "format1";
        final String idNumberPid1 = "42";
        final String valuePid1 = String.format("%s-%s:%s", libraryNumberPid1, formatPid1, idNumberPid1);
        final String libraryNumberPid2 = "002";
        final String formatPid2 = "format2";
        final String idNumberPid2 = "24";
        final String valuePid2 = String.format("%s-%s:%s", libraryNumberPid2, formatPid2, idNumberPid2);

        final ObjectFactory objectFactory = new ObjectFactory();
        final Pid pid1 = objectFactory.createPid();
        pid1.setValue(valuePid1);
        pid1.setLibraryNumber(libraryNumberPid1);
        pid1.setFormat(formatPid1);
        pid1.setIdNumber(idNumberPid1);
        final Pid pid2 = objectFactory.createPid();
        pid2.setValue(valuePid2);
        pid2.setLibraryNumber(libraryNumberPid2);
        pid2.setFormat(formatPid2);
        pid2.setIdNumber(idNumberPid2);

        final PidList pidList = objectFactory.createPidList();
        pidList.getPid().add(pid1);
        pidList.getPid().add(pid2);
        return pidList;
    }
}
