<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner ace-save-state">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="tabbable">
                    <div class="tab-content" style="border: 0px">
                        <div id="basicId" class="tab-pane in active">

                            <div class="col-sm-1"></div>

                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">策略名称</label>
                                    <div class="col-sm-8">
                                        <input  type="text" name="name" style="width:100%;" id="form-add-name">
                                    </div>
                                </div>
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">报警策略组</label>
                                    <div class="col-sm-8">
                                        <select multiple="" name="priorityId" class="form-add-priorityId tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-priorityId"
                                                style="width: 200px;">
                                            <c:forEach items="${taskPriorityInfos}" var="item">
                                                <option value="${item.id}">${item.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">报警类型</label>
                                    <div class="col-sm-8">
                                        <select multiple="" name="monitorType" class="form-add-monitorType tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-monitorType"
                                                style="width: 200px;">
                                            <c:forEach items="${monitorTypeList}" var="item">
                                                <option value="${item.key}">${item.desc}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">时间范围</label>
                                    <div class="col-sm-8">
                                        <input  type="text" name="timeRange" style="width:100%;" placeholder="00:00-23:59" id="form-add-timeRange">
                                    </div>
                                </div>

                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">延迟阈值(秒)</label>
                                    <div class="col-sm-8">
                                        <input type="text" name="threshold" style="width:100%;" value="5" id="form-add-threshold">
                                    </div>
                                </div>

                                <label class="col-sm-0 control-label no-padding-right">
                                    <a href="javascript:void(0)" onclick="buildReadDataSource()">新增</a>
                                </label>
                            </div>

                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">报警间隔(秒)</label>
                                    <div class="col-sm-8">
                                        <input  type="text" name="intervalTime" style="width:100%;" id="form-add-intervalTime">
                                    </div>
                                </div>

                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">电话报警</label>
                                    <div class="col-sm-8">
                                        <select multiple="" name="isPhone" class="form-add-isPhone tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-isPhone"
                                                style="width: 200px;">
                                            <option value="1">是</option>
                                            <option value="0" selected>否</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">钉钉报警</label>
                                    <div class="col-sm-8">
                                        <select multiple="" name="isDingD" class="form-add-isDingD tag-input-style"
                                                data-placeholder="Click to Choose..."
                                                style="width: 200px;" id="form-add-isDingD">
                                            <option value="1">是</option>
                                            <option value="0" selected>否</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-4 control-label no-padding-right">短信报警</label>
                                    <div class="col-sm-8">
                                        <select multiple="" name="isSMS" class="form-add-isSMS tag-input-style"
                                                data-placeholder="Click to Choose..." id="form-add-isSMS"
                                                style="width: 200px;">
                                            <option value="1">是</option>
                                            <option value="0" selected>否</option>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-sm-12">
                                <div class="col-sm-8 form-group">
                                    <label class="col-sm-2 control-label no-padding-right"
                                           for="form-add-other">其他配置</label>

                                    <div class="col-sm-10">
                                    <textarea type="text" name="other" class="col-sm-12" id="form-add-other"
                                              style="margin: 0px;height: 106px;width: 80%;"></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div id="copyBaseSourceDivId"></div>

                    </div>
                </div>
            </form>
        </div>

        <div id="copyReadSourceBase" style="display: none;">
            <div >

                <div class="col-sm-12">
                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">时间范围</label>
                        <div class="col-sm-8">
                            <input  type="text" name="timeRange" style="width:100%;" placeholder="00:00-23:59">
                        </div>
                    </div>

                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">延迟阈值(秒)</label>
                        <div class="col-sm-8">
                            <input type="text" name="threshold" style="width:100%;" value="5">
                        </div>
                    </div>
                    <label class="col-sm-0 control-label no-padding-right">
                        <a href="javascript:void(0)" onclick="deleteProperties(this)">删除</a>
                    </label>
                </div>

                <div class="col-sm-12">
                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">报警间隔(秒)</label>
                        <div class="col-sm-8">
                            <input  type="text" name="intervalTime" style="width:100%;">
                        </div>
                    </div>

                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">电话报警</label>
                        <div class="col-sm-8">
                            <select multiple="" name="isPhone" class="isPhone tag-input-style"
                                    data-placeholder="Click to Choose..." id="isPhone"
                                    style="width: 200px;">
                                <option value="1">是</option>
                                <option value="0" selected>否</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="col-sm-12">
                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">钉钉报警</label>
                        <div class="col-sm-8">
                            <select multiple="" name="isDingD" class="isDingD tag-input-style"
                                    data-placeholder="Click to Choose..." id="isDingD"
                                    style="width: 200px;">
                                <option value="1">是</option>
                                <option value="0" selected>否</option>
                            </select>
                        </div>
                    </div>

                    <div class="col-sm-4 form-group">
                        <label class="col-sm-4 control-label no-padding-right">短信报警</label>
                        <div class="col-sm-8">
                            <select multiple="" name="isSMS" class="isSMS tag-input-style"
                                    data-placeholder="Click to Choose..." id="isSMS"
                                    style="width: 200px;">
                                <option value="1">是</option>
                                <option value="0" selected>否</option>
                            </select>
                        </div>
                    </div>

                    <div class="col-sm-12">
                        <div class="col-sm-8 form-group">
                            <label class="col-sm-2 control-label no-padding-right"
                                   for="form-add-other">其他配置</label>

                            <div class="col-sm-10">
                                    <textarea type="text" name="other" class="col-sm-12" id="other"
                                              style="margin: 0px;height: 106px;width: 80%;"></textarea>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
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


    $('.form-add-monitorType').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    $('.form-add-isDingD').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $('.form-add-isPhone').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $('.form-add-isSMS').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });
    $('.form-add-priorityId').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '100%'
    });

    function buildReadDataSource() {
        var e = $("#copyReadSourceBase>div").clone(true);
        var id =  (new Date()).getTime();
        var isPhoneId = "isPhone"+id;
        var isDingDId = "isDingD"+id;
        var isSMSId = "isSMS"+id;

        $(e).find(".isPhone").attr("id",isPhoneId);
        $(e).find(".isPhone").attr("class",isPhoneId+" tag-input-style");

        $(e).find(".isDingD").attr("id",isDingDId);
        $(e).find(".isDingD").attr("class",isDingDId+" tag-input-style");


        $(e).find(".isSMS").attr("id",isSMSId);
        $(e).find(".isSMS").attr("class",isSMSId+" tag-input-style");
        $('#copyBaseSourceDivId').append(e);
        $('.'+isPhoneId).css('min-width', '100%').select2({
            allowClear: false,
            maximumSelectionLength: 1,
            width: '100%'
        });
        $('.'+isDingDId).css('min-width', '100%').select2({
            allowClear: false,
            maximumSelectionLength: 1,
            width: '100%'
        });
        $('.'+isSMSId).css('min-width', '100%').select2({
            allowClear: false,
            maximumSelectionLength: 1,
            width: '100%'
        });
    }

    function deleteProperties(e) {
        $(e).parent().parent().parent().remove();
    }

    function doAdd() {
        if (!validateForm()) {
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/alarmStrategy/doAdd",
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
        msgAlarmListMyTable.ajax.reload();
    }

    function validateForm() {
        if ($.trim($('#form-add-name').val()) == '') {
            alert('策略名称不能为空');
            return false;
        }
        if ($.trim($('#form-add-priorityId').val()) == '') {
            alert('报警级别不能为空');
            return false;
        }
        if ($.trim($('#form-add-monitorType').val()) == '') {
            alert('报警类型不能为空');
            return false;
        }
        if ($.trim($('#form-add-timeRange').val()) == '') {
            alert('时间范围不能为空');
            return false;
        }
        if ($.trim($('#form-add-threshold').val()) == '') {
            alert('延迟阈值不能为空');
            return false;
        }
        if ($.trim($('#form-add-intervalTime').val()) == '') {
            alert('报警间隔不能为空');
            return false;
        }
        if ($.trim($('#form-add-isPhone').val()) == '') {
            alert('电话报警不能为空');
            return false;
        }

        if ($.trim($('#form-add-isDingD').val()) == '') {
            alert('钉钉报警不能为空');
            return false;
        }

        if ($.trim($('#form-add-isSMS').val()) == '') {
            alert('短信报警不能为空');
            return false;
        }

        var count = $('#copyBaseSourceDivId').find('input[name=timeRange]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('input[name=timeRange]').eq(i).val();
            if ($.trim(e) == '') {
                alert('时间范围不能为空!');
                return false;
            }
        }
        count = $('#copyBaseSourceDivId').find('input[name=threshold]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('input[name=threshold]').eq(i).val();
            if ($.trim(e) == '') {
                alert('延迟阈值不能为空!');
                return false;
            }
        }
        count = $('#copyBaseSourceDivId').find('input[name=intervalTime]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('input[name=intervalTime]').eq(i).val();
            if ($.trim(e) == '') {
                alert('报警间隔不能为空!');
                return false;
            }
        }
        count = $('#copyBaseSourceDivId').find('select[name=isPhone]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('select[name=isPhone]').eq(i).val();
            if ($.trim(e) == '') {
                alert('电话报警不能为空!');
                return false;
            }
        }
        count = $('#copyBaseSourceDivId').find('select[name=isDingD]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('select[name=isDingD]').eq(i).val();
            if ($.trim(e) == '') {
                alert('钉钉报警不能为空!');
                return false;
            }
        }
        count = $('#copyBaseSourceDivId').find('select[name=isSMS]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('select[name=isSMS]').eq(i).val();
            if ($.trim(e) == '') {
                alert('短信报警不能为空!');
                return false;
            }
        }
        return true;
    }

</script>
