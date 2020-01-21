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
                        虚拟数据源改造成实际数据源
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="restart_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-id" name="id" value="${id}"/>


                    <div id="div-newTimeStamps" class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-virtualMediaSourceId">虚拟数据源id</label>

                        <div class="col-sm-9">
                            <input class="col-sm-8" type='text' id='form-virtualMediaSourceId' name="newTimeStamps" placeholder="只能输入一个数据源"/>
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
        var obj = {
            virtualMediaSourceId: $("#form-virtualMediaSourceId").val()
        };

        var url;
        if($("#form-id").val()==12){
            url = "${basePath}/util/virtualChangeRealFirstStep";
        }else{
            url = "${basePath}/util/virtualChangeRealSecondStep";
        }

        debugger;
        $.ajax({
            type: "post",
            url: url,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
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