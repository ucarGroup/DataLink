<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <input type="hidden" name="id" value="${menuInfo.id}">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-code">编码</label>

                    <div class="col-sm-9">
                        <input type="text" value="${menuInfo.code}" style="width:350px;height:35px"
                               id="form-update-code" name="code" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">名称</label>

                    <div class="col-sm-9">
                        <input type="text" value="${menuInfo.name}" style="width:350px;height:35px"
                               id="form-update-name" name="name" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-parentCode">父节点</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-update-parentCode" name="parentCode"
                                class="parentCode tag-input-style" data-placeholder="Click to Choose..."
                                style="width: 200px;">
                            <c:forEach items="${menuList}" var="bean">
                                <c:if test="${bean.code==menuInfo.parentCode}">
                                    <option selected="selected" value="${bean.code}">${bean.name} </option>
                                </c:if>
                                <c:if test="${bean.code!=menuInfo.parentCode}">
                                    <option value="${bean.code}">${bean.name} </option>
                                </c:if>
                            </c:forEach>
                        </select>

                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-type">菜单类型</label>

                    <div class="col-sm-6">
                        <select name="type" id="form-update-type" class="chosen-select col-sm-5">
                            <c:forEach items="${menuTypeList}" var="bean">
                                <c:if test="${bean==menuInfo.type}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=menuInfo.type}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-url">路径</label>

                    <div class="col-sm-9">
                        <input type="text" value="${menuInfo.url}" style="width:350px;height:35px" id="form-update-url"
                               name="url" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-icon">标识</label>

                    <div class="col-sm-9">
                        <input type="text" value="${menuInfo.icon}" style="width:350px;height:35px"
                               id="form-update-icon" name="icon" class="col-xs-10 col-sm-5"/>
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
    $('.type').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});
    function doEdit() {
        var code = $.trim($("#form-update-code").val());
        var name = $.trim($("#form-update-name").val());
        var parentCode = $.trim($("#form-update-parentCode").val());
        var type = $.trim($("#form-update-type").val());
        if (code == "") {
            alert("菜单编码不能为空!");
            return false;
        }
        if (name == "") {
            alert("菜单名称不能为空!");
            return false;
        }
        if (parentCode == "") {
            alert("父节点编码不能为空!");
            return false;
        }
        if (type == "") {
            alert("菜单类型不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/menu/doEdit",
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
