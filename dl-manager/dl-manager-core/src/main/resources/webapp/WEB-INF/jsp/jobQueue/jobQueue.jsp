<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="create" class="main-container">
</div>
<div id="jobRunHistoryInfoList" class="main-container">
</div>
<div id="showQueueInfoList" class="main-container">
</div>
<div id="edit" class="main-container">
</div>

<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">


            <div class="row">




                <div class="form-group col-xs-3">
                    <label class="col-sm-4 control-label">队列名称</label>

                    <div class="col-sm-8" id="srcNameDiv">
                        <select id="srcName" class="srcName width-100 chosen-select" style="width:100%">
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
                                <td>Job id</td>
                                <td>Job名称</td>
                                <td>所属的队列名称</td>
                                <td>表名称</td>
                                <td>创建时间</td>
                                <td>当前状态</td>
                                <td>操作</td>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>

            </div>
            <!-- /.page-content -->

            <div id="current-stat-wizard" class="modal">
                <div class="modal-dialog">
                    <div class="modal-content" style="width: 800px;margin-left: -100px;">
                        <div>
                            <div class="modal-body">
                                <div>
                                    <textarea id="current-stat-content" class="col-sm-12" rows="25" style="font-size: 10px" readonly></textarea>
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
                html: '<div id="close_job_queue_div" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="doCloseJobQueue();">关闭job队列</button> </p>' +
                '</div>'
            },
            {
                code: "005003012",
                html: '<div id="create_job_queue_div" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="toCreteJobQueue();">创建一个队列</button> </p>' +
                '</div>'
            },
            {
                code: "005003012",
                html: '<div id="create_job_queue_info_div" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="toShowQueueInfoList();">查看所有队列</button> </p>' +
                '</div>'
            },
            {
                code: "005003012",
                html: '<div id="create_job_queue_info_div" class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
                '<p> <button class="btn btn-sm btn-info" onclick="toShowCurrentState();">当前执行状态</button> </p>' +
                '</div>'
            }
        ], $("#OperPanel"));

        jobRunQueueTable = $('#jobRunQueueTable').DataTable({
            "bAutoWidth": true,
            "serverSide": true,//开启服务器模式:启用服务器分页
            "paging": true,//是否分页
            "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
            "bScrollInfinite": "true",
            "sScrollX": "100%",
            "ajax": {
                "url": "${basePath}/jobQueue/initJobQueue",
                "data": function (d) {
                    d.srcName = $("#srcName").val();
                    return JSON.stringify(d);
                },
                "dataType": 'json',
                "contentType": 'application/json',
                "type": 'POST'
            },
            "columns": [
                {"data": "id"},
                {"data": "jobId"},
                {"data": "jobName"},
                {"data": "queueName"},
                {"data": "tableName"},
                {"data": "createTime"},
                {"data": "jobState"},
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
                                    "<a href='javascript:showHistroyInfo(" + oData.id + ")' class='yellow'  title='运行详情'>" +
                                    "<i class='ace-icon fa fa-history bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            },
                            {
                                code: '005003002',
                                html: function () {
                                    var str;
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                            "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
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
            //doReload_queueListInfo();
        })


        function doReload_queueListInfo() {
            $.ajax({
                type: "post",
                url: "${basePath}/jobQueue/QueueListInfo",
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
        }


        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/QueueListInfo",
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


        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/doJobQueueInfoState",
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "true") {
                    document.getElementById("open_job_queue_div").style.display = "none";
                    document.getElementById("close_job_queue_div").style.display = "";
                } else {
                    document.getElementById("open_job_queue_div").style.display = "";
                    document.getElementById("close_job_queue_div").style.display = "none";
                }
            }
        });


        function toCreteJobQueue() {
            $.ajaxSetup({cache: true});
            $("#create").load("${basePath}/jobQueue/toCreateQueue");
            $("#create").show();
            $("#mainContentInner").hide();

        }


        function toShowQueueInfoList() {
            $.ajaxSetup({cache: true});
            $("#showQueueInfoList").load("${basePath}/jobQueue/toShowQueueInfoList");
            $("#showQueueInfoList").show();
            $("#mainContentInner").hide();
        }

        function toShowCurrentState() {
            $.ajax({
                type : "post",
                url : "${basePath}/jobQueue/showCurrentState",
                dataType : "json",
                data : "",
                async : true,
                success : function(data) {
                    if(data!=null && data!='') {
                        $("#current-stat-content").val(data);
                        $('#current-stat-wizard').modal('show');
                    }

                }
            });
        }




        function reset() {
            $("#create").empty();
            $("#showQueueInfoList").empty();
            $("#jobRunHistoryInfoList").empty();
        }


        function doOpenJobQueue() {
            if (confirm("确定要开启吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobQueue/doOpenOrCloseQueue?state=open",
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
                    url: "${basePath}/jobQueue/doOpenOrCloseQueue?state=close",
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



        function doDelete(id) {
            if (confirm("确定要删除数据吗？")) {
                $.ajax({
                    type: "post",
                    url: "${basePath}/jobQueue/doDeleteJobQueue?id=" + id,
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


        function showHistroyInfo(id) {
            $("#jobRunHistoryInfoList").load("${basePath}/jobQueue/toHistoryInfo?queue_id=" + id);
            $("#jobRunHistoryInfoList").show();
            $("#mainContentInner").hide();
        }




    </script>
</div>