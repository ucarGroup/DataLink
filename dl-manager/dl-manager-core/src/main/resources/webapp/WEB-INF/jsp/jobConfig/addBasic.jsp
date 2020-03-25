<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/jobConfig/jobInclude.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<!--
<link rel="stylesheet" href="${basePath}/assets/css/jsoneditor.css" />
<script src="${basePath}/assets/js/jsoneditor.js"></script>
-->
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">
                <div class="tabbable">
                    <div class="tab-content" style="border: 0px">
                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-src-type">源库类型</label>
                            <div class="col-sm-8">
                                <select multiple=""
                                        class="form-add-src-type col-sm-5"
                                        data-placeholder="Click to Choose..." id="form-add-src-type"
                                        style="width:350px;height:35px">
                                    <option grade="1" value="HBase" >HBase</option>
                                    <option grade="2" value="MySql" >MySql</option>
                                    <option grade="3" value="SqlServer" >SqlServer</option>
                                    <option grade="4" value="HDFS"  >HDFS</option>
                                    <option grade="5" value="ElasticSearch" >ElasticSearch</option>
                                    <option grade="5" value="PostgreSql" >PostgreSql</option>
                                    <option grade="6" value="SDDL" >SDDL</option>
                                    <option grade="7" value="Oracle" >Oracle</option>
                                    <option grade="8" value="HANA" >HANA</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-src-name">源库名称</label>
                            <div class="col-sm-8">
                                <select  multiple="" id="form-add-src-name" style="width:350px;height:35px" class="form-add-src-name col-sm-5"
                                        data-placeholder="Click to Choose..." name="job_src_media_source_name">
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-media-name">介质名称</label>
                            <div class="col-sm-8">
                                <select multiple="" id="form-add-media-name" style="width:350px;height:35px"
                                         data-placeholder="Click to Choose..." name="job_media_name" class="form-add-media-name col-xs-10 col-sm-8">
                                </select>
                            </div>
                        </div>


                        <div id="copyTableName">
                        </div>

                        <div id="copyTableNameDiv" style="display: none;">
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-media-name">已选中的介质</label>
                                <div class="col-sm-9">
                                    <input readonly="readonly" type="text" name="sourceTableName" class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" >
                                    <a href="javascript:void(0)" id="delTable">删除</a>
                                </div>
                            </div>
                        </div>


                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-dest-type">目标库类型</label>
                            <div class="col-sm-8">
                                ‍‍<select multiple="" id="form-add-dest-type" style="width:350px;height:35px" class="form-add-dest-type col-sm-5">
                                </select>
                            </div>
                        </div>


                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-dest-name">目标库名称</label>
                            <div class="col-sm-8">
                                <select multiple="" id="form-add-dest-name" style="width:350px;height:35px" class="form-add-dest-name col-sm-5"
                                        data-placeholder="Click to Choose..." name="job_target_media_source_name" >
                                </select>
                            </div>
                        </div>


                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" for="form-add-job_name">任务名称</label>
                            <div class="col-sm-9">
                                <input type="text" id="form-add-job_name" name="job_name"
                                       class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                            </div>
                        </div>

                        <div class="clearfix form-actions">
                            <div class="col-md-offset-5 col-md-7">
                                <button class="btn btn-info" type="button" onclick="add();">
                                    <i class="ace-icon fa fa-check bigger-110"></i>
                                    增加
                                </button>

                                &nbsp; &nbsp; &nbsp;
                                <button class="btn" type="reset" onclick="back2Main();">
                                    返回
                                    <i class="ace-icon fa fa-undo bigger-110"></i>
                                </button>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="col-sm-3 control-label no-padding-right" >job内容</label>
                            <div class="col-sm-9">
                                <div id="add_jsoneditor" style="height:50%"></div>
                            </div>
                        </div>

                    </div>
                </div>
            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>


