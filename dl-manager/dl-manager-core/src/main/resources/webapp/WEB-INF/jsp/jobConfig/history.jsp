<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="col-xs-12">
                    <div class="row">
                        <form class="form-horizontal">
                            <div class="row">

                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">运行状态</label>
                                    <div class="col-sm-8">
                                        <select class="width-100 chosen-select" id="stateType"
                                                style="width:100%">
                                            <option selected="selected" value="-1">全部</option>
                                            <option value="UNEXECUTE">UNEXECUTE</option>
                                            <option value="RUNNING">RUNNING</option>
                                            <option value="KILLED">KILLED</option>
                                            <option value="FAILED">FAILED</option>
                                            <option value="SUCCEEDED">SUCCEEDED</option>
                                            <option value="ABANDONED">ABANDONED</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group col-xs-3">
                                    <div class="col-xs-2">
                                        <div class="col-xs-2">
                                            <input id="job_name" type="hidden" name="job_name" value="${job_name}">
                                        </div>
                                        <div class="col-xs-2">
                                            <button type="button" id="refresh_job" class="btn btn-sm btn-purple">刷新</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>


                <div class="row">
                    <table id="jobHistoryListTable" class="table table-striped table-bordered table-hover">
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
                            <td>操作</td>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>

            <div class="clearfix form-actions">
                <div class="col-md-offset-5 col-md-7">
                    <button class="btn" type="reset" onclick="back2Main();">
                        返回
                        <i class="ace-icon fa fa-undo bigger-110"></i>
                    </button>
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


        <div id="json-wizard" class="modal">
            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="json-content" class="col-sm-12" rows="25" style="font-size: 10px"
                                          readonly></textarea>
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

<script type="text/javascript">
    var jobHistoryListTable;
    $(".chosen-select").chosen();

    jobHistoryListTable = $('#jobHistoryListTable').DataTable({
        "bAutoWidth": true,
        "serverSide" : true,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/jobConfig/doHistory",
            "data": function (d) {
                d.statType = $("#stateType").val();
                d.job_name = $("#job_name").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
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
                                "<a href='javascript:showErrHistoryInfo("+oData.id+")'>查看</a>" +
                                "</label>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                    else {
                        $(nTd).html("");
                    }
                }
            },
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var state = oData.state;
                    var abandonedValue = oData.abandonedValue;
                    var name = "\""+oData.job_name+"\"";
                    if(state=="RUNNING" && abandonedValue==0) {
                        $(nTd).html("" +
                                "<div class='radio'>" +
                                "<a href='javascript:doStop(" + oData.id + ","+ name +")' class='blue'  title='停止'>" +
                                "<i class='ace-icon fa fa-power-off bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;" +

                                "<div class='radio'>" +
                                "<a href='javascript:doForceStop(" + oData.id + ","+ name +")' class='red'  title='强制停止'>" +
                                "<i class='ace-icon fa fa-hand-rock-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;" +

                                "<div class='radio'>" +
                                "<a href='javascript:showConfigInfo(" + oData.id + ")' class='yellow'  title='查看历史配置'>" +
                                "<i class='ace-icon fa fa-ambulance bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }

                    else if(abandonedValue==1){
                        $(nTd).html("" +
                                "<div class='radio'>" +
                                "<a href='javascript:doDiscard(" + oData.id + ")' class='yellow'  title='废弃'>" +
                                "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp" +

                                "<div class='radio'>" +
                                "<a href='javascript:showConfigInfo(" + oData.id + ")' class='yellow'  title='查看历史配置'>" +
                                "<i class='ace-icon fa fa-ambulance bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                    else {
                        $(nTd).html("" +
                                "<div class='radio'>" +
                                "<a href='javascript:showConfigInfo(" + oData.id + ")' class='yellow'  title='查看历史配置'>" +
                                "<i class='ace-icon fa fa-ambulance bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                }
            }
        ]
    });


    $("#refresh_job").click(function () {
        jobHistoryListTable.ajax.reload();
    })

    $("#stateType").change(function () {
        jobHistoryListTable.ajax.reload();
    })

    function back2Main() {
        $("#history").hide();
        $("#mainContentInner").show();
        //jobListTable.ajax.reload();
    }


    function doStop(id,jobName) {
        if (confirm("确定要停止任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobExecution/doStop?id=" + id +"&jobName="+jobName,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("停止任务成功！");
                        jobHistoryListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doForceStop(id,jobName) {
        if (confirm("确定要强制停止任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobExecution/doForceStop?id=" + id +"&jobName="+jobName,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("强制停止任务成功！");
                        jobHistoryListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function showErrHistoryInfo(id) {
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


    function doDiscard(id) {
        if (confirm("确定要丢弃任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobExecution/doDiscard?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("丢弃任务成功！");
                        jobHistoryListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function showConfigInfo(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/jobExecution/doConfig",
            dataType: "json",
            data: "id=" + id,
            async: true,
            success: function (data) {
                if (data != null && data != '') {
                    $("#json-content").val(data);
                    $('#json-wizard').modal('show');
                }

            }
        });
    }


</script>