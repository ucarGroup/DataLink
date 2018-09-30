package com.ucar.datalink.worker.api.util.dialect;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.media.MediaSourceInfo;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.worker.api.util.dialect.oracle.OracleDialect;
import com.ucar.datalink.worker.api.util.dialect.mysql.MysqlDialect;
import com.ucar.datalink.worker.api.util.dialect.postgresql.PostgreSqlDialect;
import com.ucar.datalink.worker.api.util.dialect.sqlserver.SqlServerDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 方言工厂类
 * Created by lubiao on 2017/3/8.
 */
public class DbDialectFactory {

    private static final Logger logger = LoggerFactory.getLogger(DbDialectFactory.class);

    private static final LoadingCache<MediaSourceInfo, DbDialect> dialects;

    private static final DefaultLobHandler defaultLobHandler;

    static {
        defaultLobHandler = new DefaultLobHandler();
        defaultLobHandler.setStreamAsLob(true);

        dialects = CacheBuilder.newBuilder().softValues().build(new CacheLoader<MediaSourceInfo, DbDialect>() {
            @Override
            public DbDialect load(MediaSourceInfo mediaSourceInfo) throws Exception {
                DataSource dataSource = DataSourceFactory.getDataSource(mediaSourceInfo);
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                return jdbcTemplate.execute(
                        new ConnectionCallback<DbDialect>() {
                            @Override
                            public DbDialect doInConnection(Connection connection) throws SQLException, DataAccessException {
                                DatabaseMetaData meta = connection.getMetaData();
                                String databaseName = meta.getDatabaseProductName();
                                int databaseMajorVersion = meta.getDatabaseMajorVersion();
                                int databaseMinorVersion = meta.getDatabaseMinorVersion();
                                DbDialect dialect = generate(jdbcTemplate, databaseName,
                                        databaseMajorVersion,
                                        databaseMinorVersion, mediaSourceInfo.getType());
                                if (dialect == null) {
                                    throw new UnsupportedOperationException("no dialect for" + databaseName);
                                }

                                if (logger.isInfoEnabled()) {
                                    logger.info(String.format("--- DATABASE: %s, SCHEMA: %s ---",
                                            databaseName,
                                            (dialect.getDefaultSchema() == null) ? dialect.getDefaultCatalog() : dialect.getDefaultSchema()));
                                }

                                return dialect;
                            }
                        });
            }
        });

    }

    private static DbDialect generate(JdbcTemplate jdbcTemplate, String databaseName, int databaseMajorVersion,
                                      int databaseMinorVersion, MediaSourceType sourceType) {
        DbDialect dialect;

        if (sourceType == MediaSourceType.ORACLE) {
            dialect = new OracleDialect(jdbcTemplate, defaultLobHandler, databaseName, databaseMajorVersion,
                    databaseMinorVersion);
        } else if (sourceType == MediaSourceType.MYSQL) {
            dialect = new MysqlDialect(jdbcTemplate, defaultLobHandler, databaseName, databaseMajorVersion,
                    databaseMinorVersion);
        } else if (sourceType == MediaSourceType.SDDL) {
            dialect = new MysqlDialect(jdbcTemplate, defaultLobHandler, databaseName, databaseMajorVersion,
                    databaseMinorVersion) {
                @Override
                public boolean isGenerateSqlWithSchema() {
                    return false;//sddl类型的数据源schema是通配符，所以针对sddl数据库生成sql是不能带schema
                }
            };
        } else if (sourceType == MediaSourceType.SQLSERVER) {
            dialect = new SqlServerDialect(jdbcTemplate, defaultLobHandler, databaseName, databaseMajorVersion,
                    databaseMinorVersion);
        } else if (sourceType == MediaSourceType.POSTGRESQL) {
            dialect = new PostgreSqlDialect(jdbcTemplate, defaultLobHandler, databaseName, databaseMajorVersion,
                    databaseMinorVersion);
        } else {
            throw new DatalinkException(
                    String.format("invalid Media Source Type %s,can not generate the dialect.", sourceType));
        }

        return dialect;
    }

    public static DbDialect getDbDialect(MediaSourceInfo mediaSourceInfo) {
        return dialects.getUnchecked(mediaSourceInfo);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        DbDialect dbDialect = dialects.getIfPresent(mediaSourceInfo);
        if (dbDialect != null) {
            dialects.invalidate(mediaSourceInfo);
        }
    }
}
