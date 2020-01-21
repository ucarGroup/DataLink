package com.ucar.datalink.biz.service;

public interface HbaseService {

    /**
     * 开启表表复制接口
     *
     * @param sourceClusterZk
     * @param sourceClusterZkBasePath
     * @param targetClusterZk
     * @param targetClusterZkBasePath
     * @param peerId
     * @param tableName
     * @return
     */
    String doAddTable(String sourceClusterZk, String sourceClusterZkBasePath, String targetClusterZk,
                      String targetClusterZkBasePath, String peerId, String tableName);

    /**
     * @param sourceClusterZk
     * @param sourceClusterZkBasePath
     * @param targetClusterZk
     * @param targetClusterZkBasePath
     * @param peerId
     * @return
     */
    String doGetTables(String sourceClusterZk, String sourceClusterZkBasePath, String targetClusterZk,
                       String targetClusterZkBasePath, String peerId);

    String getZkServer_datalink();

/*    String getHbase_peerId();*/

}
