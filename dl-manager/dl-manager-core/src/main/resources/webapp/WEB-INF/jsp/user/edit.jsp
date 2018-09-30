<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <input type="hidden" name="id" value="${userInfo.id}">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-userName">用户名称</label>

                    <div class="col-sm-9">
                        <input type="text" value="${userInfo.userName}" style="width:350px;height:35px"
                               id="form-update-userName" name="userName" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-ucarEmail">集团邮箱前缀</label>

                    <div class="col-sm-9">
                        <input type="text" value="${userInfo.ucarEmail}" style="width:350px;height:35px"
                               id="form-update-ucarEmail" name="ucarEmail" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-phone">手机号</label>

                    <div class="col-sm-9">
                        <input type="text" value="${userInfo.phone}" style="width:350px;height:35px"
                               id="form-update-phone" name="phone" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-role">角色</label>

                    <div class="col-sm-6">
                        <select name="roleIdStr" class="roleIdClass col-sm-5"
                                data-placeholder="Click to Choose..." id="form-update-role"
                                multiple>
                            <c:forEach items="${roleList}" var="bean">
                                <option value="${bean.id}">${bean.code}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-isAlarm">是否接收报警</label>

                    <div class="col-sm-6">
                        <select name="isAlarm" id="form-update-isAlarm" class="chosen-select col-sm-5">
                            <option value="false">否</option>
                            <option value="true">是</option>
                        </select>
                    </div>
                </div>


            </form>
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
    $("#form-update-isAlarm").val('${userInfo.isAlarm}');
    $('.roleId').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});

    var pe = '${userInfo.roleIdStr}'.split(",");
    $('.roleIdClass').val(pe).select2({
        allowClear: false
    });

    function doEdit() {
        var userName = $.trim($("#form-update-userName").val());
        var role = $.trim($("#form-update-role").val());
        var phone = $.trim($("#form-update-phone").val());
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
            url: "${basePath}/user/doEdit",
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