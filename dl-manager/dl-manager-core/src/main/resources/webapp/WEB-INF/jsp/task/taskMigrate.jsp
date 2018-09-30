<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-migrate" class="modal-dialog">
    <div class="modal-content">
        <div id="modal-wizard-container_update">
            <div class="modal-header">

                <div class="modal-header no-padding">
                    <div class="table-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            <span class="white">&times;</span>
                        </button>
                        Task组迁移
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="migrate_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-task-id" name="taskId" value="${taskId}"/>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-migrate-currentGroup">当前分组</label>

                        <div class="col-sm-9">
                            <select class="col-sm-8" name="currentGroup" id="form-migrate-currentGroup"
                                    disabled="disabled">
                                <option value="${currentGroupId}">${currentGroupName}</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-migrate-targetGroup">目标分组</label>

                        <div class="col-sm-9">
                            <select class="col-sm-8" name="targetGroup" id="form-migrate-targetGroup">
                                <c:forEach items="${groupList}" var="bean">
                                    <option value="${bean.id}">${bean.groupName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="migrate()">
                <i class="ace-icon fa fa-save"></i>
                迁移
            </button>

            <button class="btn btn-danger" type="button" data-dismiss="modal">
                取消
                <i class="ace-icon fa fa-times"></i>
            </button>
        </div>
    </div>
</div>
<script type="text/javascript">

    function migrate() {
        var obj = {
            taskId: $("#form-task-id").val(),
            targetGroupId: $("#form-migrate-targetGroup").val()
        };

        $.ajax({
            type: "post",
            url: "${basePath}/task/doGroupMigrate",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("Task组迁移成功！");
                    $(".modal-header button").click();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>