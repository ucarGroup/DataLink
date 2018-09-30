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
    </div>
</div>
<script type="text/javascript">
    $("#taskBasic").ready(function () {
        $("#basic-groupId").chosen({allow_single_deselect: true, width: "100%"});
        $("#basic-targetState").chosen({allow_single_deselect: true, width: "100%"});
    });

    function getBasicObj() {
        var obj = {
            id: $("#basic-id").val(),
            taskName: $("#basic-taskName").val(),
            taskDesc: $("#basic-taskDesc").val(),
            targetState: $("#basic-targetState").val(),
            groupId: $("#basic-groupId").val()
        };
        return obj;
    }
</script>