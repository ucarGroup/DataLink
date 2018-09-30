<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-code">编码</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-code" name="code"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-name" name="name"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

            </form>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doAdd()">
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
    $('.code').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});
    function doAdd() {
        var code = $.trim($("#form-add-code").val());
        var name = $.trim($("#form-add-name").val());
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
            url: "${basePath}/role/doAdd",
            dataType: "json",
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("添加成功！");
                    refresh();
                } else {
                    alert(data);
                }
            }
        });
    }
    function refresh() {
        $("#add").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
