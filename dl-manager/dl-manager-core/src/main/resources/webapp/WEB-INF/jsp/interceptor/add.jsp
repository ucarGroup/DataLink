<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-name" name="name"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-desc">描述</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-desc" name="desc"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-type">类型</label>

                    <div class="col-sm-6">
                        <select name="type" id="form-add-type" class="chosen-select col-sm-5">
                            <c:forEach items="${interceptorTypeList}" var="bean">
                                <c:if test="${bean==type}">
                                    <option selected="selected" value="${bean}">${bean}</option>
                                </c:if>
                                <c:if test="${bean!=type}">
                                    <option value="${bean}">${bean}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-content">内容</label>

                    <div class="col-sm-9">
                        <textarea spellcheck="false" rows="15" id="form-add-content" name="content"
                                  class="col-sm-10"></textarea>
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
    $('.type').css('min-width', '50%').select2({allowClear: false, maximumSelectionLength: 1, width: '45%'});
    function doAdd() {
        var name = $.trim($("#form-add-name").val());
        var desc = $.trim($("#form-add-desc").val());
        var type = $.trim($("#form-add-type").val());
        var content = $.trim($("#form-add-content").val());
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
            url: "${basePath}/interceptor/doAdd",
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
