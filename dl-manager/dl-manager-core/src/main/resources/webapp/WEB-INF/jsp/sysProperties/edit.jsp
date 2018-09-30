<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <input type="hidden" name="id" value="${sysPropertiesInfo.id}">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-properties-key">参数key</label>

                    <div class="col-sm-9">
                        <input type="text" value="${sysPropertiesInfo.propertiesKey}" style="width:350px;height:35px"
                               id="form-update-properties-key" name="propertiesKey" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-properties-value">参数值</label>

                    <div class="col-sm-9">
                        <input type="text" value="${sysPropertiesInfo.propertiesValue}" style="width:350px;height:35px"
                               id="form-update-properties-value" name="propertiesValue" class="col-xs-10 col-sm-5"/>
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

    function doEdit() {
        var propertiesKey = $.trim($("#form-update-properties-key").val());
        var propertiesValue = $.trim($("#form-update-properties-value").val());

        if (propertiesKey == "") {
            alert("参数key不能为空!");
            return false;
        }
        if (propertiesValue == "") {
            alert("参数值不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/sysProperties/doEdit",
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