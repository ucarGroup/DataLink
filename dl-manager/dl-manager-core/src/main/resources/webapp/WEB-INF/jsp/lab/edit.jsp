<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <input type="hidden" name="id" value="${labInfo.id}">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-lab-name-key">机房名称</label>

                    <div class="col-sm-9">
                        <input type="text" value="${labInfo.labName}" style="width:350px;height:35px"
                               id="form-update-lab-name-key" name="labName" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-lab-desc-value">机房描述</label>

                    <div class="col-sm-9">
                        <input type="text" value="${labInfo.labDesc}" style="width:350px;height:35px"
                               id="form-update-lab-desc-value" name="labDesc" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-ip-rule-value">ip规则</label>

                    <div class="col-sm-9">
                        <input type="text" value="${labInfo.ipRule}" style="width:350px;height:35px"
                               id="form-update-ip-rule-value" name="ipRule" class="col-xs-10 col-sm-5"/>
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
    $("#form-update-isAlarm").val('${userInfo.isAlarm}');
    $("#form-update-isReceiveDataxMail").val('${userInfo.isReceiveDataxMail}');
    $('.roleId').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});

    var pe = '${userInfo.roleIdStr}'.split(",");
    $('.roleIdClass').val(pe).select2({
        allowClear: false
    });

    function doEdit() {
        var name = $.trim($("#form-update-lab-name-key").val());
        var desc = $.trim($("#form-update-lab-desc-value").val());

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
            url: "${basePath}/lab/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
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