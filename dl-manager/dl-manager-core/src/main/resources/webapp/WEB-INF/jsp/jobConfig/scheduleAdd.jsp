<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">

                        <input type="hidden" id="job_id" name="job_id" value="${scheduleView.job_id}">

                        <div class="form-group" id="form-add-scheudle-cron">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-scheudle-cron">cron表达式</label>
                            <div class="col-sm-9">
                                <input type="text" id="form-add-scheudle-cron_id" name="form-add-scheudle-cron" value="* * * * * ?"
                                       class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                            </div>
                        </div>


                        <div class="form-group" id="form-add-schedule-isretry" >
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-isretry">是否重试</label>
                            <div class="col-sm-9">
                                <select id="form-add-schedule-isretry_id" style="width:350px;height:35px" class="chosen-select col-sm-5"
                                        onchange="changeSchedule_yn(this.value)" >
                                    <option grade="0" value="false" selected>否</option>
                                    <option grade="1" value="true" >是</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group" id="form-add-schedule-maxretry" style="display: none;">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-maxretry">重试次数</label>
                            <div class="col-sm-9">
                                <input type="text" id="form-add-schedule-maxretry_id" name="form-add-schedule-maxretry" value="-1"
                                       class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                            </div>
                        </div>

                        <div class="form-group" id="form-add-schedule_retry_interval" style="display: none;">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_retry_interval">重试间隔(秒)</label>
                            <div class="col-sm-9">
                                <input type="text" id="form-add-schedule_retry_interval_id" name="" value="600"
                                       class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                            </div>
                        </div>

                        <div class="form-group" id="form-add-schedule_maxRuntime" >
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_maxRuntime">最大运行时间</label>
                            <div class="col-sm-9">
                                <input type="text" id="form-add-schedule_maxRuntime_id" name="" value="-1"
                                       class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                            </div>
                        </div>

                        <div class="form-group" id="form-add-schedule_state" >
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_state">任务状态</label>
                            <div class="col-sm-9">
                                <select id="form-add-schedule_state_id" style="width:350px;height:35px" class="chosen-select col-sm-5"  >
                                    <option grade="0" value="true" selected>启动</option>
                                    <option grade="1" value="false" >暂停</option>
                                </select>
                            </div>
                        </div>


                    </div>
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

<script type="text/javascript">


    function changeSchedule_yn(val) {
        if(val == "true") {
            document.getElementById("form-add-schedule-maxretry").style.display = "";
            document.getElementById("form-add-schedule_retry_interval").style.display = "";
        } else {
            document.getElementById("form-add-schedule-maxretry").style.display = "none";
            document.getElementById("form-add-schedule_retry_interval").style.display = "none";
        }
    }

    function doAdd() {
        var job_id = $('#job_id').val();
        var schedule_cron = $('#form-add-scheudle-cron_id').val();
        var schedule_is_retry = $('#form-add-schedule-isretry_id').val();
        var schedule_max_retry = $('#form-add-schedule-maxretry_id').val();
        var schedule_retry_interval = $('#form-add-schedule_retry_interval_id').val();
        var schedule_max_runtime = $('#form-add-schedule_maxRuntime_id').val();
        var schedule_state = $('#form-add-schedule_state_id').val();

        var json = "job_id="+job_id +"&schedule_cron="+schedule_cron +"&schedule_is_retry="+schedule_is_retry;
        json = json + "&schedule_max_retry="+schedule_max_retry +"&schedule_retry_interval="+schedule_retry_interval;
        json = json + "&schedule_max_runtime="+schedule_max_runtime +"&schedule_state="+schedule_state;

        $.ajax({
            type: "post",
            url: "${basePath}/jobSchedule/doAdd",
            dataType: "json",
            data: json,
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("创建schedule成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }


    function back2Main() {
        $("#scheduleAdd").hide();
        $("#mainContentInner").show();
        jobListTable.ajax.reload();
    }


</script>
