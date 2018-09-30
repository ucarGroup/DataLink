<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-restart" class="modal-dialog">
    <div class="modal-content">
        <div id="modal-wizard-container_update">
            <div class="modal-header">

                <div class="modal-header no-padding">
                    <div class="table-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            <span class="white">&times;</span>
                        </button>
                        重启MysqlTask
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="restart_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-restart-id" name="id" value="${id}"/>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-restart-resetPosition">是否重置</label>

                        <div class="col-sm-9">
                            <select class="col-sm-8" name="resetPosition" id="form-restart-resetPosition"
                                    onchange="onResetSwitch(this.value);">
                                <option value="false">否</option>
                                <option value="true">是</option>
                            </select>
                        </div>
                    </div>
                    <div id="div-newTimeStamps" class="form-group" style="display:none;">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-restart-newTimeStamps">重置时间</label>

                        <div class="col-sm-9">
                            <input class="col-sm-8" type='text' id='form-restart-newTimeStamps' name="newTimeStamps"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="restart()">
                <i class="ace-icon fa fa-save"></i>
                重启
            </button>

            <button class="btn btn-danger" type="button" data-dismiss="modal">
                取消
                <i class="ace-icon fa fa-times"></i>
            </button>
        </div>
    </div>
</div>
<script type="text/javascript">
    $("#div-restart").ready(function () {
        $("#form-restart-newTimeStamps").datetimepicker(
                {
                    format: 'YYYY-MM-DD HH:mm:ss'
                }
        );
    });

    function onResetSwitch(value) {
        if (value == "true") {
            $('#div-newTimeStamps').show();
        } else {
            $('#div-newTimeStamps').hide();
        }
    }

    function restart() {
        var obj = {
            id: $("#form-restart-id").val(),
            resetPosition: $("#form-restart-resetPosition").val(),
            newTimeStamps: Date.parse(new Date($("#form-restart-newTimeStamps").val()))
        };

        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/doRestartMysqlTask",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("重启成功！");
                    $(".modal-header button").click();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>