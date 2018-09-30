<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">
                <div class="col-xs-12">
                </div>

                <div class="row">
                    <form class="form-horizontal">
                        <input type="hidden" class="col-sm-12" value="${taskId}" id="taskIdForException"/>

                        <div class="form-group">
                            <div class="col-md-1"></div>
                            <input class="col-sm-2" type='text' id='startTime' name="startTime"
                                   value="${taskExceptionInfo.startTime}"/>

                            <div class="col-sm-1" style="align-content: center">
                                <table align="center">
                                    <tr>
                                        <td>至</td>
                                    </tr>
                                </table>
                            </div>

                            <input class="col-sm-2" type='text' id='endTime' name="endTime"
                                   value="${taskExceptionInfo.endTime}"/>
                            <div class="col-md-1"></div>
                            <button type="button" id="search" class="btn btn-sm btn-purple" >查询</button>
                        </div>
                    </form>
                </div>

                <div class="row">
                    <table id="exceptionTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>异常ID</td>
                            <td>机器ID</td>
                            <td>异常详情</td>
                            <td>时间</td>
                        </tr>
                        </thead>
                    </table>
                </div>

            </div>
        </div>

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

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>

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

    var exceptionTable;
    $(".chosen-select").chosen();

    exceptionTable= $('#exceptionTable').DataTable({
        "bAutoWidth" : true,
        serverSide : true,//开启服务器模式:启用服务器分页
        paging : true,//是否分页
        pagingType : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax" : {
            "url" : "${basePath}/taskMonitor/initTaskException",
            "data": function (d) {
                var start = $("#startTime").val();
                var end = $("#endTime").val();
                d.taskId = $("#taskIdForException").val();
                d.startTime = Date.parse(new Date(start));
                d.endTime = Date.parse(new Date(end));
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns":[
            {"data":"id"},
            {"data":"workerId"},
            {
                "data":"exceptionDetail",
                "bSortable":false,
                "fnCreatedCell":function (nTd, sData, oData, iRow, iCol){
                    if(oData.exceptionDetail!=''){
                        $(nTd).html("" +
                                "<div class='radio'>"+
                                "<label>" +
                                "<a href='javascript:showException("+oData.id+")'>查看</a>" +
                                "</label>" +
                                "</div> &nbsp; &nbsp;"
                        );
                    }
                }
            },
            {
                "data":"createTime",
                "sWidth": "20%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var time = formatTs(oData.createTime);
                    $(nTd).html(time);
                }
            }
        ]
    });


    $("#search").click(function () {
        exceptionTable.ajax.reload();
    });

    function showException(id) {
        $.ajax({
            type : "post",
            url : "${basePath}/taskMonitor/showException",
            dataType : "json",
            data : "id="+id,
            async : true,
            success : function(data) {
                $("#exception-content").val(data);
                $('#exception-wizard').modal('show');

            }
        });
    }

    function back2Main() {
        $("#taskException").hide();
        $("#mainContentInner").show();
    }

</script>