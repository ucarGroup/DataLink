<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <input type="hidden" name="id" value="${interceptorInfo.id}">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">名称</label>

                    <div class="col-sm-9">
                        <input type="text" value="${interceptorInfo.name}" style="width:350px;height:35px"
                               id="form-update-name" name="name" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">描述</label>

                    <div class="col-sm-9">
                        <input type="text" value="${interceptorInfo.desc}" style="width:350px;height:35px"
                               id="form-update-desc" name="desc" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-type">类型</label>

                    <div class="col-sm-6">
                        <select name="type" id="form-update-type" class="chosen-select col-sm-5">
                            <c:forEach items="${interceptorTypeList}" var="bean">
                                <c:if test="${bean==interceptorInfo.type}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=interceptorInfo.type}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-content">内容</label>

                    <div class="col-sm-9">
                        <textarea spellcheck="false" rows="15" id="form-update-content" name="content"
                                  class="col-sm-10">${interceptorInfo.content}</textarea>
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
        var name = $.trim($("#form-update-name").val());
        var desc = $.trim($("#form-update-desc").val());
        var type = $.trim($("#form-update-type").val());
        var content = $.trim($("#form-update-content").val());
        if (name == "") {
            alert("名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("描述不能为空!");
            return false;
        }
        if (type == "") {
            alert("类型不能为空!");
            return false;
        }
        if (content == "") {
            alert("内容不能为空!");
            return false;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/interceptor/doEdit",
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
