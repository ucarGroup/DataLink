<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <form class="form-horizontal">

                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">操作类型</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="operType"
                                        style="width:100%">
                                    <option value="">全部</option>
                                    <c:forEach items="${auditLogOperTypeList}" var="auditLogOperType">
                                        <option value="${auditLogOperType.value}">${auditLogOperType.desc}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">用户名称</label>

                            <div class="col-sm-8">
                                <select class="taskId width-100 chosen-select" id="userId"
                                        style="width:100%">
                                    <option value="" selected=selected>全部</option>
                                    <c:forEach items="${userInfoList}" var="userInfo">
                                        <option value="${userInfo.id}">${userInfo.userName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">操作模块名称</label>

                            <div class="col-sm-8">
                                <select class="taskId width-100 chosen-select" id="menuCode"
                                        style="width:100%">
                                    <option value="" selected=selected>全部</option>
                                    <c:forEach items="${menuInfoList}" var="menuInfo">
                                        <option value="${menuInfo.code}">${menuInfo.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">操作资源key</label>

                            <div class="col-sm-8">
                                <input class="width-100" id="operKey" placeholder="操作资源主键"/>
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">操作资源名称</label>

                            <div class="col-sm-8">
                                <input class="width-100" id="operName"/>
                            </div>
                        </div>

                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>
                    </form>
                </div>

                <div class="row">
                    <table id="auditLogTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>用户名称</td>
                            <td>操作类型</td>
                            <td>操作模块编码</td>
                            <td>操作模块名称</td>
                            <td>操作资源key</td>
                            <td>操作资源名称</td>
                            <td>操作时间</td>
                            <td>变更后记录</td>
                        </tr>
                        </thead>
                    </table>
                </div>

            </div>
        </div>
        <!-- /.page-content -->

        <div id="modalId" class="modal">

            <div class="modal-dialog">
                <div class="modal-content" style="width: 800px;margin-left: -100px;">
                    <div>
                        <div class="modal-body">
                            <div>
                                <textarea id="content" class="col-sm-12" rows="25" style="font-size: 10px"
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
    var operRecordObj={};
    var msgAlarmListMyTable;
    $(".chosen-select").chosen({
        search_contains: true,
    });

    msgAlarmListMyTable = $('#auditLogTable').DataTable({
        processing: true,
        filter: true,
        "sInfoEmpty": "No entries to show",
        serverSide: true,//开启服务器模式:启用服务器分页
        paging: true,//是否分页
        pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax": {
            "url": "${basePath}/auditLog/initAuditLog",
            "data": function (d) {
                d.operType = $("#operType").val();
                d.userId = $("#userId").val();
                d.menuCode = $("#menuCode").val();
                d.operKey = $("#operKey").val();
                d.operName = $("#operName").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "userName"},
            {"data": "operType"},
            {"data": "menuCode"},
            {"data": "menuName"},
            {"data": "operKey"},
            {"data": "operName"},
            {"data": "operTimeStr"},
            {
                "data": "operRecord",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    operRecordObj[oData.id]=oData.operRecord;
                    if (oData.operRecord != '') {
                        $(nTd).html("" +
                            "<div>" +
                            "<label>" +
                            "<a href='javascript:showCz("+oData.id+")'>查看</a>" +
                            "</label>" +
                            "</div> &nbsp; &nbsp;"
                        );
                    }
                }
            }
        ]
    });

    $("#search").click(function () {
        var operKey = $("#operKey").val();
        if(isNaN(operKey)){
            alert("操作资源key必须为数字")
            $("#operKey").val("")
            return;
        }
        msgAlarmListMyTable.ajax.reload();
    });

    function showCz(id) {
        $("#content").val(operRecordObj[id]);
        $('#modalId').modal('show');
    }

</script>