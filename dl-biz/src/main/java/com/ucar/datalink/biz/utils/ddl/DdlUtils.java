package com.ucar.datalink.biz.utils.ddl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.ddlutils.model.*;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.*;

/**
 * 参考自Alibaba-Otter
 * Created by lubiao on 2017/3/8.
 */
public class DdlUtils {

    private static final Logger logger = LoggerFactory.getLogger(DdlUtils.class);
    private static TableType[] SUPPORTED_TABLE_TYPES = new TableType[]{TableType.view, TableType.table};
    private final static Map<Integer, String> _defaultSizes = new HashMap<Integer, String>();

    static {
        _defaultSizes.put(new Integer(1), "254");
        _defaultSizes.put(new Integer(12), "254");
        _defaultSizes.put(new Integer(-1), "254");
        _defaultSizes.put(new Integer(-2), "254");
        _defaultSizes.put(new Integer(-3), "254");
        _defaultSizes.put(new Integer(-4), "254");
        _defaultSizes.put(new Integer(4), "32");
        _defaultSizes.put(new Integer(-5), "64");
        _defaultSizes.put(new Integer(7), "7,0");
        _defaultSizes.put(new Integer(6), "15,0");
        _defaultSizes.put(new Integer(8), "15,0");
        _defaultSizes.put(new Integer(3), "15,15");
        _defaultSizes.put(new Integer(2), "15,15");
    }

    /**
     * !!! Only supports MySQL
     */
    @SuppressWarnings("unchecked")
    public static List<String> findSchemas(JdbcTemplate jdbcTemplate, final String schemaPattern) {
        try {
            if (StringUtils.isEmpty(schemaPattern)) {
                return jdbcTemplate.query("show databases", new SingleColumnRowMapper(String.class));
            }
            return jdbcTemplate.query("show databases like ?",
                    new Object[]{schemaPattern},
                    new SingleColumnRowMapper(String.class));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<String>();
        }
    }

    /**
     * !!! Only supports MySQL
     */
    public static List<String> findSchemas(JdbcTemplate jdbcTemplate, final String schemaPattern,
                                           final DdlSchemaFilter ddlSchemaFilter) {
        List<String> schemas = findSchemas(jdbcTemplate, schemaPattern);
        if (ddlSchemaFilter == null) {
            return schemas;
        }
        List<String> filterSchemas = new ArrayList<>();
        for (String schema : schemas) {
            if (ddlSchemaFilter.accept(schema)) {
                filterSchemas.add(schema);
            }
        }
        return filterSchemas;
    }

    public static Table findTable(JdbcTemplate jdbcTemplate, final String catalogName, final String schemaName,
                                  final String tableName) throws Exception {
        return findTable(jdbcTemplate, catalogName, schemaName, tableName, null);
    }

