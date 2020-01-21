<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container ace-save-state" id="main-container">
</div>
<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">

                        <div class="modal-dialog" align="center">

                            <input type="hidden" name="id" id="id" value="${syncApplyView.id}">

                            <div class="form-group">
                                <div class="col-sm-12">
                                    <label class="col-sm-2 control-label no-padding-right"
                                           for="applyStatus">申请状态</label>

                                    <div class="col-sm-10">
                                        <select class="width-100 chosen-select" id="applyStatus" name="applyStatus"
                                                style="width:100%">
                                            <option value=-1>无</option>
                                            <c:forEach items="${SyncApplyStatusList}" var="item">
                                                <option value="${item}" <c:if
                                                        test="${syncApplyView.applyStatus == item}"> selected = "selected" </c:if>>${item}</option>
                                            </c:forEach>

                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-sm-12">
                                    <label class="col-sm-2 control-label no-padding-right"
                                           for="replyRemark">处理备注</label>

                                    <div class="col-sm-10">
                                        <textarea type="text" id="replyRemark" name="replyRemark" class="col-sm-12"
                                                  style="margin: 0px;height: 120px;width: 100%;">${syncApplyView.replyRemark}</textarea>
                                    </div>
                                </div>
                            </div>

                            <div class="space"></div>

                        </div>

                    </div>
                </div>
            </div>
        </form>
    </div>

    <div class="clearfix" align="center">
        <button class="btn btn-info" type="button" onclick="doProcess();">
            <i class="ace-icon fa fa-check bigger-110"></i>
            保存
        </button>

        &nbsp; &nbsp; &nbsp;
        <button class="btn" type="reset" onclick="back2Main();">
            返回
            <i class="ace-icon fa fa-undo bigger-110"></i>
        </button>
    </div>
</div>


<script type="text/javascript">


    function doProcess() {
        if (!validateForm()) {
            return;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doProcess",
            dataType: "json",
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("保存成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }


    function back2Main() {
        $("#process").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }

    function validateForm() {
        var applyStatus = $('#applyStatus').val();
        if (applyStatus == null || applyStatus.length == 0 || applyStatus == -1) {
            alert('请选择申请处理状态!');
            return false;
        }

        return true;
    }

</script>
