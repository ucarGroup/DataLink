<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


<div id="mysqlTaskAdd" class="main-content-inner">
</div>
<div id="mysqlTaskUpdate" class="main-content-inner">
</div>
<div id="shadowList" class="main-container">
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
                                        <select class="readerMediaSourceId width-20 chosen-select"
                                                id="readerMediaSourceId"
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

                        <div class="col-xs-12" id="OperPanel">

                        </div>

                        <div class="row" style="padding:0px">
                            <table id="taskTable" class="table table-striped table-bordered table-hover"
                                   style="text-align: center;width:100%">
                                <thead>
                                <tr>
                                    <th style="text-align:center;"><input type="checkbox" name="total"/></th>
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
                                    <th style="text-align:center;display:none">最后binlog文件</th>
                                    <th style="text-align:center;display:none">最后binlog位点</th>
                                    <th style="text-align:center;display:none">任务同步状态</th>
                                    <th style="text-align:center;display:none">影子位点当前时间</th>
                                    <th style="text-align:center;display:none">影子位点最后binlog文件</th>
                                    <th style="text-align:center;display:none">影子位点最后binlog位点</th>

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
        code: "004010101",
        html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
        '</div>'
    }, {
        code: "004010108",
        html: '<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">' +
        '<p> <button class="btn btn-sm btn-info" onclick="resetPosition();">批量重置位点</button> </p>' +
        '</div>'
    }], $("#OperPanel"));

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
            serverSide: true,//开启服务器模式:启用服务器分页
            paging: true,//是否分页
            pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
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
                {
                    "data": "id",
                    "bSortable": false,
                    "width": "2%",
                    render: function (data, type, row, meta) {
                        return data = "<input type='checkbox'  data-id='" + data + "'>";
                    }
                },
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
                {"data": "readerIp"},
                {"data": "latestEffectSyncLogFileName"},
                {"data": "latestEffectSyncLogFileOffset"},
                {"data": "taskSyncStatus"},
                {"data": "shadowCurrentTimeStamp"},
                {"data": "shadowLatestEffectSyncLogFileName"},
                {"data": "shadowLatestEffectSyncLogFileOffset"}

            ],
            language: {
                "sUrl": "${basePath}/assets/json/zh_CN.json",
                "infoEmpty": ""
            },
            "fnDrawCallback": function (settings) {
                var api = this.api();
                var rows = api.rows({page: 'current'}).nodes();
                var last = null;
                var tr = null;
                var preTd = null;

                api.column(0, {page: 'current'}).data().each(function (group, i) {

                    tr = $(rows[i]);

                    var t10 = tr.find("td").eq(10);
                    t10.hide();
                    var t10_text = t10.text();//t1.html();

                    var t11 = tr.find("td").eq(11);
                    t11.hide();
                    var t11_text = t11.text();//t1.html();

                    var t12 = tr.find("td").eq(12);
                    t12.hide();
                    var t12_text = t12.text();//t1.html();

                    var t13 = tr.find("td").eq(13);
                    t13.hide();
                    var t13_text = t13.text();//t1.html();

                    var t14 = tr.find("td").eq(14);
                    t14.hide();
                    var t14_text = t14.text();

                    var t15 = tr.find("td").eq(15);
                    t15.hide();
                    var t15_text = t15.text();//t1.html();

                    var t16 = tr.find("td").eq(16);
                    t16.hide();
                    var t16_text = t16.text();

                    var temp = "<tr class='detail-row'>" +
                            "    <td colspan='12'>" +
                            "        <div class='table-detail'>" +
                            "            <table border='1' width='100%' class='table table-striped table-bordered table-hover'" +
                            "                   style='padding: 0px;margin: 0px;'>" +
                            "                <tr width='auto'>" +
                            "                    <th>reader地址</th>" +

                            "                    <th>最后binlog文件</th>" +
                            "                    <th>最后binlog位点</th>" +
                            "                    <th>任务同步状态</th>" +
                            "                    <th>影子位点当前时间</th>" +
                            "                    <th>影子位点最后binlog文件</th>" +
                            "                    <th>影子位点最后binlog位点</th>" +
                            "                </tr>" +
                            "                <tbody>" +
                            "                <tr width='auto'>" +
                            "                    <td>" + t10_text + "</td>" +
                            "                    <td>" + t11_text + "</td>" +
                            "                    <td>" + t12_text + "</td>" +
                            "                    <td>" + t13_text + "</td>" +
                            "                    <td>" + t14_text + "</td>" +
                            "                    <td>" + t15_text + "</td>" +
                            "                    <td>" + t16_text + "</td>" +

                            "                </tr>" +
                            "                </tbody>" +
                            "            </table>" +
                            "        </div>" +
                            "    </td>" +
                            "</tr>";
                    $(tr).after(temp);

                    if (last !== group) {
                        preTd = $("td:eq(1)", tr);
                        preTd.attr("rowspan", 1);
                        preTd.text(group);
                        last = group;
                    } else {
                        preTd.attr("rowspan", parseInt(preTd.attr("rowspan")) + 1);
                        $("td:eq(1)", tr).remove();
                    }
                });
                $("thead th:eq(0)").removeClass("sorting_asc");
            },
            columnDefs: [
                {
                    "aTargets": [17],
                    "mData": null,
                    "bSortable": false,
                    "bSearchable": false,
                    "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                        var state = oData.targetState;
                        var listenedState = oData.listenedState;
                        getButtons([
                            {
                                code: '004010103',
                                html: function () {
                                    var str;
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:toUpdate(" + oData.id + ")' class='blue'  title='修改'  disable='true'>" +
                                            "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            },
                            {
                                code: '004010105',
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
                                code: '004010108',
                                html: function () {
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
                                code: '004010107',
                                html: function () {
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
                                code: '004010106',
                                html: function () {
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
                                code: '004010111',
                                html: function () {
                                    var str;
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:toGroupMigrate(" + oData.id + ")' class='green'  title='Task组迁移'>" +
                                            "<i class='ace-icon fa fa-random bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            },
                            {
                                code: '004010113',
                                html: function () {
                                    var str;
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:toShadowList(" + oData.id + ")' class='blue'  title='影子位点'>" +
                                            "<i class='ace-icon fa fa-list bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ], $(nTd));

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
        $("#shadowList").empty();
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

    function resetPosition() {
        var ids = new Array();
        var inputChecked = $("input[data-id]:checked");
        if (inputChecked.length < 1) {
            alert("请选择要重启的task");
            return;
        }
        for (var i = 0; i < inputChecked.length; i++) {
            if ($(inputChecked[i]).parent().siblings(":eq(4)").text() == "UNASSIGNED") {
                alert("任务状态不能为UNASSIGNED");
                return;
            }
        }
        inputChecked.each(function (i, val) {
            var dataId = $(val).attr("data-id");
            if (dataId != undefined) {
                ids.push(dataId);
            }
        });
        idStr = ids.join(",");
        toRestart(idStr);
        $("input[name='total']").removeAttr('checked');
    }

    $("input[name='total']").change(function () {
        if ($("input[name='total']").is(":checked")) {
            $("input[data-id]").prop("checked", true);
            ;
        } else {
            $("input[data-id]").removeAttr('checked');
        }
    });

    function toShadowList(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#shadowList").load("${basePath}/shadow/toShadowList?taskId=" + id + "");
        $("#shadowList").show();
        $("#main-container").hide();
    }

</script>
