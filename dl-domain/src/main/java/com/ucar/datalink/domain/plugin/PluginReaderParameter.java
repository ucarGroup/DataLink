package com.ucar.datalink.domain.plugin;


/**
 * Plugin-Reader参数基类
 * <p>
 * Created by lubiao on 2017/2/17.
 */
public abstract class PluginReaderParameter extends PluginParameter {

    /**
     * 是否需要dump fetch到的数据
     */
    private boolean dump = false;

    /**
     * Reader关联的mediaSource的id
     */
    private Long mediaSourceId;

    /**
     * 是否同步ddl操作(主要针对关系型数据库)
     */
    private boolean ddlSync = true;

    /**
     * 是否开启读端数据多路复用，该参数是否有效取决于Reader插件是否支持该功能
     * 例如，对于MysqlReader来说：一旦开启，同一worker进程内，连接的【读端数据库实例】一样的Task将复用一份儿binlog
     *
     */
    private boolean multiplexingRead;

    public boolean isDump() {
        return dump;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    public Long getMediaSourceId() {
        return mediaSourceId;
    }

    public void setMediaSourceId(Long mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public boolean isDdlSync() {
        return ddlSync;
    }

    public void setDdlSync(boolean ddlSync) {
        this.ddlSync = ddlSync;
    }

    public boolean isMultiplexingRead() {
        return multiplexingRead;
    }

    public void setMultiplexingRead(boolean multiplexingRead) {
        this.multiplexingRead = multiplexingRead;
    }
}
