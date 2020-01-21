<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<script type="text/javascript">
    var currentPageName = "mysql";
</script>



<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#readerId">Reader配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#writerId">Writer配置</a>
                    </li>
                </ul>

                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <jsp:include page="mysqlTaskBasic.jsp"/>
                    </div>

                    <!--Reader配置-->
                    <div id="readerId" class="tab-pane">
                        <jsp:include page="mysqlTaskReader.jsp"/>
                    </div>

                    <!--Writer配置-->
                    <div id="writerId" class="tab-pane">
                        <jsp:include page="taskWriter.jsp"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="add();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    更新
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

    $(".taskSyncMode").val('${taskModel.taskBasicInfo.taskSyncMode}').select2({allowClear: false, maximumSelectionLength: 1});
    if(${taskModel.taskBasicInfo.taskSyncMode=='SingleLab'}){
        $("#sourceLabIdDiv").hide();
        $("#targetLabIdDiv").hide();
    }else{
        $(".sourceLabId").val('${taskModel.taskParameter.sourceLabId}').select2({allowClear: false, maximumSelectionLength: 1});
        $(".targetLabId").val('${taskModel.taskParameter.targetLabId}').select2({allowClear: false, maximumSelectionLength: 1});
    }

    function back2Main() {
        $("#mysqlTaskUpdate").empty();
        $("#main-container").show();
        oTable.draw(false);
    }

    $('#basic-taskSyncMode').change(function () {
        debugger;
        $("#mysqlReader-mediaSourceId").empty();
        $("#mysqlReader-mediaSourceId").trigger("chosen:updated");

        var taskSyncMode = $('#basic-taskSyncMode').val();
        if (taskSyncMode == null) {
            $("#sourceLabIdDiv").hide();
            $("#targetLabIdDiv").hide();
            return;
        }

        //如果选择的是夸机房
        if(taskSyncMode == 'AcrossLab'){
            $("#sourceLabIdDiv").show();
            $("#targetLabIdDiv").show();
        }else{
            $("#sourceLabIdDiv").hide();
            $("#targetLabIdDiv").hide();
        }
        findMediaSources();

    });

    $('#basic-sourceLabId').change(function () {

        debugger;
        $("#mysqlReader-mediaSourceId").empty();
        $("#mysqlReader-mediaSourceId").trigger("chosen:updated");

        var sourceLabId = $('#basic-sourceLabId').val();
        if (sourceLabId == null) {
            return;
        }
        findMediaSources();
    });

    function findMediaSources() {
        var taskSyncModeArr = $('#basic-taskSyncMode').val();
        var taskSyncMode;
        if(taskSyncModeArr && taskSyncModeArr.length >= 1){
            taskSyncMode = taskSyncModeArr[0];
        }
        var sourceLabIdArr = $('#basic-sourceLabId').val();
        var sourceLabId;
        if(sourceLabIdArr && sourceLabIdArr.length >= 1){
            sourceLabId = sourceLabIdArr[0];
        }
        var data;
        if(taskSyncModeArr == 'SingleLab' ){
            data = "&taskSyncMode=" + taskSyncMode;
        }else{
            if(sourceLabId){
                data = "&taskSyncMode=" + taskSyncMode + "&sourceLabId=" + sourceLabId;
            }
            else{
                data = "&taskSyncMode=" + taskSyncMode + "&sourceLabId=" + 0;
            }
        }

        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/findMediaSourcesBySyncMode",
            async: true,
            dataType: "json",
            data: data,
            success: function (result) {
                if (result != null && result != '') {
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#mysqlReader-mediaSourceId").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                        $("#mysqlReader-mediaSourceId").trigger("chosen:updated");
                    }
                }
            }
        });
    }

    function add() {
        var obj = {};
        obj.taskBasicInfo = getBasicObj();
        obj.taskParameter = getTaskParameterObj();
        obj.mysqlReaderParameter = getMysqlReaderObj();
        obj.writerParameterMap = getWritersObj();
        if(obj.taskBasicInfo.alarmPriorityId == null || obj.taskBasicInfo.alarmPriorityId == '') {
            alert("请选择报警方式!");
            return ;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/doUpdateMysqlTask",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("更新成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>
