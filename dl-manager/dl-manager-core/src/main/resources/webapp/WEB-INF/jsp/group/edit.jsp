<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="edit_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-groupName">分组名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-edit-groupName"
                               value="${groupInfo.groupName}" name="groupName" placeholder="数据源名称"
                               class="col-xs-10 col-sm-5"/>
                        <input type="hidden" name="id" value="${groupInfo.id}">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-groupDesc">分组描述</label>

                    <div class="col-sm-9">
                        <textarea id="form-edit-groupDesc" name="groupDesc" class="col-xs-10 col-sm-5"
                                  style="margin: 0px; width: 354px; height: 91px;"/>
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
    $("#form-edit-groupDesc").val('${groupInfo.groupDesc}');
    function doEdit() {
        var groupName = $.trim($("#form-edit-groupName").val());
        var groupDesc = $.trim($("#form-edit-groupDesc").val());
        if (groupName == "") {
            alert("分组名称不能为空!");
            return false;
        }
        if (groupDesc == "") {
            alert("描述不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/group/doEdit",
            dataType: "json",
            data: $("#edit_form").serialize(),
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
