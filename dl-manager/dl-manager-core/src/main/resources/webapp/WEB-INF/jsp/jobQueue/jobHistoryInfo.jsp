<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container" >
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <!-- 查询 选项 -->
                <div class="row">
                    <form class="form-horizontal">
                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>

                        <button class="btn" type="reset" onclick="back2Main();">
                            返回
                            <i class="ace-icon fa fa-undo bigger-110"></i>
                        </button>

                        <input id="queue_id" type="hidden" name="job_name" value="${queue_id}">
                    </form>
                </div>




                <div class="row">
                    <table id="jobExecutionListTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>id</td>
                            <td>Job名称</td>
                            <td>运行状态</td>
                            <td>所在节点</td>
                            <td>任务平均流量</td>
                            <td>记录写入速度</td>
                            <td>完成百分比</td>
                            <td>启动时间</td>
                            <td>完成时间</td>
                            <td>读出记录总数</td>
                            <td>读写失败总数</td>
                            <td>等待读的时间(秒)</td>
                            <td>等待写的时间(秒)</td>
                            <td>异常</td>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>

        </div>
        <!-- /.page-content -->

        <div id="exception-wizard" class="modal">
            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="exception-content" class="col-sm-12" rows="25" style="font-size: 10px" readonly></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer wizard-actions">
                        <button type="button" class="btn btn-danger" data-dismiss="modal">
                            取消
                            <i class="ace-icon fa fa-times"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>





    </div>
</div>
<script type="text/javascript">
    var jobExecutionListTable;
    $(".chosen-select").chosen();

    function back2Main() {
        $("#jobRunHistoryInfoList").hide();
        $("#mainContentInner").show();
        jobRunQueueTable.ajax.reload();
    }


    $("#search").click(function () {
        jobExecutionListTable.ajax.reload();
    })

    jobExecutionListTable = $('#jobExecutionListTable').DataTable({
        "bAutoWidth": true,
        "serverSide" : true,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/jobQueue/initJobQueueExecutionInfo",
            "data": function (d) {
                d.queue_id = $("#queue_id").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data":"id"},
            {"data": "job_name"},
            {"data": "state"},
            {"data": "worker_address"},
            {"data": "byte_speed_per_second"},
            {"data": "record_speed_per_second"},
            {"data": "percentage"},
            {"data": "start_time"},
            {"data": "end_time"},
            {"data": "total_record"},
            {"data": "total_error_records"},
            {"data": "wait_reader_time"},
            {"data": "wait_writer_time"},
            {
                "data":"id",
                "bSortable":false,
                "sWidth":"10%",
                "fnCreatedCell":function (nTd, sData, oData, iRow, iCol){
                    var msg = oData.exception;
                    if(msg!=null && msg!=''){
                        $(nTd).html("" +
                                "<div class='radio'>"+
                                "<label>" +
                                "<a href='javascript:showExceptionInfo("+oData.id+")'>查看</a>" +
                                "</label>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                    else {
                        $(nTd).html("");
                    }
                }
            }
        ]
    });


    function showExceptionInfo(id) {
        $.ajax({
            type : "post",
            url : "${basePath}/jobExecution/doStat",
            dataType : "json",
            data : "id="+id,
            async : true,
            success : function(data) {
                if(data!=null && data!='') {
                    $("#exception-content").val(data);
                    $('#exception-wizard').modal('show');
                }

            }
        });
    }




</script>