<script type="text/javascript">

    var src_RMDBS = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>" +
            "<option grade=\"7\" value=\"Kudu\" >Kudu</option>" +
            "<option grade=\"8\" value=\"Oracle\" >Oracle</option>" ;

    var src_HBase = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"3\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"4\" value=\"ElasticSearch\" >ElasticSearch</option>"+
        "<option grade=\"7\" value=\"Kudu\" >Kudu</option>";

    var src_HDFS = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>" +
        "<option grade=\"7\" value=\"Kudu\" >Kudu</option>";

    var src_ES = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>" +
            "<option grade=\"7\" value=\"Kudu\" >Kudu</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>";

    var src_SDDL = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>" +
        "<option grade=\"7\" value=\"Kudu\" >Kudu</option>";

    var emtpy_json = JSON.parse("{}");

    $('.form-add-src-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.form-add-dest-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.form-add-src-name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.form-add-dest-name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.form-add-media-name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 32,
        multiple: true,
        width: '50%'
    });

    $('#form-add-src-type').change(function(){
        var type_name = $('#form-add-src-type').val();
        if(type_name==null || type_name=="") {
            $('#form-add-src-name').innerHTML = "";
            $('#form-add-src-name').html('');
            $(".form-add-src-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});

            $('#form-add-dest-type').innerHTML = "";
            $('#form-add-dest-type').html('');
            $(".form-add-dest-type").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});

            $('#form-add-dest-name').innerHTML = "";
            $('#form-add-dest-name').html('');
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});

            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            add_editor.set(emtpy_json);
            document.getElementById("form-add-job_name").value = "";

            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            return;
        }
        if(type_name == "ElasticSearch") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_ES;
        }
        else if(type_name == "HBase") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_HBase;
        }
        else if(type_name == "HDFS") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_HDFS;
        }
        if(type_name == "MySql") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "SqlServer") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "PostgreSql") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "SDDL") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_SDDL;
        }
        else if(type_name == "Kudu") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_Kudu;
        }
        else if(type_name == "Oracle") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "HANA") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 32, width: '50%'});
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else {
            //
        }

        //显示选择的高级内容
        if(type_name == "ElasticSearch") {
            document.getElementById("es_reader").style.display = "";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "HBase") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "HDFS") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        if(type_name == "MySql") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "SqlServer") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "PostgreSql") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "SDDL") {
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "";
            document.getElementById("oracle_reader").style.display = "none";
        }
        else if(type_name == "Oracle"){
            document.getElementById("es_reader").style.display = "none";
            document.getElementById("hbase_reader").style.display = "none";
            document.getElementById("hdfs_reader").style.display = "none";
            document.getElementById("mysql_reader").style.display = "none";
            document.getElementById("sqlserver_reader").style.display = "none";
            document.getElementById("postgresql_reader").style.display = "none";
            document.getElementById("sddl_reader").style.display = "none";
            document.getElementById("oracle_reader").style.display = "";
        }

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChangeInDataCenter?name="+type_name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "";
                    for(i=0;i<result.num.length;i++) {
                        var option = "<option value=" +"'"+ result.num[i] +"'" +">"+ result.val[i] +"</option>";
                        value += option;
                    }
                    document.getElementById("form-add-src-name").innerHTML = value;
                }
                else {
                    alert(result);
                }
            }
        });
    });


    $('#form-add-src-name').change(function(){
        var name = $('#form-add-src-name').val();
        if(name==null || name=="") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            add_editor.set(emtpy_json);
            document.getElementById("form-add-job_name").value = "";
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/namespaceContent?id="+name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "";
                    for(i=0;i<result.length;i++) {
                        var option = "<option value=" +"'"+ result[i] +"'" +">"+ result[i] +"</option>";
                        value += option;
                    }
                    document.getElementById("form-add-media-name").innerHTML = value;
                }
                else {
                    alert("无法获取元数据信息");
                }
            }
        });

    });

    $('#form-add-dest-type').change(function(){
        var type_name = $('#form-add-dest-type').val();
        if(type_name==null || type_name=="") {
            $('#form-add-dest-name').innerHTML = "";
            $('#form-add-dest-name').html('');
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});
            add_editor.set(emtpy_json);
            document.getElementById("form-add-job_name").value = "";

            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            return;
        }

        //显示选择的高级内容
        if(type_name == "ElasticSearch") {
            document.getElementById("es_writer").style.display = "";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "HBase") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "HDFS") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        if(type_name == "MySql") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "SqlServer") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "PostgreSql") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "";
            document.getElementById("kudu_writer").style.display = "none";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "kudu") {
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "";
            //document.getElementById("sddl_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "none";
        }
        else if(type_name == "Oracle"){
            document.getElementById("es_writer").style.display = "none";
            document.getElementById("hbase_writer").style.display = "none";
            document.getElementById("hdfs_writer").style.display = "none";
            document.getElementById("mysql_writer").style.display = "none";
            document.getElementById("sqlserver_writer").style.display = "none";
            document.getElementById("postgresql_writer").style.display = "none";
            document.getElementById("kudu_writer").style.display = "none";
            document.getElementById("oracle_writer").style.display = "";
        }

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChangeInDataCenter?name="+type_name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "";
                    for(i=0;i<result.num.length;i++) {
                        var option = "<option value=" +"'"+ result.num[i] +"'" +">"+ result.val[i] +"</option>";
                        value += option;
                    }
                    document.getElementById("form-add-dest-name").innerHTML = value;
                }
                else {
                    alert(result);
                }
            }
        });

    });

    $('#form-add-dest-name').change(function(){
        //reloadJson();
        parseJobContent();
    });

    $('#form-add-media-name').change(function(){
        var names = $("#form-add-media-name").val();
        if(names==null || names=="") {
            add_editor.set(emtpy_json);
            document.getElementById("form-add-job_name").value = "";
            return;
        }
        //reloadJson();
        parseJobContent();
    })


    function parseJobContent() {
        var srcID = $("#form-add-src-name").val();
        var destID = $('#form-add-dest-name').val();
        var names = $("#form-add-media-name").val();
        var mediaName = document.getElementById("form-add-media-name").value;
        if(srcID==null || srcID=="") {
            return;
        }
        if(destID==null || destID=="") {
            add_editor.set(emtpy_json);
            document.getElementById("form-add-job_name").value = "";
            return;
        }
        if(names==null || names=="") {
            return;
        }
        if(mediaName==null || mediaName=="") {
            return;
        }
        var timing_yn = $('#form-add-timing_yn').val();
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doParseJobContent?srcID="+srcID+"&destID="+destID+"&mediaName="+names,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if($('#form-add-timing_yn').val()=="true") {
                    document.getElementById("form-add-job_name").value = "CRON_" +mediaName + "_" + randomString(10);
                } else {
                    document.getElementById("form-add-job_name").value = mediaName + "_" + randomString(10);
                }
                add_editor.set(data);
                if(data!=null && JSON.stringify(data).indexOf("@NULL@")>-1) {
                    alert("有null字符串！");
                }
            }
        });

        var timing_yn = $('#form-add-timing_yn').val();
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doTimingDupCheck?srcID="+srcID+"&destID="+destID+"&mediaName="+names+"&timing_yn="+timing_yn,
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if(data!="SUCCESS") {
                    alert("相同源库，目标库，介质名称的定时任务有重复 -> "+data);
                }
            }
        });
    }


    function refresh() {
        $("#add").hide();
        $("#mainContentInner").show();
    }

</script>