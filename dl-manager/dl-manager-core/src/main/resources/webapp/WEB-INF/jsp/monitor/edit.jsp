<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner ace-save-state">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <div class="col-sm-12">
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-update-monitorCat">监控类型</label>

                        <div class="col-sm-9">
                            <select multiple="" name="monitorCat" class="form-update-monitorCat tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-monitorCat"
                                    style="width: 200px;" disabled="disabled">
                                <c:forEach items="${monitorCatList}" var="item">
                                    <option value="${item.key}" <c:if
                                            test="${item.key == monitorInfo.monitorCat}"> selected="selected"</c:if>>${item.desc}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-update-monitorType">监控指标</label>

                        <div class="col-sm-9">
                            <select multiple="" name="monitorType" class="form-update-monitorType tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-monitorType"
                                    style="width: 200px;">
                                <c:if test="${monitorInfo.monitorCat == '1'}">
                                    <option value="1">延迟</option>
                                    <option value="2">异常</option>
                                    <option value="3">任务状态</option>
                                    <option value="6">任务状态不匹配</option>
                                </c:if>
                                <c:if test="${monitorInfo.monitorCat == '2'}">
                                    <option value="4">worker运行状态</option>
                                    <option value="5">workerJVM状态</option>
                                </c:if>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-update-resourceId">监控项目</label>

                        <div class="col-sm-9">
                            <select multiple="" name="resourceId" class="form-update-resourceId tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-resourceId" style="width: 200px;">
                                <c:if test="${monitorInfo.monitorCat == '1'}">
                                    <c:forEach items="${taskList}" var="bean">
                                        <option value="${bean.id}" <c:if
                                                test="${bean.id == monitorInfo.resourceId}"> selected="selected"</c:if>>${bean.taskName} </option>
                                    </c:forEach>
                                </c:if>
                                <c:if test="${monitorInfo.monitorCat == '2'}">
                                    <c:forEach items="${workerList}" var="bean">
                                        <option value="${bean.id}" <c:if
                                                test="${bean.id == monitorInfo.resourceId}"> selected="selected"</c:if>>${bean.workerName} </option>
                                    </c:forEach>
                                </c:if>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-update-isEffective">是否生效</label>

                        <div class="col-sm-9">
                            <select multiple="" name="isEffective" class="isEffective tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-isEffective"
                                    style="width: 200px;">
                                <option value="1">是</option>
                                <option value="2">否</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right" for="form-update-threshold">阀值</label>

                        <div class="col-sm-9">
                            <input type="text" id="form-update-threshold" value="${monitorInfo.threshold}"
                                   name="threshold" class="col-xs-10 col-sm-5"/>

                            <div id="unit"></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-update-intervalTime">报警间隔</label>

                        <div class="col-sm-9">
                            <input type="text" id="form-update-intervalTime" value="${monitorInfo.intervalTime}"
                                   name="intervalTime" class="col-xs-10 col-sm-5"/>(单位:秒)
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label no-padding-right"
                               for="form-update-receivePeople">发送人</label>

                        <div class="col-sm-9">
                            <select multiple="" name="receivePeople" class="receivePeople tag-input-style"
                                    data-placeholder="Click to Choose..." id="form-update-receivePeople"
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
                            <input type="text" value="${monitorInfo.monitorRange}" id="form-add-monitorRange"
                                   name="monitorRange" class="col-xs-10 col-sm-5" placeholder="00:00-23:59"/>
                        </div>
                    </div>
                </div>
                <input type="hidden" value="${monitorInfo.id}" name="id">
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
    var type = '${monitorInfo.monitorType}';
    if (type == '1') {
        $('#unit').html('(单位:毫秒)')
    } else if (type == '2' || type == '3'|| type == '6') {
        $('#unit').html('(单位:个数)')
    } else if (type == '5') {
        $('#unit').html('单位:%')
    } else {
        $('#unit').html('')
    }
    var pe = '${monitorInfo.receivePeople}'.split(",");
    $(".isEffective").val('${monitorInfo.isEffective}').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $(".form-update-resourceId").val('${monitorInfo.resourceId}').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $(".form-update-monitorType").val('${monitorInfo.monitorType}').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $(".form-update-monitorCat").val('${monitorInfo.monitorCat}').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '42%'
    });
    $(".receivePeople").val(pe).select2({allowClear: true, width: '42%'});

    function doEdit() {
        $.ajax({
            type: "post",
            url: "${basePath}/monitor/doEdit",
            dataType: "json",
            data: $("#update_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
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
        msgAlarmListMyTable.ajax.reload();
    }
    $('#form-update-monitorType').on('change', function () {
        var type = $('#form-update-monitorType').val();
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
</script>
