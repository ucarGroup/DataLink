<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="workerMonitor" class="main-container">
</div>
<div id="logback" class="modal">
    <div class="modal-dialog">
        <div class="modal-content" style="width:1000px;height:800px;">
            <div id="modal-wizard-container">
                <div class="modal-header" style="width:1000px;height:70px">

                    <div class="modal-header no-padding">
                        <div class="table-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                <span class="white">&times;</span>
                            </button>
                            logback.xml
                        </div>
                    </div>
                </div>

                <div class="modal-body" style="width:1000px;height:700px">
                    <form id="logback_form" class="form-horizontal" role="form">
                        <input type="hidden" name="workerId" id="workerId"/>

                        <div class="col-sm-12">
                            <div class="col-sm-12 form-group">
                                <div class="col-sm-12">
                                    <textarea type="text" name="content" class="col-sm-12" id="content"
                                              spellcheck="false"
                                              style="height: 600px;"></textarea>
                                </div>
                            </div>
                        </div>

                    </form>
                    <div class="col-md-offset-5 col-md-7">
                        <button class="btn btn-success" type="button" onclick="doEditLogback()">
                            <i class="ace-icon fa fa-save"></i>
                            保存
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
<div class="main-container " id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="row">
                    <form class="form-horizontal">
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">所属分组</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="groupId"
                                        style="width:100%">
                                    <option value="-1">全部</option>
                                    <c:forEach items="${groupList}" var="item">
                                        <option value="${item.id}">${item.groupName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>
                    </form>
                </div>

                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="row">
                    <table id="workTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>机器ID</td>
                            <td>机器名称</td>
                            <td>机器状态</td>
                            <td>ip地址</td>
                            <td>Rest端口号</td>
                            <td>所属组</td>
                            <td>启动时间</td>
                            <td>创建时间</td>
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
    var msgAlarmListMyTable;
    $(".chosen-select").chosen();

    getButtons([{
        code: "001002002",
        html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
        '</div>'
    }], $("#OperPanel"));

    msgAlarmListMyTable = $('#workTable').DataTable({
        "bAutoWidth": true,
        "ajax": {
            "url": "${basePath}/worker/initWorker",
            "data": function (d) {
                d.groupId = $("#groupId").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "workerName"},
            {"data": "workerState"},
            {"data": "workerAddress"},
            {"data": "restPort"},
            {"data": "groupName"},
            {"data": "startTime"},
            {
                "data": "createTime",
                "bSortable": false,
                "sWidth": "20%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var time = formatTs(oData.createTime);
                    $(nTd).html(time);
                }
            },
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    getButtons([
                        {
                            code: '001002004',
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
                            code: '001002006',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:doDelete(" + oData.id + ",\"" + oData.workerState + "\")' class='red'  title='删除'>" +
                                "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '001002007',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:toEditLogback(" + oData.id + ")' class='blue'  title='logback'>" +
                                "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '001002009',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:toRestart(" + oData.id + ")' class='blue'  title='重启'>" +
                                "<i class='ace-icon fa fa-refresh bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '001002010',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:toWorkerMonitor(" + oData.id + ")' class='red'  title='监控'>" +
                                "<i class='ace-icon fa fa-area-chart bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        }
                    ], $(nTd));

                }
            }
        ]
    });

    $("#groupId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/worker/toAdd?random=" + Math.random());
        $("#add").show();
        $("#mainContentInner").hide();
    }
    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/worker/toEdit?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function toWorkerMonitor(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#workerMonitor").load("${basePath}/worker/toWorkerMonitor?id=" + id + "&random=" + Math.random());
        $("#workerMonitor").show();
        $("#mainContentInner").hide();
    }

    function reset() {
        $("#add").empty();
        $("#edit").empty();
        $("#workerMonitor").empty();
    }

    function doDelete(id, workerState) {
        if (workerState == '正常') {
            alert("当前机器处于运行状态，不能进行删除操作!");
            return;
        }

        if (confirm("确定要删除数据吗?")) {
            $.ajax({
                type: "post",
                url: "${basePath}/worker/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功!");
                        msgAlarmListMyTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function toRestart(id) {

        if (confirm("确定要重启worker吗?")) {
            $.ajax({
                type: "post",
                url: "${basePath}/worker/restartWorker?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "fail") {
                        alert("重启失败!");
                    } else {
                        alert("重启操作完成，详情如下：" + data);
                    }
                }
            });
        }

    }

    function toEditLogback(id) {
        $("#workerId").val(id);
        <%--$('#content').load("${basePath}/worker/toLogback?id=" + id + "&random=" + Math.random());--%>
        $.ajax({
            type: "post",
            url: "${basePath}/worker/toEditLogback?id=" + id + "&random=" + Math.random(),
            dataType: "json",
            data: "",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                $('#content').val(data);
            }
        });

        //显示时设置margin-left
        // 参考链接：http://blog.csdn.net/cwfreebird/article/details/52414376
        $("#logback").modal('show').css({"margin-left": "-350px"});
    }

    function doEditLogback() {

        $.ajax({
            type: "post",
            url: "${basePath}/worker/doEditLogback",
            dataType: "json",
            data: $("#logback_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    $(".modal-header button").click();
                    msgAlarmListMyTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>

