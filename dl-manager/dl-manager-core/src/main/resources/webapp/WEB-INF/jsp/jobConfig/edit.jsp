<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<%@ include file="/WEB-INF/jsp/jobConfig/jobInclude.jsp" %>
<!--
<link rel="stylesheet" href="${basePath}/assets/css/jsoneditor.css" />
<script src="${basePath}/assets/js/jsoneditor.js"></script>
-->
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="update_form" class="form-horizontal" role="form">

                <input type="hidden" id="job_id" name="id" value="${jobConfigView.id}">
                <input type="hidden" id="job_create_time" name="create_time" value="${jobConfigView.create_time}">


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-job_name">任务名称</label>
                    <div class="col-sm-9">
                        <input readonly="readonly" type="text" id="form-update-job_name" name="job_name" value="${jobConfigView.job_name}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <input type="hidden" id="job_timing_yn" name="timing_yn" value="${jobConfigView.timing_yn}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-timing_yn">是否定时任务</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-edit-timing_yn" style="width:350px;height:35px" class="chosen-select col-sm-5" onchange="changeTiming_yn(this.value)">
                        <c:if test="${jobConfigView.timing_yn=='true'}">
                            <option grade="0" value="false" >否</option>
                            <option grade="1" value="true" selected>是</option>
                        </c:if>
                        <c:if test="${jobConfigView.timing_yn=='false'}">
                            <option grade="0" value="false" selected>关闭定时任务</option>
                            <option grade="1" value="true" >开启定时任务</option>
                        </c:if>
                    </select>
                    </div>
                </div>

                <div class="form-group" id="form-edit-timing_type" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-timing_type">任务类型</label>
                    <div class="col-sm-9">
                        <select id="form-edit-timing_type_id" style="width:350px;height:35px" class="chosen-select col-sm-5" onblur="reloadJson()" >
                            <c:if test="${jobConfigView.timing_transfer_type=='FULL'}">
                                <option grade="0" value="FULL" selected>全量任务</option>
                                <option grade="1" value="INCREMENT" >增量任务</option>
                            </c:if>
                            <c:if test="${jobConfigView.timing_transfer_type=='INCREMENT'}">
                                <option grade="0" value="FULL" >全量任务</option>
                                <option grade="1" value="INCREMENT" selected>增量任务</option>
                            </c:if>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="form-edit-timing_machine" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-timing_machine">指定在哪台机器上运行</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-timing_machine_id" name="form-add-timing_machine" value="${jobConfigView.timing_target_worker}" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group" id="form-edit-timing_parameter" style="display: none;">
                    <label class="col-sm-3 control-label no-padding-right" for="form-edit-timing_type">JVM启动参数</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-edit-timing_parameter_id" name="form-add-timing_parameter" value="${jobConfigView.timing_parameter}" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
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

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" >job内容</label>
                    <div class="col-sm-9">
                        <div id="add_jsoneditor" style="height:50%"></div>
                    </div>
                </div>

                <div id="job_content_teztarea" style="display: none;">${jobConfigView.job_content}</div>

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

    var add_container = document.getElementById('add_jsoneditor');
    var add_options = {modes: ['text', 'tree']};
    var add_editor = new JSONEditor(add_container, add_options);
    var job_content = document.getElementById('job_content_teztarea').innerHTML;
    var job_json_object = JSON.parse(job_content);

    try {
        var reader_where = job_json_object.job.content[0].reader.parameter.where;
        if(typeof(reader_where)=="undefined") {
            //alert("read_where --> null");
        } else {
            reader_where = reader_where.replace(/&gt;/g,">") ;
            reader_where = reader_where.replace(/&lt;/g,"<") ;
            job_json_object.job.content[0].reader.parameter.where = reader_where;
        }
        var connection_var = job_json_object.job.content[0].reader.parameter.connection;
        if(typeof(connection_var)=="undefined") {

        } else {
            var readerQuerySql = job_json_object.job.content[0].reader.parameter.connection[0].querySql;
            if(typeof(readerQuerySql)=="undefined") {
                //alert("query_sql null");
            } else {
                var query_sql = readerQuerySql[-1];
                query_sql = query_sql.replace(/&gt;/g,">") ;
                query_sql = query_sql.replace(/&lt;/g,"<") ;
                job_json_object.job.content[-1].reader.parameter.connection[0].querySql[0] = query_sql;
            }
        }
    }catch(error) {
        //ignore
    }






    add_editor.set(job_json_object);
    //alert("ok~~");
//
    function changeTiming_yn(val) {
        if(val == "true") {
            document.getElementById("form-edit-timing_type").style.display = "";
            document.getElementById("form-edit-timing_machine").style.display = "";
            document.getElementById("form-edit-timing_parameter").style.display = "";
        } else {
            document.getElementById("form-edit-timing_type").style.display = "none";
            document.getElementById("form-edit-timing_machine").style.display = "none";
            document.getElementById("form-edit-timing_parameter").style.display = "none";
        }
    }

