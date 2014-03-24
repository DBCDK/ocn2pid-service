package dk.dbc.oclc.ocn2pid.service.ejb;

import dk.dbc.oclc.id.store.OcnStoreDao;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * This Enterprise Java Bean (EJB) facilitates access to the OCN store
 */
@Stateless
public class OcnStoreConnectorBean {
    @Resource(name = "jdbc/oclc-integration/ocn-store")
    private DataSource dataSource;

    public List<String> getLocalIdsMappedToOcn(String ocn) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            return new OcnStoreDao().getLocalIdsMappedToOcn(connection, ocn);
        }
    }
}
