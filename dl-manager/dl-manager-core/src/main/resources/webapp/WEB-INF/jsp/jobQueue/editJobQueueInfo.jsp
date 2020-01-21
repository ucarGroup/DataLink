<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<!--
<link rel="stylesheet" href="${basePath}/assets/css/jsoneditor.css" />
<script src="${basePath}/assets/js/jsoneditor.js"></script>
-->
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="start_form" class="form-horizontal" role="form">

                <input type="hidden" id="job_id" name="queue_id" value="${queueId}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="job_queue_name">队列的名称</label>
                    <div class="col-sm-9">
                        <input type="text" id="job_queue_name" name="job_queue_name" readonly="readonly"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" value="${queueName}"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="job_queue_mail">队列所属人的邮箱前缀</label>
                    <div class="col-sm-9">
                        <input type="text" id="job_queue_mail" name="job_queue_mail" value="${mail}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-timing_yn">job执行时候停止后续执行？</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-edit-timing_yn" style="width:350px;height:35px" class="chosen-select col-sm-5" name="job_fail_to_stop" >
                        <c:if test="${failToStop=='true'}">
                            <option grade="0" value="false" >否</option>
                            <option grade="1" value="true" selected>是</option>
                        </c:if>
                        <c:if test="${failToStop=='false'}">
                            <option grade="0" value="false" selected>否</option>
                            <option grade="1" value="true" >是</option>
                        </c:if>
                    </select>
                    </div>
                </div>


            </form>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doAdd();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    保存
                </button>

                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="back2showQueueInfoList();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>

    </div>
                    <!-- /.page-content -->
</div>
<script type="text/javascript">

    function back2showQueueInfoList() {
        $("#edit").hide();
        $("#showQueueInfoList").show();
        //jobQueueListTable.ajax.reload();
    }

    function doAdd() {
        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/doEditJobQueueInfo",
            dataType: "json",
            data: $("#start_form").serialize(),
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
        $("#showQueueInfoList").show();
    }

</script>
