<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<%@ include file="/WEB-INF/jsp/jobConfig/jobInclude.jsp" %>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="update_form" class="form-horizontal" role="form">

                <input type="hidden" id="id" name="id" value="${scheduleView.id}">
                <input type="hidden" id="job_id" name="job_id" value="${scheduleView.job_id}">
                <input type="hidden" id="schedule_name" name="schedule_name" value="${scheduleView.schedule_name}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-job_name">关联的job名称</label>
                    <div class="col-sm-9">
                        <input readonly="readonly" type="text" id="form-update-job_name" name="job_name" value="${scheduleView.job_name}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-src_media_name">源库名称</label>
                    <div class="col-sm-9">
                        <input readonly="readonly" type="text" id="form-edit-src_media_name" name="src_media_name" value="${scheduleView.src_media_name}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-target_media_name">目标库名称</label>
                    <div class="col-sm-9">
                        <input readonly="readonly" type="text" id="form-edit-target_media_name" name="target_media_name" value="${scheduleView.target_media_name}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-media_name">介质名称</label>
                    <div class="col-sm-9">
                        <input readonly="readonly" type="text" id="form-edit-media_name" name="media_name" value="${scheduleView.media_name}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-schedule_cron">cron表达式</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-schedule_cron" name="schedule_cron" value="${scheduleView.schedule_cron}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule-isretry" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-isretry">是否重试</label>
                    <div class="col-sm-9">
                        <select id="form-add-schedule-isretry_id" name="schedule_is_retry" style="width:350px;height:35px" class="chosen-select col-sm-5"
                                onchange="changeSchedule_yn(this.value)" >
                            <c:if test="${scheduleView.schedule_is_retry=='false'}">
                                <option grade="0" value="false" selected>否</option>
                                <option grade="1" value="true" >是</option>
                            </c:if>
                            <c:if test="${scheduleView.schedule_is_retry=='true'}">
                                <option grade="0" value="false" >否</option>
                                <option grade="1" value="true" selected>是</option>
                            </c:if>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="form-edit-schedule_max_retry">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-schedule_max_retry">最大重试次数</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-schedule_max_retry_id" name="schedule_max_retry" value="${scheduleView.schedule_max_retry}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-edit-schedule_retry_interval">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-schedule_retry_interval">重试间隔(秒)</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-schedule_retry_interval_id" name="schedule_retry_interval" value="${scheduleView.schedule_retry_interval}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


                <div class="form-group" >
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-schedule_max_runtime">最大运行时间(秒)</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-schedule_max_runtime" name="schedule_max_runtime" value="${scheduleView.schedule_max_runtime}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule_state">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_state">状态</label>
                    <div class="col-sm-9">
                        <select id="form-add-schedule_state_id" name="schedule_state" style="width:350px;height:35px" class="chosen-select col-sm-5"  >
                            <c:if test="${scheduleView.schedule_state=='true'}">
                                <option grade="1" value="true" selected>启动</option>
                                <option grade="0" value="false" >暂停</option>
                            </c:if>
                            <c:if test="${scheduleView.schedule_state=='false'}">
                                <option grade="1" value="true">启动</option>
                                <option grade="0" value="false" selected>暂停</option>
                            </c:if>
                        </select>
                    </div>
                </div>


            </form>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="edit();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    修改
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


    var state = $("#form-add-schedule-isretry_id").val();
    if(state=='true') {
        document.getElementById("form-edit-schedule_max_retry").style.display = "";
        document.getElementById("form-edit-schedule_retry_interval").style.display = "";
    } else {
        document.getElementById("form-edit-schedule_max_retry").style.display = "none";
        document.getElementById("form-edit-schedule_retry_interval").style.display = "none";
    }

    function changeSchedule_yn(val) {
        if(val == "true") {
            document.getElementById("form-edit-schedule_max_retry").style.display = "";
            document.getElementById("form-edit-schedule_retry_interval").style.display = "";
        } else {
            document.getElementById("form-edit-schedule_max_retry").style.display = "none";
            document.getElementById("form-edit-schedule_retry_interval").style.display = "none";
        }
    }



    function edit() {
//        var id = $('#job_id').val();
//        var content = add_editor.getText();
//        var jobContent = formatJson(content);
//        var timing_transfer_type = $("#form-edit-timing_type_id").val();
//        var timing_target_worker = $("#form-edit-timing_machine_id").val();
//        var timing_parameter =     $("#form-edit-timing_parameter_id").val();
//        var timing_yn =            $("#form-edit-timing_yn").val();
//
//
//        var json = "id="+id+"&timing_yn="+timing_yn+"&timing_transfer_type="+timing_transfer_type+"&timing_target_worker="+timing_target_worker
//        +"&timing_parameter="+timing_parameter+"&job_content="+jobContent;

        $.ajax({
            type: "post",
            url: "${basePath}/jobSchedule/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }

    function back2Main() {
        $("#edit").hide();
        $("#mainContentInner").show();
        jobScheduleTable.draw(false);
    }


</script>
