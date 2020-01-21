<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="col-xs-12">
                    <div class="row">
                        <form class="form-horizontal">
                            <div class="row">

                                <div class="form-group col-xs-3">
                                    <div class="col-xs-2">
                                        <div class="col-xs-2">
                                            <input id="decorateId" type="hidden" name="decorateId" value="${decorateId}">
                                        </div>
                                        <div class="col-xs-2">
                                            <button type="button" id="refresh_job" class="btn btn-sm btn-purple">刷新</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>


                <div class="row">
                    <table id="jobHistoryListTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>id</td>
                            <td>任务名称</td>
                            <td>表名称</td>
                            <td>运行状态</td>
                            <td>启动时间</td>
                            <td>完成时间</td>
                            <td>执行日志</td>
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

        <div id="exception-wizard" class="modal">
            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="exception-content" class="col-sm-12" rows="25" style="font-size: 10px" readonly></textarea>
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


        <div id="json-wizard" class="modal">
            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="json-content" class="col-sm-12" rows="25" style="font-size: 10px"
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

<script type="text/javascript">
    var jobHistoryListTable;
    $(".chosen-select").chosen();

    jobHistoryListTable = $('#jobHistoryListTable').DataTable({
        "bAutoWidth": true,
        "serverSide" : true,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/decorate/doHistory",
            "data": function (d) {
                d.decorateId = $("#decorateId").val();
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
            {"data": "statusName"},
            {"data": "createTimeFormat"},
            {"data": "updateTimeFormat"},
            {"data": "executedLog"}
        ]
    });


    $("#refresh_job").click(function () {
        jobHistoryListTable.ajax.reload();
    })

    function back2Main() {
        $("#history").hide();
        $("#mainContentInner").show();
    }




</script>