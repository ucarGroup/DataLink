<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">Kafka集群名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               value="kafka_"
                               id="form-add-name"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-mediaSourceType">数据源类型</label>

                    <div class="col-sm-9">
                        <select name="kafkaMediaSrcParameter.mediaSourceType" id="form-add-mediaSourceType"
                                style="width:350px;height:35px"
                                class="chosen-select col-xs-10 col-sm-5">
                            <option value="KAFKA">KAFKA</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-add-topic">topic</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" id="form-add-topic"
                               name="kafkaMediaSrcParameter.topic" class="col-xs-10 col-sm-5"/>
                    </div>
                </div>


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-bootstrapServers">bootstrapServers</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-bootstrapServers" name="kafkaMediaSrcParameter.bootstrapServers"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="200"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-paramters">参数</label>

                    <div class="col-sm-9">
                        <input type="text" id="form-add-paramters" name="kafkaMediaSrcParameter.paramters"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="300"/>
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
        var desc = $.trim($("#form-add-desc").val());
        var bootstrapServers = $.trim($("#form-add-bootstrapServers").val());
        if (bootstrapServers == "") {
            alert("bootstrapServers不能为空!");
            return false;
        }
        if (name == "") {
            alert("名称不能为空!");
            return false;
        }
        if (topic == "") {
            alert("topic不能为空!");
            return false;
        }
        if (desc == "") {
            alert("描述不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/kafka/doAdd",
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
        kafkaListMyTable.ajax.reload();
    }


</script>
