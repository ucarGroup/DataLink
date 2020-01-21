<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container ace-save-state" id="main-container">
</div>
<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">

                        <div class="col-sm-12">
                            <div class="form-group col-sm-4">
                                <label class="col-sm-4 control-label no-padding-right">同步类型</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="applyType" name="applyType"
                                            style="width:100%" disabled="disabled">
                                        <c:if test="${syncApplyView.applyType=='Full'}">
                                            <option selected="selected" value="Full">全量</option>
                                        </c:if>
                                        <c:if test="${syncApplyView.applyType=='Increment'}">
                                            <option selected="selected" value="Increment">增量</option>
                                        </c:if>
                                    </select>
                                </div>
                            </div>
                            <div id="isInitialDataDiv" style="display: none;">
                                <div class="form-group col-sm-4">
                                    <label class="col-sm-4 control-label no-padding-right">是否需要全量</label>

                                    <div class="col-sm-8">
                                        <select class="width-100 chosen-select" id="isInitialData" name="isInitialData"
                                                style="width:100%" readonly="readonly">
                                            <c:if test="${syncApplyView.isInitialData=='true'}">
                                                <option selected="selected" value="true">是</option>
                                            </c:if>
                                            <c:if test="${syncApplyView.isInitialData=='false'}">
                                                <option selected="selected" value="false">否</option>
                                            </c:if>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">源端类型</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${syncApplyView.srcMediaSourceType}">
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">目标端类型</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${syncApplyView.targetMediaSourceType}">
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">源库名称</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${syncApplyView.srcMediaSourceName}" id="form-add-src-name">
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right">目标库名称</label>

                                <div class="col-sm-8">
                                    <input type="text" style="width:100%;" readonly="readonly"
                                           value="${syncApplyView.targetMediaSourceName}" id="form-add-dest-name">
                                </div>
                            </div>
                        </div>


                        <div class="col-sm-12">
                            <c:if test="${approve == false}">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right" for="form-add-media-name">源表名称</label>

                                    <div class="col-sm-8">
                                        <select multiple="" name="form-add-media-name"
                                                class="form-add-media-name tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-media-name"
                                                style="width:100%;">
                                        </select>
                                    </div>
                                </div>
                            </c:if>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-approve-id">审批人</label>

                                <div class="col-sm-8">
                                    <select name="approveUserId" class="approveUserId tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-approve-id"
                                            style="width:100%;" multiple disabled="disabled">
                                        <c:forEach items="${approveUserIdList}" var="bean">
                                            <option value="${bean.id}">${bean.userName} - ${bean.ucarEmail}@ucarinc.com </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <input type="hidden" name="id" id="id" value="${syncApplyView.id}">
                        <input type="hidden" name="applyStatus" value="${syncApplyView.applyStatus}">
                        <input type="hidden" name="applyType" value="${syncApplyView.applyType}">
                        <input type="hidden" name="applyUserId" value="${syncApplyView.applyUserId}">
                        <input type="hidden" name="needNotify" value="${syncApplyView.needNotify}">
                        <input type="hidden" name="srcMediaSourceId" id="srcMediaSourceId"
                               value="${syncApplyView.srcMediaSourceId}">
                        <input type="hidden" name="targetMediaSourceId" id="targetMediaSourceId"
                               value="${syncApplyView.targetMediaSourceId}">
                        <input type="hidden" name="sourceTableNameHidden" id="sourceTableNameHidden"
                               value="${sourceTableName}">
                        <input type="hidden" name="targetTableNameHidden" id="targetTableNameHidden"
                               value="${targetTableName}">
                        <input type="hidden" name="columnMappingModeHidden" id="columnMappingModeHidden"
                               value="${columnMappingMode}">
                        <input type="hidden" name="sourceColumnHidden" id="sourceColumnHidden" value="${sourceColumn}">
                        <input type="hidden" name="targetColumnHidden" id="targetColumnHidden" value="${targetColumn}">

                        <div id="copyTableName"></div>
                        <div class="col-sm-12">
                            <div class="col-sm-8 form-group">
                                <label class="col-sm-2 control-label no-padding-right"
                                       for="form-add-applyRemark">备注</label>

                                <div class="col-sm-10">
                                    <textarea type="text" name="applyRemark" class="col-sm-12" id="form-add-applyRemark"
                                              <c:if test="${approve == true}">readonly="readonly" </c:if>
                                              style="margin: 0px;height: 106px;width: 80%;">${syncApplyView.applyRemark}</textarea>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
            <c:if test="${approve == true && detail == false}">
                <div class="col-sm-12">
                    <div class="col-sm-8 form-group">
                        <label class="col-sm-2 control-label no-padding-right" for="form-add-approveRemark">审批备注</label>

                        <div class="col-sm-10">
                                    <textarea type="text" id="approveRemark" name="approveRemark" class="col-sm-12"
                                              id="form-add-approveRemark"
                                              style="margin: 0px;height: 106px;width: 80%;">${syncApplyView.approveRemark}</textarea>
                        </div>
                    </div>
                </div>
            </c:if>
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
                                                <input type="hidden" name="tableIndex" id="tableIndex">

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
                <label class="col-sm-4 control-label no-padding-right" for="form-add-media-name">表名称</label>

                <div class="col-sm-8">
                    <input readonly="readonly" type="text" name="sourceTableName" style="width:100%;">
                </div>
            </div>

            <div class="col-sm-4 form-group">
                <label class="col-sm-4 control-label no-padding-right">表别名</label>

                <div class="col-sm-8">
                    <input type="text" name="targetTableName" style="width:100%;"
                           <c:if test="${approve == true}">readonly="readonly" </c:if>>
                </div>
            </div>
            <c:if test="${approve == false || detail == true}">
                <label class="col-sm-0 control-label no-padding-right">
                    <input type="hidden" name="index" id="index">
                    <a href="javascript:void(0)" id="modColumn" onclick="modColumn(this)">修改</a>
                    <a href="javascript:void(0)" id="delTable" onclick="delTable(this)">删除</a>
                </label>
            </c:if>
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

    <c:if test="${approve == false && detail == false}">
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doEdit();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    修改
                </button>

                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>
    </c:if>

    <c:if test="${approve == true && detail == false}">
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="doApprove();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    通过
                </button>

                &nbsp; &nbsp; &nbsp;
                <button class="btn btn-grey" type="reset" onclick="doReject();">
                    拒绝
                    <i class="ace-icon fa fa-close bigger-110"></i>
                </button>

                &nbsp; &nbsp; &nbsp;
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>
    </c:if>

