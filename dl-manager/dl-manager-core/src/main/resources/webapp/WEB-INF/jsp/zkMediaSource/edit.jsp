<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${zkMediaSourceView.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">Zk集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" value="${zkMediaSourceView.name}" name="name"
                               class="col-xs-10 col-sm-5"
                               id="form-update-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-mediaSourceType">数据源类型</label>

                    <div class="col-sm-9">
                        <select name="zkMediaSrcParameter.mediaSourceType" id="form-update-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="ZOOKEEPER">ZOOKEEPER</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-servers">ip地址</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-servers"
                               name="zkMediaSrcParameter.servers" value="${zkMediaSrcParameter.servers}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-sessionTimeout">sessionTimeout(ms)</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-sessionTimeout"
                               name="zkMediaSrcParameter.sessionTimeout" value="${zkMediaSrcParameter.sessionTimeout}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-connectionTimeout">connectionTimeout(ms)</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-connectionTimeout"
                               name="zkMediaSrcParameter.connectionTimeout"
                               value="${zkMediaSrcParameter.connectionTimeout}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">描述</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-update-desc" name="desc" value="${zkMediaSourceView.desc}"
                                  class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"/>
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
    function doEdit() {
        var name = $.trim($("#form-update-name").val());
        var servers = $.trim($("#form-update-servers").val());
        var sessionTimeout = $.trim($("#form-update-sessionTimeout").val());
        var connectionTimeout = $.trim($("#form-update-connectionTimeout").val());
        var desc = $.trim($("#form-update-desc").val());

        if (name == "") {
            alert("名称不能为空!");
            return false;
        }
        if (servers == "") {
            alert("ip地址不能为空!");
            return false;
        }
        if (sessionTimeout == "") {
            alert("sessionTimeout不能为空!");
            return false;
        }
        if (connectionTimeout == "") {
            alert("connectionTimeout不能为空!");
            return false;
        }
        if (desc == "") {
            alert("描述不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/zk/doEdit",
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
        zkListMyTable.ajax.reload();
    }
</script>
