<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-workerName">机器名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-workerName" name="workerName"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-groupId">所属分组</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-add-groupId" name="groupId" class="groupId col-xs-10 col-sm-5"
                                data-placeholder="Click to Choose..." style="width:350px;height:35px">
                            <c:forEach items="${groupList}" var="bean">
                                <option value="${bean.id}">${bean.groupName} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-workerAddress">机器IP</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-workerAddress"
                               name="workerAddress" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-restPort">Rest端口号</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-restPort" name="restPort"
                               value="8083"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-workerDesc">机器描述</label>

                    <div class="col-sm-9">
                        <textarea id="form-add-workerDesc" name="workerDesc" class="col-xs-10 col-sm-5"
                                  style="margin: 0px; width: 354px; height: 91px;"/>
                    </div>
                </div>
            </form>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doAdd()">
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
    $('.groupId').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1});
    function doAdd() {
        var workerName = $.trim($("#form-add-workerName").val());
        var groupId = $.trim($("#form-add-groupId").val());
        var workerAddress = $.trim($("#form-add-workerAddress").val());
        var restPort = $.trim($("#form-add-restPort").val());
        var workerDesc = $.trim($("#form-add-workerDesc").val());

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

        var r = /^\+?[1-9][0-9]*$/;//正整数
        var flag = r.test(restPort);
        if (!flag) {
            alert("端口号必须为正整数");
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/worker/doAdd",
            dataType: "json",
            data: $("#add_form").serialize(),
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
        $("#add").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
