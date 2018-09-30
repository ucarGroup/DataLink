<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">Zk集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-mediaSourceType">数据源类型</label>

                    <div class="col-sm-9">
                        <select name="zkMediaSrcParameter.mediaSourceType" id="form-add-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="ZOOKEEPER">ZOOKEEPER</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-servers">ip地址</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-servers"
                               name="zkMediaSrcParameter.servers" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-sessionTimeout">sessionTimeout(ms)</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-sessionTimeout"
                               name="zkMediaSrcParameter.sessionTimeout" value="6000"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-connectionTimeout">connectionTimeout(ms)</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-connectionTimeout"
                               name="zkMediaSrcParameter.connectionTimeout" value="6000"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-desc">描述</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-desc" name="desc"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"/>
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
    function doAdd() {
        var name = $.trim($("#form-add-name").val());
        var servers = $.trim($("#form-add-servers").val());
        var sessionTimeout = $.trim($("#form-add-sessionTimeout").val());
        var connectionTimeout = $.trim($("#form-add-connectionTimeout").val());
        var desc = $.trim($("#form-add-desc").val());

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
            url: "${basePath}/zk/doAdd",
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
        zkListMyTable.ajax.reload();
    }
</script>
