<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="fastAdd" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="reloadjob" class="main-container">
</div>
<div id="history" class="main-container">
</div>
<div id="jobRunQueue" class="main-container">
</div>
<div id="scheduleAdd" class="main-container">
</div>

<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">



                <p/>



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
                                        <option value="ORACLE">ORACLE</option>
                                        <option value="HANA">HANA</option>
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
                                    <option value="ORACLE">ORACLE</option>
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

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">申请ID</label>

                                <div class="col-sm-8">
                                    <input id="applyId" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">任务ID</label>
                                <div class="col-sm-8">
                                    <input id="jobconfigId" type="text" style="width:100%;">
                                </div>
                            </div>


                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">是否定时任务</label>
                                <select id="isTiming">
                                    <option selected="selected" value="all">全部</option>
                                    <option value="false">否</option>
                                    <option value="true">是</option>
                                </select>
                            </div>


                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">源表名</label>
                                <div class="col-sm-8">
                                    <input id="srcTableName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">目标表名</label>
                                <div class="col-sm-8">
                                    <input id="targetTableName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                            </div>

<!--
                            <div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">
                                <p>
                                <button class="btn btn-sm btn-info" onclick="doGo();">测试双机房切换</button>
                                </p>
                            </div>
-->

                        </div>

                    </form>
                </div>

                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="row">
                    <table id="jobConfigTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <th style="text-align:center;"><input type="checkbox" name="total"/></th>
                            <td>任务ID</td>
                            <td>任务名称</td>
                            <td>源库名称</td>
                            <td>目标库名称</td>
                            <td>源表名</td>
                            <td>目标表名</td>
                            <td>是否服务化job</td>
                            <!--
                                                        <td>定时任务类型</td>
                                                        <td>定时表达式</td>
                                                        <td>定时状态</td>
                                                        <td>定时机器</td>
                            -->
                            <td>创建时间</td>
                            <td>任务状态</td>
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
                                                   for="form-start-jvmArgsXms">Jvm参数(-Xms)</label>

                                            <div class="col-sm-9">
                                                <input type="text" name="jvmArgsXms" id="form-start-jvmArgsXms"
                                                       class="col-sm-8" value="2g"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-start-jvmArgsXmx">Jvm参数(-Xmx)</label>

                                            <div class="col-sm-9">
                                                <input type="text" name="jvmArgsXmx" id="form-start-jvmArgsXmx"
                                                       class="col-sm-8" value="4g"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-start-worker"> 目标机器 </label>

                                            <div class="col-sm-9" id="form-start-worker">
                                                <input type="text" name="worker" class="col-sm-8" readonly/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="form-start-isDebug"> 是否调试 </label>

                                            <div class="col-sm-9">
                                                <select name="isDebug" id="form-start-isDebug" class="col-sm-8">
                                                    <option value="no">否</option>
                                                    <option value="yes">是</option>
                                                </select>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <div class="modal-footer wizard-actions">
                                <button class="btn btn-success" type="button" onclick="doStart()">
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

                <div id="id-md5-wizard" class="modal">
                    <div class="modal-dialog">
                        <div class="modal-content" style="width: 800px;margin-left: -100px;">
                            <div>
                                <div class="modal-body">
                                    <div>
                                        <textarea id="id-md5-content" class="col-sm-12" rows="25"
                                                  style="font-size: 10px" readonly></textarea>
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


                <div id="queue-wizard" class="modal">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div id="queue-wizard-container">
                                <div class="modal-header">
                                    <div class="modal-header no-padding">
                                        <div class="table-header">
                                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                <span class="white">&times;</span>
                                            </button>
                                            加入队列
                                        </div>
                                    </div>
                                </div>

                                <div class="modal-body">
                                    <form id="queue_start_form" class="form-horizontal" role="form">
                                        <input type="hidden" name="queue_form_start_jobId" id="queue_form_start_jobId"/>


                                        <div class="form-group">
                                            <label class="col-sm-3 control-label no-padding-right"
                                                   for="queue_form-start"> 选择一个队列 </label>

                                            <div class="col-sm-9" id="queue_form-start">
                                                <input type="text" name="queueName" class="col-sm-8" readonly/>
                                            </div>
                                        </div>

                                    </form>
                                </div>
                            </div>

                            <div class="modal-footer wizard-actions">
                                <button class="btn btn-success" type="button" onclick="doPushQueue()">
                                    <i class="ace-icon fa fa-save"></i>
                                    添加到队列
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
    var jobListTable
    $(".chosen-select").chosen();

    getButtons([
        {
            code: "005001002",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
            '</div>'
        },
        {
            code: "005001004",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="fastAdd();">快速新增</button> </p>' +
            '</div>'
        },
        {
            code: "005001014",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="reloadJob();">重新加载数据源</button> </p>' +
            '</div>'
        },
        {
            code: "005001016",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="batchAddToJobQueue();">批量加入job队列</button> </p>' +
            '</div>'
        },
        {
            code: "005001017",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="batchToStart();">批量启动job</button> </p>' +
            '</div>'
        }

    ], $("#OperPanel"));

    jobListTable = $('#jobConfigTable').DataTable({
        "bAutoWidth": true,
        "serverSide": true,//开启服务器模式:启用服务器分页
        "paging": true,//是否分页
        "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite": "true",
        "sScrollX": "100%",
        "ajax": {
            "url": "${basePath}/jobConfig/initJob",
            "data": function (d) {
                d.isTiming = $("#isTiming").val();
                d.srcType = $("#srcType").val();
                d.destType = $("#destType").val();
                d.srcName = $("#srcName").val();
                d.destName = $("#destName").val();
                d.mediaName = $("#mediaName").val();
                d.jobName = $("#jobName").val();
                d.applyId = $("#applyId").val();
                d.jobconfigId = $("#jobconfigId").val();
                d.srcTableName = $("#srcTableName").val();
                d.targetTableName = $("#targetTableName").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data":"id",
                "bSortable": false,
                "width": "5%",
                render: function(data,type,row,meta){
                    return data = "<input type='checkbox'  data-id='"+data+"'>";
                }
            },
            {"data": "id"},
            {"data": "job_name"},
            {"data": "job_src_media_source_name"},
            {"data": "job_target_media_source_name"},
            {"data": "job_media_name"},
            {"data": "job_media_target_name"},
            {"data": "timing_yn"},
            //{"data": "timing_transfer_type"},
            //{"data": "timing_expression"},
            //{"data": "timing_parameter"},
            //{"data": "timing_target_worker"},
            {"data": "create_time"},
            {"data": "currentState"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var name = "\"" + oData.job_name + "\"";
                    var timing_yn = oData.timing_yn;
                    getButtons([
                        {
                            code: '005001009',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:toStart(" + oData.id + "," + name + ")' class='green'  title='启动'>" +
                                        "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
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
                                str = "<div class='radio'>" +
                                        "<a href='javascript:toPushQueue(" + oData.id + ")' class='yellow'  title='加入队列'>" +
                                        "<i class='ace-icon fa fa-space-shuttle bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '005001013',
                            html: function () {
                                var str;
                                if (timing_yn == "true") {
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:showIdMd5Info(" + oData.id + ")' class='yellow'  title='id编码'>" +
                                            "<i class='ace-icon fa fa-paperclip bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005001014',
                            html: function () {
                                var str;
                                if (timing_yn == "true") {
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:toSchedule(" + oData.id + ")' class='yellow'  title='增加schedule'>" +
                                            "<i class='ace-icon fa fa-hourglass-start bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code: '005001015',
                            html: function () {
                                var str;
                                if (timing_yn == "true") {
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:toReloadJobContent(" + oData.id + ")' class='yellow'  title='重新加载job内容'>" +
                                            "<i class='ace-icon fa fa-check-square-o  bigger-130'></i>" + "</a>" +
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


    $(document).ready(function () {
        var table = $('#jobConfigTable').DataTable();
        $('#jobConfigTable').on('click', 'tr', function () {
            //alert($(this).getText());
            $(this).toggleClass('selected');
            //alert("?????~~~~~~~~~~~~ !!! @@")
        });

    });


    function doGo() {
        if (confirm("双机房切换？？？")) {
            //alert("结果 -> "+ idList);
            $.ajax({
                type: "post",
                url: "${basePath}/jobConfig/doGo",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("切换成功！");
                        //jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
            //alert( table.rows('.selected').data().length +' row(s) selected' );
        }
    }







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

    $("#search").click(function () {
        jobListTable.ajax.reload();
    })


    function batchToStart() {
        if (confirm("确定要批量启动job吗？")) {
            debugger;
            var ids = new Array();
            var inputChecked = $("input[data-id]:checked");
            if(inputChecked.length < 1) {
                alert("请选择要启动的job");
                return;
            }

            inputChecked.each(function(i,val){
                var dataId = $(val).attr("data-id");
                if(dataId != undefined){
                    ids.push(dataId);
                }
            });
            var idList = ids.join(",");

            toStart(idList,"批量启动job")
        }
    }

    function toStart(id, job_name) {
        //load works address
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/works",
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "‍‍<select name=\"worker\" class=\"col-sm-8\">";
                    value += "<option value='-1'>动态选择</option>";
                    for (i = 0; i < result.length; i++) {
                        var option = "<option value=" + "'" + result[i] + "'>" + result[i] + "</option>";
                        value += option;
                    }
                    value += "</select>";
                    document.getElementById("form-start-worker").innerHTML = value;
                }

                else {
                    alert("no found zk workers ip " + result);
                }
            }
        });

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/JVMSize",
            async: true,
            dataType: "json",
            success: function (result) {
                if(result != null) {
                    var maxSize = result.split(",")[0];
                    var minSize = result.split(",")[1];
                    $('#form-start-jvmArgsXmx').val(maxSize);
                    $('#form-start-jvmArgsXms').val(minSize);
                } else {
                    alert("获取默认jvm size失败！");
                }
            }
        });
        $('#form-start-jobId').val(id);
        $('#form-start-jobNameDisp').val(job_name);
        $('#form-start-worker').val("-1");
        $('#start-wizard').modal('show');
    }

    function doStart() {
        debugger;
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doStart",
            dataType: "json",
            data: $("#start_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                debugger;
                if (data == "success") {
                    alert("启动成功！");
                    $('#start-wizard').modal('hide');
                    //jobListTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }


    function toAdd(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        //$("#add").load("${basePath}/jobConfig/toAdd?random=" + Math.random());
        $("#add").load("${basePath}/jobConfig/toAdd");
        $("#add").show();
        $("#mainContentInner").hide();
    }

    function fastAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#fastAdd").load("${basePath}/jobConfig/toFastAdd?random=" + Math.random());
        $("#fastAdd").show();
        $("#mainContentInner").hide();
    }

    function reloadJob() {
        reset();//每次必须先reset，把已开界面资源清理掉
        //$("#reloadjob").load("${basePath}/jobConfig/toReloadJob?random=" + Math.random());
        $("#reloadjob").load("${basePath}/jobConfig/toReloadJob");
        $("#reloadjob").show();
        $("#mainContentInner").hide();
    }

    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        //$("#edit").load("${basePath}/jobConfig/toEdit?id=" + id + "&random=" + Math.random());
        $("#edit").load("${basePath}/jobConfig/toEdit?id=" + id + "");
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function doHistory(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#history").load("${basePath}/jobConfig/toHistory?id=" + id + "");
        $("#history").show();
        $("#mainContentInner").hide();
    }

    function toSchedule(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#scheduleAdd").load("${basePath}/jobSchedule/toAdd?id=" + id + "");
        $("#scheduleAdd").show();
        $("#mainContentInner").hide();
    }

    function toReloadJobContent(id) {
        if (confirm("要重新加载job内容？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobConfig/toReloadJobContent?id="+id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("重新加载job内容成功！");
                        jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    $("input[name='total']").change(function(){
        if($("input[name='total']").is(":checked")) {
            $("input[data-id]").prop("checked",true); ;
        }else {
            $("input[data-id]").removeAttr('checked');
        }
    });

    function batchAddToJobQueue() {
        if (confirm("确定要批量加入运行队列吗？")) {
            debugger;
            var ids = new Array();
            var inputChecked = $("input[data-id]:checked");
            if(inputChecked.length < 1) {
                alert("请选择要加入队列的job");
                return;
            }

            inputChecked.each(function(i,val){
                var dataId = $(val).attr("data-id");
                if(dataId != undefined){
                    ids.push(dataId);
                }
            });
            var idList = ids.join(",");

            toPushQueue(idList)
        }
    }

    function toPushQueue(id) {
        //load works address
        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/allExecuteJobQueueInfo",
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "‍‍<select name=\"queueName\" class=\"col-sm-8\">";
                    for (i = 0; i < result.length; i++) {
                        var option = "<option value=" + "'" + result[i] + "'>" + result[i] + "</option>";
                        value += option;
                    }
                    value += "</select>";
                    document.getElementById("queue_form-start").innerHTML = value;
                }

                else {
                    alert("no found queue " + result);
                }
            }
        });

        $('#queue_form_start_jobId').val(id);
        $('#queue_form-start').val("-1");
        $('#queue-wizard').modal('show');
    }

    function doPushQueue() {
        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/doPushQueue",
            dataType: "text",
            data: $("#queue_start_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("添加成功！");
                    $('#queue-wizard').modal('hide');
                    //jobListTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }




    function reset() {
        $("#add").empty();
        $("#fastAdd").empty();
        $("#edit").empty();
        $("#reloadjob").empty();
        $("#history").empty();
        $("#scheduleAdd").empty();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobConfig/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        jobListTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function showIdMd5Info(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doJobConfigMD5Info?id=" + id,
            dataType: "json",
            data: "id=" + id,
            async: true,
            success: function (data) {
                if (data != null && data != '') {
                    $("#id-md5-content").val(data);
                    $('#id-md5-wizard').modal('show');
                }

            }
        });
    }


</script>