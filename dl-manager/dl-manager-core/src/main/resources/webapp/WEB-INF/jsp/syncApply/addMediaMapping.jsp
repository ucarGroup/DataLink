<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-addMediaMapping" class="modal-dialog">
    <div class="modal-content">
        <div id="modal-wizard-container_update">
            <div class="modal-header">

                <div class="modal-header no-padding">
                    <div class="table-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            <span class="white">&times;</span>
                        </button>
                        增加映射
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <form id="add_mediaMapping_form" class="form-horizontal" role="form">
                    <input type="hidden" id="form-apply-id" name="applyId" value="${applyId}"/>

                    <div class="form-group">
                        <label class="col-sm-2 control-label no-padding-right"
                               for="form-task-id">选择Task</label>

                        <div class="col-sm-7">
                            <select multiple="" name="taskId" class="taskId tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-task-id"
                                    style="width:100%;">
                                <c:forEach items="${taskList}" var="bean">
                                    <option value="${bean.id}">${bean.taskName} </option>
                                </c:forEach>
                            </select>
                        </div>

                    </div>
                </form>
            </div>
        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="doAddMediaMapping()">
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

    $('.taskId').css('min-width', '100%').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
    function doAddMediaMapping() {
        /*var obj = {
         applyId: $("#form-apply-id").val(),
         taskId: $("#form-task-id").val()//taskId的值是数组形式："["88"]"
         };*/

        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doAddMediaMapping",
            dataType: "json",
            data: $("#add_mediaMapping_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("同步申请增加映射成功！");
                    $(".modal-header button").click();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>