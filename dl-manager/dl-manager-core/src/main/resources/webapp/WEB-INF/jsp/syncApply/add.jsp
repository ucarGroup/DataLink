<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="mysqlTaskRestart" class="modal">
</div>
<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <div class="tab-content" style="border: 0px">
                    <div id="basicId" class="tab-pane in active">

                        <div class="col-sm-1"></div>
                        <div class="col-sm-10">
                            （注：hdfs相关的同步，申请入口已转移到大数据平台，请前往dspider进行配置！）
                        </div>
                        <div class="col-sm-12">
                            <div class="form-group col-sm-4">
                                <label class="col-sm-4 control-label no-padding-right">同步类型</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="applyType" name="applyType"
                                            style="width:100%">
                                        <option value="Full" selected="selected">全量</option>
                                        <option value="Increment">增量</option>
                                    </select>
                                </div>
                            </div>
                            <div id="isInitialDataDiv" style="display: none;">
                                <div class="form-group col-sm-4">
                                    <label class="col-sm-4 control-label no-padding-right">是否需要全量</label>

                                    <div class="col-sm-8">
                                        <select class="width-100 chosen-select" id="isInitialData" name="isInitialData"
                                                style="width:100%">
                                            <option value="false" selected="selected">否</option>
                                            <option value="true">是</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div id="srcMediaSourceTypeList" style="display: none;">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right"
                                           for="form-add-src-type">源端类型</label>

                                    <div class="col-sm-8">
                                        <select multiple="" name="srcMediaSourceType"
                                                class="form-add-src-type tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-src-type"
                                                style="width:100%;">
                                            <c:forEach items="${srcMediaSourceTypeList}" var="bean">
                                                <option value="${bean}">${bean} </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div id="srcMediaSourceTypeForIncrement" style="display: none;">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right"
                                           for="form-add-src-type-increment">源端类型</label>

                                    <div class="col-sm-8">
                                        <select multiple="" name="srcMediaSourceType"
                                                class="form-add-src-type-increment tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-src-type-increment"
                                                style="width:100%;">
                                            <option value="MYSQL">MYSQL</option>
                                            <option value="HBASE">HBASE</option>
                                            <option value="SDDL">SDDL</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-dest-type">目标端类型</label>

                                <div class="col-sm-8">
                                    <select multiple=""
                                            class="form-add-dest-type tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-dest-type"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                            <div id="hiddenAttrDiv_es_isprefixColumn" style="display: none;">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">字段表前缀</label>
                                    <div class="col-sm-8">
                                        <select name="esIsPrefixColumn" class="col-sm-12" >
                                            <option value="false">否</option>
                                            <option value="true">是</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-src-name">源库名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="srcMediaSourceId"
                                            class="form-add-src-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-src-name"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-dest-name">目标库名称</label>
                                <div class="col-sm-8">
                                    <select multiple="" name="targetMediaSourceId"
                                            class="form-add-dest-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-dest-name"
                                            style="width:100%;">
                                    </select>
                                </div>
                            </div>

                        </div>


                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-media-name">源表名称</label>

                                <div class="col-sm-8">
                                    <select multiple="" name="form-add-media-name"
                                            class="form-add-media-name tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-media-name"
                                            style="width:100%;">
                                    </select>
                                    （支持选择多张表、配置表别名、列黑白名单）
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-4 control-label no-padding-right"
                                       for="form-add-approveUserId">审批人</label>

                                <div class="col-sm-8">
                                    <select name="approveUserId" class="approveUserId tag-input-style"
                                            data-placeholder="Click to Choose..." id="form-add-approveUserId"
                                            style="width:100%;" multiple>
                                        <c:forEach items="${approveUserIdList}" var="bean">
                                            <option value="${bean.id}">${bean.userName}
                                            <c:if test="${bean.userType == 0}">
                                               - ${bean.ucarEmail}@ucarinc.com
                                            </c:if>
                                             <c:if test="${bean.userType == 1}">
                                               - ${bean.ucarEmail}@luckincoffee.com
                                             </c:if>

                                            </option>
                                        </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
                <div id="copyTableName" class="col-sm-12"></div>
                <div class="col-sm-12">
                    <div class="col-sm-8 form-group">
                        <label class="col-sm-2 control-label no-padding-right"
                               for="form-add-applyRemark">备注</label>

                        <div class="col-sm-10">
                                    <textarea type="text" name="applyRemark" class="col-sm-12" id="form-add-applyRemark"
                                              style="margin: 0px;height: 106px;width: 80%;"></textarea>
                        </div>
                        <div class="col-sm-2"></div>
                        <div class="col-sm-10">
                            提示：<br>
                            1.带有全量的同步申请，全量Job会自动生成，可自行修改、启动执行、查看运行历史等<br>
                            （所在位置：全量任务-Job配置管理-根据申请ID筛选Job）<br>
                            2.目标端为ES的：<br>
                            （1）“表别名”格式为index.type<br>
                            （2）“聚合列名称”默认为空，即为主键id，单表同步时不用填<br>
                            （3）若需要多表聚合到ES，则子表的聚合列名称要填子表关联主表的列，主表的聚合列名称可默认为空<br>
                            （4）“字段表前缀”默认关闭，若需要多表聚合到ES，则要开启“字段表前缀”，会在ES的每个字段的前面增加 “表名称|” <br>
                            3.源端为HBase时，如果是按月分表，则增量和全量需要分开申请，增量同步选择后缀为“_${yyyyMM}”通配符的表，全量同步按需求选择特定月份的表<br>
                            4.若此处没有您需要的的同步类型，或者需要配置定时任务，或者其他任何疑问，请联系数据同步答疑QQ:2676875315
                        </div>
                        </div>
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
                <label class="col-sm-4 control-label no-padding-right" for="form-add-media-name">表名称</label>
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

    <div id="hiddenAttrDiv_joinColumn" style="display: none;">
        <div class="col-sm-4 form-group">
            <label class="col-sm-4 control-label no-padding-right">聚合列名称</label>
            <div class="col-sm-8">
                <input type="text" name="joinColumn" style="width:100%;">
            </div>
        </div>
    </div>

    <div id="hiddenAttrDiv_whereCondition" style="display: none;">
        <div class="col-sm-4 form-group">
            <label class="col-sm-4 control-label no-padding-right">源端where条件</label>
            <div class="col-sm-8">
                <input type="text" name="whereCondition" style="width:100%;" maxlength="9999">
            </div>
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
                提交
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

    $('.form-add-src-type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-src-type-increment').css('min-width', '100%').select2({
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

    $('.approveUserId').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $('.form-add-media-name').css('min-width', '100%').select2({
        allowClear: false,
        width: '100%'
    });

    $("#srcMediaSourceTypeList").show();
    $('#applyType').change(function () {
        var applyType = $("#applyType").val();
        if (applyType == "Increment") {
            $("#isInitialDataDiv").show();
            $("#srcMediaSourceTypeList").hide();
            $("#srcMediaSourceTypeForIncrement").show();
        } else {
            $("#isInitialDataDiv").hide();
            $("#srcMediaSourceTypeForIncrement").hide();
            $("#srcMediaSourceTypeList").show();
        }
    });

    $('#form-add-src-type').change(function () {
        var srcMediaSourceType = $('#form-add-src-type').val();
        if (srcMediaSourceType == null) {
            $('#form-add-media-name').html('');
            $("#copyTableName").html('');
            $(".form-add-src-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/getMediaSourcesAndTargetTypes",
            async: true,
            dataType: "json",
            data: "&mediaSourceType=" + srcMediaSourceType,
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-add-src-name').html('');
                    $('#form-add-dest-name').html('');
                    $("#form-add-dest-type").html('');
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#form-add-src-name").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                    }

                    if (result.targetTypeList != null && result.targetTypeList.length > 0) {
                        for (var i = 0; i < result.targetTypeList.length; i++) {
                            $("#form-add-dest-type").append("<option value=" + "'" + result.targetTypeList[i] + "'" + ">" + result.targetTypeList[i] + "</option>");
                        }
                    }
                }
            }
        });
    });

    $('#form-add-src-type-increment').change(function () {
        var srcMediaSourceType = $('#form-add-src-type-increment').val();
        if (srcMediaSourceType == null) {
            $('#form-add-media-name').html('');
            $("#copyTableName").html('');
            $(".form-add-src-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            $(".form-add-media-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/getMediaSourcesAndTargetTypes",
            async: true,
            dataType: "json",
            data: "&mediaSourceType=" + srcMediaSourceType,
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-add-src-name').html('');
                    $('#form-add-dest-name').html('');
                    $("#form-add-dest-type").html('');
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#form-add-src-name").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                    }

                    if (result.targetTypeList != null && result.targetTypeList.length > 0) {
                        for (var i = 0; i < result.targetTypeList.length; i++) {
                            $("#form-add-dest-type").append("<option value=" + "'" + result.targetTypeList[i] + "'" + ">" + result.targetTypeList[i] + "</option>");
                        }
                    }
                }
            }
        });
    });

    $('#form-add-dest-type').change(function () {
        var targetMediaSourceType = $('#form-add-dest-type').val();
        if (targetMediaSourceType == null || targetMediaSourceType == "") {
            $('#form-add-dest-name').html('');
            $(".form-add-dest-name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
            //document.getElementById("form-add-es-colum-join-div").style.display = "none";
            return;
        }
        //if(targetMediaSourceType == "ELASTICSEARCH") {
        //    document.getElementById("form-add-es-colum-join-di").style.display = "";
        //}
        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/getMediaSourcesAndTargetTypes",
            async: true,
            dataType: "json",
            data: "&mediaSourceType=" + targetMediaSourceType,
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-add-dest-name').html('');
                    //$('#form-add-sourceTableName').html('');
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#form-add-dest-name").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                    }
                }
                else {
                    alert(result);
                }
            }
        });
    });

    $('#form-add-src-name').change(function () {
        var name = $('#form-add-src-name').val();
        if (name == null || name == "") {
            $('#form-add-media-name').innerHTML = "";
            $('#form-add-media-name').html('');
            $(".form-add-media-name").val('').select2({allowClear: false, width: '100%'});
            document.getElementById("copyTableName").innerHTML = "";
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/namespaceContent?id=" + name,
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
    });


    $('#form-add-media-name').change(function () {

        var destName = $('#form-add-dest-name').val();
        if(!destName){
            alert("请先选择目标库");
            return;
        }

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

            var targetMediaSourceType = $('#form-add-dest-type').val();
            var srcMediaSourceType = $('#form-add-src-type').val();
            var srcMediaSourceTypeIncrement = $('#form-add-src-type-increment').val();
            if(targetMediaSourceType == "ELASTICSEARCH") {
                var es = $("#hiddenAttrDiv_joinColumn>div").clone(true);
                es.find('input[name=joinColumn]').val('');
                e.append(es);
//                var es_2 = $("#hiddenAttrDiv_es_isprefixColumn>div").clone(true);
//                es_2.find('input[name=joinColumn]').val('');
//                e.append(es_2);
                document.getElementById("hiddenAttrDiv_es_isprefixColumn").style.display = "";
            }
            if(srcMediaSourceTypeIncrement=="MYSQL" || srcMediaSourceType=="MYSQL" || srcMediaSourceType=="SQLSERVER" || srcMediaSourceType=="ORACLE" || srcMediaSourceType=="HANA") {
                var condition= $("#hiddenAttrDiv_whereCondition>div").clone(true);
                condition.find('input[name=whereCondition]').val('');
                e.append(condition);
            }
            $('#copyTableName').append(e);
        }
    });

    $('#delTable').click(function () {
        $(this).parent().parent().remove();
    });


    $('#modColumn').click(function () {
        obj = this;

        //set data
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
        //alert($("#add_form").serialize());

        $.ajax({
            type: "post",
            url: "${basePath}/sync/apply/doAdd",
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
        $("#add").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
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
        if (names == null || names.length == 0) {
            alert('源表不能为空!');
            return false;
        }

        if ($.trim($('#form-add-approveUserId').val()) == '') {
            alert('审批人不能为空');
            return false;
        }

        return true;
    }

</script>
