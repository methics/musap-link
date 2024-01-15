package fi.methics.webapp.musaplink.util.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import fi.methics.webapp.musaplink.util.MusapLinkConf;

/**
 * MUSAP Link JDBC handler class
 * <p>Provides an easy way to create DB {@link Connection} handles.
 */
public class MusapDb {

    private static final Log log = LogFactory.getLog(MusapDb.class);

    public static final String COLUMN_CANNOT_ACCEPT_NULL = "23502";
    public static final String DUPLICATE_KEY_GENERIC     = "23000";
    public static final String DUPLICATE_KEY             = "23505";
    public static final String VALUE_TOO_LONG            = "12899";
    public static final String INTEGRITY_CONSTRAINT_VIOLATION = "23";

    public static final String MSG_DUPLICATE_KEY = "duplicate key value";
    
    private static DataSource datasource;
    
    /**
     * Initialize PostgreSQL {@link DataSource}
     */
    public static void initDataSource(MusapLinkConf conf) {
        if (datasource != null) return;
        datasource = new DataSource();
        datasource.setPoolProperties(new PoolProperties());
        datasource.setUrl(conf.getDbUrl());
        if (conf.getDbUsername() != null) datasource.setUsername(conf.getDbUsername());
        if (conf.getDbPassword() != null) datasource.setPassword(conf.getDbPassword());
        datasource.setMaxActive(100);
        datasource.setDriverClassName(conf.getDriverClass());
    }
    
    /**
     * Get a SQLite database connection
     * @return {@link Connection}
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (datasource == null) {
            initDataSource(MusapLinkConf.getInstance());
        }
        Connection conn = datasource.getConnection();
        if (conn.isClosed()) {
            log.error("Tomcat DataSource returned an already closed connection");
            throw new SQLException("Connection closed");
        }
        return conn;
    }
    
    /**
     * Was it duplicate insert?
     * @param e SQL Exception
     * @return true for duplicate insert
     */
    public static boolean wasItDuplicateInsert(final SQLException e) {
        final String   state = e.getSQLState();
        final String     msg = e.getMessage();
        final int vendorCode = e.getErrorCode();
        if (log.isTraceEnabled()) {
            log.trace("Checking if insert was duplicate");
            log.trace("  SQL State = " + state);
            log.trace("  ErrorCode = " + vendorCode);
        }
        if (state.startsWith(DUPLICATE_KEY)) return true;
        if (state.startsWith(DUPLICATE_KEY_GENERIC)) return true;
        if (state.startsWith(INTEGRITY_CONSTRAINT_VIOLATION)) return true;
        if (msg.startsWith(MSG_DUPLICATE_KEY)) return true;
        return false;
    }
    

    public static Integer getInteger(ResultSet result, int i) throws SQLException {
        int ret = result.getInt(i);
        if (result.wasNull()) {
            return null;
        }
        return Integer.valueOf(ret);
    }
}
