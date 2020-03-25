<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>

                    <li class="advance_id" >
                        <a data-toggle="tab" href="#advance_id">高级配置</a>
                    </li>


                    <!-- reader配置-->
                    <li id="es_reader" style="display:none" >
                        <a data-toggle="tab" href="#es_reader_id">ElasticSearch Reader</a>
                    </li>
                    <li id="hbase_reader" style="display:none" onclick="getHbaseRegionCount()">
                        <a data-toggle="tab" href="#hbase_reader_id">HBase Reader</a>
                    </li>
                    <li id="hdfs_reader" style="display:none" >
                        <a data-toggle="tab" href="#hdfs_reader_id">HDFS Reader</a>
                    </li>
                    <li id="mysql_reader" style="display:none" >
                        <a data-toggle="tab" href="#mysql_reader_id">MySql Reader</a>
                    </li>
                    <li id="sqlserver_reader" style="display:none" >
                        <a data-toggle="tab" href="#sqlserver_reader_id">SqlServer Reader</a>
                    </li>
                    <li id="postgresql_reader" style="display:none" >
                        <a data-toggle="tab" href="#postgresql_reader_id">PostgreSql Reader</a>
                    </li>
                    <li id="sddl_reader" style="display:none" >
                        <a data-toggle="tab" href="#sddl_reader_id">SDDL Reader</a>
                    </li>
                    <li id="oracle_reader" style="display:none" >
                        <a data-toggle="tab" href="#oracle_reader_id">Oracle Reader</a>
                    </li>

                    <!-- writer配置 -->
                    <li id="es_writer" style="display:none" >
                        <a data-toggle="tab" href="#es_writer_id">ElasticSearch Writer</a>
                    </li>
                    <li id="hbase_writer" style="display:none" >
                        <a data-toggle="tab" href="#hbase_writer_id">HBase Writer</a>
                    </li>
                    <li id="hdfs_writer" style="display:none" >
                        <a data-toggle="tab" href="#hdfs_writer_id">HDFS Writer</a>
                    </li>
                    <li id="mysql_writer" style="display:none" >
                        <a data-toggle="tab" href="#mysql_writer_id">MySql Writer</a>
                    </li>
                    <li id="sqlserver_writer" style="display:none" >
                        <a data-toggle="tab" href="#sqlserver_writer_id">SqlServer Writer</a>
                    </li>
                    <li id="postgresql_writer" style="display:none" >
                        <a data-toggle="tab" href="#postgresql_writer_id">PostgreSql Writer</a>
                    </li>
                    <li id="sddl_writer" style="display:none" >
                        <a data-toggle="tab" href="#sddl_writer_id">SDDL Writer</a>
                    </li>
                    <li id="kudu_writer" style="display:none" >
                        <a data-toggle="tab" href="#kudu_writer_id">Kudu Writer</a>
                    </li>
                    <li id="oracle_writer" style="display:none" >
                        <a data-toggle="tab" href="#oracle_writer_id">Oracle Writer</a>
                    </li>

                    <li>
                        <a data-toggle="tab" href="#timetaskId">定时任务配置</a>
                    </li>
                </ul>


                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <jsp:include page="addBasic.jsp"/>
                    </div>

                    <!--高级配置-->
                    <div id="advance_id" class="tab-pane">
                        <jsp:include page="addAdvance.jsp"/>
                    </div>


                    <!--Reader配置-->
                    <div id="es_reader_id" class="tab-pane">
                        <jsp:include page="advance/esReader.jsp"/>
                    </div>
                    <div id="hbase_reader_id" class="tab-pane">
                        <jsp:include page="advance/hbaseReader.jsp"/>
                    </div>
                    <div id="hdfs_reader_id" class="tab-pane">
                        <jsp:include page="advance/hdfsReader.jsp"/>
                    </div>
                    <div id="mysql_reader_id" class="tab-pane">
                        <jsp:include page="advance/mysqlReader.jsp"/>
                    </div>
                    <div id="sqlserver_reader_id" class="tab-pane">
                        <jsp:include page="advance/sqlserverReader.jsp"/>
                    </div>
                    <div id="postgresql_reader_id" class="tab-pane">
                        <jsp:include page="advance/postgresqlReader.jsp"/>
                    </div>
                    <div id="sddl_reader_id" class="tab-pane">
                        <jsp:include page="advance/sddlReader.jsp"/>
                    </div>

                    <div id="oracle_reader_id" class="tab-pane">
                        <jsp:include page="advance/oracleReader.jsp"/>
                    </div>


                    <!--Writer配置-->
                    <div id="es_writer_id" class="tab-pane">
                        <jsp:include page="advance/esWriter.jsp"/>
                    </div>
                    <div id="hbase_writer_id" class="tab-pane">
                        <jsp:include page="advance/hbaseWriter.jsp"/>
                    </div>
                    <div id="hdfs_writer_id" class="tab-pane">
                        <jsp:include page="advance/hdfsWriter.jsp"/>
                    </div>
                    <div id="mysql_writer_id" class="tab-pane">
                        <jsp:include page="advance/mysqlWriter.jsp"/>
                    </div>
                    <div id="sqlserver_writer_id" class="tab-pane">
                        <jsp:include page="advance/sqlserverWriter.jsp"/>
                    </div>
                    <div id="postgresql_writer_id" class="tab-pane">
                        <jsp:include page="advance/postgresqlWriter.jsp"/>
                    </div>
                    <div id="sddl_writer_id" class="tab-pane">
                        <jsp:include page="advance/sddlWriter.jsp"/>
                    </div>
                    <div id="kudu_writer_id" class="tab-pane">
                        <jsp:include page="advance/kuduWriter.jsp"/>
                    </div>
                    <div id="oracle_writer_id" class="tab-pane">
                        <jsp:include page="advance/oracleWriter.jsp"/>
                    </div>

                    <!--定时任务配置-->
                    <div id="timetaskId" class="tab-pane">
                        <jsp:include page="addTimeTask.jsp"/>
                    </div>

                    <!-- schedule配置
                    <div id="scheduleId" class="tab-pane">
                        <jsp:include page="addSchedule.jsp"/>
                    </div>
                    -->

                </div>
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
    </div>
    <!-- /.page-content -->
