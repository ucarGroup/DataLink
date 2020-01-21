<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-restart" class="modal-dialog" style="width:800px;height:600px;">
    <div class="modal-content">
        <div id="modal-wizard-container_update">
            <div class="modal-header">

                <div class="modal-header no-padding">
                    <div class="table-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            <span class="white">&times;</span>
                        </button>
                        单库切机房重试
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="restart_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-reTry-id" name="id" value="${id}"/>
                    <input type="hidden" id="form-reTry-url" name="id" value="doublecenter/dbSwitchLab_4_dbms"/>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-reTry-type">重试类型</label>

                        <div class="col-sm-9">
                            <select class="col-sm-8" name="resetPosition" id="form-reTry-type"
                                    onchange="onSwitch(this.value);">
                                <option value="singleDB">单库(mysql或sqlServer)</option>
                                <option value="sddl">SDDL</option>
                            </select>
                        </div>
                    </div>

                    <div id="div-newTimeStamps" class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-reTry-content">重试内容</label>

                        <div class="col-sm-9">
                            <textarea class="col-sm-8" style="height:300px;" id="form-reTry-content"></textarea>
                        </div>
                    </div>

                </form>
            </div>

        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="reTry()">
                <i class="ace-icon fa fa-save"></i>
                重试
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

    function onSwitch(value) {
        if (value == "singleDB") {
            $("#form-reTry-url").attr("value",'doublecenter/dbSwitchLab_4_dbms');//填充内容
        } else {
            $("#form-reTry-url").attr("value",'doublecenter/sddlSwitchLab_4_dbms');//填充内容
        }
    }

    function reTry() {
        var url = $("#form-reTry-url").val();
        var json = $("#form-reTry-content").val();
        if(!json){
            alert("重试内容不能为空");
            return;
        }
        debugger;
        $.ajax({
            type: "post",
            url: "${basePath}/" + url,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(json),
            async: false,
            error: function (xhr, status, err) {
                debugger;
                var jsonStr = xhr.responseJSON;
                alert(jsonStr.message);
            },
            success: function (data) {
                debugger;
                alert("发起切机房重试成功，后台正在切换中！");
            }
        });
    }
</script>