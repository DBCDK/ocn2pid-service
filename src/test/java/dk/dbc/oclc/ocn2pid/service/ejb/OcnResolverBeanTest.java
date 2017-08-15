package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.ocn2pid.service.dto.Pid;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import dk.dbc.ocnrepo.OcnRepo;
import org.junit.Test;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OcnResolverBeanTest {
    private final OcnRepo ocnRepo = mock(OcnRepo.class);
    private final String ocn = "42";
    private final String libraryNumberPid1 = "001";
    private final String formatPid1 = "format1";
    private final String idNumberPid1 = "42";
    private final String pid1 = String.format("%s-%s:%s", libraryNumberPid1, formatPid1, idNumberPid1);
    private final String libraryNumberPid2 = "002";
    private final String formatPid2 = "format2";
    private final String idNumberPid2 = "24";
    private final String pid2 = String.format("%s-%s:%s", libraryNumberPid2, formatPid2, idNumberPid2);

    @Test(expected = EJBException.class)
    public void getPidListByOcn_ocnStoreConnectorBeanReturnsInvalidPid_throws() throws SQLException {
        final List<String> pids = Collections.singletonList("invalidPid");
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(pids);
        final OcnResolverBean ocnResolver = getInitializedBean();
        ocnResolver.getPidListByOcn(ocn, Collections.emptySet());
    }

    @Test
    public void getPidListByOcn_ocnStoreConnectorBeanReturnsEmptyList_returnsEmptyPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Collections.emptyList());
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn, Collections.emptySet());
        assertThat(pidList.getPid().isEmpty(), is(true));
    }

    @Test
    public void getPidListByOcn_libraryNumberFilterArgIsNull_returnsPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Arrays.asList(pid1, pid2));
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn, null);
        assertThat(pidList.getPid().size(), is(2));
        final Pid pidListEntry1 = pidList.getPid().get(0);
        assertThat(pidListEntry1.getValue(), is(pid1));
        assertThat(pidListEntry1.getLibraryNumber(), is(libraryNumberPid1));
        assertThat(pidListEntry1.getFormat(), is(formatPid1));
        assertThat(pidListEntry1.getIdNumber(), is(idNumberPid1));
        final Pid pidListEntry2 = pidList.getPid().get(1);
        assertThat(pidListEntry2.getValue(), is(pid2));
        assertThat(pidListEntry2.getLibraryNumber(), is(libraryNumberPid2));
        assertThat(pidListEntry2.getFormat(), is(formatPid2));
        assertThat(pidListEntry2.getIdNumber(), is(idNumberPid2));
    }

    @Test
    public void getPidListByOcn_libraryNumberFilterArgIsEmpty_returnsPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Arrays.asList(pid1, pid2));
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn, Collections.emptySet());
        assertThat(pidList.getPid().size(), is(2));
        assertThat(pidList.getPid().get(0).getValue(), is(pid1));
        assertThat(pidList.getPid().get(1).getValue(), is(pid2));
    }

    @Test
    public void getPidListByOcn_libraryNumberFilterArgSingleEntryMatch_returnsPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Arrays.asList(pid1, pid2));
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn,
            new HashSet<>(Collections.singletonList(libraryNumberPid1)));
        assertThat(pidList.getPid().size(), is(1));
        assertThat(pidList.getPid().get(0).getValue(), is(pid1));
    }

    @Test
    public void getPidListByOcn_libraryNumberFilterArgMultipleEntriesMatch_returnsPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Arrays.asList(pid1, pid2));
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn, new HashSet<>(Arrays.asList(libraryNumberPid1, libraryNumberPid2)));
        assertThat(pidList.getPid().size(), is(2));
        assertThat(pidList.getPid().get(0).getValue(), is(pid1));
        assertThat(pidList.getPid().get(1).getValue(), is(pid2));
    }

    @Test
    public void getPidListByOcn_libraryNumberFilterArgSingleEntryNoMatch_returnsEmptyPidList() throws SQLException {
        when(ocnRepo.pidListFromOcn(ocn)).thenReturn(Arrays.asList(pid1, pid2));
        final OcnResolverBean ocnResolver = getInitializedBean();
        final PidList pidList = ocnResolver.getPidListByOcn(ocn,
            new HashSet<>(Collections.singletonList("no-match")));
        assertThat(pidList.getPid().isEmpty(), is(true));
    }

    @Test
    public void getOcnByPid() {
        final OcnResolverBean ocnResolverBean = getInitializedBean();
        final String pid = "870970-basis:44260441";
        when(ocnRepo.getOcnByPid(anyString())).thenReturn("871992862");
        final String ocn = ocnResolverBean.getOcnByPid(pid);
        assertThat("ocn", ocn, is("871992862"));
    }

    @Test(expected = NoResultException.class)
    public void getOcnByPid_noResultFound() {
        when(ocnRepo.getOcnByPid(anyString())).thenThrow(NoResultException.class);
        final OcnResolverBean ocnResolverBean = getInitializedBean();
        ocnResolverBean.getOcnByPid("noSuchPid");
        fail("no exception thrown");
    }

    private OcnResolverBean getInitializedBean() {
        final OcnResolverBean ocnResolverBean = new OcnResolverBean();
        ocnResolverBean.ocnRepo = ocnRepo;
        return ocnResolverBean;
    }
}
