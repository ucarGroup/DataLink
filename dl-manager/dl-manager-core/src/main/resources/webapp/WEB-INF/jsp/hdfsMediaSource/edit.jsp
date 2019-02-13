<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${hdfsMediaSourceView.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">Hadoop集群名称</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" value="${hdfsMediaSourceView.name}" name="name"
                               class="col-xs-10 col-sm-5"
                               id="form-update-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-mediaSourceType">数据源类型</label>
                    <div class="col-sm-9">
                        <select name="hdfsMediaSrcParameter.mediaSourceType" id="form-update-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="HDFS">HDFS</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-nameServices">nameServices</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-nameServices"
                               name="hdfsMediaSrcParameter.nameServices" value="${hdfsMediaSrcParameter.nameServices}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-nameNode1">nameNode1</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-nameNode1"
                               name="hdfsMediaSrcParameter.nameNode1" value="${hdfsMediaSrcParameter.nameNode1}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-nameNode2">nameNode2</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-nameNode2"
                               name="hdfsMediaSrcParameter.nameNode2" value="${hdfsMediaSrcParameter.nameNode2}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-hadoopUser">HadoopUser</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-update-hadoopUser"
                               name="hdfsMediaSrcParameter.hadoopUser" value="${hdfsMediaSrcParameter.hadoopUser}"
                               class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-zkMediaSourceId">所属Zk集群</label>
                    <div class="col-sm-9">
                        <select name="hdfsMediaSrcParameter.zkMediaSourceId" id="form-update-zkMediaSourceId"
                                style="width:350px;height:35px" class="chosen-select col-sm-5">
                            <c:forEach items="${zkMediaSourceList}" var="bean">
                                <c:if test="${bean.id==hdfsMediaSrcParameter.zkMediaSourceId}">
                                    <option selected="selected" value="${bean.id}">${bean.name} </option>
                                </c:if>
                                <c:if test="${bean.id!=hdfsMediaSrcParameter.zkMediaSourceId}">
                                    <option value="${bean.id}">${bean.name} </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <%--<div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-spark_cube_meta">Spark cube地址</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-spark_cube_meta" name="hdfsMediaSrcParameter.sparkcubeAddress" value="${hdfsMediaSrcParameter.sparkcubeAddress}"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"/>
                    </div>
                </div>--%>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">描述</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-update-desc" name="desc" value="${hdfsMediaSourceView.desc}"
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
        var nameServices = $.trim($("#form-update-nameServices").val());
        var nameNode1 = $.trim($("#form-update-nameNode1").val());
        var nameNode2 = $.trim($("#form-update-nameNode2").val());
        var hadoopUser = $.trim($("#form-update-hadoopUser").val());
//        var spark_cube = $.trim($("#form-add-spark_cube_meta").val());
        var desc = $.trim($("#form-update-desc").val());

        if (name == "") {
            alert("名称不能为空!");
            return false;
        }
        if (nameServices == "") {
            alert("nameServices不能为空!");
            return false;
        }
        if (nameNode1 == "") {
            alert("nameNode1不能为空!");
            return false;
        }
        if (nameNode2 == "") {
            alert("nameNode2不能为空!");
            return false;
        }
        if (hadoopUser == "") {
            alert("HadoopUser不能为空!");
            return false;
        }
        /*if(spark_cube == "") {
            alert("Spark cube地址不能为空！");
            return false;
        }*/
        if (desc == "") {
            alert("描述不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/hdfs/doEdit",
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
        hdfsListMyTable.ajax.reload();
    }
</script>