</div>

<script type="text/javascript">

    var add_container = document.getElementById('add_jsoneditor');
    var add_options = {modes: ['text', 'tree']};
    var add_editor = new JSONEditor(add_container, add_options);

    function back2Main() {
        $("#add").hide();
        $("#mainContentInner").show();
        jobListTable.ajax.reload();
    }

    function add() {
        if (!validateForm()) {
            return;
        }
        var srcType = $('#form-add-src-type').val();
        var destType = $('#form-add-dest-type').val();
        var srcID = $("#form-add-src-name").val();
        var destID = $('#form-add-dest-name').val();
        var names = $('#form-add-media-name').val();
        var names_string = "";
        if(names.length == 1) {
            names_string = names[0].value;
        }
        else {
            for(i=0;i<names.length-1;i++) {
                names_string += names[i].value;
                names_string += ",";
            }
            names_string += names[names.length-1].value;
        }

        names_string = names;
        var job_name = $("#form-add-job_name").val();
        var job_content = add_editor.getText();
        job_content = formatJson(job_content);
        if(job_content!=null && job_content.indexOf("@NULL@")>-1) {
            alert("映射配置中有包含null，无法保存配置，请检查映射关系!");
            return;
        }

        //一些特殊的扩展属性
        var hbase_split_count = $("#form-add-hbase_split_num").val();

        //定时任务相关的熟悉
        var timing_yn = $('#form-add-timing_yn').val();
        var timing_type = $('#form-add-timing_type_id').val();
        var timing_machine = $('#form-add-timing_machine_id').val();
        var timing_parameter = $('#form-add-timing_parameter').val();
        if(timing_yn=="true") {
            if(timing_parameter == "") {
                alert("定时器不填的话默认用 ：  -Xms2G -Xmx5G");
            }
        }
        var json = "srcType="+srcType+"&destType="+destType+"&job_src_media_source_name="+srcID+"&job_target_media_source_name="+destID+
                        "&job_media_name="+names_string+"&job_content="+job_content+"&job_name="+job_name +"&hbase_split_count="+hbase_split_count+
                "&timing_yn="+timing_yn+"&timing_transfer_type="+timing_type+"&timing_target_worker="+timing_machine+"&timing_parameter="+timing_parameter;

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doAdd",
            dataType: "json",
            data: json,
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



    function getHbaseRegionCount() {
        var srcID = $("#form-add-src-name").val();
        var names = $("#form-add-media-name").val();
        if(srcID==null || ""==srcID) {
            return;
        }
        if(null==names || ""==names) {
            return;
        }
        var content = document.getElementById("form-add-hbase_regions");
        if(content!=null && content>0) {
            return;
        }
        content.disabled = "disabled";
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/caclRegion?id="+srcID+"&tableName="+names,
            dataType: "json",
            data: "",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if("-1" == data) {
                    alert("获取不到region数量")
                } else {
                    content.value = data;
                }
            }
        });

    }


    function reloadJson() {
        //获取页面选择的源端的id(MediaSourceInfo的id)，目标端的id，介质名称
        var srcName = document.getElementById("form-add-src-name");
        var mediaName = document.getElementById("form-add-media-name");
        var destName = document.getElementById("form-add-dest-name");
        if(srcName==null || srcName=="" ) {
            return;
        }
        if(destName==null || destName=="") {
            return;
        }
        if(mediaName==null || mediaName=="") {
            return;
        }

        var destID = destName.options[destName.options.selectedIndex].value;
        var srcID = srcName.options[srcName.options.selectedIndex].value;
        var mediaNameValue = mediaName.options[mediaName.options.selectedIndex].value;
        //获取源端类型，目标端类型
        var srcType = document.getElementById("form-add-src-type");
        var srcTypeValue = srcType.options[srcType.options.selectedIndex].value;
        var destType = document.getElementById("form-add-dest-type");
        var destTypeValue = destType.options[destType.options.selectedIndex].value;

        //拼接 介质名称组成一个字符串
        var mediaNameValue = $("#form-add-media-name").val();


        //开始生成扩展属性的json
        var reader = {};
        var writer = {};
        var advance = {};
        var timing = {};
        var property = {};


        //处理高级属性
        var channel = document.getElementById("form-add-channel").value;
        var adaptModify = $('#form-add-adapt-modify').val();
        advance.channel = channel;
        advance.adaptModify = adaptModify;
        property.srcID = srcID;
        property.destID = destID;
        property.mediaName = mediaNameValue;

        //处理定时任务相关熟悉
        var timing_yn = $('#form-add-timing_yn').val();
        var timing_type = $('#form-add-timing_type_id').val();
        var timing_machine = $('#form-add-timing_machine_id').val();
        var timing_parameter = $('#form-add-timing_parameter').val();
        timing.isOpen = timing_yn;
        timing.type = timing_type;
        timing.workAddress = timing_machine;
        timing.parameter = timing_parameter;

        //处理源端，reader属性
        if(srcTypeValue == "ElasticSearch") {
            var esReaderIndexType = $('#form-add-es_reader_indextype').val();
            reader.esReaderIndexType = esReaderIndexType;
            var esReaderQuery = $('#form-add-es_reader_query').val();
            reader.esReaderQuery = esReaderQuery;
        }
        else if(srcTypeValue == "HBase") {
            var hbaseSpecifiedNum = $('#form-add-hbase_specified_num').val();
            reader.hbaseSpecifiedNum = hbaseSpecifiedNum;
        }
        else if(srcTypeValue == "HDFS") {
            var compress = $('#form-add-hdfs_compress').val();
            var path = document.getElementById("form-add-hdfs_path").value;
            var ignoreException = document.getElementById("form-add-hdfs_ignoreException").value;
            var hdfsUser = document.getElementById("form-add-hdfs_user").value;
            var specifiedPreDate = document.getElementById("form-add-hdfs_specifiedPreDate").value;
            var hdfsPaths = document.getElementById("form-add-hdfs_paths").value;
            reader.compress = compress;
            reader.path = path;
            reader.ignoreException = ignoreException;
            reader.hsdfUser = hdfsUser;
            reader.specifiedPreDate = specifiedPreDate;
            reader.hdfsPaths = hdfsPaths;
        }
        if(srcTypeValue == "MySql") {
            var where = document.getElementById("form-add-mysql_where").value;
            var querySql = document.getElementById("form-add-mysql_querysql").value;
            var jdbcUrl = document.getElementById("form-add-mysql_jdbcurl").value;
            reader.where = where;
            reader.querySql = querySql;
            reader.jdbcReaderUrl = jdbcUrl;
        }
        else if(srcTypeValue == "SqlServer") {
            var where = document.getElementById("form-add-sqlserver_where").value;
            var querySql = document.getElementById("form-add-sqlserver_querysql").value;
            var jdbcUrl = document.getElementById("form-add-sqlserver_jdbcurl").value;
            reader.where = where;
            reader.querySql = querySql;
            reader.jdbcReaderUrl = jdbcUrl;
        }
        else {
            //
        }


        //处理目标端，wirter属性
        if(destTypeValue == "ElasticSearch") {
            var joinColumn = document.getElementById("form-add-es_joinColumn").value;
            var esWriterIndexType = document.getElementById("form-add-es_writer_indextype").value;
            writer.joinColumn = joinColumn;
            writer.esWriterIndexType = esWriterIndexType;
            var esWriterPreDel = document.getElementById("form-add-es_writer_predel").value;
            writer.esWriterPreDel = esWriterPreDel;
        }
        else if(destTypeValue == "HBase") {
            var columnFamily = document.getElementById("form-add-hbase_columnFamily").value;
            writer.columnFamily = columnFamily;
        }
        else if(destTypeValue == "HDFS") {
            var hdfsPathType = document.getElementById("form-add-hdfs_path_type").value;
            writer.hdfsPathType = hdfsPathType;
            var hdfsPreDel = document.getElementById("form-add-hdfs_pre_del").value;
            writer.hdfsPreDel = hdfsPreDel;
        }

        if(destTypeValue == "MySql") {
            var preSql = document.getElementById("form-add-mysql_presql").value;
            var postSql = document.getElementById("form-add-mysql_postsql").value;
            writer.preSql = preSql;
            writer.postSql = postSql;
        }
        else if(destTypeValue == "SqlServer") {
            var preSql = document.getElementById("form-add-sqlserver_presql").value;
            var postSql = document.getElementById("form-add-sqlserver_postsql").value;
            var identityMode = document.getElementById("form-add-sqlserver_identity").value;
            writer.preSql = preSql;
            writer.postSql = postSql;
            writer.identityInsertMode = identityMode;
        } else if(destTypeValue == "Kudu") {
            var preSql = document.getElementById("form-add-kudu_presql").value;
            var postSql = document.getElementById("form-add-kudu_postsql").value;
            writer.preSql = preSql;
            writer.postSql = postSql;
        }
        else {
            //
        }

        property.advance = advance;
        property.reader = reader;
        property.writer = writer;
        property.timing = timing;
        var json = JSON.stringify(property);

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/realoadJson",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: json,
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                add_editor.set(data);
                if(data!=null && JSON.stringify(data).indexOf("@NULL@") >-1) {
                    alert("映射配置中有包含null，请检查映射关系!");
                }
                var job_name = document.getElementById("form-add-job_name");
                if(job_name.value==null || job_name.value=="") {
                    if(isOpen=="true") {
                        mediaNameValue = "CRON_" + mediaNameValue;
                    }
                    job_name.value = mediaNameValue + "_" + randomString(10);
                } else {
                    var index = job_name.value.indexOf("CRON_");
                    if(isOpen=="true") {
                        if (index == -1) {
                            job_name.value = "CRON_" + job_name.value;
                        }
                    } else {
                        if(index >= 0) {
                            job_name.value = job_name.value.substring(5);
                        }
                    }
                }
            }
        });
        return;
    }


    function validateForm() {
        if ($.trim($('#form-add-src-name').val()) == '') {
            alert('源库不能为空');
            return false;
        }
        if ($.trim($('#form-add-dest-name').val()) == '') {
            alert('目标库不能为空');
            return false;
        }
        if ($.trim($('#form-add-media-name').val()) == '') {
            alert('必选选择一个介质');
            return false;
        }
        if ($.trim($('#form-add-job_name').val()) == '') {
            alert('任务名称不能为空');
            return false;
        }
        return true;
    }

    function randomString(len) {
        len = len || 32;
        var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
        var maxPos = $chars.length;
        var pwd = '';
        for (i = 0; i < len; i++) {
            pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
        }
        return pwd;
    }

    function repeat(s, count) {
        return new Array(count + 1).join(s);
    }

    function formatJson(json) {
        var i           = 0,
                len          = 0,
                tab         = "    ",
                targetJson     = "",
                indentLevel = 0,
                inString    = false,
                currentChar = null;
        for (i = 0, len = json.length; i < len; i += 1) {
            currentChar = json.charAt(i);
            switch (currentChar) {
                case '{':
                case '[':
                    if (!inString) {
                        targetJson += currentChar + "\n" + repeat(tab, indentLevel + 1);
                        indentLevel += 1;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case '}':
                case ']':
                    if (!inString) {
                        indentLevel -= 1;
                        targetJson += "\n" + repeat(tab, indentLevel) + currentChar;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ',':
                    if (!inString) {
                        targetJson += ",\n" + repeat(tab, indentLevel);
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ':':
                    if (!inString) {
                        targetJson += ": ";
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ' ':
                case "\n":
                case "\t":
                    if (inString) {
                        targetJson += currentChar;
                    }
                    break;
                case '"':
                    if (i > 0 && json.charAt(i - 1) !== '\\') {
                        inString = !inString;
                    }
                    targetJson += currentChar;
                    break;
                default:
                    targetJson += currentChar;
                    break;
            }
        }
        return targetJson;
    }



</script>
