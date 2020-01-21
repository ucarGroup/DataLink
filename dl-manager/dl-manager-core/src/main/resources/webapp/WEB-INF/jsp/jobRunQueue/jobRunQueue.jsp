<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="jobRunQueueConfigInfoList" class="main-container">
</div>
<div id="jobRunQueueExecutionInfoList" class="main-container">
</div>
<div id="jobRunQueueEdit" class="main-container">
</div>

<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">


            <div class="row">
                <div class="form-group col-xs-3">
                    <label class="col-sm-4 control-label">队列状态</label>

                    <div class="col-sm-8">
                        <select class="width-100 chosen-select" id="stateType" style="width:100%">
                            <option selected="selected" value="-1">全部</option>
                            <option value="INIT">INIT</option>
                            <option value="READY">READY</option>
                            <option value="PROCESSING">PROCESSING</option>
                            <option value="STOP">STOP</option>
                            <option value="FAILED">FAILED</option>
                            <option value="SUCCEEDED">SUCCEEDED</option>
                        </select>
                    </div>
                </div>

                <div class="col-xs-2">
                    <button type="flush_button" id="search" class="btn btn-sm btn-purple">刷新</button>
                </div>

                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="col-xs-12">
                    <div class="row">
                        <table id="jobRunQueueTable" class="table table-striped table-bordered table-hover"
                               style="text-align: left;width:100%">
                            <thead>
                            <tr>
                                <td>id</td>
                                <td>Job id 列表</td>
                                <td>正在处理的id列表</td>
                                <td>队列job数量</td>
                                <td>失败的job数量</td>
                                <td>成功的job数量</td>
                                <td>队列状态</td>
                                <td>创建时间</td>
                                <td>更新时间</td>
                                <td>操作</td>
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
        var jobRunQueueTable;
        $(".chosen-select").chosen();

        getButtons([
            {
                code: "005003011",
                html: '<div id="open_job_queue_div" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="doOpenJobQueue();">开启job队列</button> </p>' +
                '</div>'
            },
            {
                code: "005003011",
                html: '<div id="close_job_queue_div" style="display:none" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="doCloseJobQueue();">关闭job队列</button> </p>' +
                '</div>'
            }], $("#OperPanel"));

        jobRunQueueTable = $('#jobRunQueueTable').DataTable({
            "bAutoWidth": true,
            "serverSide": true,//开启服务器模式:启用服务器分页
            "paging": true,//是否分页
            "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
            "bScrollInfinite": "true",
            "sScrollX": "100%",
            "ajax": {
                "url": "${basePath}/jobRunQueue/initJobQueue",
                "data": function (d) {
                    d.stateType = $("#stateType").val();
                    return JSON.stringify(d);
                },
                "dataType": 'json',
                "contentType": 'application/json',
                "type": 'POST'
            },
            "columns": [
                {"data": "id"},
                {"data": "jobIdList"},
                {"data": "currentPorcessId"},
                {"data": "jobCount"},
                {"data": "successCount"},
                {"data": "failureCount"},
                {"data": "queueState"},
                {"data": "createTime"},
                {"data": "modifyTime"},
                {
                    "data": "id",
                    "bSortable": false,
                    "sWidth": "10%",
                    "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                        var msg = oData.queueState;
                        //alert(oData.id +"\t"+msg);
                        getButtons([
                            {
                                code: '005003003',
                                html: function () {
                                    var str;
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:showQueueInfo(" + oData.id + ")' class='yellow'  title='查看'>" +
                                    "<i class='ace-icon fa fa-history bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            },
                            {
                                code: '005003002',
                                html: function () {
                                    var str;
                                    if ("INIT" == msg || "READY" == msg || "SUCCEEDED" == msg || "FAILED" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                        "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003009',
                                html: function () {
                                    var str;
                                    if ("INIT" == msg || "READY" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doTop(" + oData.id + ")' class='green'  title='置顶'>" +
                                        "<i class='ace-icon fa fa-hand-pointer-o bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003010',
                                html: function () {
                                    var str;
                                    if ("INIT" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doReady(" + oData.id + ")' class='blue'  title='启动'>" +
                                        "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003005',
                                html: function () {
                                    var str;
                                    if ("INIT" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:toEdit(" + oData.id + ")' class='blue'  title='修改'>" +
                                        "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003010',
                                html: function () {
                                    var str;
                                    if ("READY" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doInit(" + oData.id + "," + name + ")' class='red'  title='还原为INIT'>" +
                                        "<i class='ace-icon fa fa-hand-rock-o bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003012',
                                html: function () {
                                    var str;
                                    if ("SUCCEEDED" == msg || "FAILED" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doRestart(" + oData.id + ")' class='yellow'  title='再次执行'>" +
                                        "<i class='ace-icon fa fa-paperclip bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003007',
                                html: function () {
                                    var str;
                                    if ("SUCCEEDED" == msg || "FAILED" == msg || "PROCESSING" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doExecutionInfo(" + oData.id + ")' class='red'  title='运行详情'>" +
                                        "<i class='ace-icon fa fa-hand-rock-o bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003010',
                                html: function () {
                                    var str;
                                    if ("PROCESSING" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doStop(" + oData.id + ")' class='blue'  title='停止'>" +
                                        "<i class='ace-icon fa fa-power-off bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            },
                            {
                                code: '005003010',
                                html: function () {
                                    var str;
                                    if ("STOP" == msg) {
                                        str = "<div class='radio'>" +
                                        "<a href='javascript:doStart(" + oData.id + ")' class='blue'  title='启动'>" +
                                        "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    }
                                    return str;
                                }
                            }
                        ], $(nTd));


                    }
                }
            ]
        });


        $("#search").click(function () {
            jobRunQueueTable.ajax.reload();
        })

        function doOpenJobQueue() {
            if (confirm("确定要开启吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doOpenOrCloseQueue?state=open",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("启动成功！");
                        } else {
                            alert(data);
                        }
                    }
                });
            }
            document.getElementById("open_job_queue_div").style.display = "none";
            document.getElementById("close_job_queue_div").style.display = "";
        }

        function doCloseJobQueue() {
            if (confirm("确定要关闭吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doOpenOrCloseQueue?state=close",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("关闭成功！");
                        } else {
                            alert(data);
                        }
                    }
                });
            }
            document.getElementById("open_job_queue_div").style.display = "";
            document.getElementById("close_job_queue_div").style.display = "none";
        }


        function showQueueInfo(id) {
            $("#jobRunQueueConfigInfoList").load("${basePath}/jobRunQueue/toJobRunQueueConfigInfo?queue_id=" + id);
            $("#jobRunQueueConfigInfoList").show();
            $("#mainContentInner").hide();
        }

        function doDelete(id) {
            if (confirm("确定要删除数据吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doDeleteJobQueue?id=" + id,
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("删除成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }


        function doStart(id) {
            if (confirm("确定要启动吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doUpdateState?id=" + id + "&state=PROCESSING",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("启动成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }


        function doStop(id) {
            if (confirm("确定要停止吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doUpdateState?id=" + id + "&state=STOP",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("停止成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }


        function doRestart(id) {
            alert("目前不支持！");
            return;
            if (confirm("是否要再次执行？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doUpdateJobRunQueueById?id=" + id,
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("再次启动成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }


        function doExecutionInfo(id) {
            $("#jobRunQueueExecutionInfoList").load("${basePath}/jobRunQueue/toExecutionInfo?queue_id=" + id);
            $("#jobRunQueueExecutionInfoList").show();
            $("#mainContentInner").hide();
        }

        function doTop(id) {
            if (confirm("是否要置顶？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doTop?id=" + id,
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("置顶成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }

        function doReady(id) {
            if (confirm("确定要启动吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doUpdateState?id=" + id + "&state=READY",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("启动成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });

                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doOpenOrCloseQueue?state=open",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
                document.getElementById("open_job_queue_div").style.display = "none";
                document.getElementById("close_job_queue_div").style.display = "";
            }
        }

        function doInit(id) {
            if (confirm("确定要还原吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobRunQueue/doUpdateState?id=" + id + "&state=INIT",
                    dataType: "json",
                    async: false,
                    error: function (xhr, status, err) {
                        alert(err);
                    },
                    success: function (data) {
                        if (data == "success") {
                            alert("还原成功！");
                            jobRunQueueTable.ajax.reload();
                        } else {
                            alert(data);
                        }
                    }
                });
            }
        }

        function toEdit(id) {
            $("#jobRunQueueEdit").load("${basePath}/jobRunQueue/toEdit?queue_id=" + id);
            $("#jobRunQueueEdit").show();
            $("#mainContentInner").hide();
        }


    </script>
</div>