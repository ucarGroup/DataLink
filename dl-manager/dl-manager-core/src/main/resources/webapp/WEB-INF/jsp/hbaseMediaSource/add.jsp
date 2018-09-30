<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">HBase集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-desc">HBase集群描述</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="desc" class="col-xs-10 col-sm-5"
                               id="form-add-desc"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-znodeParent">znode路径</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-znodeParent"
                               name="hbaseMediaSrcParameter.znodeParent" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-keyvalueMaxsize">KeyValue最大长度</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-keyvalueMaxsize" name="hbaseMediaSrcParameter.keyvalueMaxsize"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" value="1048576000"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-zkMediaSourceId">所属Zk集群</label>
                    <div class="col-sm-9">
                        <select name="hbaseMediaSrcParameter.zkMediaSourceId" id="form-add-zkMediaSourceId"
                                style="width:350px;height:35px" class="chosen-select col-sm-5">
                            <c:forEach items="${zkMediaSourceList}" var="bean">
                                <option value="${bean.id}">${bean.name} </option>
                            </c:forEach>
                        </select>
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
        var desc = $.trim($("#form-add-desc").val());
        var hdfsUrl = $.trim($("#form-add-HDFSurl").val());
        var znodeParent = $.trim($("#form-add-znodeParent").val());
        var zkMediaSourceId = $.trim($("#form-add-zkMediaSourceId").val());
        var keyvalueMaxsize = $.trim($("#form-add-keyvalueMaxsize").val());

        if (name == "") {
            alert("集群名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("集群描述不能为空!");
            return false;
        }
        if (znodeParent == "") {
            alert("znode路径不能为空!");
            return false;
        }

        if (zkMediaSourceId == "") {
            alert("所属的ZK集群不能为空!");
            return false;
        }

        if (keyvalueMaxsize == "") {
            alert("keyvalue最大长度不能为空!");
            return false;
        }


        $.ajax({
            type: "post",
            url: "${basePath}/hbase/doAdd",
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
        hbaseListMyTable.ajax.reload();
    }
</script>
