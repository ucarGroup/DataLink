<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">Fq集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-mediaSourceType">数据源类型</label>

                    <div class="col-sm-9">
                        <select name="fqMediaSrcParameter.mediaSourceType" id="form-add-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="FLEXIBLEQ">FLEXIBLEQ</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-zkMediaSourceId">所属Zk集群</label>

                    <div class="col-sm-9">
                        <select name="fqMediaSrcParameter.zkMediaSourceId" id="form-add-zkMediaSourceId"
                                style="width:350px;height:35px" class="chosen-select col-sm-5">
                            <c:forEach items="${zkMediaSourceList}" var="bean">
                                <option value="${bean.id}">${bean.name} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-clusterPrefix">Fq集群前缀</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-clusterPrefix"
                               name="fqMediaSrcParameter.clusterPrefix" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-topic">topic</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-topic"
                               name="fqMediaSrcParameter.topic" class="col-xs-10 col-sm-5"/>
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
        var topic = $.trim($("#form-add-topic").val());
        var zkMediaSourceId = $.trim($("#form-add-zkMediaSourceId").val());
        var clusterPrefix = $.trim($("#form-add-clusterPrefix").val());
        var desc = $.trim($("#form-add-desc").val());

        if (name == "") {
            alert("名称不能为空!");
            return false;
        }
        if (topic == "") {
            alert("topic不能为空!");
            return false;
        }
        if (zkMediaSourceId == "") {
            alert("所属Zk集群不能为空!");
            return false;
        }
        if (clusterPrefix == "") {
            alert("Fq集群名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("描述不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/fq/doAdd",
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
        fqListMyTable.ajax.reload();
    }
</script>
