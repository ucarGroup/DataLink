<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<!--
<link rel="stylesheet" href="${basePath}/assets/css/jsoneditor.css" />
<script src="${basePath}/assets/js/jsoneditor.js"></script>
-->
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="start_form" class="form-horizontal" role="form">


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="job_queue_name">队列的名称</label>
                    <div class="col-sm-9">
                        <input type="text" id="job_queue_name" name="job_queue_name"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="job_queue_mail">队列所属人的邮箱前缀</label>
                    <div class="col-sm-9">
                        <input type="text" id="job_queue_mail" name="job_queue_mail"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="job_queue_fail_to_stop">job执行时候停止后续执行？</label>
                    <div class="col-sm-9">
                        <select id="job_queue_fail_to_stop" class="tag-input-style" style="width:350px;height:35px" name="job_fail_to_stop" >
                            <option grade="1" value="true" selected >是</option>
                            <option grade="2" value="false" >否</option>
                        </select>
                    </div>
                </div>


            </form>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doAdd();">
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

    </div>
                    <!-- /.page-content -->
</div>
<script type="text/javascript">

    function back2Main() {
        $("#create").hide();
        $("#mainContentInner").show();
    }

    function doAdd() {
        $.ajax({
            type: "post",
            url: "${basePath}/jobQueue/doAdd",
            dataType: "json",
            data: $("#start_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("添加成功！");
                    refresh();
                } else {
                    alert(data);
                }
            }
        });
    }

    function refresh() {
        $("#create").hide();
        $("#mainContentInner").show();
    }

</script>