<c:if test="${detail == true}">
    <div class="clearfix form-actions">
        <div class="col-md-offset-5 col-md-7">

            <button class="btn" type="reset" onclick="back2Main();">
                返回
                <i class="ace-icon fa fa-undo bigger-110"></i>
            </button>

        </div>
    </div>
</c:if>

</div>

<!-- /.page-content -->

<script type="text/javascript">

    var obj = null;
    var dualList = $('select[name="duallistbox_demo1[]"]').bootstrapDualListbox({
        infoTextFiltered: '<span class="label label-purple label-lg">Filtered</span>',
        infoText: false
    });
    var container = dualList.bootstrapDualListbox('getContainer');

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

    var pe = '${syncApplyView.approveUserId}'.split(",");
    $(".approveUserId").val(pe).select2({allowClear: false, width: '100%'});

    var applyType = $("#applyType").val();
    if (applyType == "Increment") {
        $("#isInitialDataDiv").show();
    } else {
        $("#isInitialDataDiv").hide();
    }

    var srcMsId = $('#srcMediaSourceId').val();
    if (srcMsId == null || srcMsId == "") {
        $('#form-add-media-name').innerHTML = "";
        $('#form-add-media-name').html('');
        $(".form-add-media-name").val('').select2({allowClear: false, width: '100%'});
        document.getElementById("copyTableName").innerHTML = "";
    } else {
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/namespaceContent?id=" + srcMsId,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "";
                    for (i = 0; i < result.length; i++) {
                        var option = "<option value=" + "'" + result[i] + "'" + ">" + result[i] + "</option>";
                        value += option;
                    }
                    document.getElementById("form-add-media-name").innerHTML = value;
                }
                else {
                    alert("无法获取元数据");
                }
            }
        });
    }

    $('#form-add-media-name').change(function () {
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
            //改变hidden的值
            var srcTable = $("#sourceTableNameHidden").val();
            var srcTableArr = srcTable.split(',');
            srcTableArr.splice(count, 1, value);
            $("#sourceTableNameHidden").val(srcTableArr);

            var tarTable = $("#targetTableNameHidden").val();
            var tarTableArr = tarTable.split(',');

            if (value == '(.*)') {
                e.find('input[name=targetTableName]').val('');
                e.find('input[name=targetTableName]').attr('readonly', 'readonly');

                tarTableArr.splice(count, 1, '');
                $("#targetTableNameHidden").val(tarTableArr);
            } else {
                e.find('input[name=targetTableName]').val(value);

                tarTableArr.splice(count, 1, value);
                $("#targetTableNameHidden").val(tarTableArr);
            }

            var mappingMode = $("#columnMappingModeHidden").val();
            var mode = mappingMode.split(",");
            mode.splice(i, 1, 'NONE');
            $("#columnMappingModeHidden").val(mode);

            var scn = $("#sourceColumnHidden").val();
            var tcn = $("#targetColumnHidden").val();
            var srcList = scn.split(',');
            srcList.splice(i, 1, '');
            $("#sourceColumnHidden").val(srcList);
            var tarList = tcn.split(',');
            tarList.splice(i, 1, '');
            $("#targetColumnHidden").val(tarList);

            e.find('input[name=index]').val(count);
            $('#copyTableName').append(e);
        }
    });

    function delTable(o) {
        //改变hidden的值
        var count = $('#copyTableName').find('input[name=targetTableName]').length;
        for (var i = 0; i < count; i++) {
            var srcTableName = $(o).parent().parent().find('input[name=sourceTableName]').val();
            if (srcTableName == $('#copyTableName').find('input[name=sourceTableName]').eq(i).val()) {
                var sourceTableName = $("#sourceTableNameHidden").val();
                var src = sourceTableName.split(",");
                src.splice(i, 1);
                $("#sourceTableNameHidden").val(src);
                var targetTableName = $("#targetTableNameHidden").val();
                var tar = targetTableName.split(",");
                tar.splice(i, 1);
                $("#targetTableNameHidden").val(tar);
                var mappingMode = $("#columnMappingModeHidden").val();
                var mode = mappingMode.split(",");
                mode.splice(i, 1);
                $("#columnMappingModeHidden").val(mode);
                var scn = $("#sourceColumnHidden").val();
                var tcn = $("#targetColumnHidden").val();
                var srcList = scn.split(',');
                srcList.splice(i, 1);
                $("#sourceColumnHidden").val(srcList);
                var tarList = tcn.split(',');
                tarList.splice(i, 1);
                $("#targetColumnHidden").val(tarList);
            }
        }

        $(o).parent().parent().remove();
    }

    var sourceTableName = $("#sourceTableNameHidden").val();
    var targetTableName = $("#targetTableNameHidden").val();
    var src = sourceTableName.split(",");
    var tar = targetTableName.split(",");
    for (var i = 0; i < src.length; i++) {
        var srcTableName = src[i];
        var tarTableName = tar[i];
        if (srcTableName != null) {
            var e = $("#copyTableNameDiv>div").clone(true);
            e.find('input[name=sourceTableName]').val(srcTableName);
            if (srcTableName == '(.*)') {
                e.find('input[name=targetTableName]').val('');
                e.find('input[name=targetTableName]').attr('readonly', 'readonly');
            } else {
                e.find('input[name=targetTableName]').val(tarTableName);
            }
            e.find('input[name=index]').val(i);
            $('#copyTableName').append(e);
        }
    }

    function modColumn(o) {
        obj = o;
        //根据当前表的数量重置index的值
        var count = $('#copyTableName').find('input[name=targetTableName]').length;
        for (var i = 0; i < count; i++) {
            var srcTableName = $(o).parent().parent().find('input[name=sourceTableName]').val();
            if (srcTableName == $('#copyTableName').find('input[name=sourceTableName]').eq(i).val()) {
                $(o).parent().find('input[name=index]').val(i);
            }
        }
        //取index的值
        //方法一：传this（当前节点），找到其父节点中的index
        var index = $(o).parent().find('input[name=index]').val();
        //方法二：传e（按钮事件），找到按钮父节点中的index
//        var index = $(e.target).parent().find('input[name=index]').val();
        //方法三：传this（当前节点），取前一个节点即index
//        var index = $(o).prev().val();
        var mappingMode = $("#columnMappingModeHidden").val();
        var mode = mappingMode.split(",");
        var columnMappingMode = mode[index];
        if (mappingMode == '' || columnMappingMode == 'NONE' || columnMappingMode == '') {
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

        var tableName = $(o).parent().parent().find('input[name=sourceTableName]').val();
        var id = $('#srcMediaSourceId').val();
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
                dualList.parent().find('input[name=tableIndex]').val(index);
                var scn = $("#sourceColumnHidden").val();
                var tcn = $("#targetColumnHidden").val();
                if (scn != null && scn != '') {////scn = "violation_name+status,service_type_id+city_id"
                    var srcList = scn.split(',');
                    var srccol = srcList[index];
                    if (srccol != null && srccol != '') {
                        var sarray = srccol.split('+');
                        for (var i = 0; i < sarray.length; i++) {
                            dualList.find('option[value="' + sarray[i] + '"]').attr('selected', 'selected');
                            dualList.bootstrapDualListbox('refresh', true);
                        }
                    }
                    var tarList = tcn.split(',');
                    var tarcol = tarList[index];
                    if (tarcol != null && tarcol != '') {
                        var tarray = tarcol.split('+');
                        for (var i = 0; i < tarray.length; i++) {
                            var e = $("#copyColumnNameDiv>div").clone(true);
                            e.find('input[name=sourceColumnName]').val(sarray[i]);
                            e.find('input[name=targetColumnName]').val(tarray[i]);
                            $('#copyColumnName').append(e);
                        }
                    }
                }

            }
        });

        $('#myModal').modal('show');
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
        var index = $('#tableIndex').val();
        //改变hidden的值
        var columnMappingMode = $('#form-add-columnMappingMode').val();
        var modes = $("#columnMappingModeHidden").val();
        var modeArr = modes.split(',');
        modeArr.splice(index, 1, columnMappingMode);
        $("#columnMappingModeHidden").val(modeArr);

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
        var src = $("#sourceColumnHidden").val();
        var srcArr = src.split(',');
        srcArr.splice(index, 1, scn);
        $("#sourceColumnHidden").val(srcArr);

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
        var tar = $("#targetColumnHidden").val();
        var tarArr = tar.split(',');
        tarArr.splice(index, 1, tcn);
        $("#targetColumnHidden").val(tarArr);

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

    function doEdit() {
        if (!validateForm()) {
            return;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doEdit",
            dataType: "json",
            data: $("#add_form").serialize(),
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

    function doApprove() {
        var applyId = $("#id").val();
        var status = "APPROVED";
        var approveRemark = $("#approveRemark").val();
        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doApproveOrReject",
            dataType: "json",
            data: "&applyId=" + applyId + "&status=" + status + "&approveRemark=" + approveRemark,
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("审批成功！");
                    back2Main();
                } else {
                    alert(data);
                }
            }
        });
    }

    function doReject() {
        var applyId = $("#id").val();
        var status = "REJECTED";
        var approveRemark = $("#approveRemark").val();
        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doApproveOrReject",
            dataType: "json",
            data: "&applyId=" + applyId + "&status=" + status + "&approveRemark=" + approveRemark,
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("审批成功！");
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
        msgAlarmListMyTable.draw(false);
    }

    function validateForm() {
        var names = $('#copyTableName').find('input[name=sourceTableName]');
        if (names == null || names.length == 0) {
            alert('源表不能为空!');
            return false;
        }

        return true;
    }

</script>
