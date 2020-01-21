<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="taskBasic" class="col-sm-12">
    <div class="col-sm-6">
        <input type="hidden" name="id" class="col-sm-12" value="${taskModel.taskBasicInfo.id}"
               id="basic-id"/>

        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-groupId">所属分组</label>

                            <div class="col-sm-10">
                                <select name="groupId" id="basic-groupId"
                                        class="chosen-select col-sm-12">
                                    <c:forEach items="${taskModel.groupList}" var="bean">
                                        <c:if test="${bean.id==taskModel.taskBasicInfo.groupId}">
                                            <option selected="selected" value="${bean.id}">${bean.groupName}</option>
                                        </c:if>
                                        <c:if test="${bean.id!=taskModel.taskBasicInfo.groupId}">
                                            <option value="${bean.id}">${bean.groupName}</option>
                                        </c:if>
                                    </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-taskName">Task名称</label>

                <div class="col-sm-10">
                    <input type="text" name="taskName" class="col-sm-12" value="${taskModel.taskBasicInfo.taskName}"
                           id="basic-taskName"/>
                </div>
            </div>
        </div>
        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-taskDesc">Task描述</label>

                <div class="col-sm-10">
                    <input type="text" name="taskDesc" class="col-sm-12" value="${taskModel.taskBasicInfo.taskDesc}"
                           id="basic-taskDesc"/>
                </div>
            </div>
        </div>
        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-targetState">Task状态</label>

                <div class="col-sm-10">
                    <select name="targetState" id="basic-targetState"
                            class="chosen-select col-sm-12">
                        <c:forEach items="${taskModel.targetStateList}" var="bean">
                            <c:if test="${bean==taskModel.taskBasicInfo.targetState}">
                                <option selected="selected" value="${bean}">${bean}</option>
                            </c:if>
                            <c:if test="${bean!=taskModel.taskBasicInfo.targetState}">
                                <option value="${bean}">${bean}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-taskSyncMode">机房同步模式</label>

                <div class="col-sm-10">
                    <select multiple="false" id="basic-taskSyncMode" name="taskSyncMode" class="taskSyncMode chosen-select col-sm-12"
                            data-placeholder="Click to Choose..." >
                        <c:forEach items="${taskModel.taskSyncModes}" var="bean">
                            <option value="${bean.code}">${bean.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="col-sm-12" id="sourceLabIdDiv">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-sourceLabId">源机房</label>

                <div class="col-sm-10">
                    <select multiple="false" id="basic-sourceLabId" name="sourceLabId" class="sourceLabId chosen-select col-sm-12"
                            data-placeholder="Click to Choose..." >
                        <c:forEach items="${taskModel.labInfoList}" var="bean">
                            <option value="${bean.id}">${bean.labName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="col-sm-12" id="targetLabIdDiv">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-targetLabId">目标机房</label>

                <div class="col-sm-10">
                    <select multiple="false" id="basic-targetLabId" name="targetLabId" class="targetLabId chosen-select col-sm-12"
                            data-placeholder="Click to Choose..." >
                        <c:forEach items="${taskModel.labInfoList}" var="bean">
                            <option value="${bean.id}">${bean.labName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>

        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-isMultiCopy">多路复制</label>

                <div class="col-sm-10">
                    <select name="dump" id="basic-isMultiCopy" class="col-sm-12">
                        <option selected="selected" value="false">否</option>
                        <option value="true">是</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="col-sm-12">
            <div class="col-sm-12 form-group">
                <label class="col-sm-2 control-label no-padding-right"
                       for="basic-alarmPriorityId">报警策略组</label>

                <div class="col-sm-10">
                    <select name="alarmPriorityId" id="basic-alarmPriorityId" class="col-sm-12">
                        <option value="">无</option>
                        <c:forEach items="${alarmPriorityInfoList}" var="bean">
                            <option value="${bean.id}"
                                    <c:if test="${taskModel.taskBasicInfo.id != null && bean.id == taskModel.taskBasicInfo.alarmPriorityId}" >selected</c:if>
                            >${bean.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>


    </div>
</div>
<script type="text/javascript">

    $("#taskBasic").ready(function () {
        $("#basic-groupId").chosen({allow_single_deselect: true, width: "100%"});
        $("#basic-targetState").chosen({allow_single_deselect: true, width: "100%"});
    });

    function getBasicObj() {

        var sourceLabIdArr = $("#basic-sourceLabId").val();
        var sourceLabId;
        if(sourceLabIdArr && sourceLabIdArr.length >= 1){
            sourceLabId = sourceLabIdArr[0];
        }
        var targetLabIdArr = $("#basic-targetLabId").val();
        var targetLabId;
        if(targetLabIdArr && targetLabIdArr.length >= 1){
            targetLabId = targetLabIdArr[0];
        }

        var taskSyncModeArr = $("#basic-taskSyncMode").val();
        var taskSyncMode;
        if(taskSyncModeArr && taskSyncModeArr.length >= 1){
            taskSyncMode = taskSyncModeArr[0];
        }

        var alarmPriorityId = $("#basic-alarmPriorityId").val();

        var obj = {
            id: $("#basic-id").val(),
            taskName: $("#basic-taskName").val(),
            taskDesc: $("#basic-taskDesc").val(),
            targetState: $("#basic-targetState").val(),
            groupId: $("#basic-groupId").val(),
            taskSyncMode: taskSyncMode,
            sourceLabId: sourceLabId,
            targetLabId: targetLabId,
            alarmPriorityId : alarmPriorityId
        };
        return obj;
    }

    function getTaskParameterObj() {

        var sourceLabIdArr = $("#basic-sourceLabId").val();
        var sourceLabId;
        if(sourceLabIdArr && sourceLabIdArr.length >= 1){
            sourceLabId = sourceLabIdArr[0];
        }
        var targetLabIdArr = $("#basic-targetLabId").val();
        var targetLabId;
        if(targetLabIdArr && targetLabIdArr.length >= 1){
            targetLabId = targetLabIdArr[0];
        }

        var obj = {
            id: $("#basic-id").val(),
            sourceLabId: sourceLabId,
            targetLabId: targetLabId
        };
        return obj;
    }
</script>