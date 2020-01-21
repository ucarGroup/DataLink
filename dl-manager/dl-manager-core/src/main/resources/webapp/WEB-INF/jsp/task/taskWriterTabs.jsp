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
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="rdbms-useUpsert">是否开启upsert</label>

                    <div class="col-sm-7">
                        <select id='rdbms-useUpsert' name="useUpsert" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].useUpsert==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-rdbms'].useUpsert==false}">
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
<div id="div-fq" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">fq Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-fq" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='fq-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-fq'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='fq-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-serializeMode">Serialize Mode</label>

                    <div class="col-sm-7">
                        <select id='fq-serializeMode' name="serializeMode" style="width:100%;">
                            <c:forEach items="${taskModel.serializeModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-fq'].serializeMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-fq'].serializeMode}">
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
                           for="fq-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='fq-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-fq'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='fq-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-fq'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-fq'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-serializeMode">Partition Mode</label>

                    <div class="col-sm-7">
                        <select id='fq-partitionMode' name="partitionMode" style="width:100%;">
                            <c:forEach items="${taskModel.partitionModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-fq'].partitionMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-fq'].partitionMode}">
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
                           for="fq-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='fq-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-fq'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='fq-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].merging==false}">
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
                           for="fq-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='fq-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="fq-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='fq-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-fq'].perfStatistic==false}">
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
<div id="div-dove" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">dove Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-dove" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='dove-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-dove'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='dove-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-serializeMode">Serialize Mode</label>

                    <div class="col-sm-7">
                        <select id='dove-serializeMode' name="serializeMode" style="width:100%;">
                            <c:forEach items="${taskModel.serializeModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-dove'].serializeMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-dove'].serializeMode}">
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
                           for="dove-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='dove-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-dove'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='dove-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-dove'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-dove'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-serializeMode">Partition Mode</label>

                    <div class="col-sm-7">
                        <select id='dove-partitionMode' name="partitionMode" style="width:100%;">
                            <c:forEach items="${taskModel.partitionModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-dove'].partitionMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-dove'].partitionMode}">
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
                           for="dove-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='dove-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-dove'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='dove-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].merging==false}">
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
                           for="dove-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='dove-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="dove-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='dove-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-dove'].perfStatistic==false}">
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
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="hdfs-socketTimeout">Client SocketTimeout(ms)</label>

                    <div class="col-sm-7">
                        <input type='text' id='hdfs-socketTimeout' name="socketTimeout"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-hdfs'].socketTimeout}"/>
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


<div id="div-kudu" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">kudu Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-kudu" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kudu-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='kudu-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kudu'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kudu-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='kudu-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].useBatch==false}">
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
                           for="kudu-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='kudu-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kudu'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kudu-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='kudu-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-kudu'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-kudu'].retryMode}">
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
                           for="kudu-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='kudu-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kudu'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kudu-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='kudu-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].merging==false}">
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
                           for="kudu-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='kudu-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kudu-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='kudu-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kudu'].perfStatistic==false}">
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


<div id="div-kafka" class="col-sm-12 panel panel-info" style="display:none;">
    <div class="panel-heading">
        <h1 class="panel-title">kafka Writer</h1>
    </div>
    <div class="panel-body">
        <form id="form-kafka" class="form-horizontal" role="form">
            <div class="col-sm-3">
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-poolSize">线程池Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='kafka-poolSize' name="poolSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kafka'].poolSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-useBatch">是否批量写入</label>

                    <div class="col-sm-7">
                        <select id='kafka-useBatch' name="useBatch" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].useBatch==true}">
                                <option selected="selected" value="true">是</option>
                                <option value="false">否</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].useBatch==false}">
                                <option value="true">是</option>
                                <option selected="selected" value="false">否</option>
                            </c:if>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-serializeMode">Serialize Mode</label>

                    <div class="col-sm-7">
                        <select id='kafka-serializeMode' name="serializeMode" style="width:100%;">
                            <c:forEach items="${taskModel.serializeModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-kafka'].serializeMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-kafka'].serializeMode}">
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
                           for="kafka-batchSize">Batch Size</label>

                    <div class="col-sm-7">
                        <input type='text' id='kafka-batchSize' name="batchSize"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kafka'].batchSize}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-retryMode">重试模式</label>

                    <div class="col-sm-7">
                        <select id='kafka-retryMode' name="retryMode" style="width:100%;">
                            <c:forEach items="${taskModel.retryModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-kafka'].retryMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-kafka'].retryMode}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-serializeMode">Partition Mode</label>

                    <div class="col-sm-7">
                        <select id='kafka-partitionMode' name="partitionMode" style="width:100%;">
                            <c:forEach items="${taskModel.partitionModeList}" var="bean">
                                <c:if test="${bean==taskModel.writerParameterMap['writer-kafka'].partitionMode}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=taskModel.writerParameterMap['writer-kafka'].partitionMode}">
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
                           for="kafka-maxRetryTimes">最大重试次数</label>

                    <div class="col-sm-7">
                        <input type='text' id='kafka-maxRetryTimes' name="maxRetryTimes"
                               style="width:100%;"
                               value="${taskModel.writerParameterMap['writer-kafka'].maxRetryTimes}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-merging">是否压缩合并</label>

                    <div class="col-sm-7">
                        <select id='kafka-merging' name="merging" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].merging==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].merging==false}">
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
                           for="kafka-dryRun">是否DryRun</label>

                    <div class="col-sm-7">
                        <select id='kafka-dryRun' name="dryRun" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].dryRun==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].dryRun==false}">
                                <option selected="selected" value="false">否</option>
                                <option value="true">是</option>
                            </c:if>

                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-5 control-label no-padding-right"
                           for="kafka-perfStatistic">PerfStatistic</label>

                    <div class="col-sm-7">
                        <select id='kafka-perfStatistic' name="perfStatistic" style="width:100%;">
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].perfStatistic==true}">
                                <option value="false">否</option>
                                <option selected="selected" value="true">是</option>
                            </c:if>
                            <c:if test="${taskModel.writerParameterMap['writer-kafka'].perfStatistic==false}">
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




