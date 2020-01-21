package com.ucar.datalink.flinker.plugin.rdbms.reader.util;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.rdbms.reader.Constant;
import com.ucar.datalink.flinker.plugin.rdbms.reader.Key;
import com.ucar.datalink.flinker.plugin.rdbms.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SingleTableSplitUtil {
    private static final Logger LOG = LoggerFactory
            .getLogger(SingleTableSplitUtil.class);

    public static DataBaseType DATABASE_TYPE;

    private SingleTableSplitUtil() {
    }

    public static List<Configuration> splitSingleTable(
    Configuration configuration, int adviceNum) {
        LOG.info("work in split single table, do getPkRange()");
        List<Configuration> pluginParams = new ArrayList<Configuration>();

        Pair<Object, Object> minMaxPK = getPkRange(configuration);

        if (null == minMaxPK) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "根据切分主键切分表失败. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
        }

        String splitPkName = configuration.getString(Key.SPLIT_PK);
        String column = configuration.getString(Key.COLUMN);
        String table = configuration.getString(Key.TABLE);
        String where = configuration.getString(Key.WHERE, null);

        boolean hasWhere = StringUtils.isNotBlank(where);

        configuration.set(Key.QUERY_SQL, buildQuerySql(column, table, where));
        if (null == minMaxPK.getLeft() || null == minMaxPK.getRight()) {
            // 切分后获取到的start/end 有 Null 的情况
            pluginParams.add(configuration);
            return pluginParams;
        }

        boolean isStringType = Constant.PK_TYPE_STRING.equals(configuration
                .getString(Constant.PK_TYPE));
        boolean isLongType = Constant.PK_TYPE_LONG.equals(configuration
                .getString(Constant.PK_TYPE));

        List<String> rangeList;
        if (isStringType) {
            LOG.info("current type is string");
            rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                    String.valueOf(minMaxPK.getLeft()),
                    String.valueOf(minMaxPK.getRight()), adviceNum,
                    splitPkName, "'", DATABASE_TYPE);
            LOG.info("range list->"+rangeList.toString());
        } else if (isLongType) {
            LOG.info("current type is long");
            rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                    new BigInteger(minMaxPK.getLeft().toString()),
                    new BigInteger(minMaxPK.getRight().toString()),
                    adviceNum, splitPkName);
            LOG.info("range list->"+rangeList.toString());
        } else {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
        }

        String tempQuerySql;
        List<String> allQuerySql = new ArrayList<String>();

        if (null != rangeList) {
            for (String range : rangeList) {
                Configuration tempConfig = configuration.clone();

                tempQuerySql = buildQuerySql(column, table, where)
                        + (hasWhere ? " and " : " where ") + range;

                allQuerySql.add(tempQuerySql);
                LOG.info("temp Query Sql -> "+tempQuerySql);
                tempConfig.set(Key.QUERY_SQL, tempQuerySql);
                pluginParams.add(tempConfig);
            }
        } else {
            pluginParams.add(configuration);
        }

        // deal pk is null
        Configuration tempConfig = configuration.clone();
        tempQuerySql = buildQuerySql(column, table, where)
                + (hasWhere ? " and " : " where ")
                + String.format(" %s IS NULL", splitPkName);

        allQuerySql.add(tempQuerySql);

        LOG.info("After split(), allQuerySql=[\n{}\n].",
                StringUtils.join(allQuerySql, "\n"));

        tempConfig.set(Key.QUERY_SQL, tempQuerySql);
        pluginParams.add(tempConfig);

        return pluginParams;
    }

    public static String buildQuerySql(String column, String table,
                                          String where) {
        String querySql;

        if (StringUtils.isBlank(where)) {
            querySql = String.format(Constant.QUERY_SQL_TEMPLATE_WITHOUT_WHERE,
                    column, table);
        } else {
            querySql = String.format(Constant.QUERY_SQL_TEMPLATE, column,
                    table, where);
        }

        return querySql;
    }

    @SuppressWarnings("resource")
    private static Pair<Object, Object> getPkRange(Configuration configuration) {
        String pkRangeSQL = genPKRangeSQL(configuration);

        int fetchSize = configuration.getInt(Constant.FETCH_SIZE);
        String jdbcURL = configuration.getString(Key.JDBC_URL);
        String username = configuration.getString(Key.USERNAME);
        String password = configuration.getString(Key.PASSWORD);
        String table = configuration.getString(Key.TABLE);

        Connection conn = DBUtil.getConnection(DATABASE_TYPE, jdbcURL, username, password);
        Pair<Object, Object> minMaxPK = checkSplitPk(conn, pkRangeSQL, fetchSize, table, username, configuration);
        DBUtil.closeDBResources(null, null, conn);
        return minMaxPK;
    }

    public static void precheckSplitPk(Connection conn, String pkRangeSQL, int fetchSize,
                                                       String table, String username) {
        Pair<Object, Object> minMaxPK = checkSplitPk(conn, pkRangeSQL, fetchSize, table, username, null);
        if (null == minMaxPK) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "根据切分主键切分表失败. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
        }
    }

    /**
     * 检测splitPk的配置是否正确。
     * configuration为null, 是precheck的逻辑，不需要回写PK_TYPE到configuration中
     *
     */
    private static Pair<Object, Object> checkSplitPk(Connection conn, String pkRangeSQL, int fetchSize,  String table,
                                                     String username, Configuration configuration) {
        ResultSet rs = null;
        Pair<Object, Object> minMaxPK = null;
        try {
            try {
                LOG.info("DBUtil.query(),pkRangeSQL->"+pkRangeSQL+"  fetchSize->"+fetchSize);
                rs = DBUtil.query(conn, pkRangeSQL, fetchSize);
            }catch (Exception e) {
                throw RdbmsException.asQueryException(DATABASE_TYPE, e, pkRangeSQL,table,username);
            }
            ResultSetMetaData rsMetaData = rs.getMetaData();
            if (isPKTypeValid(rsMetaData)) {
                if (isStringType(rsMetaData.getColumnType(1))) {
                    if(configuration != null) {
                        configuration
                                .set(Constant.PK_TYPE, Constant.PK_TYPE_STRING);
                    }
                    while (DBUtil.asyncResultSetNext(rs)) {
                        minMaxPK = new ImmutablePair<Object, Object>(
                                rs.getString(1), rs.getString(2));
                    }
                } else if (isLongType(rsMetaData.getColumnType(1))) {
                    if(configuration != null) {
                        configuration.set(Constant.PK_TYPE, Constant.PK_TYPE_LONG);
                    }

                    while (DBUtil.asyncResultSetNext(rs)) {
                        minMaxPK = new ImmutablePair<Object, Object>(
                                rs.getString(1), rs.getString(2));

                        // check: string shouldn't contain '.', for oracle
                        String minMax = rs.getString(1) + rs.getString(2);
                        if (StringUtils.contains(minMax, '.')) {
                            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                                    "您配置的DataX切分主键(splitPk)有误. 因为您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
                        }
                    }
                } else {
                    throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                            "您配置的DataX切分主键(splitPk)有误. 因为您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
                }
            } else {
                throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                        "您配置的DataX切分主键(splitPk)有误. 因为您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
            }
        } catch(DataXException e) {
            throw e;
        } catch (Exception e) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK, "DataX尝试切分表发生错误. 请检查您的配置并作出修改.", e);
        } finally {
            DBUtil.closeDBResources(rs, null, null);
        }

        return minMaxPK;
    }

    private static boolean isPKTypeValid(ResultSetMetaData rsMetaData) {
        boolean ret = false;
        try {
            int minType = rsMetaData.getColumnType(1);
            int maxType = rsMetaData.getColumnType(2);

            boolean isNumberType = isLongType(minType);

            boolean isStringType = isStringType(minType);

            if (minType == maxType && (isNumberType || isStringType)) {
                ret = true;
            }
        } catch (Exception e) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "DataX获取切分主键(splitPk)字段类型失败. 该错误通常是系统底层异常导致. 请联系旺旺:askdatax或者DBA处理.");
        }
        return ret;
    }

    // warn: Types.NUMERIC is used for oracle! because oracle use NUMBER to
    // store INT, SMALLINT, INTEGER etc, and only oracle need to concern
    // Types.NUMERIC
    private static boolean isLongType(int type) {
        boolean isValidLongType = type == Types.BIGINT || type == Types.INTEGER
                || type == Types.SMALLINT || type == Types.TINYINT;

        switch (SingleTableSplitUtil.DATABASE_TYPE) {
            case Oracle:
                isValidLongType |= type == Types.NUMERIC;
                break;
            default:
                break;
        }
        return isValidLongType;
    }

    private static boolean isStringType(int type) {
        return type == Types.CHAR || type == Types.NCHAR
                || type == Types.VARCHAR || type == Types.LONGVARCHAR
                || type == Types.NVARCHAR;
    }

    private static String genPKRangeSQL(Configuration configuration) {

        String splitPK = configuration.getString(Key.SPLIT_PK).trim();
        String table = configuration.getString(Key.TABLE).trim();
        String where = configuration.getString(Key.WHERE, null);
        return genPKSql(splitPK,table,where);
    }

    public static String genPKSql(String splitPK, String table, String where){

        String minMaxTemplate = "SELECT MIN(%s),MAX(%s) FROM %s";
        String pkRangeSQL = String.format(minMaxTemplate, splitPK, splitPK,
                table);
        if (StringUtils.isNotBlank(where)) {
            pkRangeSQL = String.format("%s WHERE (%s AND %s IS NOT NULL)",
                    pkRangeSQL, where, splitPK);
        }
        return pkRangeSQL;
    }


}