    public static Table findTable(final JdbcTemplate jdbcTemplate, final String catalogName, final String schemaName,
                                  final String tableName, final DdlUtilsFilter filter) throws Exception {
        return (Table) jdbcTemplate.execute(new ConnectionCallback() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                Table table = null;
                DatabaseMetaDataWrapper metaDataWrapper = new DatabaseMetaDataWrapper();

                try {
                    if (filter != null) {
                        con = filter.filterConnection(con);
                        Assert.notNull(con);
                    }
                    DatabaseMetaData databaseMetaData = con.getMetaData();
                    if (filter != null) {
                        databaseMetaData = filter.filterDataBaseMetaData(jdbcTemplate, con, databaseMetaData);
                        Assert.notNull(databaseMetaData);
                    }

                    metaDataWrapper.setMetaData(databaseMetaData);
                    metaDataWrapper.setTableTypes(TableType.toStrings(SUPPORTED_TABLE_TYPES));
                    metaDataWrapper.setCatalog(catalogName);
                    metaDataWrapper.setSchemaPattern(schemaName);

                    String convertTableName = tableName;
                    if (databaseMetaData.storesUpperCaseIdentifiers()) {
                        metaDataWrapper.setCatalog(catalogName.toUpperCase());
                        metaDataWrapper.setSchemaPattern(StringUtils.isBlank(schemaName) ? null : schemaName.toUpperCase());
                        convertTableName = tableName.toUpperCase();
                    }
                    if (databaseMetaData.storesLowerCaseIdentifiers()) {
                        metaDataWrapper.setCatalog(catalogName.toLowerCase());
                        metaDataWrapper.setSchemaPattern(StringUtils.isBlank(schemaName) ? null : schemaName.toLowerCase());
                        convertTableName = tableName.toLowerCase();
                    }

                    ResultSet tableResultSet = null;
                    try {
                        tableResultSet = metaDataWrapper.getTables(convertTableName);

                        while ((tableResultSet != null) && tableResultSet.next()) {
                            Map<String, Object> values = readColumns(tableResultSet, getDescriptorsForTable());
                            table = generateTable(metaDataWrapper, values);
                            if (table.getName().equalsIgnoreCase(tableName)) {
                                break;
                            }
                        }
                    } finally {
                        JdbcUtils.closeResultSet(tableResultSet);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(table);

                return table;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Table> findTables(final JdbcTemplate jdbcTemplate, final String catalogName,
                                         final String schemaName, final String tableNamePattern,
                                         final DdlUtilsFilter filter, final DdlTableNameFilter tableNameFilter)
            throws Exception {
        return (List<Table>) jdbcTemplate.execute(new ConnectionCallback() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                List<Table> tables = new ArrayList<Table>();
                DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();

                try {
                    if (filter != null) {
                        con = filter.filterConnection(con);
                        Assert.notNull(con);
                    }
                    DatabaseMetaData databaseMetaData = con.getMetaData();
                    if (filter != null) {
                        databaseMetaData = filter.filterDataBaseMetaData(jdbcTemplate, con, databaseMetaData);
                        Assert.notNull(databaseMetaData);
                    }

                    metaData.setMetaData(databaseMetaData);
                    metaData.setTableTypes(TableType.toStrings(SUPPORTED_TABLE_TYPES));
                    metaData.setCatalog(catalogName);
                    metaData.setSchemaPattern(schemaName);

                    String convertTableName = tableNamePattern;
                    if (databaseMetaData.storesUpperCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toUpperCase());
                        metaData.setSchemaPattern(schemaName.toUpperCase());
                        convertTableName = tableNamePattern.toUpperCase();
                    }
                    if (databaseMetaData.storesLowerCaseIdentifiers()) {
                        metaData.setCatalog(catalogName.toLowerCase());
                        metaData.setSchemaPattern(schemaName.toLowerCase());
                        convertTableName = tableNamePattern.toLowerCase();
                    }

                    ResultSet tableData = null;
                    try {
                        tableData = metaData.getTables(convertTableName);

                        while ((tableData != null) && tableData.next()) {
                            Map<String, Object> values = readColumns(tableData, getDescriptorsForTable());

                            Table table = generateTable(metaData, values);
                            if ((tableNameFilter == null)
                                    || tableNameFilter.accept(catalogName, schemaName, table.getName())) {
                                tables.add(table);
                            }
                        }
                    } finally {
                        JdbcUtils.closeResultSet(tableData);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                for (Table table : tables) {
                    makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(table);
                }

                return tables;
            }
        });
    }

    /**
     * Treat tables with no primary keys as a table with all primary keys.
     */
    private static void makeAllColumnsPrimaryKeysIfNoPrimaryKeysFound(Table table) {
        if ((table != null) && (table.getPrimaryKeyColumns() != null) && (table.getPrimaryKeyColumns().length == 0)) {
            Column[] allCoumns = table.getColumns();

            for (Column column : allCoumns) {
                column.setPrimaryKey(true);
            }
        }
    }

    private static Table generateTable(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
        String tableName = (String) values.get("TABLE_NAME");
        Table table = null;

        if ((tableName != null) && (tableName.length() > 0)) {
            table = new Table();
            table.setName(tableName);
            table.setType((String) values.get("TABLE_TYPE"));
            table.setCatalog((String) values.get("TABLE_CAT"));
            table.setSchema((String) values.get("TABLE_SCHEM"));
            table.setDescription((String) values.get("REMARKS"));
            table.addColumns(generateColumns(metaData, tableName));
            table.addIndices(generateIndices(metaData, tableName));

            Collection<String> primaryKeys = readPrimaryKeyNames(metaData, tableName);

            for (Object key : primaryKeys) {
                Column col = table.findColumn((String) key, true);

                if (col != null) {
                    col.setPrimaryKey(true);
                } else {
                    throw new NullPointerException(String.format("%s pk %s is null - %s %s",
                            tableName,
                            key,
                            ToStringBuilder.reflectionToString(metaData, ToStringStyle.SIMPLE_STYLE),
                            ToStringBuilder.reflectionToString(values, ToStringStyle.SIMPLE_STYLE)));
                }
            }
        }

        return table;
    }

    private static List<MetaDataColumnDescriptor> getDescriptorsForTable() {
        List<MetaDataColumnDescriptor> result = new ArrayList<>();

        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_TYPE", Types.VARCHAR, "UNKNOWN"));
        result.add(new MetaDataColumnDescriptor("TABLE_CAT", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_SCHEM", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("REMARKS", Types.VARCHAR));

        return result;
    }

    private static List<MetaDataColumnDescriptor> getDescriptorsForColumn() {
        List<MetaDataColumnDescriptor> result = new ArrayList<>();

        // As suggested by Alexandre Borgoltz, we're reading the COLUMN_DEF
        // first because Oracle
        // has problems otherwise (it seemingly requires a LONG column to be the
        // first to be read)
        // See also DDLUTILS-29
        result.add(new MetaDataColumnDescriptor("COLUMN_DEF", Types.VARCHAR));

        // we're also reading the table name so that a model reader impl can
        // filter manually
        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("COLUMN_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TYPE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("DATA_TYPE", Types.INTEGER, new Integer(Types.OTHER)));
        result.add(new MetaDataColumnDescriptor("NUM_PREC_RADIX", Types.INTEGER, new Integer(10)));
        result.add(new MetaDataColumnDescriptor("DECIMAL_DIGITS", Types.INTEGER, new Integer(0)));
        result.add(new MetaDataColumnDescriptor("COLUMN_SIZE", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("IS_NULLABLE", Types.VARCHAR, "YES"));
        result.add(new MetaDataColumnDescriptor("IS_AUTOINCREMENT", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("REMARKS", Types.VARCHAR));

        return result;
    }

    private static List<MetaDataColumnDescriptor> getDescriptorsForIndex() {
        List<MetaDataColumnDescriptor> result = new ArrayList<>();
        result.add(new MetaDataColumnDescriptor("TABLE_CAT", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_SCHEM", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("NON_UNIQUE", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("INDEX_QUALIFIER", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("INDEX_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("TYPE", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("ORDINAL_POSITION", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("COLUMN_NAME", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("ASC_OR_DESC", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("CARDINALITY", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("PAGES", Types.VARCHAR));
        result.add(new MetaDataColumnDescriptor("FILTER_CONDITION", Types.VARCHAR));
        return result;
    }

    private static List<MetaDataColumnDescriptor> getDescriptorsForPK() {
        List<MetaDataColumnDescriptor> result = new ArrayList<>();

        result.add(new MetaDataColumnDescriptor("COLUMN_NAME", Types.VARCHAR));

        // we're also reading the table name so that a model reader impl can
        // filter manually
        result.add(new MetaDataColumnDescriptor("TABLE_NAME", Types.VARCHAR));

        // the name of the primary key is currently only interesting to the pk
        // index name resolution
        result.add(new MetaDataColumnDescriptor("PK_NAME", Types.VARCHAR));

        return result;
    }

    private static List<Column> generateColumns(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException {
        ResultSet columnsResultSet = null;

        try {
            columnsResultSet = metaData.getColumns(tableName, null);

            List<Column> columns = new ArrayList<>();
            Map<String, Object> values;

            for (; columnsResultSet.next(); columns.add(generateOneColumn(metaData, values))) {
                Map<String, Object> tmp = readColumns(columnsResultSet, getDescriptorsForColumn());
                if (tableName.equalsIgnoreCase((String) tmp.get("TABLE_NAME"))) {
                    values = tmp;
                } else {
                    break;
                }
            }

            return columns;
        } finally {
            JdbcUtils.closeResultSet(columnsResultSet);
        }
    }

    private static Column generateOneColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
        Column column = new Column();

        column.setName((String) values.get("COLUMN_NAME"));
        column.setDefaultValue((String) values.get("COLUMN_DEF"));

        Integer typeCode = (Integer) values.get("DATA_TYPE");
        if (typeCode == -9) {
            typeCode = 12;//特殊处理，如果typeCode是-9(nvarchar类型)，将typeCode转换为12(varchar),因为apache ddlutils不支持nvarchar类型
        }
        column.setTypeCode(typeCode);


        String typeName = (String) values.get("TYPE_NAME");
        if ((typeName != null) && typeName.startsWith("TIMESTAMP")) {
            column.setTypeCode(Types.TIMESTAMP);
        }
        // modify 2013-09-25，处理下unsigned
        if ((typeName != null) && StringUtils.containsIgnoreCase(typeName, "UNSIGNED")) {
            // 如果为unsigned，往上调大一个量级，避免数据溢出
            switch (column.getTypeCode()) {
                case Types.TINYINT:
                    column.setTypeCode(Types.SMALLINT);
                    break;
                case Types.SMALLINT:
                    column.setTypeCode(Types.INTEGER);
                    break;
                case Types.INTEGER:
                    column.setTypeCode(Types.BIGINT);
                    break;
                case Types.BIGINT:
                    //之前的逻辑是为了怕溢出所以故意调高了一个量级，将bigint -> decimal 这样不会溢出
                    //这里改回来，还是将 BIGINT UNSIGNED -> BIGINT
                    column.setTypeCode(Types.BIGINT);
                    break;
                default:
                    break;
            }
        }

        Integer precision = (Integer) values.get("NUM_PREC_RADIX");

        if (precision != null) {
            column.setPrecisionRadix(precision.intValue());
        }

        String size = (String) values.get("COLUMN_SIZE");

        if (size == null) {
            size = (String) _defaultSizes.get(new Integer(column.getTypeCode()));
        }

        // we're setting the size after the precision and radix in case
        // the database prefers to return them in the size value
        column.setSize(size);

        int scale = 0;
        Object dec_digits = values.get("DECIMAL_DIGITS");

        if (dec_digits instanceof String) {
            scale = (dec_digits == null) ? 0 : NumberUtils.toInt(dec_digits.toString());
        } else if (dec_digits instanceof Integer) {
            scale = (dec_digits == null) ? 0 : (Integer) dec_digits;
        }

        if (scale != 0) {
            column.setScale(scale);
        }

        column.setRequired("NO".equalsIgnoreCase(((String) values.get("IS_NULLABLE")).trim()));
        column.setAutoIncrement("YES".equalsIgnoreCase(((String) values.get("IS_AUTOINCREMENT")).trim()));
        column.setDescription((String) values.get("REMARKS"));
        return column;
    }

    private static Map<String, Object> readColumns(ResultSet resultSet, List<MetaDataColumnDescriptor> columnDescriptors)
            throws SQLException {
        printMetaData(resultSet);

        Map<String, Object> values = new HashMap<>();
        for (MetaDataColumnDescriptor descriptor : columnDescriptors) {
            values.put(descriptor.getName(), descriptor.readColumn(resultSet));
        }
        return values;
    }

    private static Collection<String> readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName)
            throws SQLException {
        ResultSet pkData = null;

        try {
            List<String> pks = new ArrayList<>();
            Map<String, Object> values;

            for (pkData = metaData.getPrimaryKeys(tableName); pkData.next(); pks.add(readPrimaryKeyName(metaData,
                    values))) {
                values = readColumns(pkData, getDescriptorsForPK());
            }

            return pks;
        } finally {
            JdbcUtils.closeResultSet(pkData);
        }
    }

    private static String readPrimaryKeyName(DatabaseMetaDataWrapper metaData, Map<String, Object> values)
            throws SQLException {
        return (String) values.get("COLUMN_NAME");
    }

    private static List<Index> generateIndices(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException {
        List<Index> indexes = new ArrayList<>();
        ResultSet indicesResultSet = null;

        try {
            indicesResultSet = metaData.getIndices(tableName, true, false);
            Map<String, Object> values;
            for (; indicesResultSet.next(); generateOneIndex(metaData, values, indexes)) {
                Map<String, Object> tmp = readColumns(indicesResultSet, getDescriptorsForIndex());
                if (tableName.equalsIgnoreCase((String) tmp.get("TABLE_NAME"))) {
                    values = tmp;
                } else {
                    break;
                }
            }
        } finally {
            JdbcUtils.closeResultSet(indicesResultSet);
        }

        return indexes;
    }

    private static void generateOneIndex(DatabaseMetaDataWrapper metaData, Map<String, Object> values, List<Index> indexes) throws SQLException {
        if (StringUtils.isBlank((String) values.get("INDEX_NAME"))) {
            //sqlserver数据库取到的元数据中有INDEX_NAME为空的情况，此处做一下过滤
            return;
        }

        Optional<Index> optional = indexes.stream().filter(i -> i.getName().equals((String) values.get("INDEX_NAME"))).findFirst();
        IndexColumn column = new IndexColumn();
        column.setName((String) values.get("COLUMN_NAME"));
        column.setOrdinalPosition(Integer.valueOf((String) values.get("ORDINAL_POSITION")));

        if (optional.isPresent()) {
            Index index = optional.get();
            index.addColumn(column);
        } else {
            String nonUnique = (String) values.get("NON_UNIQUE");
            if ("false".equals(nonUnique)) {
                UniqueIndex uniqueIndex = new UniqueIndex();
                uniqueIndex.setName((String) values.get("INDEX_NAME"));
                uniqueIndex.addColumn(column);
                indexes.add(uniqueIndex);
            } else {
                NonUniqueIndex nonUniqueIndex = new NonUniqueIndex();
                nonUniqueIndex.setName((String) values.get("INDEX_NAME"));
                nonUniqueIndex.addColumn(column);
                indexes.add(nonUniqueIndex);
            }
        }
    }

    private static void printMetaData(ResultSet resultSet) {
        if (logger.isDebugEnabled()) {
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                logger.debug("Print Columns Info");
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    logger.debug(metaData.getColumnName(i) + "  " + metaData.getColumnTypeName(i) + " " + resultSet.getString(i));
                }
            } catch (Exception e) {
                logger.debug("Something goew wrong when print meta data.", e);
            }
        }
    }
}
