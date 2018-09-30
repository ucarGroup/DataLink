<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-userName">用户名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-userName" name="userName"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-ucarEmail">集团邮箱前缀</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-ucarEmail" name="ucarEmail"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-phone">手机号</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-phone" name="phone"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-role">角色</label>

                    <div class="col-sm-6">
                        <select name="roleIdStr" class="roleIdClass col-sm-5"
                                data-placeholder="Click to Choose..." id="form-add-role"
                                 multiple>
                            <c:forEach items="${roleList}" var="bean">
                                <option value="${bean.id}">${bean.code}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-isAlarm">是否接收报警</label>

                    <div class="col-sm-6">
                        <select name="isAlarm" id="form-add-isAlarm" class="chosen-select col-sm-5">
                            <option value="false">否</option>
                            <option value="true">是</option>
                        </select>
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
    $('.roleId').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});

    $('.roleIdClass').select2({
        allowClear: false
    });

    function doAdd() {
        var userName = $.trim($("#form-add-userName").val());
        var role = $.trim($("#form-add-role").val());
        var phone = $.trim($("#form-add-phone").val());
        if (userName == "") {
            alert("用户名称不能为空!");
            return false;
        }
        if (role == "") {
            alert("角色不能为空!");
            return false;
        }
        if (phone == "") {
            alert("手机号不能为空!");
            return false;
        }
        var r = /^1[3|4|5|7|8]\d{9}$/;
        var flag = r.test(phone);
        if (!flag) {
            alert("手机号格式不正确");
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/user/doAdd",
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