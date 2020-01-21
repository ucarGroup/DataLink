<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>

                <input id="queue_id" type="hidden" name="job_name" value="${queue_id}">

                <div class="row">
                    <table id="jobQueueTableInfo" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>任务ID</td>
                            <td>任务名称</td>
                            <td>源库名称</td>
                            <td>目标库名称</td>
                            <td>介质名称</td>
                            <td>是否定时</td>
                            <td>创建时间</td>
                        </tr>
                        </thead>
                    </table>
                </div>


            </div>
        </div>
        <!-- /.page-content -->

    </div>
</div>
<script type="text/javascript">
    var jobQueueTableInfo
    //$(".chosen-select").chosen();

    function back2Main() {
        $("#jobRunQueueConfigInfoList").hide();
        $("#mainContentInner").show();
        jobRunQueueTable.ajax.reload();
    }


    jobQueueTableInfo = $('#jobQueueTableInfo').DataTable({
        "bAutoWidth": true,
        "serverSide" : true,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/jobRunQueue/initJobQueueConfigInfo",
            "data": function (d) {
                d.queue_id = $("#queue_id").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "job_name"},
            {"data": "job_src_media_source_name"},
            {"data": "job_target_media_source_name"},
            {"data": "job_media_name"},
            {"data": "timing_yn"},
            {"data": "create_time"}
        ]
    });
    

</script>