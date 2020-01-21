<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="row">
                    <form class="form-horizontal">
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">任务级别</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="priority" style="width:100%">
                                    <option value="-1" selected="selected">全部</option>
                                    <option value="1">1</option>
                                    <option value="2">2</option>
                                    <option value="3">3</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-3 control-label">名称</label>

                            <div class="col-sm-8">
                                <input id="name" type="text" style="width:100%;">
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
                    <table id="monitorTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>名称</td>
                            <td>等级</td>
                            <td>创建时间</td>
                            <td>修改时间</td>
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

    getButtons([
        {
            code: "006003002",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
            '</div>'
        }

    ], $("#OperPanel"));

    msgAlarmListMyTable = $('#monitorTable').DataTable({
        "bAutoWidth": true,
        serverSide: true,//开启服务器模式:启用服务器分页
        paging: true,//是否分页
        pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax": {
            "url": "${basePath}/taskPriority/initList",
            "data": function (d) {
                d.name = $("#name").val();
                d.priority = $("#priority").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "name"},
            {
                "data": "priority",
                "bSortable": false,
                "sWidth": "20%"/*,
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    if (oData.priority == 1) {
                        $(nTd).html("一级任务");
                    } else if (oData.priority == 2) {
                        $(nTd).html("二级任务");
                    } else if (oData.priority == 3) {
                        $(nTd).html("三级任务");
                    }
                }*/
            },
            {"data": "createTime"},
            {"data": "modifyTime"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    getButtons([
                        {
                            code: '006001004',
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
                            code: '006001006',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        }
                    ], $(nTd));
                }
            }
        ]
    });

    //    $("#monitorCat").change(function () {
    //        msgAlarmListMyTable.ajax.reload();
    //    });

    $("#monitorType").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#groupId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#resourceId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#isEffective").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/taskPriority/toAdd?random=" + Math.random());
        $("#add").show();
        $("#mainContentInner").hide();
    }
    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/taskPriority/toEdit?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function reset() {
        $("#add").empty();
        $("#edit").empty();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/taskPriority/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        msgAlarmListMyTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }


    function doAllStart() {
        if (confirm("确定要启动所有任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/monitor/doAllStart",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        msgAlarmListMyTable.ajax.reload();
                        alert("启动成功！");

                    } else {
                        alert(data);
                    }
                }
            });
        }
    }
    function doAllStop() {
        if (confirm("确定要停止所有任务吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/monitor/doAllStop",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        msgAlarmListMyTable.ajax.reload();
                        alert("停止成功！");
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function createAllDataxMonitor() {
         if (confirm("确定要创建所有datax任务监控？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/monitor/createAllDataxMonitor",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        msgAlarmListMyTable.ajax.reload();
                        alert("创建成功！");
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }


    function doStart(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/monitor/doStart?id=" + id,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    msgAlarmListMyTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    function doPause(id) {
        $.ajax({
            type: "post",
            url: "${basePath}/monitor/doPause?id=" + id,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    msgAlarmListMyTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }

    $("#monitorCat").change(function () {
        var monitorCat = $('#monitorCat').val();

        $("#monitorType").val(-1);
        $("#resourceId").val(-1);

        $.ajax({
            type: "post",
            url: "${basePath}/monitor/getMonitorType?monitorCat=" + monitorCat,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    document.getElementById("monitorType").innerHTML = "";
                    document.getElementById("resourceId").innerHTML = "";

                    $("<option value=\"-1\">全部</option>").appendTo(".monitorType");
                    for (i = 0; i < result.key.length; i++) {
                        $("<option value='" + result.key[i] + "' >" + result.desc[i] + "</option>").appendTo(".monitorType");
                    }
                    $(".monitorType").trigger("chosen:updated");

                    $("<option value=\"-1\">全部</option>").appendTo(".resourceId");
                    for (i = 0; i < result.resourceId.length; i++) {
                        $("<option value='" + result.resourceId[i] + "' >" + result.resourceName[i] + "</option>").appendTo(".resourceId");
                    }
                    $(".resourceId").trigger("chosen:updated");
                }
                else {
                    alert(result);
                }
            }
        });

        msgAlarmListMyTable.ajax.reload();
    })
</script>