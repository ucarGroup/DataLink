package com.ucar.datalink.worker.api.util.dialect;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.biz.utils.ddl.DdlUtilsFilter;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.ModeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * DbDialect抽象类
 * Created by lubiao on 2017/3/8.
 */
public abstract class AbstractDbDialect implements DbDialect {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractDbDialect.class);
    protected int databaseMajorVersion;
    protected int databaseMinorVersion;
    protected String databaseName;
    protected SqlTemplate sqlTemplate;
    protected JdbcTemplate jdbcTemplate;
    protected TransactionTemplate transactionTemplate;
    protected LobHandler lobHandler;
    protected LoadingCache<List<String>, Table> tables;

    public AbstractDbDialect(final JdbcTemplate jdbcTemplate, LobHandler lobHandler) {
        this.jdbcTemplate = jdbcTemplate;
        this.lobHandler = lobHandler;
        // 初始化transction
        this.transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // 初始化一些数据
        jdbcTemplate.execute(new ConnectionCallback() {

            public Object doInConnection(Connection c) throws SQLException, DataAccessException {
                DatabaseMetaData meta = c.getMetaData();
                databaseName = meta.getDatabaseProductName();
                databaseMajorVersion = meta.getDatabaseMajorVersion();
                databaseMinorVersion = meta.getDatabaseMinorVersion();

                return null;
            }
        });

        initTables(jdbcTemplate);
    }

    public AbstractDbDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                             int minorVersion) {
        this.jdbcTemplate = jdbcTemplate;
        this.lobHandler = lobHandler;
        // 初始化transction
        this.transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        this.databaseName = name;
        this.databaseMajorVersion = majorVersion;
        this.databaseMinorVersion = minorVersion;

        initTables(jdbcTemplate);
    }

    public Table findTable(String schema, String table, boolean useCache) {
        List<String> key = Arrays.asList(schema, table);
        if (!useCache) {
            tables.invalidate(key);
        }

        return tables.getUnchecked(key);
    }

    public Table findTable(String schema, String table) {
        return findTable(schema, table, true);
    }

    public void reloadTable(String schema, String table) {
        if (StringUtils.isNotEmpty(table)) {
            tables.invalidate(Arrays.asList(schema, table));
        } else {
            // 如果没有存在表名，则直接清空所有的table，重新加载
            tables.invalidateAll();
        }
    }

    public String getName() {
        return databaseName;
    }

    public int getMajorVersion() {
        return databaseMajorVersion;
    }

    @Override
    public int getMinorVersion() {
        return databaseMinorVersion;
    }

    public String getVersion() {
        return databaseMajorVersion + "." + databaseMinorVersion;
    }

    public LobHandler getLobHandler() {
        return lobHandler;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

    public void destory() {
    }

    // ================================ helper method ==========================

    private void initTables(final JdbcTemplate jdbcTemplate) {
        this.tables = CacheBuilder.newBuilder().softValues().build(new CacheLoader<List<String>, Table>() {
            @Override
            public Table load(List<String> names) throws Exception {
                Assert.isTrue(names.size() == 2);
                try {
                    beforeFindTable(jdbcTemplate, names.get(0), names.get(0), names.get(1));
                    DdlUtilsFilter filter = getDdlUtilsFilter(jdbcTemplate, names.get(0), names.get(0), names.get(1));
                    Table table = DdlUtils.findTable(
                            jdbcTemplate,
                            getActualSchemaName(names.get(0)),
                            isGetTablesWithSchema() ? getActualSchemaName(names.get(0)) : null,
                            names.get(1),
                            filter);
                    afterFindTable(table, jdbcTemplate, names.get(0), names.get(0), names.get(1));
                    if (table == null) {
                        throw new NestableRuntimeException("no found table [" + names.get(0) + "." + names.get(1)
                                + "] , pls check");
                    } else {
                        return table;
                    }
                } catch (Exception e) {
                    throw new NestableRuntimeException("find table [" + names.get(0) + "." + names.get(1) + "] error",
                            e);
                }
            }
        });

    }

    protected DdlUtilsFilter getDdlUtilsFilter(JdbcTemplate jdbcTemplate, String catalogName, String schemaName, String tableName) {
        // we need to return null for backward compatibility
        return null;
    }

    protected void beforeFindTable(JdbcTemplate jdbcTemplate, String catalogName, String schemaName, String tableName) {

    }

    protected void afterFindTable(Table table, JdbcTemplate jdbcTemplate, String catalogName, String schemaName, String tableName) {

    }

    private String getActualSchemaName(String schemaName) {
        if (StringUtils.isBlank(schemaName)) {
            return schemaName;
        }

        MediaInfo.ModeValue modeValue = ModeUtils.parseMode(schemaName);
        if (modeValue.getMode().isMulti()) {
            return modeValue.getMultiValue().get(0);//针对分库分表的场景，获取第一个库的信息使用即可
        }
        return schemaName;
    }
}
