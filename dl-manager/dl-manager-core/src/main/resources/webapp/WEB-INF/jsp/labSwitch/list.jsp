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
                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="row">
                    <table id="userTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>版本号</td>
                            <td>源机房</td>
                            <td>目标机房</td>
                            <td>切换进度</td>
                            <td>切换结果</td>
                            <td>开始时间</td>
                            <td>结束时间</td>
                            <td>切机房发起时间</td>
                            <td>异常</td>
                        </tr>
                        </thead>
                    </table>
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
                                <textarea id="exception-content" class="col-sm-12" rows="25" style="font-size: 10px"
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
    var msgAlarmListMyTable;
    $(".chosen-select").chosen();


    msgAlarmListMyTable = $('#userTable').DataTable({
        "bSort": false,
        "bAutoWidth": true,
        "ajax": {
            "url": "${basePath}/doublecenter/intLabSwitch",
            "data": {}
        },
        "columns": [
            {
                "data": "id",
                "bSortable": true
            },
            {
                "data": "version",
                "bSortable": true
            },
            {
                "data": "fromCenter",
                "bSortable": true
            },
            {
                "data": "targetCenter",
                "bSortable": true
            },
            {
                "data": "switchProgressName",
                "bSortable": true
            },
            {
                "data": "statusName",
                "bSortable": true
            },
            {
                "data": "startTime",
                "bSortable": true,
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var time = formatTs(oData.startTime);
                    $(nTd).html(time);
                }
            },
            {
                "data": "endTime",
                "bSortable": true,
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var time = formatTs(oData.endTime);
                    $(nTd).html(time);
                }
            },
            {
                "data": "switchStartTime",
                "bSortable": true,
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var time = formatTs(oData.switchStartTime);
                    $(nTd).html(time);
                }
            },
            {
                "data": "labSwitchException",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    if (oData.labSwitchException != '') {
                        $(nTd).html("" +
                            "<div class='radio'>" +
                            "<label>" +
                            "<a href='javascript:showException(" + oData.version + ")'>查看</a>" +
                            "</label>" +
                            "</div> &nbsp; &nbsp;"
                        );
                    }
                }
            }

        ]
    });

    function showException(version) {
        $.ajax({
            type: "post",
            url: "${basePath}/doublecenter/showException",
            dataType: "json",
            data: "version=" + version,
            async: true,
            success: function (data) {
                $("#exception-content").val(data);
                $('#exception-wizard').modal('show');

            }
        });
    }

    function reset() {
        $("#add").empty();
        $("#edit").empty();
    }
</script>