//    function changeSchedule_yn(val) {
//        if(val == "true") {
//            document.getElementById("form-update-scheudle-cron").style.display = "";
//            document.getElementById("form-update-schedule-isretry").style.display = "";
//            document.getElementById("form-update-schedule-maxretry").style.display = "";
//            document.getElementById("form-update-schedule_retry_interval").style.display = "";
//            document.getElementById("form-update-schedule_maxRuntime").style.display = "";
//            document.getElementById("form-update-schedule-onlineState").style.display = "";
//            document.getElementById("form-update-schedule-isSuppend").style.display = "";
//        } else {
//            document.getElementById("form-update-scheudle-cron").style.display = "none";
//            document.getElementById("form-update-schedule-isretry").style.display = "none";
//            document.getElementById("form-update-schedule-maxretry").style.display = "none";
//            document.getElementById("form-update-schedule_retry_interval").style.display = "none";
//            document.getElementById("form-update-schedule_maxRuntime").style.display = "none";
//            document.getElementById("form-update-schedule-onlineState").style.display = "none";
//            document.getElementById("form-update-schedule-isSuppend").style.display = "none";
//        }
//    }

    var job_timing_yn = $('#job_timing_yn').val();
    if(job_timing_yn=="true") {
        document.getElementById("form-edit-timing_type").style.display = "";
        document.getElementById("form-edit-timing_machine").style.display = "";
        document.getElementById("form-edit-timing_parameter").style.display = "";
    }

    function edit() {
        var id = $('#job_id').val();
        var job_name = $('#form-update-job_name').val();
        var job_create_time = $('#job_create_time').val();
        var content = add_editor.getText();
        var jobContent = formatJson(content);
        var timing_transfer_type = $("#form-edit-timing_type_id").val();
        var timing_target_worker = $("#form-edit-timing_machine_id").val();
        var timing_parameter =     $("#form-edit-timing_parameter_id").val();
        var timing_yn =            $("#form-edit-timing_yn").val();
//        if(timing_yn=="true") {
//            if(timing_transfer_type == "") {
//                alert("定时器类型必须选择");
//            }
//            if(timing_parameter == "") {
//                alert("定时器参数必须填写");
//            }
//        }

        var json = "id="+id+"&job_name="+job_name+"&timing_yn="+timing_yn+"&timing_transfer_type="+timing_transfer_type+"&timing_target_worker="+timing_target_worker
        +"&timing_parameter="+timing_parameter+"&create_time="+job_create_time+"&job_content="+jobContent;

//        var schedule_cron = $("#form-update-scheudle-cron_id").val();
//        var schedule_is_retry = $("#form-update-schedule-isretry_id").val();
//        var schedule_max_retry = $("#form-update-schedule-maxretry_id").val();
//        var schedule_retry_interval = $("#form-update-schedule_retry_interval_id").val();
//        var schedule_max_runtime = $("#form-update-schedule_maxRuntime_id").val();
//        var schedule_online_state = $("#form-update-schedule-onlineState_id").val();
//        var schedule_is_suppend = $("#form-update-schedule-isSuppend_id").val();
//        json = json +"&schedule_cron="+schedule_cron +"&schedule_is_retry="+schedule_is_retry;
//        json = json +"&schedule_max_retry="+schedule_max_retry +"&schedule_retry_interval="+schedule_retry_interval;
//        json = json+"&schedule_max_runtime="+schedule_max_runtime +"&schedule_online_state="+schedule_online_state;
//        json = json+"&schedule_is_suppend="+schedule_is_suppend;


        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doEdit",
            dataType: "json",
            data: json,
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
        jobListTable.draw(false);
    }

    function repeat(s, count) {
        return new Array(count + 1).join(s);
    }

    function formatJson(json) {
        var i           = 0,
                len          = 0,
                tab         = "    ",
                targetJson     = "",
                indentLevel = 0,
                inString    = false,
                currentChar = null;
        for (i = 0, len = json.length; i < len; i += 1) {
            currentChar = json.charAt(i);
            switch (currentChar) {
                case '{':
                case '[':
                    if (!inString) {
                        targetJson += currentChar + "\n" + repeat(tab, indentLevel + 1);
                        indentLevel += 1;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case '}':
                case ']':
                    if (!inString) {
                        indentLevel -= 1;
                        targetJson += "\n" + repeat(tab, indentLevel) + currentChar;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ',':
                    if (!inString) {
                        targetJson += ",\n" + repeat(tab, indentLevel);
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ':':
                    if (!inString) {
                        targetJson += ": ";
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ' ':
                case "\n":
                case "\t":
                    if (inString) {
                        targetJson += currentChar;
                    }
                    break;
                case '"':
                    if (i > 0 && json.charAt(i - 1) !== '\\') {
                        inString = !inString;
                    }
                    targetJson += currentChar;
                    break;
                default:
                    targetJson += currentChar;
                    break;
            }
        }
        return targetJson;
    }


</script>
