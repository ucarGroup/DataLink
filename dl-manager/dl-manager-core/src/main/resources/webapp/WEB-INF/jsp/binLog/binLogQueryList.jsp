<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<style type="text/css">

    #idTemp input,label,select{
        margin-right: 20px;
    }

    #idTemp select{
        width: 120px;
    }


</style>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">
                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="row">
                    <form class="form-horizontal">

                        <div class="form-group">

                            <div id="idTemp" style="margin-left: 20px;">

                                <label>时间</label>
                                <input type='text'id='startTime' name="startTime"/>
                                <label >至</label>
                                <input type='text' id='endTime' name="endTime"
                                       value="${currentTime}"/>

                                <label>目标状态</label>
                                <select id="targetState" name="targetState">
                                    <option value="-1">请选择</option>
                                    <option value="STARTED" selected>STARTED</option>
                                    <option value="PAUSED">PAUSED</option>
                                </select>

                                <label>所属分组</label>
                                <select id="groupId">
                                    <option value="">全部</option>
                                    <c:forEach items="${groupList}" var="item">
                                        <option value="${item.id}">${item.groupName}</option>
                                    </c:forEach>
                                </select>

                                <button type="button" id="search" class="btn btn-sm btn-purple" >查询</button>

                            </div>

                        </div>
                    </form>
                </div>

                <div class="row">
                    <table id="binLogTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>任务id</td>
                            <td>任务名称</td>
                            <td>目标状态</td>
                            <td>实际状态</td>
                            <td>任务级别</td>
                            <td>所属分组</td>
                            <td>binlog位点</td>
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

    $("#startTime").datetimepicker(
        {
            format: 'YYYY-MM-DD HH:mm:ss'
        }
    );
    $("#endTime").datetimepicker(
        {
            format: 'YYYY-MM-DD HH:mm:ss'
        }
    );

    var binLogTable;
    $(".chosen-select").chosen();

    binLogTable = $('#binLogTable').DataTable({
        "bAutoWidth": true,
        "ajax": {
            "url": "${basePath}/binLogQuery/initDatas",
            "data": function (d) {
                var start = $("#startTime").val();
                var end = $("#endTime").val();
                d.startTime = Date.parse(new Date(start));
                d.endTime = Date.parse(new Date(end));
                d.targetState = $("#targetState").val();
                d.groupId = $("#groupId").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [

            {"data": "id"},
            {"data": "taskName"},
            {"data": "targetState"},
            {"data": "actualState"},
            {"data": "taskPriority"},
            {"data": "groupId"},
            {"data": "currentTimeStamp"}
        ]
    });

    $("#search").click(function () {
        binLogTable.ajax.reload();
    });

</script>