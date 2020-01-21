<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-lab-name-key">机房名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-lab-name-key" name="labName"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-lab-desc-value">机房描述</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-lab-desc-value" name="labDesc"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-ip-rule-value">ip规则</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-ip-rule-value" name="ipRule"
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
    $('.roleId').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});

    $('.roleIdClass').select2({
        allowClear: false
    });

    function doAdd() {

        var name = $.trim($("#form-add-lab-name-key").val());
        var desc = $.trim($("#form-add-lab-desc-value").val());

        if (name == "") {
            alert("机房名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("机房描述不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/lab/doAdd",
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