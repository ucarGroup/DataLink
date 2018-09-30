<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="taskMysqlReader" class="col-sm-12">
    <form class="form-horizontal" role="form">
        <div class="col-sm-4">
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-mediaSourceId">关联数据源</label>

                <div class="col-sm-7">
                    <select name="mediaSourceId" id="mysqlReader-mediaSourceId"
                            class="chosen-select col-sm-12">
                        <c:forEach items="${taskModel.mediaSourceList}" var="bean">
                            <c:if test="${bean.id==taskModel.mysqlReaderParameter.mediaSourceId}">
                                <option selected="selected" value="${bean.id}">${bean.name}</option>
                            </c:if>
                            <c:if test="${bean.id!=taskModel.mysqlReaderParameter.mediaSourceId}">
                                <option value="${bean.id}">${bean.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-startTimeStamps">初始日志时间</label>

                <div class="col-sm-7">
                    <input type='text' class="form_datetime"
                           id='mysqlReader-startTimeStamps' name="startTimeStamps" style="width:100%;"
                            <c:if test="${taskModel.mysqlReaderParameter.startTimeStamps!=null}">
                                <jsp:useBean id="dateValue" class="java.util.Date"/>
                                <c:set target="${dateValue}" property="time"
                                       value="${taskModel.mysqlReaderParameter.startTimeStamps}"/>
                                <fmt:formatDate var="formatTime" value="${dateValue}" pattern="yyyy-MM-dd HH:mm:ss"
                                                type="both"/>
                                value="${formatTime}"
                            </c:if>
                            />
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-messageBatchSize">Message Batch Size</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-messageBatchSize"
                           name="messageBatchSize" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.messageBatchSize}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-memoryStorageBufferSize">缓存记录数</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-memoryStorageBufferSize"
                           name="memoryStorageBufferSize" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.memoryStorageBufferSize}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-memoryStorageBufferMemUnit">缓存记录单元大小(byte)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-memoryStorageBufferMemUnit"
                           name="memoryStorageBufferMemUnit" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.memoryStorageBufferMemUnit}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-sendBufferSize">SendBufferSize(byte)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-sendBufferSize"
                           name="sendBufferSize" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.sendBufferSize}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-receiveBufferSize">ReceiveBufferSize(byte)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-receiveBufferSize"
                           name="receiveBufferSize" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.receiveBufferSize}"/>
                </div>
            </div>
        </div>

        <div class="col-sm-4">
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-fallbackIntervalInSeconds">DB切换回退时间(s)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-fallbackIntervalInSeconds"
                           name="fallbackIntervalInSeconds" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.fallbackIntervalInSeconds}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-defaultConnectionTimeoutInSeconds">连接超时时间(s)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;"
                           id="mysqlReader-defaultConnectionTimeoutInSeconds"
                           name="defaultConnectionTimeoutInSeconds" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.defaultConnectionTimeoutInSeconds}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-detectingSQL">心跳检测SQL</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-detectingSQL"
                           name="detectingSQL" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.detectingSQL}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-detectingIntervalInSeconds">心跳检测频率</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-detectingIntervalInSeconds"
                           name="detectingIntervalInSeconds" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.detectingIntervalInSeconds}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-detectingTimeoutThresholdInSeconds">心跳超时时间(s)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;"
                           id="mysqlReader-detectingTimeoutThresholdInSeconds"
                           name="detectingTimeoutThresholdInSeconds" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.detectingTimeoutThresholdInSeconds}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-detectingRetryTimes">心跳失败重试次数</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-detectingRetryTimes"
                           name="detectingRetryTimes" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.detectingRetryTimes}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-blackFilter">Black Filter</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-blackFilter"
                           name="blackFilter" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.blackFilter}"/>
                </div>
            </div>
        </div>

        <div class="col-sm-4">
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-filteredEventTypes">过滤事件类型</label>

                <div class="col-sm-7">
                    <select multiple="" id="mysqlReader-filteredEventTypes" name="filteredEventTypes"
                            class="filteredEventTypes col-xs-10 col-sm-8"
                            data-placeholder="Click to Choose...">
                        <c:forEach items="${taskModel.filterEventTypeList}" var="bean">
                            <c:set var="found" value="false"/>
                            <c:forEach items="${taskModel.mysqlReaderParameter.filteredEventTypes}" var="value">
                                <c:if test="${value == bean}">
                                    <c:set var="found" value="true"/>
                                </c:if>
                            </c:forEach>
                            <c:if test="${found==true}">
                                <option selected="selected" value="${bean}">${bean}</option>
                            </c:if>
                            <c:if test="${found!=true}">
                                <option value="${bean}">${bean}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-batchTimeout">BatchTimeout(ms)</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="mysqlReader-batchTimeout"
                           name="batchTimeout" class="col-xs-10 col-sm-5"
                           value="${taskModel.mysqlReaderParameter.batchTimeout}"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-dump">是否dump</label>

                <div class="col-sm-7">
                    <select name="dump" id="mysqlReader-dump" class="col-sm-12">
                        <c:if test="${taskModel.mysqlReaderParameter.dump==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.mysqlReaderParameter.dump==false}">
                            <option selected="selected" value="false">否</option>
                            <option value="true">是</option>
                        </c:if>

                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-dumpDetail">是否dump详情</label>

                <div class="col-sm-7">
                    <select name="dumpDetail" id="mysqlReader-dumpDetail" class="col-sm-12">
                        <c:if test="${taskModel.mysqlReaderParameter.dumpDetail==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.mysqlReaderParameter.dumpDetail==false}">
                            <option selected="selected" value="false">否</option>
                            <option value="true">是</option>
                        </c:if>

                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-groupSinkMode">GroupSinkMode</label>

                <div class="col-sm-7">
                    <select name="groupSinkMode" id="mysqlReader-groupSinkMode" class="col-sm-12">
                        <c:forEach items="${taskModel.groupSinkModeList}" var="bean">
                            <c:if test="${bean==taskModel.mysqlReaderParameter.groupSinkMode}">
                                <option selected="selected" value="${bean}">${bean}</option>
                            </c:if>
                            <c:if test="${bean!=taskModel.mysqlReaderParameter.groupSinkMode}">
                                <option value="${bean}">${bean}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-ddlSync">DdlSync</label>

                <div class="col-sm-7">
                    <select name="ddlSync" id="mysqlReader-ddlSync" class="col-sm-12">
                        <c:if test="${taskModel.mysqlReaderParameter.ddlSync==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.mysqlReaderParameter.ddlSync==false}">
                            <option selected="selected" value="false">否</option>
                            <option value="true">是</option>
                        </c:if>

                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="mysqlReader-perfStatistic">PerfStatistic</label>

                <div class="col-sm-7">
                    <select name="perfStatistic" id="mysqlReader-perfStatistic" class="col-sm-12">
                        <c:if test="${taskModel.mysqlReaderParameter.perfStatistic==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.mysqlReaderParameter.perfStatistic==false}">
                            <option selected="selected" value="false">否</option>
                            <option value="true">是</option>
                        </c:if>

                    </select>
                </div>
            </div>
        </div>
    </form>
