<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-rdbms" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">Rdbms Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-rdbms" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-syncMode">同步模式</label>

                    <div class="col-sm-7">
                        <select id='rdbms-syncMode' name="syncMode" style="width:100%;"
                                onchange="javascript:rdbmsSyncModeChange()">
                            <c:forEach items="${taskModel.rdbSyncModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-rdbms'].syncMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-rdbms'].syncMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='rdbms-merging' name="merging" style="width:100%;"
                                onchange="javascript:rdbmsMergingChange()">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].merging==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='rdbms-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='rdbms-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-rdbms'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='rdbms-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-rdbms'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-rdbms'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='rdbms-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].perfStatistic==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='rdbms-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-rdbms'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='rdbms-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-rdbms'].poolSize}"/>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='rdbms-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-syncAutoAddColumn">自动加字段</label>

                    <div class="col-sm-7">
                        <select id='rdbms-syncAutoAddColumn' name="syncAutoAddColumn" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].syncAutoAddColumn==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].syncAutoAddColumn==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<div id="div-es" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">Es Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-es" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='es-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-es'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='es-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-es'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-es'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='es-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-es'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='es-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-es'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-es'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='es-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-es'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='es-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-es'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-es'].merging==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='es-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-es'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-es'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="es-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='es-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-es'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-es'].perfStatistic==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<div id="div-hdfs" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">Hdfs Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-hdfs" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='hdfs-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-streamLeisureLimit">Stream Leisure Limit(ms)</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-streamLeisureLimit' name="streamLeisureLimit"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].streamLeisureLimit}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-mysqlBinlogPath">mysqlBinlog路径</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-mysqlBinlogPath' name="mysqlBinlogPath"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].mysqlBinlogPath}"/>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='hdfs-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-hdfs'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-hdfs'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-hdfsPacketSize">Hdfs Packet Size(Byte)</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-hdfsPacketSize' name="hdfsPacketSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].hdfsPacketSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-hbasePath">hbase路径</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-hbasePath' name="hbasePath"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].hbasePath}"/>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='hdfs-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].merging==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='hdfs-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].perfStatistic==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='hdfs-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hdfs'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-commitMode">Commit Mode</label>

                    <div class="col-sm-7">
                        <select id='hdfs-commitMode' name="commitMode" style="width:100%;">
                            <c:forEach items="${taskModel.commitModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-hdfs'].commitMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-hdfs'].commitMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-hsyncInterval">Hsync Interval(ms)</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-hsyncInterval' name="hsyncInterval"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].hsyncInterval}"/>
                    </div>
                </div>

            </div>

        </form>
    </div>
</div>
<div id="div-sddl" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">SDDL Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-sddl" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="sddl-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='sddl-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-sddl'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-sddl'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="sddl-missMatchSkipTables">过滤表白名单(多项以#分隔)</label>

                    <div class="col-sm-7">
                        <textarea id="sddl-missMatchSkipTables" name="missMatchSkipTables"
                                  style="margin: 0px; width: 354px; height: 91px;">${taskModel.writerParameterMap['writer-sddl'].missMatchSkipTables}</textarea>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="sddl-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='sddl-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-sddl'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-sddl'].perfStatistic==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="sddl-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='sddl-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-sddl'].maxRetryTimes}"/>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="sddl-syncAutoAddColumn">自动加字段</label>

                    <div class="col-sm-7">
                        <select id='sddl-syncAutoAddColumn' name="syncAutoAddColumn" style="width:100%;">
                                <option selected="selected" value="false">否</option>
                        </select>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>
<div id="div-hbase" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">HBase Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-hbase" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='hbase-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hbase'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='hbase-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='hbase-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hbase'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='hbase-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-hbase'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-hbase'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='hbase-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hbase'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='hbase-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].merging==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='hbase-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hbase-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='hbase-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-hbase'].perfStatistic==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>
