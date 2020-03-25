<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-src-type">源端类型</label>
                                <div class="col-sm-8">
                                    <select multiple=""
                                            class="form-add-src-type tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-src-type"
                                            style="width:100%;">
                                        <option grade="1" value="HBase" >HBase</option>
                                        <option grade="2" value="MySql" >MySql</option>
                                        <option grade="3" value="SqlServer" >SqlServer</option>
                                        <option grade="4" value="HDFS"  >HDFS</option>
                                        <option grade="5" value="ElasticSearch" >ElasticSearch</option>
                                        <option grade="6" value="PostgreSql" >PostgreSql</option>
                                        <option grade="7" value="Oracle" >Oracle</option>
                                        <option grade="8" value="HANA" >HANA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-dest-type">目标端类型</label>
                                <div class="col-sm-8">
                                    <select multiple=""
                                            class="form-add-dest-type tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-dest-type"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-src-name">源库名称</label>
                                <div class="col-sm-8">
                                    <select multiple="" name="job_src_media_source_name"
                                            class="form-add-src-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-src-name"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-dest-name">目标库名称</label>
                                <div class="col-sm-8">
                                    <select multiple="" name="job_target_media_source_name"
                                            class="form-add-dest-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-dest-name"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group" id="form-add-es-colum-join-div" style="display: none;">
                                <label class="col-sm-4 control-label no-padding-right">es cloumn join</label>
                                <div class="col-sm-8">
                                    <input id="es_column_join" type="text" name="es_column_join" style="width:100%;">
                                </div>
                            </div>
                        </div>


                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-media-name">介质名称</label>
                                <div class="col-sm-8">
                                    <select multiple="" name="form-add-media-name" class="form-add-media-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-media-name"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-timing-yn">是否定时任务</label>
                                <div class="col-sm-8">
                                    <select multiple=""
                                            class="form-add-timing-yn tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-timing-yn"
                                            style="width:100%;" onchange="changeTiming_yn(this.value)" name="timing_yn">
                                        <option value="true" >开启</option>
                                        <option value="false" >关闭</option>
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group" id="form-add-schedule-yn_div" style="display: none;">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-schedule-yn">创建schedule？</label>
                                <div class="col-sm-8">
                                    <select multiple=""
                                            class="form-add-schedule-yn tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-schedule-yn"
                                            style="width:100%;" name="create_schedule">
                                        <option value="true" >创建</option>
                                        <option value="false" >不创建</option>
                                    </select>
                                </div>
                            </div>

                        </div>


                        <div class="col-sm-12">
                        </div>

                        <div id="copyTableName"></div>
                    </div>
                </div>
            </div>

            <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
                 aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                ×
                            </button>
                            <h4 class="modal-title" id="myModalLabel">
                                修改字段
                            </h4>
                        </div>
                        <div class="modal-body">
                            <div class="tabbable">
                                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                                    <li class="active">
                                        <a data-toggle="tab" href="#baseId">基础配置</a>
                                    </li>
                                </ul>
                                <div class="tab-content" style="border: 0px">
                                    <!--基础配置-->
                                    <div id="baseId" class="tab-pane in active">
                                        <div class="form-group">

                                            <div class="col-sm-12">
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-columnMappingMode">映射模式</label>

                                                    <div class="col-sm-8">
                                                        <select name="columnMappingMode" class="col-sm-12"
                                                                id="form-add-columnMappingMode" style="width:100%;">
                                                            <option value="NONE">无</option>
                                                            <option value="INCLUDE">白名单</option>
                                                            <option value="EXCLUDE">黑名单</option>
                                                        </select>
                                                    </div>
                                                </div>

                                            </div>

                                        </div>
                                        <div class="form-group" id="duallistbox_demo1" style="display: none;">
                                            <div class="col-sm-12">
                                                <select multiple="multiple" size="10" name="duallistbox_demo1[]"
                                                        id="dualList">
                                                </select>

                                                <div class="hr hr-16 hr-dotted"></div>
                                            </div>
                                        </div>
                                        <div class="form-group" id="copyColumnName">
                                        </div>
                                    </div>

                                </div>

                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                            <button type="button" class="btn btn-primary" id="setColumn">提交</button>
                        </div>
                    </div>
                </div>
            </div>



        </form>
    </div>



    <div id="copyTableNameDiv" style="display: none;">
        <div class="col-sm-12">
            <div class="col-sm-4 form-group">
                <label class="col-sm-4 control-label no-padding-right" for="form-add-media-name">已选中的介质</label>
                <div class="col-sm-8">
                    <input readonly="readonly" type="text" name="sourceTableName" style="width:100%;">
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-4 control-label no-padding-right">表别名</label>
                <div class="col-sm-8">
                    <input type="text" name="targetTableName" style="width:100%;">
                </div>
            </div>
            <label class="col-sm-0 control-label no-padding-right">
                <a href="javascript:void(0)" id="modColumn">修改</a>
                <a href="javascript:void(0)" id="delTable">删除</a>
            </label>
            <input type="hidden" name="columnMappingModeHidden" value="NONE">
            <input type="hidden" name="sourceColumnHidden">
            <input type="hidden" name="targetColumnHidden">
        </div>
    </div>

    <div class="form-group" id="copyColumnNameDiv" style="display: none;">
        <div class="col-sm-12">
            <div class="col-sm-6 form-group">
                <label class="col-sm-4 control-label no-padding-right">字段名称</label>

                <div class="col-sm-7">
                    <input readonly="readonly" type="text" name="sourceColumnName">
                </div>
            </div>
            <div class="col-sm-6 form-group">
                <label class="col-sm-4 control-label no-padding-right">字段别名</label>

                <div class="col-sm-7">
                    <input type="text" name="targetColumnName">
                </div>
            </div>
        </div>
    </div>

    <div class="clearfix form-actions">
        <div class="col-md-offset-5 col-md-7">
            <button class="btn btn-info" type="button" onclick="doAdd();">
                <i class="ace-icon fa fa-check bigger-110"></i>
                保存
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

