package com.ucar.datalink.reader.hbase.replicate;

import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Server;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.catalog.CatalogTracker;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.protobuf.generated.RPCProtos;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;

/**
 * Base class that mimics a {@link org.apache.hadoop.hbase.regionserver.HRegionServer}
 * <p>
 * Created by lubiao on 2017/11/15.
 */
public abstract class BaseHRegionServer implements AdminProtos.AdminService.BlockingInterface, Server,
        org.apache.hadoop.hbase.ipc.PriorityFunction {

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- Implementations for BlockingInterface -------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public AdminProtos.GetRegionInfoResponse getRegionInfo(RpcController controller, AdminProtos.GetRegionInfoRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.GetStoreFileResponse getStoreFile(RpcController controller, AdminProtos.GetStoreFileRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.GetOnlineRegionResponse getOnlineRegion(RpcController controller, AdminProtos.GetOnlineRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.OpenRegionResponse openRegion(RpcController controller, AdminProtos.OpenRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.CloseRegionResponse closeRegion(RpcController controller, AdminProtos.CloseRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.FlushRegionResponse flushRegion(RpcController controller, AdminProtos.FlushRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.SplitRegionResponse splitRegion(RpcController controller, AdminProtos.SplitRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.CompactRegionResponse compactRegion(RpcController controller, AdminProtos.CompactRegionRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.MergeRegionsResponse mergeRegions(RpcController controller, AdminProtos.MergeRegionsRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.ReplicateWALEntryResponse replicateWALEntry(RpcController controller, AdminProtos.ReplicateWALEntryRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.ReplicateWALEntryResponse replay(RpcController controller, AdminProtos.ReplicateWALEntryRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.RollWALWriterResponse rollWALWriter(RpcController controller, AdminProtos.RollWALWriterRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.GetServerInfoResponse getServerInfo(RpcController controller, AdminProtos.GetServerInfoRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.StopServerResponse stopServer(RpcController controller, AdminProtos.StopServerRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public AdminProtos.UpdateFavoredNodesResponse updateFavoredNodes(RpcController controller, AdminProtos.UpdateFavoredNodesRequest request) throws ServiceException {
        throw new UnsupportedOperationException("No need to support.");
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //------------------------------------------- Implementations for org.apache.hadoop.hbase.Server -------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public ZooKeeperWatcher getZooKeeper() {
        throw new UnsupportedOperationException("No need to support.");
    }

    @Override
    public CatalogTracker getCatalogTracker() {
        return null;
    }

    @Override
    public ServerName getServerName() {
        throw new UnsupportedOperationException("No need to support.");
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------- Implementations for org.apache.hadoop.hbase.Abortable --------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void abort(String why, Throwable e) {

    }

    @Override
    public boolean isAborted() {
        return false;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------- Implementations for org.apache.hadoop.hbase.Abortable --------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public int getPriority(RPCProtos.RequestHeader header, Message param) {
        return org.apache.hadoop.hbase.HConstants.NORMAL_QOS;
    }

    @Override
    public boolean isStopped() {
        throw new UnsupportedOperationException("No need to support.");
    }
}
