<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="edit" class="main-container">
</div>
<div id="history" class="main-container">
</div>

<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <!-- 查询 选项 -->
                <div class="row">
                    <!-- 查询 选项 -->
                    <div class="row">
                        <form class="form-horizontal">
                            <div class="row">
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
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">源库名称</label>

                                    <div class="col-sm-8" id="srcNameDiv">
                                        <!--
                                        <select id="srcName" class="width-100 chosen-select" id="srcName" style="width:100%">
                                        </select>
                                        -->
                                        <select id="srcName" class="srcName width-100 chosen-select" style="width:100%">
                                        </select>
                                    </div>
                                </div>

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
                            </div>
                            <!-- end row -->

                            <div class="row">
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">介质名称</label>

                                    <div class="col-sm-8">
                                        <select class="mediaName width-100 chosen-select" id="mediaName" style="width:100%">
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">Job名称</label>

                                    <div class="col-sm-8">
                                        <input id="jobName" type="text" style="width:100%;">
                                    </div>
                                </div>


                                <div class="form-group col-xs-3" style="display: none;">
                                    <label class="col-sm-4 control-label">是否定时任务</label>
                                    <select id="isTiming">
                                        <option value="-1">全部</option>
                                        <option value="0">否</option>
                                        <option value="1" selected>是</option>
                                    </select>
                                </div>

                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">状态</label>
                                    <select id="schedule_state">
                                        <option selected="selected" value="-1">全部</option>
                                        <option value="1">启动</option>
                                        <option value="0">停止</option>
                                    </select>
                                </div>

                                <div class="form-group col-xs-3">
                                    <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                                </div>

                            </div>

                            <div class="row">

                            </div>
                        </form>
                    </div>
                </div>


                <div class="row">
                    <table id="jobScheduleTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>id</td>
                            <td>关联的Job名称</td>
                            <td>源库名称</td>
                            <td>目标库名称</td>
                            <td>介质名称</td>
                            <td>cron表达式</td>
                            <td>最大重试次数</td>
                            <td>重试间隔(秒)</td>
                            <td>最大运行时间(秒)</td>
                            <td>状态</td>
                            <td>操作</td>
                        </tr>
                        </thead>
                    </table>
                </div>

                <div id="start-wizard" class="modal">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div id="modal-wizard-container">
                                <div class="modal-header">
                                    <div class="modal-header no-padding">
                                        <div class="table-header">
                                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                <span class="white">&times;</span>
                                            </button>
                                            启动
                                        </div>
                                    </div>
                                </div>

                                <div class="modal-body">
                                    <form id="start_form" class="form-horizontal" role="form">
                                        <input type="hidden" name="jobId" id="form-start-jobId"/>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-start-jobNameDisp">Job名称</label>

                                            <div class="col-sm-9">
                                                <input type="text" name="jobNameDisp" id="form-start-jobNameDisp"
                                                       class="col-sm-8" readonly/>
                                            </div>
                                        </div>


                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-time-express"> 时间表达式 </label>

                                            <div class="col-sm-9" id="form-time-express">
                                                <input type="text" name="timeExpress" class="col-sm-8"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-time-on"> 从指定的表达式时间执行到现在？</label>
                                            <div class="col-sm-9">
                                                <select name="timeToContinue" id="form-time-on" class="col-sm-8">
                                                    <option value="false">否</option>
                                                    <option value="true">是</option>
                                                </select>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <div class="modal-footer wizard-actions">
                                <button class="btn btn-success" type="button" onclick="doStartSync()">
                                    <i class="ace-icon fa fa-save"></i>
                                    启动任务
                                </button>
                                <button class="btn btn-danger" type="button" data-dismiss="modal">
                                    取消
                                    <i class="ace-icon fa fa-times"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>
        <!-- /.page-content -->


    </div>
