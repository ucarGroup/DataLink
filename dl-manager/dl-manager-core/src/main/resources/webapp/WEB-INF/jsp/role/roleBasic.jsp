<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="roleBasic" class="col-sm-12">

    <div class="row">
        <form id="update_form" class="form-horizontal" role="form">
            <div class="form-group">
                <input type="hidden" name="id" value="${roleInfo.id}" id="basic-id"/>
                <label class="col-sm-3 control-label no-padding-right" for="form-update-code">编码</label>

                <div class="col-sm-9">
                    <input type="text" value="${roleInfo.code}" style="width:350px;height:35px"
                           id="form-update-code" name="code" class="col-xs-10 col-sm-5"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-update-name">名称</label>

                <div class="col-sm-9">
                    <input type="text" value="${roleInfo.name}" style="width:350px;height:35px"
                           id="form-update-name" name="name" class="col-xs-10 col-sm-5"/>
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
            <button class="btn" type="reset" onclick="refreshList()">
                返回
                <i class="ace-icon fa fa-undo bigger-110"></i>
            </button>
        </div>
    </div>
</div>


<script type="text/javascript">
    $('.code').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});
    function doEdit() {
        var code = $.trim($("#form-update-code").val());
        var name = $.trim($("#form-update-name").val());
        if (code == "") {
            alert("角色编码不能为空!");
            return false;
        }
        if (name == "") {
            alert("角色名称不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/role/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    refreshList();
                } else {
                    alert(data);
                }
            }
        });
    }
    function refreshList() {
        $("#edit").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
