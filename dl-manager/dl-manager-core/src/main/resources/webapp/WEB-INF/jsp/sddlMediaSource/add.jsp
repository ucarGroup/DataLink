<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner ace-save-state">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#primaryId">Primary</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#secondaryId">Secondary</a>
                    </li>
                </ul>
                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="primaryId" class="tab-pane in active">
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right"
                                   for="form-add-sddlName">SDDL名称</label>

                            <div class="col-sm-9">
                                <input type="text" style="width:350px;height:35px" id="form-add-sddlName"
                                       name="sddlName" class="col-xs-10 col-sm-5"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right"
                                   for="form-add-primaryRdbId">Primary DBs</label>

                            <div class="col-sm-9">
                                <select multiple="" name="primaryRdbId" class="primaryRdbId tag-input-style"
                                        data-placeholder="Click to Choose..." id="form-add-primaryRdbId"
                                        style="width:350px;">
                                    <c:forEach items="${mediaSourceInfoList}" var="bean">
                                        <option value="${bean.id}">${bean.name} </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right"
                                   for="form-add-proxyDbId">Proxy DB</label>

                            <div class="col-sm-9">
                                <select multiple="" name="proxyDbId" class="proxyDbId tag-input-style"
                                        data-placeholder="Click to Choose..." id="form-add-proxyDbId"
                                        style="width:350px;">
                                    <c:forEach items="${mediaSourceInfoList}" var="bean">
                                        <option value="${bean.id}">${bean.name} </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-sddlDesc">描述</label>

                            <div class="col-sm-9">
                        <textarea id="form-add-sddlDesc" name="sddlDesc" class="col-xs-10 col-sm-5"
                                  style="margin: 0px; width: 350px; height: 91px;"/>
                            </div>
                        </div>
                    </div>
                    <div id="secondaryId" class="tab-pane">
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right"
                                   for="form-add-secondaryRdbId">Secondary DBs</label>

                            <div class="col-sm-9">
                                <select multiple="" name="secondaryRdbId" class="secondaryRdbId tag-input-style"
                                        data-placeholder="Click to Choose..." id="form-add-secondaryRdbId"
                                        style="width:100%;">
                                    <c:forEach items="${mediaSourceInfoList}" var="bean">
                                        <option value="${bean.id}">${bean.name} </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
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
    $('.primaryRdbId').css('min-width', '100%').select2({
        allowClear: false,
        width: '45%'
    });
    $('.secondaryRdbId').css('min-width', '100%').select2({
        allowClear: false,
        width: '45%'
    });

    $('.proxyDbId').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '45%'
    });

    function doAdd() {
        var sddlName = $.trim($("#form-add-sddlName").val());
        var rdbId = $.trim($("#form-add-primaryRdbId").val());
        var proxyDbId = $.trim($("#form-add-proxyDbId").val());
        var sddlDesc = $.trim($("#form-add-sddlDesc").val());
        if (sddlName == "") {
            alert("sddl名称不能为空!");
            return false;
        }
        if (rdbId == "") {
            alert("[Primary DBs]不能为空!");
            return false;
        }
        if(proxyDbId == ""){
            alert("[Proxy DB]不能为空!");
            return false;
        }
        if (sddlDesc == "") {
            alert("描述不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/sddl/doAdd",
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
