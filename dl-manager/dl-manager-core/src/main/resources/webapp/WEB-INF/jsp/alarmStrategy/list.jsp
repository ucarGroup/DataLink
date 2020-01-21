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
                            <label class="col-sm-4 control-label">报警策略组</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="priorityId" style="width:100%">
                                    <option value="-1" selected="selected">全部</option>
                                    <c:forEach items="${alarmPriorityInfoList}" var="bean" >
                                        <option value="${bean.id}" >${bean.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">策略名称</label>

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
                            <td>策略名称</td>
                            <td>报警策略组</td>
                            <td>监控类型</td>
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
            code: "006004002",
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
            "url": "${basePath}/alarmStrategy/initList",
            "data": function (d) {
                d.priorityId = $("#priorityId").val();
                d.name = $("#name").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "name"},
            {"data": "priorityName"},
            {"data": "monitorType"},
            {"data": "createTime"},
            {"data": "modifyTime"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    getButtons([
                        {
                            code: '006004004',
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
                            code: '006004006',
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

    $("#monitorType").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#groupId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#resourceId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    });

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/alarmStrategy/toAdd?random=" + Math.random());
        $("#add").show();
        $("#mainContentInner").hide();
    }
    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/alarmStrategy/toEdit?id=" + id + "&random=" + Math.random());
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
                url: "${basePath}/alarmStrategy/doDelete?id=" + id,
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
</script>