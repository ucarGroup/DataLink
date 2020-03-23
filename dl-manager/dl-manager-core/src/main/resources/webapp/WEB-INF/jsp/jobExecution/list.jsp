<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <!-- 查询 选项 -->
                <div class="row">
                    <form class="form-horizontal">
                        <div class="row">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">运行状态</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="stateType" style="width:100%">
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
                                <label class="col-sm-4 control-label">源库类型</label>

                                <div class="col-sm-8">
                                    <select id="srcType" class="width-100 chosen-select" id="srcType"
                                            style="width:100%">
                                        <option selected="selected" value="-1">全部</option>
                                        <option value="ElasticSearch">ElasticSearch</option>
                                        <option value="HBase">HBase</option>
                                        <option value="HDFS">HDFS</option>
                                        <option value="MySql">MySql</option>
                                        <option value="SqlServer">SqlServer</option>
                                        <option value="PostgreSql">PostgreSql</option>
                                        <option value="SDDL">SDDL</option>
                                        <option value="ORACLE">ORACLE</option>
                                        <option value="HANA">HANA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">源库名称</label>

                                <div class="col-sm-8">
                                    <select id="srcName" class="srcName width-100 chosen-select" id="srcName"
                                            style="width:100%">
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">介质名称</label>

                                <div class="col-sm-8">
                                    <select class="mediaName width-100 chosen-select" id="mediaName" style="width:100%">
                                    </select>
                                </div>
                            </div>
                        </div>
                        <!-- end row -->

                        <div class="row">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">目标类型</label>

                                <div class="col-sm-8">
                                    ‍‍<select id="destType" class="width-100 chosen-select" id="destType"
                                              style="width:100%">
                                    <option selected="selected" value="-1">全部</option>
                                    <option value="ElasticSearch">ElasticSearch</option>
                                    <option value="HBase">HBase</option>
                                    <option value="HDFS">HDFS</option>
                                    <option value="MySql">MySql</option>
                                    <option value="SqlServer">SqlServer</option>
                                    <option value="PostgreSql">PostgreSql</option>
                                    <option value="SDDL">SDDL</option>
                                    <option value="KUDU">KUDU</option>
                                </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">目标名称</label>

                                <div class="col-sm-8">
                                    <select class="destName width-100 chosen-select" id="destName" style="width:100%">
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">Job名称</label>

                                <div class="col-sm-8">
                                    <input id="jobName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">运行id</label>
                                <div class="col-sm-8">
                                    <input id="executionId" type="text" style="width:100%;">
                                </div>
                            </div>



                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">开始时间</label>
                                <div class="col-sm-8">
                                    <input id="startTime" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">结束时间</label>
                                <div class="col-sm-8">
                                    <input id="endTime" type="text" style="width:100%;">
                                </div>
                            </div>




                            <div class="col-xs-2">
                                <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                            </div>
                        </div>
                    </form>
                </div>


                <div class="row">
                    <table id="jobExecutionTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
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

        </div>
        <!-- /.page-content -->


        <div id="exception-wizard" class="modal">
            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="exception-content" class="col-sm-12" rows="25" style="font-size: 10px"
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
</div>
<script type="text/javascript">
    var jobListTable;
    $(".chosen-select").chosen();

    //$("#form-restart-newTimeStamps").datetimepicker(
    //        {
    //            format: 'YYYY-MM-DD HH:mm:ss'
    //        }
    //);


    jobListTable = $('#jobExecutionTable').DataTable({
        "bAutoWidth": true,
        "serverSide": true,//开启服务器模式:启用服务器分页
        "paging": true,//是否分页
        "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite": "true",
        "sScrollX": "100%",
        "ajax": {
            "url": "${basePath}/jobExecution/initJob",
            "data": function (d) {
                d.stateType = $("#stateType").val();
                d.srcType = $("#srcType").val();
                d.destType = $("#destType").val();
                d.srcName = $("#srcName").val();
                d.destName = $("#destName").val();
                d.mediaName = $("#mediaName").val();
                d.jobName = $("#jobName").val();
                d.executionId = $("#executionId").val();
                d.startTime = $("#startTime").val();
                d.endTime = $("#endTime").val();
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
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var msg = oData.exception;
                    if (msg != null && msg != '') {
                        $(nTd).html("" +
                                "<div class='radio'>" +
                                "<label>" +
                                "<a href='javascript:showExceptionInfo(" + oData.id + ")'>查看</a>" +
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
                    var name = "\"" + oData.job_name + "\"";
                    getButtons([
                        {
                            code: '005002003',
                            html: function () {
                                var str;
                                if (state == "RUNNING" && abandonedValue == 0) {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:doStop(" + oData.id + "," + name + ")' class='blue'  title='停止'>" +
                                    "<i class='ace-icon fa fa-power-off bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005002004',
                            html: function () {
                                var str;
                                if (state == "RUNNING" && abandonedValue == 0) {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:doForceStop(" + oData.id + "," + name + ")' class='red'  title='强制停止'>" +
                                    "<i class='ace-icon fa fa-hand-rock-o bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005002005',
                            html: function () {
                                var str;
                                if (abandonedValue == 1) {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:doDiscard(" + oData.id + ")' class='yellow'  title='废弃'>" +
                                    "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005002006',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:showConfigInfo(" + oData.id + ")' class='yellow'  title='查看历史配置'>" +
                                        "<i class='ace-icon fa fa-ambulance bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"

                                return str;
                            }
                        }
                    ], $(nTd));

                }
            }
        ]
    });


    var src_RMDBS = "<option selected=\"selected\" value=\"-1\">全部</option>" +
            "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>";

    var src_HBase = "<option selected=\"selected\" value=\"-1\">全部</option>" +
            "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"HDFS\"  >HDFS</option>";

    var src_HDFS = "<option selected=\"selected\" value=\"-1\">全部</option>" +
            "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>";

    var src_ES = "<option selected=\"selected\" value=\"-1\">全部</option>" +
            "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>";

    var all = "<option value=\"-1\">全部</option>"

    $("#srcType").change(function () {
        var type_name = $('#srcType').val();
        if (type_name == "-1") {
            $("").appendTo(".srcName");
            $(".srcName").trigger("chosen:updated");
            $("").appendTo(".mediaName");
            $(".mediaName").trigger("chosen:updated");
            return;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChange?name=" + type_name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    document.getElementById("srcName").innerHTML = "";
                    $("<option value=\"-1\">全部</option>").appendTo(".srcName");
                    for (i = 0; i < result.num.length; i++) {
                        $("<option value='" + result.num[i] + "' >" + result.val[i] + "</option>").appendTo(".srcName");
                    }
                    $(".srcName").trigger("chosen:updated");
                }
                else {
                    alert(result);
                }
            }
        });
        //jobListTable.ajax.reload();
    })

    $("#destType").change(function () {
        var type_name = $('#destType').val();
        if (type_name == "-1") {
            $("").appendTo(".destName");
            $(".destName").trigger("chosen:updated");
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChange?name=" + type_name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    document.getElementById("destName").innerHTML = "";
                    $("<option value=\"-1\">全部</option>").appendTo(".destName");
                    for (i = 0; i < result.num.length; i++) {
                        $("<option value='" + result.num[i] + "' >" + result.val[i] + "</option>").appendTo(".destName");
                    }
                    $(".destName").trigger("chosen:updated");
                }
                else {
                    alert(result);
                }
            }
        });
        //jobListTable.ajax.reload();
    })


    $("#srcName").change(function () {
        var srcID = $('#srcName').val();
        if (srcID == "-1") {
            $("").appendTo(".mediaName");
            $(".mediaName").trigger("chosen:updated");
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/namespaceContent?id=" + srcID,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    document.getElementById("mediaName").innerHTML = "";
                    $("<option value=\"-1\">全部</option>").appendTo(".mediaName");
                    for (i = 0; i < result.length; i++) {
                        $("<option value='" + result[i] + "' >" + result[i] + "</option>").appendTo(".mediaName");
                    }
                    $(".mediaName").trigger("chosen:updated");
                }
                else {
                    alert("无法获取元数据信息");
                }
            }
        });
    })

    $("#search").click(function () {
        jobListTable.ajax.reload();
    })


    function doStart(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/jobExecution/doStart?id=" + id,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("启动成功！");
                    jobListTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    function doStop(id, jobName) {
        if (confirm("确定要停止任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobExecution/doStop?id=" + id + "&jobName=" + jobName,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("停止任务成功！");
                        jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doForceStop(id, jobName) {
        if (confirm("确定要强制停止任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobExecution/doForceStop?id=" + id + "&jobName=" + jobName,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("强制停止任务成功！");
                        jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
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
                        jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function showExceptionInfo(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/jobExecution/doStat",
            dataType: "json",
            data: "id=" + id,
            async: true,
            success: function (data) {
                if (data != null && data != '') {
                    $("#exception-content").val(data);
                    $('#exception-wizard').modal('show');
                }

            }
        });
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