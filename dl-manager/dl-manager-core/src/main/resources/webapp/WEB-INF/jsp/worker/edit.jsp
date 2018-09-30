<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#javaoptsDiv">worker运行内存参数</a>
                    </li>
                </ul>

                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">
                        <form id="update_form" class="form-horizontal" role="form">
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-workerName">机器名称</label>

                                <div class="col-sm-9">
                                    <input value="${workerInfo.workerName}" type="text" style="width:350px;height:35px"
                                           id="form-update-workerName" name="workerName" class="col-xs-10 col-sm-5"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-groupId">所属分组</label>

                                <div class="col-sm-9">
                                    <select multiple="" id="form-update-groupId" name="groupId" class="groupId col-xs-10 col-sm-5"
                                            data-placeholder="Click to Choose..." style="width:350px;height:35px">
                                        <c:forEach items="${groupList}" var="bean">
                                            <option value="${bean.id}">${bean.groupName} </option>
                                        </c:forEach>
                                    </select>
                                    <input type="hidden" name="id" value="${workerInfo.id}">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-workerAddress">机器IP</label>

                                <div class="col-sm-9">
                                    <input type="text" value="${workerInfo.workerAddress}" style="width:350px;height:35px"
                                           id="form-update-workerAddress" name="workerAddress" class="col-xs-10 col-sm-5"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-restPort">Rest端口号</label>

                                <div class="col-sm-9">
                                    <input type="text" value="${workerInfo.restPort}" style="width:350px;height:35px"
                                           id="form-update-restPort" name="restPort" class="col-xs-10 col-sm-5"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-workerDesc">机器描述</label>

                                <div class="col-sm-9">
                                    <textarea id="form-update-workerDesc" name="workerDesc" class="col-xs-10 col-sm-5"
                                  style="margin: 0px; width: 354px; height: 91px;"/>
                                </div>
                            </div>
                            <input type="hidden" name="javaopts" id="javaopts">
                        </form>
                    </div>

                    <div id="javaoptsDiv" class="tab-pane">
                        <div class="col-sm-12 panel panel-info">
                            <div class="panel-heading">
                                <h1 class="panel-title">JVM运行参数</h1>
                            </div>
                            <div class="panel-body">
                                <form id="form-javaopts" class="form-horizontal" role="form">
                                    <div class="form-group">
                                        <div class="col-sm-9">
                                            <textarea id="javaoptsOld" name="javaoptsOld" class="col-xs-10 col-sm-5"
                                                      style="margin: 0px; width: 800px; height: 150px;" readonly="readonly"/>
                                        </div>
                                    </div>
                                    编辑区：
                                    <div class="form-group">
                                        <div class="col-sm-9">
                                            <textarea id="javaoptsTest" name="javaoptsTest" class="col-xs-10 col-sm-5"
                                              style="margin: 0px; width: 800px; height: 150px;"/>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doEdit()">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    保存
                </button>
                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="refresh()">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>
    </div>
    <!-- /.page-content -->
</div>

<script type="text/javascript">
    $("#form-update-workerDesc").val('${workerInfo.workerDesc}');
    $("#javaoptsOld").val('${workerInfo.javaopts}');
    $("#javaoptsTest").val('${workerInfo.javaopts}');

    $(".groupId").val('${workerInfo.groupId}').select2({allowClear: false});
    function doEdit() {
        var workerName = $.trim($("#form-update-workerName").val());
        var groupId = $.trim($("#form-update-groupId").val());
        var workerAddress = $.trim($("#form-update-workerAddress").val());
        var restPort = $.trim($("#form-update-restPort").val());
        var workerDesc = $.trim($("#form-update-workerDesc").val());

        if (workerName == "") {
            alert("机器名称不能为空!");
            return false;
        }
        if (groupId == "") {
            alert("分组名称不能为空!");
            return false;
        }
        if (workerAddress == "") {
            alert("机器ip不能为空!");
            return false;
        }
        if (workerDesc == "") {
            alert("机器描述不能为空!");
            return false;
        }

        var javaoptsOld = $("#javaoptsOld").val();
        var javaoptsTest = $("#javaoptsTest").val();
        if (javaoptsTest != javaoptsOld) {
            if (!confirm("JVM运行参数已发生变更，是否更新新到worker内存配置文件中？")) {
                return;
            }
            $("#javaopts").val(javaoptsTest);
        }

        var r = /^\+?[1-9][0-9]*$/;//正整数
        var flag = r.test(restPort);
        if (!flag) {
            alert("端口号必须为正整数");
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/worker/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    refresh();
                } else {
                    alert(data);
                }
            }
        });
    }
    function refresh() {
        $("#edit").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