</div>
<script type="text/javascript">
    $("#taskMysqlReader").ready(function () {
        $("#mysqlReader-mediaSourceId").chosen({allow_single_deselect: true, width: "100%"});
        $(".filteredEventTypes").css('min-width', '100%').select2({allowClear: false, width: '100%'});
        $(".form_datetime").datetimepicker(
                {
                    format: 'YYYY-MM-DD HH:mm:ss'
                }
        );
    });

    function getMysqlReaderObj() {
        var obj = {
            mediaSourceId: $("#mysqlReader-mediaSourceId").val(),
            startTimeStamps: Date.parse(new Date($("#mysqlReader-startTimeStamps").val())),
            blackFilter: $("#mysqlReader-blackFilter").val(),
            batchTimeout: $("#mysqlReader-batchTimeout").val(),
            defaultConnectionTimeoutInSeconds: $("#mysqlReader-defaultConnectionTimeoutInSeconds").val(),
            detectingIntervalInSeconds: $("#mysqlReader-detectingIntervalInSeconds").val(),
            detectingRetryTimes: $("#mysqlReader-detectingRetryTimes").val(),
            detectingSQL: $("#mysqlReader-detectingSQL").val(),
            detectingTimeoutThresholdInSeconds: $("#mysqlReader-detectingTimeoutThresholdInSeconds").val(),
            dump: $("#mysqlReader-dump").val(),
            dumpDetail: $("#mysqlReader-dumpDetail").val(),
            fallbackIntervalInSeconds: $("#mysqlReader-fallbackIntervalInSeconds").val(),
            filteredEventTypes: $("#mysqlReader-filteredEventTypes").val(),
            memoryStorageBufferMemUnit: $("#mysqlReader-memoryStorageBufferMemUnit").val(),
            memoryStorageBufferSize: $("#mysqlReader-memoryStorageBufferSize").val(),
            messageBatchSize: $("#mysqlReader-messageBatchSize").val(),
            receiveBufferSize: $("#mysqlReader-receiveBufferSize").val(),
            sendBufferSize: $("#mysqlReader-sendBufferSize").val(),
            groupSinkMode:$("#mysqlReader-groupSinkMode").val(),
            ddlSync:$("#mysqlReader-ddlSync").val(),
            perfStatistic:$("#mysqlReader-perfStatistic").val()
        };
        return obj;
    }
</script>