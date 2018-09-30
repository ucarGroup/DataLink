<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${esMediaSourceView.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">ES集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name" value="${esMediaSourceView.name}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-desc">ES集群描述</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="desc" class="col-xs-10 col-sm-5"
                               id="form-add-desc" value="${esMediaSourceView.desc}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-clusterHosts">集群IP(逗号分隔)</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-clusterHosts"
                               name="esMediaSrcParameter.clusterHosts" class="col-xs-10 col-sm-5"
                               value="${esMediaSourceView.esMediaSrcParameter.clusterHosts}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-httpPort">HTTP端口</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-httpPort"
                               name="esMediaSrcParameter.httpPort" class="col-xs-10 col-sm-5"
                               value="${esMediaSourceView.esMediaSrcParameter.httpPort}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-tcpPort">TCP端口</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-tcpPort" name="esMediaSrcParameter.tcpPort"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"
                               value="${esMediaSourceView.esMediaSrcParameter.tcpPort}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-userName">用户名</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-userName" name="esMediaSrcParameter.userName"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"
                               value="${esMediaSourceView.esMediaSrcParameter.userName}"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-password">密码</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-password" name="esMediaSrcParameter.password"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"
                               value="${esMediaSourceView.esMediaSrcParameter.password}"/>
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
        var name = $.trim($("#form-add-name").val());
        var desc = $.trim($("#form-add-desc").val());
        var clusterHosts = $.trim($("#form-add-clusterHosts").val());
        var httpPort = $.trim($("#form-add-httpPort").val());
        var tcpPort = $.trim($("#form-add-tcpPort").val());
        var userName = $.trim($("#form-add-userName").val());
        var password = $.trim($("#form-add-password").val());

        if (name == "") {
            alert("集群名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("集群描述不能为空!");
            return false;
        }
        if (clusterHosts == "") {
            alert("集群IP不能为空!");
            return false;
        }
        if (httpPort == "") {
            alert("HTTP端口不能为空!");
            return false;
        }
        if (tcpPort == "") {
            alert("TCP端口不能为空!");
            return false;
        }
        if (userName == "") {
            alert("用户名不能为空!");
            return false;
        }
        if (password == "") {
            alert("密码不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/es/doEdit",
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
        esListMyTable.ajax.reload();
    }
</script>