<script type="text/javascript">

    var src_RMDBS = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>"  +
            "<option grade=\"7\" value=\"Kudu\" >Kudu</option>" ;

    var src_HBase = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"3\" value=\"PostgreSql\" >PostgreSql</option>" ;

    var src_HDFS = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"HDFS\"  >HDFS</option>" +
            "<option grade=\"5\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"6\" value=\"PostgreSql\" >PostgreSql</option>" ;

    var src_ES = "<option grade=\"1\" value=\"HBase\" >HBase</option>" +
            "<option grade=\"2\" value=\"MySql\" >MySql</option>" +
            "<option grade=\"3\" value=\"SqlServer\" >SqlServer</option>" +
            "<option grade=\"4\" value=\"ElasticSearch\" >ElasticSearch</option>" +
            "<option grade=\"5\" value=\"PostgreSql\" >PostgreSql</option>" ;


    var obj = null;
    var dualList = $('select[name="duallistbox_demo1[]"]').bootstrapDualListbox({
        infoTextFiltered: '<span class="label label-purple label-lg">Filtered</span>',
        infoText: false
    });
    var container = dualList.bootstrapDualListbox('getContainer');



    function changeTiming_yn(val) {
        if(val == "true") {
            document.getElementById("form-add-schedule-yn_div").style.display = "";
        } else {
            document.getElementById("form-add-schedule-yn_div").style.display = "none";
        }

    }

    $('.form-add-src-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-dest-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-src-name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-dest-name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-media-name').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $('.form-add-timing-yn').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $('.form-add-schedule-yn').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $('#form-add-src-type').change(function(){
        var type_name = $('#form-add-src-type').val();
        if(type_name==null || type_name=="") {
            $('#form-add-src-name').innerHTML = "";
            $('#form-add-src-name').html('');
            $(".form-add-src-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});

            $('#form-add-dest-type').innerHTML = "";
            $('#form-add-dest-type').html('');
            $(".form-add-dest-type").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});

            $('#form-add-dest-name').innerHTML = "";
            $('#form-add-dest-name').html('');
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});

            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false,  width: '100%'});

            document.getElementById("copyTableName").innerHTML = "";
            $(".form-add-timing-yn").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            document.getElementById("form-add-schedule-yn_div").style.display = "none";
            return;
        }
        if(type_name == "ElasticSearch") {
            document.getElementById("form-add-dest-type").innerHTML = src_ES;
        }
        else if(type_name == "HBase") {
            document.getElementById("form-add-dest-type").innerHTML = src_HBase;
        }
        else if(type_name == "HDFS") {
            document.getElementById("form-add-dest-type").innerHTML = src_HDFS;
        }
        if(type_name == "MySql") {
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "SqlServer") {
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "PostgreSql") {
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "Oracle") {
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else if(type_name == "HANA") {
            document.getElementById("form-add-dest-type").innerHTML = src_RMDBS;
        }
        else {
            //
        }

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChange?name="+type_name,
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

    $('#form-add-dest-type').change(function(){
        var type_name = $('#form-add-dest-type').val();
        if(type_name==null || type_name=="") {
            $('#form-add-dest-name').innerHTML = "";
            $('#form-add-dest-name').html('');
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            document.getElementById("form-add-es-colum-join-div").style.display = "none";
            return;
        }
        if(type_name == "ElasticSearch") {
            document.getElementById("form-add-es-colum-join-div").style.display = "";
        }

        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChange?name="+type_name,
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

    $('#form-add-src-name').change(function(){
        var name = $('#form-add-src-name').val();
        if(name==null || name=="") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, width: '100%'});
            document.getElementById("copyTableName").innerHTML = "";
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
                    alert("无法获取元数据");
                }
            }
        });
    });



    $('#form-add-media-name').change(function(){
        var value = $("#form-add-media-name").val();
        $(".sourceTableName").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
        $(".form-add-media-name").val('').select2({allowClear: true, maximumSelectionLength: 1, width: '100%'});
        var count = $('#copyTableName').find('input[name=targetTableName]').length;
        for (var i = 0; i < count; i++) {
            var iv = $('#copyTableName').find('input[name=sourceTableName]').eq(i).val();
            if (iv == value) {
                return;
            }
        }

        if (value != null) {
            var e = $("#copyTableNameDiv>div").clone(true);
            e.find('input[name=sourceTableName]').val(value);
            if (value == '(.*)') {
                e.find('input[name=targetTableName]').val('');
                e.find('input[name=targetTableName]').attr('readonly', 'readonly');
            } else {
                e.find('input[name=targetTableName]').val(value);
            }
            $('#copyTableName').append(e);
        }
    });


    $('#delTable').click(function () {
        $(this).parent().parent().remove();
    });

    $('#modColumn').click(function () {
        obj = this;
        var columnMappingMode = $(obj).parent().parent().find('input[name=columnMappingModeHidden]').val();
        if (columnMappingMode == 'NONE') {
            $("#copyColumnName").hide();
            $("#duallistbox_demo1").hide();
        } else {
            $("#copyColumnName").show();
            $("#duallistbox_demo1").show();
        }
        if (columnMappingMode != null && columnMappingMode != '') {
            $('#form-add-columnMappingMode').val(columnMappingMode);
        } else {
            $('#form-add-columnMappingMode').val('NONE');
        }
        getColumnInfo(this);
        $('#myModal').modal('show');
    });

    function getColumnInfo(e) {
        var tableName = $(e).parent().parent().find('input[name=sourceTableName]').val();
        var id = $("#form-add-src-name").val();
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/getColumnName",
            async: false,
            dataType: "json",
            data: "&id=" + id + "&tableName=" + tableName,
            success: function (result) {
                $("#dualList").html("");
                $("#copyColumnName").html("");
                for (var i = 0; i < result.length; i++) {
                    dualList.append("<option value=" + "'" + result[i] + "'" + ">" + result[i] + "</option>");
                    dualList.bootstrapDualListbox('refresh', true);
                }
                //set data
                var scn = $(obj).parent().parent().find('input[name=sourceColumnHidden]').val();
                var tcn = $(obj).parent().parent().find('input[name=targetColumnHidden]').val();
                if (scn != null && scn != '') {
                    var sarray = scn.split('+');
                    var tarray = tcn.split('+');
                    for (var i = 0; i < sarray.length; i++) {
                        dualList.find('option[value="' + sarray[i] + '"]').attr('selected', 'selected');
                        dualList.bootstrapDualListbox('refresh', true);
                    }
                    for (var i = 0; i < tarray.length; i++) {
                        var e = $("#copyColumnNameDiv>div").clone(true);
                        e.find('input[name=sourceColumnName]').val(sarray[i]);
                        e.find('input[name=targetColumnName]').val(tarray[i]);
                        $('#copyColumnName').append(e);
                    }
                }

            }
        });
    }

    $('#form-add-columnMappingMode').change(function () {
        var val = $("#form-add-columnMappingMode").val();
        if (val == 'INCLUDE' || val == 'EXCLUDE') {
            $("#copyColumnName").show();
            $("#duallistbox_demo1").show();
        } else {
            $("#copyColumnName").html('');
            $("#duallistbox_demo1").hide();
        }
    });

    $('#setColumn').click(function () {

        var columnMappingMode = $('#form-add-columnMappingMode').val();
        $(obj).parent().parent().find('input[name=columnMappingModeHidden]').val(columnMappingMode);

        var sourceColumnName = $('input[name=sourceColumnName]');
        var scn = '';
        if (sourceColumnName != null && sourceColumnName.length > 1) {
            for (var i = 0; i < sourceColumnName.length - 1; i++) {
                if (i == sourceColumnName.length - 2) {
                    scn += sourceColumnName.eq(i).val();
                } else {
                    scn += sourceColumnName.eq(i).val() + '+';
                }
            }
        }
        $(obj).parent().parent().find('input[name=sourceColumnHidden]').val(scn);

        var targetColumnName = $('input[name=targetColumnName]');
        var tcn = '';
        if (targetColumnName != null && targetColumnName.length > 1) {
            for (var i = 0; i < targetColumnName.length - 1; i++) {
                if (i == targetColumnName.length - 2) {
                    tcn += targetColumnName.eq(i).val();
                } else {
                    tcn += targetColumnName.eq(i).val() + '+';
                }
            }
        }
        $(obj).parent().parent().find('input[name=targetColumnHidden]').val(tcn);
        $('#myModal').modal('hide');
    });

    $('#dualList').on('change', function () {
        var ops = $('[name="duallistbox_demo1[]"]').val();
        var obj = $("#copyColumnName>div").find('input[name=sourceColumnName]');
        if (ops != null && ops.length > 0) {
            for (var i = 0; i < ops.length; i++) {
                var flag = false;
                for (var q = 0; q < obj.length; q++) {
                    if (obj.eq(q).val() == ops[i]) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                } else {
                    var e = $("#copyColumnNameDiv>div").clone(true);
                    e.find('input[name=sourceColumnName]').val(ops[i]);
                    e.find('input[name=targetColumnName]').val(ops[i]);
                    $('#copyColumnName').append(e);
                }
            }
            var array = [];
            for (var q = 0; q < obj.length; q++) {
                var flag = false;
                for (var i = 0; i < ops.length; i++) {
                    if (obj.eq(q).val() == ops[i]) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                } else {
                    array.push(obj.eq(q));
                }
            }
            if (array != null && array.length > 0) {
                for (var i = 0; i < array.length; i++) {
                    array[i].parent().parent().parent().remove();
                }
            }

        } else {
            $('#copyColumnName').html('');
        }
    })


    function doAdd() {
        if (!validateForm()) {
            return;
        }

        var data_form = $("#add_form").serialize();
        //alert(data_form);
        //return;

        var src_meida_id = $("#form-add-src-name").val();
        var dest_media_id = $('#form-add-dest-name').val();
        var src_name = $('#copyTableName').find('input[name=sourceTableName]');
        var target_name = $('#copyTableName').find('input[name=targetTableName]');
        var timing_yn = $('#form-add-timing-yn').val();
        var schedule_yn = $('#form-add-schedule-yn').val();
        var es_column_join = $('#es_column_join').val();
        if(src_name==null || src_name.length==0) {
            return;
        }
        var src_names_string = "";
        var target_names_string = "";
        if(src_name.length == 1) {
            src_names_string = src_name[0].value;
            target_names_string = target_name[0].value;
        }
        else {
            for(i=0;i<src_name.length-1;i++) {
                if(target_name[i]==null || target_name[i]=="") {
                    alert("表别名不能为空");
                    return;
                }
                src_names_string += src_name[i].value;
                src_names_string += ",";
                target_names_string += target_name[i].value;
                target_names_string += ",";
            }
            src_names_string += src_name[src_name.length-1].value;
            target_names_string += target_name[target_name.length-1].value;
        }

        var data = "srcID="+src_meida_id+"&destID="+dest_media_id+"&srcName="+src_names_string+"&destName="+target_names_string+"&timing_yn="+timing_yn;
        data = data +"&schedule_yn="+schedule_yn+"&es_column_join="+es_column_join;
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/doFastAdd?",
            dataType: "json",
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("添加成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }


    function back2Main() {
        $("#fastAdd").hide();
        $("#mainContentInner").show();
        jobListTable.ajax.reload();
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
        var names = $('#copyTableName').find('input[name=sourceTableName]');
        if (names==null || names.length==0) {
            alert('必选选择一个介质');
            return false;
        }

        return true;
    }
</script>