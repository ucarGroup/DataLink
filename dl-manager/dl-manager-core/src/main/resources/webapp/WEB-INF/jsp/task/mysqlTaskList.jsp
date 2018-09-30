<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


<div id="mysqlTaskAdd" class="main-content-inner">
</div>
<div id="mysqlTaskUpdate" class="main-content-inner">
</div>
<div id="mysqlTaskRestart" class="modal">
</div>
<div id="taskGroupMigrate" class="modal">
</div>
<div class="main-container ace-save-state" id="main-container">
    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="row">
                    <div class="col-xs-12 pull-left">
                        <div class="row">
                            <form class="form-horizontal">
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">源数据库</label>

                                    <div class="col-sm-8">
                                        <select class="width-20 chosen-select" id="readerMediaSourceId"
                                                style="width:100%">
                                            <option value="-1">全部</option>
                                            <c:forEach items="${mediaSourceList}" var="item">
                                                <option value="${item.id}">${item.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">所属分组</label>

                                    <div class="col-sm-8">
                                        <select class="width-20 chosen-select" id="groupId" style="width:100%">
                                            <option value="-1">全部</option>
                                            <c:forEach items="${groupList}" var="item">
                                                <option value="${item.id}">${item.groupName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label no-padding-right"
                                           for="id">任务名称</label>

                                    <div class="col-sm-8">
                                        <select class="id width-100 chosen-select" id="id"
                                                style="width:100%">
                                            <option value="-1">全部</option>
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

                        <div class="col-xs-12"  id="OperPanel">

                        </div>

                        <div class="row" style="padding:0px">
                            <table id="taskTable" class="table table-striped table-bordered table-hover"
                                   style="text-align: center;width:100%">
                                <thead>
                                <tr>
                                    <th style="text-align:center;">任务ID</th>
                                    <th style="text-align:center;">任务名称</th>
                                    <th style="text-align:center;">详情</th>
                                    <th style="text-align:center;">目标状态</th>
                                    <th style="text-align:center;">实际状态</th>
                                    <th style="text-align:center;">所属分组</th>
                                    <th style="text-align:center;">所属机器</th>
                                    <th style="text-align:center;" class="detail-col">当前日志时间</th>
                                    <th style="text-align:center;">最近启动时间</th>
                                    <th style="text-align:center;display:none">reader地址</th>
                                    <th style="text-align:center;width: 150px">操作</th>
                                </tr>
                                </thead>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    getButtons([{
        code:"004010101",
        html:'<div class="pull-left tableTools-container" style="padding-top: 10px;">'+
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>'+
        '</div>'
    }],$("#OperPanel"));

    var oTable;
    $(document).ready(function () {
        $('.chosen-select').chosen({allow_single_deselect: true, width: "100%"});
        $("#mysqlTaskRestart").on('hide.bs.modal', function () {
            oTable.ajax.reload();
        });
        $("#taskGroupMigrate").on('hide.bs.modal', function () {
            oTable.ajax.reload();
        });

        oTable = $('#taskTable').DataTable({
            processing: true,
            filter: true,
            ordering: true,
            "sInfoEmpty": "No entries to show",
            serverSide : true,//开启服务器模式:启用服务器分页
            paging : true,//是否分页
            pagingType : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
            ajax: {
                "url": "${basePath}/mysqlTask/mysqlTaskDatas",
                "data": function (d) {
                    d.readerMediaSourceId = $("#readerMediaSourceId").val();
                    d.groupId = $("#groupId").val();
                    d.id = $("#id").val();
                    return JSON.stringify(d);
                },
                "dataType": 'json',
                "contentType": 'application/json',
                "type": 'POST'
            },
            columns: [
                {"data": "id"},
                {"data": "taskName"},
                {
                    "data": "detail",
                    "bSortable": false,
                    "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {

                        var temp = "<div class='action-buttons'>" +
                            "    <a onclick='showDetailBtn(this)' href='#' class='green bigger-140 show-details-btn' title='Show Details'>" +
                            "        <i class='ace-icon fa fa-angle-double-down'></i>" +
                            "        <span class='sr-only'>Details</span>" +
                            "    </a>" +
                            "</div>";
                        $(nTd).html(temp);
                    }
                },
                {"data": "targetState"},
                {"data": "listenedState"},
                {"data": "groupId"},
                {
                    "data": "workerId",
                    render: function (data, type, row) {

                        if (data != null) {
                            return data;
                        } else {
                            return "UNASSIGNED";
                        }
                    }
                },
                {"data": "currentTimeStamp"},
                {"data": "startTime"},
                {"data": "readerIp"}
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
                    var t1 = tr.find("td").eq(9);
                    t1.hide();
                    var t1_text = t1.text();//t1.html();
                    var temp = "<tr class='detail-row'>" +
                        "    <td colspan='10'>" +
                        "        <div class='table-detail'>" +
                        "            <table border='1' width='100%' class='table table-striped table-bordered table-hover' style='padding: 0px;margin: 0px;' >" +
                        "                <tr width='auto'>" +
                        "                    <th>reader地址</th>" +
                        "                </tr>" +
                        "                <tbody>" +
                        "                <tr width='auto'>" +
                        "                    <td>" + t1_text + "</td>" +
                        "                </tr>" +
                        "                </tbody>" +
                        "            </table>" +
                        "        </div>" +
                        "    </td>" +
                        "</tr>";
                    $(tr).after(temp);

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
            columnDefs: [
                {
                "aTargets": [10],
                "mData": null,
                "bSortable": false,
                "bSearchable": false,
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var state = oData.targetState;
                    var listenedState = oData.listenedState;
                    getButtons([
                        {
                            code:'004010103',
                            html:function() {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:toUpdate(" + oData.id + ")' class='blue'  title='修改'  disable='true'>" +
                                "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code:'004010105',
                            html:function() {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code:'004010108',
                            html:function() {
                                var str;
                                if (listenedState != "UNASSIGNED") {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:toRestart(" + oData.id + ")' class='green'  title='重启'>" +
                                    "<i class='ace-icon fa fa-refresh bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code:'004010107',
                            html:function() {
                                var str;
                                if (state == "PAUSED") {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:doResume(" + oData.id + ")' class='inverse'  title='恢复运行'>" +
                                    "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code:'004010106',
                            html:function() {
                                var str;
                                if (state != "PAUSED") {
                                    str = "<div class='radio'>" +
                                    "<a href='javascript:doPause(" + oData.id + ")' class='grey'  title='暂停'>" +
                                    "<i class='ace-icon fa fa-pause bigger-130'></i>" + "</a>" +
                                    "</div> &nbsp; &nbsp;"
                                }
                                return str;
                            }
                        },
                        {
                            code:'004010111',
                            html:function() {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:toGroupMigrate(" + oData.id + ")' class='green'  title='Task组迁移'>" +
                                "<i class='ace-icon fa fa-random bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        }
                    ],$(nTd));

                }

            }]
        });

        $("#readerMediaSourceId").change(function () {
            oTable.ajax.reload();
        })

        $("#id").change(function () {
            oTable.ajax.reload();
        })

        $("#search").click(function () {
            oTable.ajax.reload();
        })

        $("#groupId").change(function () {
            var groupId = $('#groupId').val();

            $("#id").val(-1);

            $.ajax({
                type: "post",
                url: "${basePath}/task/getTaskListByGroupId?groupId=" + groupId,
                async: true,
                dataType: "json",
                success: function (result) {
                    if (result != null && result != '') {
                        document.getElementById("id").innerHTML = "";

                        $("<option value=\"-1\">全部</option>").appendTo(".id");
                        for (i = 0; i < result.taskIds.length; i++) {
                            $("<option value='" + result.taskIds[i] + "' >" + result.taskNames[i] + "</option>").appendTo(".id");
                        }
                        $(".id").trigger("chosen:updated");
                    }
                    else {
                        alert(result);
                    }
                }
            });

            oTable.ajax.reload();
        });

    });

    function showDetailBtn(obj) {
        $(obj).closest('tr').next().toggleClass('open');
        $(obj).find(ace.vars['.icon']).toggleClass('fa-angle-double-down').toggleClass('fa-angle-double-up');
    }

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#mysqlTaskAdd").load("${basePath}/mysqlTask/toAddMysqlTask");
        $("#mysqlTaskAdd").show();
        $("#main-container").hide();
    }

    function toUpdate(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#mysqlTaskUpdate").load("${basePath}/mysqlTask/toUpdateMysqlTask?id=" + id + "&random=" + Math.random());
        $("#mysqlTaskUpdate").show();
        $("#main-container").hide();
    }

    function reset() {
        $("#mysqlTaskAdd").empty();
        $("#mysqlTaskUpdate").empty();
    }

    function doDelete(id) {
        if (confirm("确定要删除该Task吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/mysqlTask/deleteMysqlTask?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        oTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doPause(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/pauseMysqlTask?id=" + id,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("暂停成功！");
                    oTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    function doResume(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/resumeMysqlTask?id=" + id,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("恢复成功！");
                    oTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    function toGroupMigrate(id) {
        var migrateDiv = $("#taskGroupMigrate");
        migrateDiv.empty();
        migrateDiv.load("${basePath}/task/toGroupMigrate?id=" + id + "&random=" + Math.random());
        migrateDiv.modal('show');
    }

    function toRestart(id) {
        var restartDiv = $("#mysqlTaskRestart");
        restartDiv.empty();
        restartDiv.load("${basePath}/mysqlTask/toRestartMysqlTask?id=" + id + "&random=" + Math.random());
        restartDiv.modal('show');
    }



</script>
