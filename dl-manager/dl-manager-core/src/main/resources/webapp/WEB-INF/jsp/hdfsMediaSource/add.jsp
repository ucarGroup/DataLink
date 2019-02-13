<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">Hadoop集群名称</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-mediaSourceType">数据源类型</label>
                    <div class="col-sm-9">
                        <select name="hdfsMediaSrcParameter.mediaSourceType" id="form-add-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="HDFS">HDFS</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-nameServices">nameServices</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-nameServices"
                               name="hdfsMediaSrcParameter.nameServices" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-nameNode1">nameNode1</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-nameNode1"
                               name="hdfsMediaSrcParameter.nameNode1" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-nameNode2">nameNode2</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-nameNode2"
                               name="hdfsMediaSrcParameter.nameNode2" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-hadoopUser">HadoopUser</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-hadoopUser"
                               name="hdfsMediaSrcParameter.hadoopUser" value="increment" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-zkMediaSourceId">所属Zk集群</label>
                    <div class="col-sm-9">
                        <select name="hdfsMediaSrcParameter.zkMediaSourceId" id="form-add-zkMediaSourceId"
                                style="width:350px;height:35px" class="chosen-select col-sm-5">
                            <c:forEach items="${zkMediaSourceList}" var="bean">
                                <option value="${bean.id}">${bean.name} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <%--<div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-spark_cube_meta">spark cube地址</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-spark_cube_meta" name="hdfsMediaSrcParameter.sparkcubeAddress"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50"/>
                    </div>
                </div>--%>

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
        var nameServices = $.trim($("#form-add-nameServices").val());
        var nameNode1 = $.trim($("#form-add-nameNode1").val());
        var nameNode2 = $.trim($("#form-add-nameNode2").val());
        var hadoopUser = $.trim($("#form-add-hadoopUser").val());
//        var spark_cube = $.trim($("#form-add-spark_cube_meta").val());
        var desc = $.trim($("#form-add-desc").val());

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
            url: "${basePath}/hdfs/doAdd",
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
        hdfsListMyTable.ajax.reload();
    }
</script>
