<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="mysqlTaskRestart" class="modal">
</div>
<div id="reSwitchDbOrSddl" class="modal">
</div>
<div id="reNotifyDbms" class="modal">
</div>
<div id="virtualChangeReal" class="modal">
</div>

<div id="checkJobContentDiv" class="modal">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <table id="operateTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>操作名称</td>
                            <td>操作描述</td>
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



    var operateTable;
    $(".chosen-select").chosen();

    $(document).ready(function () {
        $("#mysqlTaskRestart").on('hide.bs.modal', function () {
            operateTable.ajax.reload();
        });

        $("#reSwitchDbOrSddl").on('hide.bs.modal', function () {
            operateTable.ajax.reload();
        });

        $("#reNotifyDbms").on('hide.bs.modal', function () {
            operateTable.ajax.reload();
        });

        $("#virtualChangeReal").on('hide.bs.modal', function () {
            operateTable.ajax.reload();
        });

        $("#checkJobContentDiv").on('hide.bs.modal', function () {
            operateTable.ajax.reload();
        });
    });

    operateTable = $('#operateTable').DataTable({
        "bAutoWidth": true,
        "ajax": {
            "url": "${basePath}/util/initOperations",
            "data": {}
        },
        "columns": [
            {"data": "id"},
            {"data": "name"},
            {"data": "desc"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {

                    if(oData.id == 98){

                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:oneKeyReverse(" + oData.id + ")' class='red'  title=''>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "（谨慎执行）</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));

                    }
                    else if(oData.id == 99){

                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:toRestart()' class='red'  title=''>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "（谨慎执行）</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));

                    }
                    else if(oData.id == 12 || oData.id == 13){

                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:virtualChangeReal(" + oData.id + ")' class='blue'  title='执行'>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));

                    }
                    else if(oData.id == 14){
                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                            "<a href='javascript:checkJobContent(" + oData.id + ")' class='blue'  title='执行'>" +
                                            "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                            "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));
                    }else if(oData.id == 15){
                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:reSwitchDbOrSddl(" + oData.id + ")' class='blue'  title='重试'>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));
                    }
                    else if(oData.id == 16){
                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:reNotifyDbms(" + oData.id + ")' class='blue'  title='重试'>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));
                    }
                    else{
                        getButtons([
                            {
                                code:'007004002',
                                html:function() {
                                    var str;
                                    str = "<div class='radio'>" +
                                        "<a href='javascript:doOperation(" + oData.id + ")' class='blue'  title='执行'>" +
                                        "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                    return str;
                                }
                            }
                        ],$(nTd));
                    }

                }
            }
        ]
    });

    function oneKeyReverse(id) {
        debugger;
        debugger;
        if (confirm("该操作是高危操作！！！你确定要执行吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/util/doOperation?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data.result == "success") {
                        if(data.msg){
                            alert("执行成功！" + data.msg);
                        }else{
                            alert("执行成功！");
                        }
                        operateTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function toRestart(id) {
        if (confirm("该操作是高危操作！！你确定要执行吗？")) {
            debugger;
            var restartDiv = $("#mysqlTaskRestart");
            restartDiv.empty();
            restartDiv.load("${basePath}/util/toRestartMysqlTask?id=" + id + "&random=" + Math.random());
            restartDiv.modal('show');
        }
    }

    function reSwitchDbOrSddl(id) {
        debugger;
        var restartDiv = $("#reSwitchDbOrSddl");
        restartDiv.empty();
        restartDiv.load("${basePath}/util/reSwitchDbOrSddl?id=" + id + "&random=" + Math.random());
        restartDiv.modal('show');
    }


    function reNotifyDbms(id) {
        debugger;
        var restartDiv = $("#reNotifyDbms");
        restartDiv.empty();
        restartDiv.load("${basePath}/util/reNotifyDbms?id=" + id + "&random=" + Math.random());
        restartDiv.modal('show');
    }

    function virtualChangeReal(id) {
        var restartDiv = $("#virtualChangeReal");
        restartDiv.empty();
        restartDiv.load("${basePath}/util/toVirtualChangeReal?id=" + id + "&random=" + Math.random());
        restartDiv.modal('show');
    }


    //check job content is right after double center switch over
    function checkJobContent(id) {
        var checkJobContentDiv = $("#checkJobContentDiv");
        checkJobContentDiv.empty();
        checkJobContentDiv.load("${basePath}/util/checkJobContent?id=" + id + "&random=" + Math.random());
        checkJobContentDiv.modal('show');

/*
        if (confirm("确定要执行吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/util/checkJobContent?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert("失败");
                    alert(err);
                },
                success: function (data) {
                    var result = data.result;
                    alert(result);
                    $("#check-job-content").val(data);
                    $('#check-job-wizard').modal('show');
                }
            });
        }
*/
    }

    function doOperation(id) {
        debugger;
        debugger;
        if (confirm("确定要执行吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/util/doOperation?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data.result == "success") {
                        if(data.msg){
                            alert("执行成功！" + data.msg);
                        }else{
                            alert("执行成功！");
                        }
                        operateTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }
</script>