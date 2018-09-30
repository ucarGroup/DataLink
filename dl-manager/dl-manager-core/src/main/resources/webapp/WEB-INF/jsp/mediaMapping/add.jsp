<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="mysqlTaskRestart" class="modal">
</div>
<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-taskId">任务名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="taskId" class="taskId tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-taskId"
                                            style="width:100%;">
                                        <c:forEach items="${taskList}" var="bean">
                                            <option value="${bean.id}">${bean.taskName} </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-srcMediaSourceId">源库名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="srcMediaSourceId"
                                            class="srcMediaSourceId tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-srcMediaSourceId"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-targetMediaNamespaceId">目标库名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="targetMediaNamespaceId"
                                            class="targetMediaNamespaceId tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-targetMediaNamespaceId"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">目标Schema</label>

                                <div class="col-sm-8">
                                    <input type="text" name="targetMediaNamespace" id="form-add-targetMediaNamespace"
                                           style="width:100%;">
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right" for="form-add-sourceTableName">源表名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="sourceTableName" class="sourceTableName tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-sourceTableName"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>
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
                                    <li>
                                        <a data-toggle="tab" href="#extendId">扩展配置</a>
                                    </li>
                                </ul>
                                <div class="tab-content" style="border: 0px">
                                    <!--基础配置-->
                                    <div id="baseId" class="tab-pane in active">
                                        <div class="form-group">
                                            <div class="col-sm-12">
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-interceptorId">拦截器名称</label>

                                                    <div class="col-sm-8">
                                                        <select name="interceptorId" class="col-sm-12"
                                                                id="form-add-interceptorId"
                                                                style="width:100%;">
                                                            <option selected="selected" value=-1>无</option>
                                                            <c:forEach items="${interceptorList}" var="bean">
                                                                <option value="${bean.id}">${bean.name} </option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-valid">是否有效</label>

                                                    <div class="col-sm-8">
                                                        <select name="valid" class="col-sm-12" id="form-add-valid"
                                                                style="width:100%;">
                                                            <option value="true">是</option>
                                                            <option value="false">否</option>
                                                        </select>
                                                    </div>
                                                </div>
                                            </div>
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
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-writePriority">优先级</label>

                                                    <div class="col-sm-8">
                                                        <input type="text" name="writePriority" id="form-add-writePriority"
                                                               value="5"
                                                               style="width:100%;">
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-sm-12">
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-joinColumn">聚合列名称</label>

                                                    <div class="col-sm-8">
                                                        <input type="text" name="joinColumn" id="form-add-joinColumn"
                                                               value="5"
                                                               style="width:100%;">
                                                    </div>
                                                </div>
                                                <div class="col-sm-6 form-group">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="form-add-esUsePrefix">EsUsePrefix</label>

                                                    <div class="col-sm-8">
                                                        <select name="esUsePrefix" class="col-sm-12"
                                                                id="form-add-esUsePrefix"
                                                                style="width:100%;">
                                                            <option value="true">是</option>
                                                            <option value="false">否</option>
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

                                    <!--扩展配置-->
                                    <div id="extendId" class="tab-pane">

                                        <div id="accordion" class="accordion-style1 panel-group">

                                            <div class="panel panel-default">
                                                <div class="panel-heading">
                                                    <h4 class="panel-title">
                                                        <a class="accordion-toggle collapsed" data-toggle="collapse"
                                                           data-parent="#accordion" href="#collapseSix">
                                                            <i class="ace-icon fa fa-angle-right bigger-110"
                                                               data-icon-hide="ace-icon fa fa-angle-down"
                                                               data-icon-show="ace-icon fa fa-angle-right"></i>
                                                            &nbsp;[地理位置合并配置](Es专用)
                                                        </a>
                                                    </h4>
                                                </div>

                                                <div class="panel-collapse collapse" id="collapseSix">
                                                    <div class="panel-body">
                                                    <textarea name="geoPositionConf" id="form-add-geoPositionConf"
                                                              class="col-xs-12" rows="7"/>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="panel panel-default">
                                                <div class="panel-heading">
                                                    <h4 class="panel-title">
                                                        <a class="accordion-toggle collapsed" data-toggle="collapse"
                                                           data-parent="#accordion" href="#collapseSeven">
                                                            <i class="ace-icon fa fa-angle-right bigger-110"
                                                               data-icon-hide="ace-icon fa fa-angle-down"
                                                               data-icon-show="ace-icon fa fa-angle-right"></i>
                                                            &nbsp;[Parameter配置]
                                                        </a>
                                                    </h4>
                                                </div>

                                                <div class="panel-collapse collapse" id="collapseSeven">
                                                    <div class="panel-body">
                                                    <textarea name="parameter" id="form-add-parameter" class="col-xs-12"
                                                              rows="7"/>
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="panel panel-default">
                                                <div class="panel-heading">
                                                    <h4 class="panel-title">
                                                        <a class="accordion-toggle collapsed" data-toggle="collapse"
                                                           data-parent="#accordion" href="#collapseEight">
                                                            <i class="ace-icon fa fa-angle-right bigger-110"
                                                               data-icon-hide="ace-icon fa fa-angle-down"
                                                               data-icon-show="ace-icon fa fa-angle-right"></i>
                                                            &nbsp;[要跳过的主键ID]
                                                        </a>
                                                    </h4>
                                                </div>

                                                <div class="panel-collapse collapse" id="collapseEight">
                                                    <div class="panel-body">
                                                    <textarea name="skipIds" id="form-add-skipIds"
                                                              class="col-xs-12" rows="7"/>
                                                    </div>
                                                </div>
                                            </div>
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
                <label class="col-sm-4 control-label no-padding-right" for="form-add-sourceTableName">表名称</label>

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
            <input type="hidden" name="writePriorityHidden" value="5">
            <input type="hidden" name="validHidden" value="true">
            <input type="hidden" name="esUsePrefixHidden" value="true">
            <input type="hidden" name="geoPositionConfHidden">
            <input type="hidden" name="skipIdsHidden">
            <input type="hidden" name="parameterHidden">
            <input type="hidden" name="joinColumnHidden">
            <input type="hidden" name="interceptorIdHidden">
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
    var obj = null;
    var dualList = $('select[name="duallistbox_demo1[]"]').bootstrapDualListbox({
        infoTextFiltered: '<span class="label label-purple label-lg">Filtered</span>',
        infoText: false
    });
    var container = dualList.bootstrapDualListbox('getContainer');
    $('.taskId').css('min-width', '100%').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
    $('.srcMediaSourceId').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $('.targetMediaNamespaceId').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $('.sourceTableName').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $("#mysqlTaskRestart").on('hide.bs.modal', function () {
        back2Main();
    });

    $('#form-add-taskId').change(function () {
        var taskId = $('#form-add-taskId').val();
        if (taskId == null) {
            $('#form-add-sourceTableName').html('');
            $("#copyTableName").html('');
            $('input[name=targetMediaNamespace]').val('');
            $(".srcMediaSourceId").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".targetMediaNamespaceId").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".sourceTableName").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/getSourceDataBase",
            async: true,
            dataType: "json",
            data: "&taskId=" + taskId,
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-add-srcMediaSourceId').html('');
                    $('#form-add-targetMediaNamespaceId').html('');
                    //$('#form-add-sourceTableName').html('');
                    $("#form-add-srcMediaSourceId").append("<option value=" + "'" + result.source.id + "'" + ">" + result.source.name + "</option>");
                    $(".srcMediaSourceId").val(result.source.id).select2({
                        allowClear: false,
                        maximumSelectionLength: 1,
                        width: '100%'
                    });

                    if (result.target != null && result.target.length > 0) {
                        for (var i = 0; i < result.target.length; i++) {
                            $("#form-add-targetMediaNamespaceId").append("<option value=" + "'" + result.target[i].id + "'" + ">" + result.target[i].name + "</option>");
                        }
                        /*for(var i=0; i<result.tableName.length; i++) {
                         $("#form-add-sourceTableName").append("<option value="+"'"+result.tableName[i]+"'"+">"+result.tableName[i]+"</option>");
                         }*/
                    }
                }
            }
        });
    });

    $('#form-add-targetMediaNamespaceId').change(function () {
        var taskId = $('#form-add-taskId').val();
        var targetMediaNamespaceId = $('#form-add-targetMediaNamespaceId').val();
        if (targetMediaNamespaceId == null) {
            $("#copyTableName").html('');
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/getTableName",
            async: true,
            dataType: "json",
            data: "&taskId=" + taskId + "&targetMediaNamespaceId=" + targetMediaNamespaceId,
            success: function (result) {
                if (result != null && result != '') {
                    $('input[name=targetMediaNamespace]').val('');
                    $('input[name=targetMediaNamespace]').val(result.targetNamespace);
                    $('#form-add-sourceTableName').html('');
                    if (result.tableNameList != null && result.tableNameList.length > 0) {
                        for (var i = 0; i < result.tableNameList.length; i++) {
                            $("#form-add-sourceTableName").append("<option value=" + "'" + result.tableNameList[i] + "'" + ">" + result.tableNameList[i] + "</option>");
                        }
                    }
                }
            }
        });
    });

    $('#form-add-sourceTableName').change(function () {
        var value = $("#form-add-sourceTableName").val();
        $(".sourceTableName").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
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
            if (value == '(.*)' || ("" + value).indexOf("{yyyy}") >= 0 || ("" + value).indexOf("{yyyyMM}") >= 0) {
                e.find('input[name=targetTableName]').val('');
                if(value == '(.*)'){
                    e.find('input[name=targetTableName]').attr('readonly', 'readonly');
                }
            } else {
                e.find('input[name=targetTableName]').val(value);
            }
            $('#copyTableName').append(e);
        }
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

    $('#delTable').click(function () {
        $(this).parent().parent().remove();
    });

    $('#modColumn').click(function () {
        obj = this;

        //set data
        var interceptorId = $(obj).parent().parent().find('input[name=interceptorIdHidden]').val();
        if (interceptorId != null && interceptorId != '') {
            $('#form-add-interceptorId').val(interceptorId);
        } else {
            $('#form-add-interceptorId').val(-1);
        }

        var valid = $(obj).parent().parent().find('input[name=validHidden]').val();
        if (valid != null && valid != '') {
            $('#form-add-valid').val(valid);
        } else {
            $('#form-add-valid').val(1);
        }

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

        var writePriority = $(obj).parent().parent().find('input[name=writePriorityHidden]').val();
        if (writePriority != null && writePriority != '') {
            $('#form-add-writePriority').val(writePriority);
        } else {
            $('#form-add-writePriority').val(5);
        }

        var joinColumn = $(obj).parent().parent().find('input[name=joinColumnHidden]').val();
        if (joinColumn != null && joinColumn != '') {
            $('#form-add-joinColumn').val(joinColumn);
        } else {
            $('#form-add-joinColumn').val('');
        }

        var esUsePrefix = $(obj).parent().parent().find('input[name=esUsePrefixHidden]').val();
        if (esUsePrefix != null && esUsePrefix != '') {
            $('#form-add-esUsePrefix').val(esUsePrefix);
        } else {
            $('#form-add-esUsePrefix').val(true);
        }

        var geoPositionConf = $(obj).parent().parent().find('input[name=geoPositionConfHidden]').val();
        if (geoPositionConf != null && geoPositionConf != '') {
            $('#form-add-geoPositionConf').val(geoPositionConf);
        } else {
            $('#form-add-geoPositionConf').val('');
        }

        var skipIds = $(obj).parent().parent().find('input[name=skipIdsHidden]').val();
        if (skipIds != null && skipIds != '') {
            $('#form-add-skipIds').val(skipIds);
        } else {
            $('#form-add-skipIds').val('');
        }

        var parameter = $(obj).parent().parent().find('input[name=parameterHidden]').val();
        if (parameter != null && parameter != '') {
            $('#form-add-parameter').val(parameter);
        } else {
            $('#form-add-parameter').val('');
        }

        getColumnInfo(this);
        $('#myModal').modal('show');
    });

    function getColumnInfo(e) {
        var tableName = $(e).parent().parent().find('input[name=sourceTableName]').val();
        var id = $("#form-add-srcMediaSourceId").val();
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
                    var sarray = scn.split(',');
                    var tarray = tcn.split(',');
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
        var interceptorId = $('#form-add-interceptorId').val();
        $(obj).parent().parent().find('input[name=interceptorIdHidden]').val(interceptorId);

        var valid = $('#form-add-valid').val();
        $(obj).parent().parent().find('input[name=validHidden]').val(valid);

        var columnMappingMode = $('#form-add-columnMappingMode').val();
        $(obj).parent().parent().find('input[name=columnMappingModeHidden]').val(columnMappingMode);

        var writePriority = $('#form-add-writePriority').val();
        $(obj).parent().parent().find('input[name=writePriorityHidden]').val(writePriority);

        var joinColumn = $('#form-add-joinColumn').val();
        $(obj).parent().parent().find('input[name=joinColumnHidden]').val(joinColumn);

        var esUsePrefix = $('#form-add-esUsePrefix').val();
        $(obj).parent().parent().find('input[name=esUsePrefixHidden]').val(esUsePrefix);

        var geoPositionConf = $('#form-add-geoPositionConf').val();
        $(obj).parent().parent().find('input[name=geoPositionConfHidden]').val(geoPositionConf);

        var skipIds = $('#form-add-skipIds').val();
        $(obj).parent().parent().find('input[name=skipIdsHidden]').val(skipIds);

        var parameter = $('#form-add-parameter').val();
        $(obj).parent().parent().find('input[name=parameterHidden]').val(parameter);

        var sourceColumnName = $('input[name=sourceColumnName]');
        var scn = '';
        if (sourceColumnName != null && sourceColumnName.length > 1) {
            for (var i = 0; i < sourceColumnName.length - 1; i++) {
                if (i == sourceColumnName.length - 2) {
                    scn += sourceColumnName.eq(i).val();
                } else {
                    scn += sourceColumnName.eq(i).val() + ',';
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
                    tcn += targetColumnName.eq(i).val() + ',';
                }
            }
        }
        $(obj).parent().parent().find('input[name=targetColumnHidden]').val(tcn);
        $('#myModal').modal('hide');
    });

    function doAdd() {
        var taskId = $('#form-add-taskId').val();

        if (!validateForm()) {
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/doAdd",
            dataType: "json",
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    if (confirm("添加成功!\n是否需要进行位点重置操作?")) {
                        toRestartMysqlTask(taskId);
                    } else {
                        back2Main();
                    }
                } else {
                    alert(data);
                }
            }
        });
    }


    function back2Main() {
        $("#add").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }

    function validateForm() {
        if ($.trim($('#form-add-taskId').val()) == '') {
            alert('任务名称不能为空');
            return false;
        }
        if ($.trim($('#form-add-srcMediaSourceId').val()) == '') {
            alert('源库不能为空');
            return false;
        }

        if ($.trim($('#form-add-targetMediaNamespaceId').val()) == '') {
            alert('目标库不能为空');
            return false;
        }

        var skipIds = $.trim($('input[name=skipIdsHidden]').val());
        if (skipIds != null && skipIds != '' && !isSkipIds(skipIds)) {
            alert("请输入正确的要跳过的主键ID格式，例如：1,3,5 或者 [1-5],[10-20]");
            return false;
        }
        /*if ($.trim($('#form-add-targetMediaNamespace').val()) == '') {
         alert('目标Schema不能为空');
         return false;
         }*/

        var sourceTableName = $('#copyTableName').find('input[name=targetTableName]').length;
        if (sourceTableName == 0) {
            alert("源表不能为空!");
            return;
        }

        /*for(var i=0; i<$('#copyTableName').find('input[name=targetTableName]').length; i++) {
         var e = $('#copyTableName').find('input[name=targetTableName]').eq(i).val();
         if($.trim(e) == '') {
         alert('表别名不能为空!');
         return false;
         }
         }*/
        return true;
    }

    function isSkipIds(val) {
        var skipIdsReg1 = /^([0-9]+,)*[0-9]+$/;
        var skipIdsReg2 = /^(\[[0-9]+\-[0-9]+],)*(\[[0-9]+\-[0-9]+])$/;
        return (skipIdsReg1.test(val) || skipIdsReg2.test(val));
    }

    function toRestartMysqlTask(id) {
        var restartDiv = $("#mysqlTaskRestart");
        restartDiv.empty();
        restartDiv.load("${basePath}/mysqlTask/toRestartMysqlTask?id=" + id + "&random=" + Math.random());
        restartDiv.modal('show');
    }
</script>
