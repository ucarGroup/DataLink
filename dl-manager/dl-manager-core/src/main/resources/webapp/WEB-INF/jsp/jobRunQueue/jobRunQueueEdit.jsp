<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<%@ include file="/WEB-INF/jsp/jobConfig/jobInclude.jsp" %>
<!--
<link rel="stylesheet" href="${basePath}/assets/css/jsoneditor.css" />
<script src="${basePath}/assets/js/jsoneditor.js"></script>
-->
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="update_form" class="form-horizontal" role="form">

                <input type="hidden" id="queue_id" name="id" value="${view.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-job_id_list">job id列表</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-update-job_id_list" name="form-update-job_id_list" value="${view.jobIdList}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


            </form>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="edit();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    修改
                </button>

                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

    function back2Main() {
        $("#jobRunQueueEdit").hide();
        $("#mainContentInner").show();
        jobRunQueueTable.ajax.reload();
    }

    function edit() {
        var id = $('#queue_id').val();
        var jobIdList = $('#form-update-job_id_list').val();


        var json = "id="+id+"&jobIdList="+jobIdList;
        $.ajax({
            type: "post",
            url: "${basePath}/jobRunQueue/doUpdateInitStateJobQueue",
            dataType: "json",
            data: json,
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }


</script>