</div>
<script type="text/javascript">
    var jobScheduleTable;
    $(".chosen-select").chosen();

    $("#search").click(function () {
        jobScheduleTable.ajax.reload();
    })


    jobScheduleTable = $('#jobScheduleTable').DataTable({
        "bAutoWidth": true,
        "serverSide": true,//开启服务器模式:启用服务器分页
        "paging": true,//是否分页
        "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite": "true",
        "sScrollX": "100%",
        "ajax": {
            "url": "${basePath}/jobSchedule/initSchedule",
            "data": function (d) {
                d.isTiming = $("#isTiming").val();
                d.srcType = $("#srcType").val();
                d.destType = $("#destType").val();
                d.srcName = $("#srcName").val();
                d.destName = $("#destName").val();
                d.mediaName = $("#mediaName").val();
                d.jobName = $("#jobName").val();
                d.schedule_state = $("#schedule_state").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "job_name"},
            {"data": "src_media_name"},
            {"data": "target_media_name"},
            {"data": "media_name"},
            {"data": "schedule_cron"},
            {"data": "schedule_max_retry"},
            {"data": "schedule_retry_interval"},
            {"data": "schedule_max_runtime"},
            {"data": "schedule_state"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var schedule_state = oData.schedule_state;
                    //var name = oData.job_name;
                    var name = "\"" + oData.job_name + "\"";
                    var sync_state = oData.fillDataState;
                    //alert("sync_stat->"+sync_stat);
                    getButtons([
                        {
                            code: '005001006',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:toEdit(" + oData.id + ")' class='blue'  title='修改'>" +
                                        "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '005001008',
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
                            code: '005001007',
                            html: function () {
                                var str;
                                if (schedule_state == "启动") {
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:doStop(" + oData.id + ")' class='blue'  title='暂停任务'>" +
                                            "<i class='ace-icon fa fa-hand-paper-o bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005001008',
                            html: function () {
                                var str;
                                if (schedule_state == "暂停") {
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:doStart(" + oData.id + ")' class='blue'  title='启动任务'>" +
                                            "<i class='ace-icon fa fa-hand-o-right bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005001011',
                            html: function () {
                                var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:doHistory(" + oData.id + ")' class='yellow'  title='运行历史'>" +
                                        "<i class='ace-icon fa fa-history bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '005001012',
                            html: function () {
                                var str;

                                if(sync_state == "false") {
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:toStart(" + oData.id + "," + name + ")' class='yellow'  title='启动补数据'>" +
                                        "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005001013',
                            html: function () {
                                var str;
                                if(sync_state == "true") {
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:canalSync(" + oData.id + "," + name + ")' class='yellow'  title='关闭补数据'>" +
                                        "<i class='ace-icon fa fa-bomb bigger-130'></i>" + "</a>" +
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

    function toStart(id, job_name) {
        $('#form-start-jobId').val(id);
        $('#form-start-jobNameDisp').val(job_name);
        $('#start-wizard').modal('show');
    }

    function toEdit(id) {
        $.ajaxSetup({cache: true});
        $("#edit").load("${basePath}/jobSchedule/toEdit?id=" + id + "");
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobSchedule/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        jobScheduleTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }


    function doStop(id) {
        if (confirm("确定要停止任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobSchedule/doStop?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("停止成功！");
                        jobScheduleTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doStart(id) {
        if (confirm("确定要启动任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobSchedule/doStart?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("启动成功！");
                        jobScheduleTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doStartSync() {
        $.ajax({
            type: "post",
            url: "${basePath}/jobSchedule/doStartSync",
            dataType: "text",
            data: $("#start_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("启动补数据成功！");
                    $('#start-wizard').modal('hide');
                    jobScheduleTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    function canalSync(id, job_name) {
        $.ajax({
            type: "post",
            url: "${basePath}/jobSchedule/canalSync?id="+id+"&name="+job_name,
            dataType: "text",
            data: $("#start_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("关闭补数据成功！");
                    $('#start-wizard').modal('hide');
                    jobScheduleTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }




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
                    //$("#srcName").chosen();
                }
                else {
                    alert(result);
                }
            }
        });
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

    function doHistory(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#history").load("${basePath}/jobSchedule/toHistory?id=" + id + "");
        $("#history").show();
        $("#mainContentInner").hide();
    }

    function reset() {
        $("#edit").empty();
        $("#history").empty();
    }

    $("#stopSchedule").click(function () {
        if (confirm("确定要停止schedule服务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobSchedule/stopSchedule",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("停止schedule服务成功！");
                        jobScheduleTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    })

    $("#startSchedule").click(function () {
        if (confirm("确定要启动schedule服务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobSchedule/startSchedule",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("启动schedule服务成功！");
                        jobScheduleTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    })


</script>