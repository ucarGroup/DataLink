<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="history" class="main-container">
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
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="id">任务名称</label>

                                <div class="col-sm-8">
                                    <select class="id width-100 chosen-select" id="taskId"
                                            style="width:100%">
                                        <option value="-1">全部</option>
                                        <c:forEach items="${taskList}" var="item">
                                            <option value="${item.id}">${item.taskName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>


                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">表名称</label>

                                <div class="col-sm-8">
                                    <input id="tableName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                            </div>
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
                            <td>id</td>
                            <td>任务名称</td>
                            <td>表名称</td>
                            <td>补录数据主键表达式</td>
                            <td>备注</td>
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
    var jobListTable
    $(".chosen-select").chosen();

    getButtons([
        {
            code: "004010401",
            html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
            '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
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
            "url": "${basePath}/decorate/queryDecorate",
            "data": function (d) {
                d.taskId = $("#taskId").val();
                d.tableName = $("#tableName").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "taskName"},
            {"data": "tableName"},
            {"data": "statement"},
            {"data": "remark"},
            {"data": "createTime"},
            {"data": "modifyTime"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var name = "\"" + oData.job_name + "\"";
                    var timing_yn = oData.timing_yn;
                    getButtons([
                        {
                            code: '004010402',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:doStart(" + oData.id + ")' class='green'  title='启动'>" +
                                        "<i class='ace-icon fa fa-play bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '004010403',
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
                            code: '004010404',
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
                            code: '004010405',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:doHistory(" + oData.id + ")' class='yellow'  title='运行历史'>" +
                                        "<i class='ace-icon fa fa-history bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
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
            $(this).toggleClass('selected');
        });

    });


    $("#search").click(function () {
        jobListTable.ajax.reload();
    })


    function doStart(id) {
        if (confirm("确定要补录数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/decorate/start?id=" + id,
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
    }

    function toAdd(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        //$("#add").load("${basePath}/jobConfig/toAdd?random=" + Math.random());
        $("#add").load("${basePath}/decorate/toAddDecorate");
        $("#add").show();
        $("#mainContentInner").hide();
    }

    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#edit").load("${basePath}/decorate/toUpdateDecorate?id=" + id + "");
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function doHistory(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $.ajaxSetup({cache: true});
        $("#history").load("${basePath}/decorate/toHistory?id=" + id + "");
        $("#history").show();
        $("#mainContentInner").hide();
    }


    function reset() {
        $("#add").empty();
        $("#edit").empty();
        $("#history").empty();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/decorate/deleteDecorate?id=" + id,
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

</script>