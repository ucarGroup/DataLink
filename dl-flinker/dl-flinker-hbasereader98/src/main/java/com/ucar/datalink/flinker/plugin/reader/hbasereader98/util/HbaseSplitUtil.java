package com.ucar.datalink.flinker.plugin.reader.hbasereader98.util;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HTableManager;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HbaseReaderErrorCode;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Key;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class HbaseSplitUtil {
    private final static Logger LOG = LoggerFactory.getLogger(HbaseSplitUtil.class);


    public static List<Configuration> split(Configuration configuration) {
        byte[] startRowkeyByte = HbaseUtil.convertUserStartRowkey(configuration);
        byte[] endRowkeyByte = HbaseUtil.convertUserEndRowkey(configuration);

			/* 如果用户配置了 startRowkey 和 endRowkey，需要确保：startRowkey <= endRowkey */
        if (startRowkeyByte.length != 0 && endRowkeyByte.length != 0
                && Bytes.compareTo(startRowkeyByte, endRowkeyByte) > 0) {
            throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE, "Hbasereader 中 startRowkey 不得大于 endRowkey.");
        }

        HTable htable = HbaseUtil.initHtable(configuration);

        List<Configuration> resultConfigurations;

        try {
            Pair<byte[][], byte[][]> regionRanges = htable.getStartEndKeys();
            if (null == regionRanges) {
                throw DataXException.asDataXException(HbaseReaderErrorCode.SPLIT_ERROR, "获取源头 Hbase 表的 rowkey 范围失败.");
            }

            resultConfigurations = HbaseSplitUtil.doSplit(configuration, startRowkeyByte, endRowkeyByte,
                    regionRanges);

            LOG.info("HBaseReader split job into {} tasks.", resultConfigurations.size());

            return resultConfigurations;
        } catch (Exception e) {
            throw DataXException.asDataXException(HbaseReaderErrorCode.SPLIT_ERROR, "切分源头 Hbase 表失败.", e);
        } finally {
            try {
                HTableManager.closeHTable(htable);
            } catch (Exception e) {
                //
            }
        }
    }


    private static List<Configuration> doSplit(Configuration config, byte[] startRowkeyByte,
                                               byte[] endRowkeyByte, Pair<byte[][], byte[][]> regionRanges) {

        List<Configuration> configurations = new ArrayList<Configuration>();

        for (int i = 0; i < regionRanges.getFirst().length; i++) {

            byte[] regionStartKey = regionRanges.getFirst()[i];
            byte[] regionEndKey = regionRanges.getSecond()[i];

            // 当前的region为最后一个region
            // 如果最后一个region的start Key大于用户指定的userEndKey,则最后一个region，应该不包含在内
            // 注意如果用户指定userEndKey为"",则此判断应该不成立。userEndKey为""表示取得最大的region
            if (Bytes.compareTo(regionEndKey, HConstants.EMPTY_BYTE_ARRAY) == 0
                    && (endRowkeyByte.length != 0 && (Bytes.compareTo(
                    regionStartKey, endRowkeyByte) > 0))) {
                continue;
            }

            // 如果当前的region不是最后一个region，
            // 用户配置的userStartKey大于等于region的endkey,则这个region不应该含在内
            if ((Bytes.compareTo(regionEndKey, HConstants.EMPTY_BYTE_ARRAY) != 0)
                    && (Bytes.compareTo(startRowkeyByte, regionEndKey) >= 0)) {
                continue;
            }

            // 如果用户配置的userEndKey小于等于 region的startkey,则这个region不应该含在内
            // 注意如果用户指定的userEndKey为"",则次判断应该不成立。userEndKey为""表示取得最大的region
            if (endRowkeyByte.length != 0
                    && (Bytes.compareTo(endRowkeyByte, regionStartKey) <= 0)) {
                continue;
            }

            Configuration p = config.clone();

            String thisStartKey = getStartKey(startRowkeyByte, regionStartKey);

            String thisEndKey = getEndKey(endRowkeyByte, regionEndKey);

            p.set(Key.START_ROWKEY, thisStartKey);
            p.set(Key.END_ROWKEY, thisEndKey);

            LOG.debug("startRowkey:[{}], endRowkey:[{}] .", thisStartKey, thisEndKey);

            configurations.add(p);
        }

        return configurations;
    }

    private static String getEndKey(byte[] endRowkeyByte, byte[] regionEndKey) {
        if (endRowkeyByte == null) {// 由于之前处理过，所以传入的userStartKey不可能为null
            throw new IllegalArgumentException("userEndKey should not be null!");
        }

        byte[] tempEndRowkeyByte;

        if (endRowkeyByte.length == 0) {
            tempEndRowkeyByte = regionEndKey;
        } else if (Bytes.compareTo(regionEndKey, HConstants.EMPTY_BYTE_ARRAY) == 0) {
            // 为最后一个region
            tempEndRowkeyByte = endRowkeyByte;
        } else {
            if (Bytes.compareTo(endRowkeyByte, regionEndKey) > 0) {
                tempEndRowkeyByte = regionEndKey;
            } else {
                tempEndRowkeyByte = endRowkeyByte;
            }
        }

        return Bytes.toStringBinary(tempEndRowkeyByte);
    }

    private static String getStartKey(byte[] startRowkeyByte, byte[] regionStarKey) {
        if (startRowkeyByte == null) {// 由于之前处理过，所以传入的userStartKey不可能为null
            throw new IllegalArgumentException(
                    "userStartKey should not be null!");
        }

        byte[] tempStartRowkeyByte;

        if (Bytes.compareTo(startRowkeyByte, regionStarKey) < 0) {
            tempStartRowkeyByte = regionStarKey;
        } else {
            tempStartRowkeyByte = startRowkeyByte;
        }

        return Bytes.toStringBinary(tempStartRowkeyByte);
    }
}
