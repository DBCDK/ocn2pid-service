package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.commons.jdbc.util.CursoredResultSet;
import dk.dbc.oclc.ocn2pid.service.dto.Pid;
import dk.dbc.oclc.ocn2pid.service.dto.PidList;
import dk.dbc.ocnrepo.OcnRepo;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import org.junit.Test;

import jakarta.ejb.EJBException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(ocnRepo.getOcnByPid(anyString())).thenReturn(
            Optional.of("871992862"));
        final Optional<String> ocn = ocnResolverBean.getOcnByPid(pid);
        assertThat("ocn", ocn.get(), is("871992862"));
    }

    @Test
    public void getOcnByPid_noResultFound() {
        when(ocnRepo.getOcnByPid(anyString())).thenReturn(Optional.empty());
        final OcnResolverBean ocnResolverBean = getInitializedBean();
        Optional<String> ocn = ocnResolverBean.getOcnByPid("noSuchPid");

        assertThat("ocn not present", ocn.isPresent(), is(false));
    }

    // this test is more a test of the mocks than of the actual code
    // some proper integration testing with a real database cursor is needed
    @Test
    public void getEntitiesWithLHR() {
        MockedIterator iterator = new MockedIterator(10);
        CursoredResultSet<WorldCatEntity> resultSet = mock(CursoredResultSet.class);
        when(resultSet.iterator()).thenReturn(iterator);
        when(ocnRepo.getEntitiesWithLHR()).thenReturn(resultSet);
        OcnResolverBean ocnResolverBean = getInitializedBean();

        CursoredResultSet<WorldCatEntity> entities = ocnResolverBean
            .getEntitiesWithLHR();
        int i = 0;
        for(WorldCatEntity entity : entities) {
            i++;
            if(i == 0) {
                assertThat("pid", entity.getPid(), is("870970-basis:44260441"));
            }
        }
        assertThat("total entities", i, is(10));
        assertThat("end of stream", entities.iterator().hasNext(), is(false));
    }

    private OcnResolverBean getInitializedBean() {
        final OcnResolverBean ocnResolverBean = new OcnResolverBean();
        ocnResolverBean.ocnRepo = ocnRepo;
        return ocnResolverBean;
    }

    private class MockedIterator implements Iterator<WorldCatEntity> {
        int total;
        int i;
        public MockedIterator(int total) {
            this.total = total;
        }

        @Override
        public boolean hasNext() {
            return i < total;
        }

        @Override
        public WorldCatEntity next() {
            i++;
            return new WorldCatEntity().withPid("870970-basis:44260441");
        }
    }
}
