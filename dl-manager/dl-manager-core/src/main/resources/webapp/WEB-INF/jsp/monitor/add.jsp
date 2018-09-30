<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner ace-save-state">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="col-sm-12">
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-monitorCat">监控类型</label>

                        <div class="col-sm-9">
                            <select multiple="" name="monitorCat" class="form-add-monitorCat tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-add-monitorCat"
                                    style="width: 200px;">
                                <c:forEach items="${monitorCatList}" var="item">
                                    <option value="${item.key}">${item.desc}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-monitorType">监控指标</label>

                        <div class="col-sm-9">
                            <select multiple="" name="monitorType" class="form-add-monitorType tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-add-monitorType"
                                    style="width: 200px;">
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-resourceId">任务名称</label>

                        <div class="col-sm-9">
                            <select multiple="" name="resourceId" class="form-add-resourceId tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-add-resourceId" style="width: 200px;">
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-isEffective">是否生效</label>

                        <div class="col-sm-9">
                            <select multiple="" name="isEffective" class="isEffective tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-add-isEffective"
                                    style="width: 200px;">
                                <option value="1">是</option>
                                <option value="2">否</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-threshold">阀值</label>

                        <div class="col-sm-9">
                            <input type="text" id="form-add-threshold" value="3" name="threshold"
                                   class="col-xs-10 col-sm-5"/>

                            <div id="unit"></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-intervalTime">报警间隔</label>

                        <div class="col-sm-9">
                            <input type="text" id="form-add-intervalTime" name="intervalTime"
                                   class="col-xs-10 col-sm-5"/>(单位:秒)
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-receivePeople">发送人</label>

                        <div class="col-sm-9">
                            <select multiple="" name="receivePeople" class="receivePeople tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-add-receivePeople"
                                    style="width: 200px;">
                                <c:forEach items="${userList}" var="bean">
                                    <option value="${bean.id}">${bean.userName} </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-add-monitorRange">监控时间范围</label>

                        <div class="col-sm-9">
                            <input type="text" id="form-add-monitorRange" name="monitorRange" class="col-xs-10 col-sm-5"
                                   placeholder="00:00-23:59"/>
                        </div>
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
    $('.form-add-monitorCat').css('min-width', '42%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $('.form-add-resourceId').css('min-width', '42%').select2({allowClear: false, maximumSelectionLength: 1, width: '42%'});
    $('.form-add-monitorType').css('min-width', '42%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $('.receivePeople').css('min-width', '42%').select2({allowClear: false, width: '42%'});
    $(".isEffective").val(1).select2({allowClear: false, maximumSelectionLength: 1, width: '42%'});

    function doAdd() {
        $.ajax({
            type: "post",
            url: "${basePath}/monitor/doAdd",
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

    $('#form-add-monitorType').on('change', function () {
        var type = $('#form-add-monitorType').val();
        if (type == '1') {
            $('#unit').html('(单位:毫秒)')
        } else if (type == '2' || type == '3'|| type == '6') {
            $('#unit').html('(单位:个数)')
        } else if (type == '5') {
            $('#unit').html('单位:%')
        } else {
            $('#unit').html('')
        }
    });

    $("#form-add-monitorCat").change(function () {
        var monitorCat = $('#form-add-monitorCat').val();

        if (monitorCat == null) {
            $('#form-add-monitorType').html('');
            $("#form-add-resourceId").html('');
            $(".form-add-monitorType").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '42%'});
            $(".form-add-resourceId").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '42%'});
            return;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/monitor/getMonitorTypeListByCat?monitorCat=" + monitorCat,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-add-monitorType').html('');
                    $('#form-add-resourceId').html('');

                    for (i = 0; i < result.key.length; i++) {
                        $("<option value='" + result.key[i] + "' >" + result.desc[i] + "</option>").appendTo(".form-add-monitorType");
                    }
                    $(".form-add-monitorType").trigger("chosen:updated");

                    for (i = 0; i < result.resourceId.length; i++) {
                        $("<option value='" + result.resourceId[i] + "' >" + result.resourceName[i] + "</option>").appendTo(".form-add-resourceId");
                    }
                    $(".form-add-resourceId").trigger("chosen:updated");
                }
                else {
                    alert(result);
                }
            }
        });
    })
</script>
