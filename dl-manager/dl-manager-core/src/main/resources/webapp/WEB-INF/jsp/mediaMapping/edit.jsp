<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="page-content">
    <div class="row">
        <form id="update_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">任务名称</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${mediaMappingInfo.taskName}">
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">源库名称</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${mediaMappingInfo.srcMediaSourceName}">
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">目标库名称</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${mediaMappingInfo.targetMediaSourceName}">
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">目标Schema</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${mediaMappingInfo.targetMediaNamespace}">
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">表名称</label>

                                <div class="col-sm-8">
                                    <input style="width:100%;" type="text" readonly="readonly"
                                           value="${mediaMappingInfo.srcMediaName}" name="srcMediaName">
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">表别名</label>

                                <div class="col-sm-8">
                                    <c:if test="${mediaMappingInfo.srcMediaName=='(.*)'}">
                                        <input style="width:100%;" readonly="readonly" type="text"
                                               value="${mediaMappingInfo.targetMediaName}" name="targetMediaName">
                                    </c:if>
                                    <c:if test="${mediaMappingInfo.srcMediaName!='(.*)'}">
                                        <input style="width:100%;" type="text"
                                               value="${mediaMappingInfo.targetMediaName}" name="targetMediaName">
                                    </c:if>
                                </div>
                            </div>
                            <label class="col-sm-0 control-label no-padding-right">
                                <a href="javascript:void(0)" id="modColumn">修改</a>
                            </label>
                            <input type="hidden" name="columnMappingModeHidden" id="columnMappingModeHidden"
                                   value="${mediaMappingInfo.columnMappingMode}">
                            <input type="hidden" name="writePriorityHidden" id="writePriorityHidden"
                                   value="${mediaMappingInfo.writePriority}">
                            <input type="hidden" name="joinColumnHidden" id="joinColumnHidden"
                                   value="${mediaMappingInfo.joinColumn}">
                            <input type="hidden" name="validHidden" id="validHidden"
                                   value="${mediaMappingInfo.valid}">
                            <input type="hidden" name="esUsePrefixHidden" id="esUsePrefixHidden"
                                   value="${mediaMappingInfo.esUsePrefix}">
                            <input type="hidden" name="geoPositionConfHidden" id="geoPositionConfHidden"
                                   value='${mediaMappingInfo.geoPositionConf}'>
                            <input type="hidden" name="skipIdsHidden" id="skipIdsHidden"
                                   value='${mediaMappingInfo.skipIds}'>
                            <input type="hidden" name="parameterHidden" id="parameterHidden"
                                   value='${mediaMappingInfo.parameter}'>
                            <input type="hidden" name="interceptorIdHidden" id="interceptorIdHidden"
                                   value="${mediaMappingInfo.interceptorId}">
                            <input type="hidden" name="sourceColumnHidden" id="sourceColumnHidden"
                                   value="${sourceColumn}">
                            <input type="hidden" name="targetColumnHidden" id="targetColumnHidden"
                                   value="${targetColumn}">
                            <input type="hidden" name="mediaColumnMappingId" id="mediaColumnMappingId"
                                   value="${mediaMappingInfo.id}">
                            <input type="hidden" name="id" id="id" value="${mediaMappingInfo.id}">
                        </div>
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
                                                            <c:if test="${mediaMappingInfo.interceptorId==null}">
                                                                <option selected="selected" value=-1>无</option>
                                                                <c:forEach items="${interceptorList}" var="bean">
                                                                    <option value="${bean.id}">${bean.name} </option>
                                                                </c:forEach>
                                                            </c:if>
                                                            <c:if test="${mediaMappingInfo.interceptorId!=null}">
                                                                <option value=-1>无</option>
                                                                <c:forEach items="${interceptorList}" var="bean">
                                                                    <option value="${bean.id}" <c:if
                                                                            test="${bean.id == mediaMappingInfo.interceptorId}"> selected="selected"</c:if>>${bean.name }</option>
                                                                </c:forEach>
                                                            </c:if>
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
                                                               value="${mediaMappingInfo.writePriority}"
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
                                                               value="${mediaMappingInfo.joinColumn}"
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
                                        <div class="form-group" id="copyColumnName"></div>
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
                                                              class="col-xs-12"
                                                              rows="7">${mediaMappingInfo.geoPositionConf}</textarea>
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
                                                    <textarea name="parameter" id="form-add-parameter"
                                                              class="col-xs-12"
                                                              rows="7">${mediaMappingInfo.parameter}</textarea>
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
                                                              class="col-xs-12"
                                                              rows="7">${mediaMappingInfo.skipIds}</textarea>
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
            <button class="btn btn-info" type="button" onclick="doEdit();">
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
    });

    $('#modColumn').click(function () {
        obj = this;

        //set data
        var interceptorId = $('#interceptorIdHidden').val();
        if (interceptorId != null && interceptorId != '') {
            $('#form-add-interceptorId').val(interceptorId);
        }
        var valid = $('#validHidden').val();
        if (valid != null && valid != '') {
            $('#form-add-valid').val(valid);
        }
        var columnMappingMode = $('#columnMappingModeHidden').val();
        if (columnMappingMode != null && columnMappingMode != '') {
            $('#form-add-columnMappingMode').val(columnMappingMode);
        }
        var writePriority = $('#writePriorityHidden').val();
        if (writePriority != null && writePriority != '') {
            $('#form-add-writePriority').val(writePriority);
        }

        var joinColumn = $('#joinColumnHidden').val();
        if (joinColumn != null && joinColumn != '') {
            $('#form-add-joinColumn').val(joinColumn);
        }

        var esUsePrefix = $('#esUsePrefixHidden').val();
        if (esUsePrefix != null && esUsePrefix != '') {
            $('#form-add-esUsePrefix').val(esUsePrefix);
        }

        var geoPositionConf = $('#geoPositionConfHidden').val();
        if (geoPositionConf != null && geoPositionConf != '') {
            $('#form-add-geoPositionConf').val(geoPositionConf);
        }

        var skipIds = $('#skipIdsHidden').val();
        if (skipIds != null && skipIds != '') {
            $('#form-add-skipIds').val(skipIds);
        }

        var parameter = $('#parameterHidden').val();
        if (parameter != null && parameter != '') {
            $('#form-add-parameter').val(parameter);
        }

        getColumnInfo(this);
        var mode = $('#columnMappingModeHidden').val()
        if (mode == 'INCLUDE' || mode == 'EXCLUDE') {
            $('#duallistbox_demo1').show();
        } else {
            $('#duallistbox_demo1').hide();
        }

        $('#myModal').modal('show');
    });

    function getColumnInfo(e) {
        var tableName = '${mediaMappingInfo.srcMediaName}';
        var id = ${mediaMappingInfo.srcMediaSourceId};
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
                var scn = $('#sourceColumnHidden').val();
                var tcn = $('#targetColumnHidden').val();
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
        $('#interceptorIdHidden').val(interceptorId);

        var valid = $('#form-add-valid').val();
        $('#validHidden').val(valid);

        var columnMappingMode = $('#form-add-columnMappingMode').val();
        $('#columnMappingModeHidden').val(columnMappingMode);

        var writePriority = $('#form-add-writePriority').val();
        $('#writePriorityHidden').val(writePriority);

        var joinColumn = $('#form-add-joinColumn').val();
        $('#joinColumnHidden').val(joinColumn);

        var geoPositionConf = $('#form-add-geoPositionConf').val();
        $('#geoPositionConfHidden').val(geoPositionConf);

        var skipIds = $('#form-add-skipIds').val();
        $('#skipIdsHidden').val(skipIds);

        var parameter = $('#form-add-parameter').val();
        $('#parameterHidden').val(parameter);

        var esUsePrefix = $('#form-add-esUsePrefix').val();
        $('#esUsePrefixHidden').val(esUsePrefix);

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
        $('#sourceColumnHidden').val(scn);

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
        $('#targetColumnHidden').val(tcn);
        $('#myModal').modal('hide');
    });


    function doEdit() {
        if (!checkForSave()) {
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }

    function back2Main() {
        $("#edit").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }

    function checkForSave() {
        var skipIds = $.trim($("#skipIdsHidden").val());
        if (skipIds != null && skipIds != '' && !isSkipIds(skipIds)) {
            alert("请输入正确的要跳过的主键ID格式，例如：1,3,5 或者 [1-5],[10-20]");
            return false;
        }
        return true;
    }

    function isSkipIds(val) {
        var skipIdsReg1 = /^([0-9]+,)*[0-9]+$/;
        var skipIdsReg2 = /^(\[[0-9]+\-[0-9]+],)*(\[[0-9]+\-[0-9]+])$/;
        return (skipIdsReg1.test(val) || skipIdsReg2.test(val));
    }

</script>
