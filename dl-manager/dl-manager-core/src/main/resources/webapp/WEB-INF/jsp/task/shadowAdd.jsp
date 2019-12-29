<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <input type="hidden" value="${taskId}" id="taskId">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-mappingIds">增量映射</label>

                    <div class="col-sm-6">
                        <select multiple="" name="mappingIds"
                                class="form-add-src-name col-sm-4"
                                data-placeholder="Click to Choose..." id="form-add-mappingIds"
                                style="width:100%;">
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-newTimeStamps">重置时间</label>

                    <div class="col-sm-8">
                        <input class="col-sm-4" type='text' id='form-add-newTimeStamps' name="newTimeStamps"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-note">备注</label>

                    <div class="col-sm-8">
                        <input class="col-sm-4" type='text' id='form-add-note' name="note"/>
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

    $('.form-add-src-name').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $("#add_form").ready(function () {
        $("#form-add-newTimeStamps").datetimepicker(
            {
                format: 'YYYY-MM-DD HH:mm:ss'
            }
        );
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/getAllMediaMappings?taskId="+$("#taskId").val(),
            async: true,
            dataType: "json",
            data: "",
            success: function (result) {
                if (result != null && result != '') {
                    if (result.mediaMappingList != null && result.mediaMappingList.length > 0) {
                        for (var i = 0; i < result.mediaMappingList.length; i++) {
                            $("#form-add-mappingIds").append("<option value=" + "'" + result.mediaMappingList[i].id + "'" + ">" + result.mediaMappingList[i].taskName+"|"+
                                result.mediaMappingList[i].srcMediaSourceName+"|"+result.mediaMappingList[i].targetMediaSourceName+"|"+
                                result.mediaMappingList[i].srcMediaName+"|"+result.mediaMappingList[i].targetMediaName+ "</option>");
                        }
                    }
                }
            }
        });
    });

    function doAdd() {
        var mappingIds = $.trim($("#form-add-mappingIds").val());
        var newTimeStamps = $.trim($("#form-add-newTimeStamps").val());
        var taskId = $.trim($("#taskId").val());
        var note = $.trim($("#form-add-note").val());
        if (mappingIds == "") {
            alert("增量映射不能为空!");
            return false;
        }
        if (newTimeStamps == "") {
            alert("重置时间不能为空!");
            return false;
        }
        var obj = {
            mappingIds: mappingIds,
            newTimeStamps: Date.parse(new Date(newTimeStamps)),
            taskId:taskId,
            note:note
        };
        $.ajax({
            type: "post",
            url: "${basePath}/shadow/doAdd",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
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
        $("#taskShadowAdd").hide();
        $("#taskShadowMainContent").show();
        taskShadowListTable.ajax.reload();
    }
</script>