<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="taskShadowAdd" class="main-content-inner">
</div>

<div class="page-content" id="taskShadowMainContent">
    <div class="row">
        <div class="col-xs-12">

            <div class="col-xs-12">
                <div class="row">
                    <form class="form-horizontal">
                        <div class="row">

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">状态</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="state"
                                            style="width:100%">
                                        <option selected="selected" value="">全部</option>
                                        <option value="INIT">INIT</option>
                                        <option value="EXECUTING">EXECUTING</option>
                                        <option value="COMPLETE">COMPLETE</option>
                                        <option value="DISCARD">DISCARD</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <div class="col-xs-2">
                                    <div class="col-xs-2">
                                        <input id="taskId" type="hidden" name="taskId" value="${taskId}">
                                    </div>
                                    <div class="col-xs-2">
                                        <button type="button" id="refresh_shadow" class="btn btn-sm btn-purple">刷新
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <div class="col-xs-12" id="OperPanel">

            </div>

            <div class="row">
                <table id="taskShadowListTable" width="100%" class="table table-striped table-bordered table-hover">
                    <thead>
                    <tr>
                        <td>id</td>
                        <td>任务名称</td>
                        <td>状态</td>
                        <td>映射id</td>
                        <td>重置时间</td>
                        <td>创建时间</td>
                        <td>修改时间</td>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>

    </div>
    <!-- /.page-content -->

</div>

<script type="text/javascript">
    var taskShadowListTable;
    $(".chosen-select").chosen();
    getButtons([{
        code: "004010114",
        html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>' +
        '</div>'
    }], $("#OperPanel"));

    taskShadowListTable = $('#taskShadowListTable').DataTable({
        "bAutoWidth": true,
        "serverSide": true,//开启服务器模式:启用服务器分页
        "paging": true,//是否分页
        "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite": "true",
        "sScrollX": "100%",
        "ajax": {
            "url": "${basePath}/shadow/doShadowList",
            "data": function (d) {
                d.state = $("#state").val();
                d.taskId = $("#taskId").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "taskName"},
            {"data": "state"},
            {"data": "mappingIds"},
            {"data": "resetTime"},
            {"data": "createTime"},
            {"data": "modifyTime"}
        ]
    });


    $("#refresh_shadow").click(function () {
        taskShadowListTable.ajax.reload();
    })

    $("#state").change(function () {
        taskShadowListTable.ajax.reload();
    })

    function back2Main() {
        $("#shadowList").hide();
        $("#main-container").show();
    }

    function reset() {
        $("#taskShadowAdd").empty();
    }

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        var taskId = $("#taskId").val();
        $("#taskShadowAdd").load("${basePath}/shadow/toAddShadow?taskId=" + taskId);
        $("#taskShadowAdd").show();
        $("#taskShadowMainContent").hide();
    }
</script>