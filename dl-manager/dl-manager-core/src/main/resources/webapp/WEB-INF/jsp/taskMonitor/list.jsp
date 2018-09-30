<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="taskStatistic" class="main-container">
</div>
<div id="taskException" class="main-container">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">
                <div class="col-xs-12">
                </div>

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
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">任务名称</label>

                            <div class="col-sm-8">
                                <select class="taskId width-100 chosen-select" id="taskId"
                                        style="width:100%">
                                    <option value="-1" selected=selected>全部</option>
                                    <c:forEach items="${taskList}" var="item">
                                        <option value="${item.id}">${item.taskName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>
                    </form>
                </div>

                <div class="row">
                    <table id="monitorTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>任务ID</td>
                            <td>任务名称</td>
                            <td>延迟时间(毫秒)</td>
                            <td>目标状态</td>
                            <td>实际状态</td>
                            <td>所属机器</td>
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


    </div>
</div>
<script type="text/javascript">
    var msgAlarmListMyTable;
    $(".chosen-select").chosen();

    msgAlarmListMyTable = $('#monitorTable').DataTable({
        processing: true,
        filter: true,
        ordering: true,
        "sInfoEmpty": "No entries to show",
        serverSide: true,//开启服务器模式:启用服务器分页
        paging: true,//是否分页
        pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax": {
            "url": "${basePath}/taskMonitor/initTaskMonitor",
            "data": function (d) {
                d.taskId = $("#taskId").val();
                d.groupId = $("#groupId").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "taskId"},
            {"data": "taskName"},
            {"data": "delayTime"},
            {"data": "targetState"},
            {"data": "listenedState"},
            {
                "data": "workerId", render: function (data, type, row) {
                if (data != null) {
                    return data;
                } else {
                    return "UNASSIGNED";
                }
            }
            },


            {
                "data": "exception",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    if (oData.exception != '') {
                        $(nTd).html("" +
                                "<div class='radio'>" +
                                "<label>" +
                                "<a href='javascript:showException(" + oData.exceptionId + ")'>查看</a>" +
                                "</label>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                }
            }
        ],
        language: {
            "sUrl": "${basePath}/assets/json/zh_CN.json",
            "infoEmpty": ""
        },
        "drawCallback": function (settings) {
            var api = this.api();
            var rows = api.rows({page: 'current'}).nodes();
            var last = null;
            var tr = null;
            var preTd = null;

            api.column(0, {page: 'current'}).data().each(function (group, i) {

                tr = $(rows[i]);
                if (last !== group) {
                    preTd = $("td:first", tr);
                    preTd.attr("rowspan", 1);
                    preTd.text(group);
                    last = group;
                } else {
                    preTd.attr("rowspan", parseInt(preTd.attr("rowspan")) + 1);
                    $("td:first", tr).remove();
                }
            });
        },
        columnDefs: [{
            "aTargets": [7],
            "mData": null,
            "bSortable": false,
            "bSearchable": false,
            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {

                getButtons([
                    {
                        code: '006002002',
                        html: function () {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:toTaskStatistic(" + oData.taskId + ")' class='red'  title='性能统计'>" +
                            "<i class='ace-icon fa fa-area-chart bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    },
                    {
                        code: '006002004',
                        html: function () {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:toExceptionHistory(" + oData.taskId + ")' class='green'  title='异常历史'>" +
                            "<i class='ace-icon fa fa-history bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    }
                ], $(nTd));

            }

        }]
    });

    $("#taskId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#groupId").change(function () {
        var groupId = $('#groupId').val();

        $("#taskId").val(-1);

        $.ajax({
            type: "post",
            url: "${basePath}/task/getTaskListByGroupId?groupId=" + groupId,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    document.getElementById("taskId").innerHTML = "";

                    $("<option value=\"-1\">全部</option>").appendTo(".taskId");
                    for (i = 0; i < result.taskIds.length; i++) {
                        $("<option value='" + result.taskIds[i] + "' >" + result.taskNames[i] + "</option>").appendTo(".taskId");
                    }
                    $(".taskId").trigger("chosen:updated");
                }
                else {
                    alert(result);
                }
            }
        });

        msgAlarmListMyTable.ajax.reload();
    });

    function showException(exceptionId) {
        $.ajax({
            type: "post",
            url: "${basePath}/taskMonitor/showException",
            dataType: "json",
            data: "id=" + exceptionId,
            async: true,
            success: function (data) {
                $("#exception-content").val(data);
                $('#exception-wizard').modal('show');

            }
        });
    }

    function toTaskStatistic(taskId) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#taskStatistic").load("${basePath}/taskMonitor/toTaskStatistic?taskId=" + taskId + "&random=" + Math.random());
        $("#taskStatistic").show();
        $("#mainContentInner").hide();
    }

    function toExceptionHistory(taskId) {
        reset();
        $("#taskException").load("${basePath}/taskMonitor/toTaskException?taskId=" + taskId + "&random=" + Math.random());
        $("#taskException").show();
        $("#mainContentInner").hide();
    }

    function reset() {
        $("#taskStatistic").empty();
        $("#taskException").empty();
    }

</script>