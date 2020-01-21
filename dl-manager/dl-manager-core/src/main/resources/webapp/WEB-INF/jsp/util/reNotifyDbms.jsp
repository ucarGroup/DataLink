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
                        单库切换，手动通知dbms结果
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="restart_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-id" name="id" value="${id}"/>


                    <div id="div-newTimeStamps" class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-batchId">批次号</label>

                        <div class="col-sm-9">
                            <input class="col-sm-8" type='text' id='form-batchId' name="newTimeStamps" placeholder=""/>
                        </div>
                    </div>
                </form>
            </div>

        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="change()">
                <i class="ace-icon fa fa-save"></i>
                确定
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
    });

    function change() {

        var batchId = $("#form-batchId").val();
        debugger;
        $.ajax({
            type: "post",
            url: "${basePath}/doublecenter/reNotifyDbms?batchId=" + batchId,
            contentType: "application/json; charset=utf-8",
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
                    $(".modal-header button").click();
                } else {
                    alert(data);
                }

            }
        });
    }
</script>