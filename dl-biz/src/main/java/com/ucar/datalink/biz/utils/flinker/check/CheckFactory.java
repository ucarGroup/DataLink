package com.ucar.datalink.biz.utils.flinker.check;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yang.wang09 on 2018-05-17 15:56.
 */
public class CheckFactory {

    private static Logger logger = LoggerFactory.getLogger(CheckFactory.class);

    public static ICheck getModifyCheckType(MediaSourceInfo srcInfo, MediaSourceInfo destInfo) {
        if(srcInfo.getType()== MediaSourceType.MYSQL || srcInfo.getType()== MediaSourceType.SQLSERVER ||
        srcInfo.getType()== MediaSourceType.SDDL || srcInfo.getType()== MediaSourceType.POSTGRESQL) {
            return srcRDBMS(destInfo);
        }

        else if(srcInfo.getType() == MediaSourceType.HBASE) {
            return srcHBase(destInfo);
        }

        else if(srcInfo.getType() == MediaSourceType.HDFS) {
            return srcHDFS(destInfo);
        }

        else if(srcInfo.getType() == MediaSourceType.ELASTICSEARCH) {
            return srcElasticSearch(destInfo);
        }

        else {
            //抛错
            logger.error("unknown media source type "+srcInfo.getType());
            throw new UnsupportedOperationException("unknown media source type "+srcInfo.getType());
        }

    }


    private static ICheck srcRDBMS(MediaSourceInfo target) {
        if(target.getType()== MediaSourceType.MYSQL || target.getType()== MediaSourceType.SQLSERVER ||
        target.getType()== MediaSourceType.SDDL || target.getType()== MediaSourceType.POSTGRESQL) {
            return new CheckRdbms2Rdbms();
        }
        else if(target.getType() == MediaSourceType.HDFS) {
            return new CheckRdbms2Hdfs();
        }
        else if(target.getType() == MediaSourceType.HBASE) {
            return new CheckRdbms2Hbase();
        }
        else if(target.getType()== MediaSourceType.ELASTICSEARCH) {
            return new CheckRdbms2ES();
        }
        else {
            logger.error("unknown media source type "+target.getType());
            throw new UnsupportedOperationException("unknown media source type "+target.getType());
        }

    }

    private static ICheck srcHBase(MediaSourceInfo target) {
        if(target.getType()== MediaSourceType.HDFS) {
            return new CheckHbase2Hdfs();
        }
        else if(target.getType()== MediaSourceType.HBASE) {
            return new CheckHbase2Hbase();
        }
        else {
            logger.error("unknown media source type "+target.getType());
            throw new UnsupportedOperationException("unknown media source type "+target.getType());
        }
    }

    private static ICheck srcHDFS(MediaSourceInfo target) {
        if(target.getType() == MediaSourceType.ELASTICSEARCH) {
            return new CheckHdfs2ES();
        }
        else if(target.getType() == MediaSourceType.HBASE) {
            return new CheckHdfs2Hbase();
        }
        else if(target.getType() == MediaSourceType.HDFS) {
            return new CheckHdfs2Hdfs();
        }
        else if(target.getType()== MediaSourceType.MYSQL || target.getType()== MediaSourceType.SQLSERVER ||
        target.getType()== MediaSourceType.SDDL || target.getType()== MediaSourceType.POSTGRESQL) {
            return new CheckHdfs2Rdbms();
        }
        else {
            logger.error("unknown media source type "+target.getType());
            throw new UnsupportedOperationException("unknown media source type "+target.getType());
        }
    }

    //ELASTICSEARCH
    private static ICheck srcElasticSearch(MediaSourceInfo target) {
        if(target.getType() == MediaSourceType.ELASTICSEARCH) {
            return new CheckES2ES();
        }
        else if(target.getType() == MediaSourceType.HBASE) {
            return new CheckES2Hbase();
        }
        else if(target.getType()== MediaSourceType.MYSQL || target.getType()== MediaSourceType.SQLSERVER ||
        target.getType()== MediaSourceType.SDDL || target.getType()== MediaSourceType.POSTGRESQL) {
            return new CheckES2Rdbms();
        }
        else {
            logger.error("unknown media source type "+target.getType());
            throw new UnsupportedOperationException("unknown media source type "+target.getType());
        }
    }

}
