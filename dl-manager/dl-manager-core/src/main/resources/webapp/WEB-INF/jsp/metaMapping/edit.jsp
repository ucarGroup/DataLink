<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${metaMappingView.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-src-type">源端类型</label>
                    <div class="col-sm-9">
                        <select multiple=""
                                class="form-add-src-type col-sm-5"
                                data-placeholder="Click to Choose..." id="form-add-src-type"
                                style="width:350px;height:35px" name="srcMediaSourceType">
                            <option grade="1" value="HBase" >HBase</option>
                            <option grade="2" value="MySql" >MySql</option>
                            <option grade="3" value="SqlServer" >SqlServer</option>
                            <option grade="4" value="HDFS"  >HDFS</option>
                            <option grade="5" value="ElasticSearch" >ElasticSearch</option>
                            <option grade="6" value="PostgreSql" >PostgreSql</option>
                            <option grade="7" value="SDDL" >SDDL</option>
                            <option grade="8" value="SDDL" >DATAX</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-target-type">目标端类型</label>
                    <div class="col-sm-9">
                        <select multiple=""
                                class="form-add-target-type col-sm-5"
                                data-placeholder="Click to Choose..." id="form-add-target-type"
                                style="width:350px;height:35px" name="targetMediaSourceType">
                            <option grade="1" value="HBase" >HBase</option>
                            <option grade="2" value="MySql" >MySql</option>
                            <option grade="3" value="SqlServer" >SqlServer</option>
                            <option grade="4" value="HDFS"  >HDFS</option>
                            <option grade="5" value="ElasticSearch" >ElasticSearch</option>
                            <option grade="6" value="PostgreSql" >PostgreSql</option>
                            <option grade="7" value="SDDL" >SDDL</option>
                            <option grade="8" value="SDDL" >DATAX</option>
                        </select>
                    </div>
                </div>


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-src-mapping">源端映射类型</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="src_mapping" class="col-xs-10 col-sm-5"
                               id="form-add-src-mapping" vlaue="${metaMappingView.srcMappingType}"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-target-mapping">目标端映射类型</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="target_mapping" class="col-xs-10 col-sm-5"
                               id="form-add-target-mapping" value="${metaMappingView.targetMappingType}"/>
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

    $('.form-add-src-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.form-add-target-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    function doEdit() {
        var src_type = $.trim($("#form-add-src-type").val());
        var target_type = $.trim($("#form-add-target-type").val());
        var src_mapping = $.trim($("#form-add-src-mapping").val());
        var target_mapping = $.trim($("#form-add-target-mapping").val());

        if (src_type == "") {
            alert("源端类型不能为空!");
            return false;
        }
        if (target_type == "") {
            alert("目标端类型不能为空!");
            return false;
        }
        if (src_mapping == "") {
            alert("源端映射类型不能为空!");
            return false;
        }

        if (target_mapping == "") {
            alert("目标端映射类型不能为空!");
            return false;
        }


        $.ajax({
            type: "post",
            url: "${basePath}/metaMapping/doEdit",
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
        $("#edit").hide();
        $("#mainContentInner").show();
        metaMappingTable.ajax.reload();
    }

</script>
