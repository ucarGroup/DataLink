<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-12 control-label no-padding-left" for="form-add-timing_yn">如果是定时任务，除了在此进行配置，还需要在Job配置管理页面，点击每行记录右侧的"增加schedule"按钮，进行附加配置</label>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-timing_yn">是否定时任务</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-add-timing_yn" style="width:350px;height:35px" class="chosen-select col-sm-5"
                                  onchange="changeTiming_yn(this.value)">
                        <option grade="0" value="false" selected>否</option>
                        <option grade="1" value="true" >是</option>
                    </select>
                    </div>
                </div>

                <div class="form-group" id="form-add-timing_type" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-timing_type">定时任务类型</label>
                    <div class="col-sm-9">
                        <select id="form-add-timing_type_id" style="width:350px;height:35px" class="chosen-select col-sm-5" onblur="reloadJson()" >
                            <option grade="0" value="FULL" selected>全量任务</option>
                            <option grade="1" value="INCREMENT" >增量任务</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="form-add-timing_machine" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-timing_machine">定时任务机器</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-timing_machine_id" name="form-add-timing_machine" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-add-timing_parameter_id" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-timing_type">定时任务参数</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-timing_parameter" name="form-add-timing_parameter" value="-Xms2G -Xmx5G" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


            </form>
        </div>

    </div>
<!-- /.page-content -->
</div>
<script type="text/javascript">

    function changeTiming_yn(val) {
        if(val == "true") {
            document.getElementById("form-add-timing_type").style.display = "";
            document.getElementById("form-add-timing_machine").style.display = "";
            document.getElementById("form-add-timing_parameter_id").style.display = "";
            reloadJson();
            dupTimingCheck();
            var job_name = document.getElementById("form-add-job_name");
            if(job_name.value==null || job_name.value=="") {
                return;
            }
            var index = job_name.value.indexOf("CRON_");
            if(index == -1) {
                job_name.value = "CRON_" + job_name.value;
            }
        } else {
            document.getElementById("form-add-timing_type").style.display = "none";
            document.getElementById("form-add-timing_machine").style.display = "none";
            document.getElementById("form-add-timing_parameter_id").style.display = "none";
            var job_name = document.getElementById("form-add-job_name");
            if(job_name.value==null || job_name.value=="") {
                return;
            }
            var index = job_name.value.indexOf("CRON_");
            if(index >= 0) {
                job_name.value = job_name.value.substring(5);
            }
        }

    }

    function dupTimingCheck() {
        var timing_yn = $('#form-add-timing_yn').val();
        var srcID = $('#form-add-src-name').val();
        var destID = $('#form-add-dest-name').val();
        var mediaNameValue = $('#form-add-media-name').val();
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doTimingDupCheck?srcID="+srcID+"&destID="+destID+"&mediaName="+mediaNameValue+"&timing_yn="+timing_yn,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if(data!="SUCCESS") {
                    alert("相同源库，目标库，介质名称的定时任务有重复 -> "+data);
                }
            }
        });
    }

</script>
