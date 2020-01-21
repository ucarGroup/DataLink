<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner ace-save-state">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <input type="hidden" name="id"  value="${taskPriorityInfo.id}"/>
                <div class="col-sm-12">
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-name">名称</label>
                        <div class="col-sm-9">
                            <input type="text" name="name"  value="${taskPriorityInfo.name}"
                                   id="form-add-name" class="col-xs-10 col-sm-5"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-update-priority">任务等级</label>

                        <div class="col-sm-9">
                            <select multiple="" name="priority" class="form-update-priority tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-priority" style="width: 200px;">
                                <c:forEach items="${priorityTypeList}" var="item">
                                        <option value="${item.key}"
                                    <c:if test="${item.key == taskPriorityInfo.priority}">
                                         selected = "selected"
                                    </c:if>
                                        >${item.key}</option>

                                </c:forEach>

                            </select>
                        </div>
                    </div>
                </div>
            </form>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doEdit()">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    保存
                </button>
                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="refresh()">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>
    </div>
    <!-- /.page-content -->
</div>

<script type="text/javascript">
    $('.form-update-priority').css('min-width', '42%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    function doEdit() {
        $.ajax({
            type: "post",
            url: "${basePath}/taskPriority/doEdit",
            dataType: "json",
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    refresh();
                } else {
                    alert(data);
                }
            }
        });
    }
    function refresh() {
        $("#edit").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }

</script>
