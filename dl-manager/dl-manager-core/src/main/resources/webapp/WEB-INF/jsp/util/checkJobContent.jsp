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
                        检查job内容不正确的如下
                    </div>
                </div>
            </div>

            <div class="modal-body">
                <div class="table-body">
                    <textarea id="check-job-content" class="col-sm-12" rows="25"
                              style="font-size: 10px" readonly>
                        ${str}
                    </textarea>
                </div>
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
    //$("#div-restart").ready(function () {
    //});


</script>