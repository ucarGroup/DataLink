<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_yn">是否开启schedule？</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-add-schedule_yn" style="width:350px;height:35px" class="chosen-select col-sm-5"
                                  onchange="changeSchedule_yn(this.value)">
                        <option grade="0" value="false" selected>否</option>
                        <option grade="1" value="true" >是</option>
                    </select>
                    </div>
                </div>

                <div class="form-group" id="form-add-scheudle-cron" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-scheudle-cron">cron表达式</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-scheudle-cron_id" name="form-add-scheudle-cron" value="* * * * *" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


                <div class="form-group" id="form-add-schedule-isretry" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-isretry">是否重试</label>
                    <div class="col-sm-9">
                        <select id="form-add-schedule-isretry_id" style="width:350px;height:35px" class="chosen-select col-sm-5" onblur="reloadJson()" >
                            <option grade="0" value="false" selected>否</option>
                            <option grade="1" value="true" >是</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule-maxretry" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-maxretry">每次重试间隔(秒)</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-schedule-maxretry_id" name="form-add-schedule-maxretry" value="600" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule_retry_interval" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_retry_interval">最大重试次数</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-schedule_retry_interval_id" name="" value="2" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule_maxRuntime" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule_maxRuntime">最大运行时间</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-schedule_maxRuntime_id" name="" value="-1" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule-onlineState" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-onlineState">任务状态</label>
                    <div class="col-sm-9">
                        <select id="form-add-schedule-onlineState_id" style="width:350px;height:35px" class="chosen-select col-sm-5" onblur="reloadJson()" >
                            <option grade="0" value="false" selected>未上线</option>
                            <option grade="1" value="true" >已上线</option>
                            <option grade="1" value="true" >已下线</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="form-add-schedule-isSuppend" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-schedule-isSuppend">是否暂停</label>
                    <div class="col-sm-9">
                        <select id="form-add-schedule-isSuppend_id" style="width:350px;height:35px" class="chosen-select col-sm-5" onblur="reloadJson()" >
                            <option grade="0" value="false" selected>否</option>
                            <option grade="1" value="true" >是</option>
                        </select>
                    </div>
                </div>


            </form>
        </div>

    </div>
<!-- /.page-content -->
</div>
<script type="text/javascript">

    function changeSchedule_yn(val) {
        if(val == "true") {
            document.getElementById("form-add-scheudle-cron").style.display = "";
            document.getElementById("form-add-schedule-isretry").style.display = "";
            document.getElementById("form-add-schedule-maxretry").style.display = "";
            document.getElementById("form-add-schedule_retry_interval").style.display = "";
            document.getElementById("form-add-schedule_maxRuntime").style.display = "";
            document.getElementById("form-add-schedule-onlineState").style.display = "";
            document.getElementById("form-add-schedule-isSuppend").style.display = "";
        } else {
            document.getElementById("form-add-scheudle-cron").style.display = "none";
            document.getElementById("form-add-schedule-isretry").style.display = "none";
            document.getElementById("form-add-schedule-maxretry").style.display = "none";
            document.getElementById("form-add-schedule_retry_interval").style.display = "none";
            document.getElementById("form-add-schedule_maxRuntime").style.display = "none";
            document.getElementById("form-add-schedule-onlineState").style.display = "none";
            document.getElementById("form-add-schedule-isSuppend").style.display = "none";
        }

    }


</script>
