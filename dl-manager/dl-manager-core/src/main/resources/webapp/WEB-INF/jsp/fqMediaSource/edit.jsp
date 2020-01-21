<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${fqMediaSourceView.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">Fq集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" value="${fqMediaSourceView.name}" name="name"
                               class="col-xs-10 col-sm-5"
                               id="form-update-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-mediaSourceType">数据源类型</label>

                    <div class="col-sm-9">
                        <select name="fqMediaSrcParameter.mediaSourceType" id="form-update-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="FLEXIBLEQ">FLEXIBLEQ</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-zkMediaSourceId">所属Zk集群</label>

                    <div class="col-sm-9">
                        <select name="fqMediaSrcParameter.zkMediaSourceId" id="form-update-zkMediaSourceId"
                                style="width:350px;height:35px" class="chosen-select col-sm-5">
                            <c:forEach items="${zkMediaSourceList}" var="bean">
                                <c:if test="${bean.id==fqMediaSrcParameter.zkMediaSourceId}">
                                    <option selected="selected" value="${bean.id}">${bean.name} </option>
                                </c:if>
                                <c:if test="${bean.id!=fqMediaSrcParameter.zkMediaSourceId}">
                                    <option value="${bean.id}">${bean.name} </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-clusterPrefix">Fq集群前缀</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-clusterPrefix"
                               name="fqMediaSrcParameter.clusterPrefix" value="${fqMediaSrcParameter.clusterPrefix}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-topic">topic</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-topic"
                               name="fqMediaSrcParameter.topic" value="${fqMediaSrcParameter.topic}" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">描述</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-update-desc" name="desc" value="${fqMediaSourceView.desc}"
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
        var topic = $.trim($("#form-update-topic").val());
        var zkMediaSourceId = $.trim($("#form-update-zkMediaSourceId").val());
        var clusterPrefix = $.trim($("#form-update-clusterPrefix").val());
        var desc = $.trim($("#form-update-desc").val());

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
            url: "${basePath}/fq/doEdit",
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
        fqListMyTable.draw(false);
    }
</script>
