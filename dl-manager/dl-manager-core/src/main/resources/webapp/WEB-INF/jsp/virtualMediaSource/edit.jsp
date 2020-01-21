<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="update_form" class="form-horizontal" role="form">
                <input type="hidden" name="id" value="${mediaSourceInfo.id}">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-name">虚拟数据源名称</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-update-name" value="${mediaSourceInfo.name}"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">虚拟数据源描述</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="desc" class="col-xs-10 col-sm-5"
                               id="form-update-desc" value="${mediaSourceInfo.desc}"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-update-real-type">数据源类型</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-update-real-type" name="simulateMsType" class="form-update-real-type col-xs-10 col-sm-5"
                                data-placeholder="Click to Choose..." style="width:350px;height:35px">
                            <c:forEach items="${mediaSourceTypeList}" var="bean">
                                <option value="${bean}">${bean} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right"
                           for="form-update-realMediaSource">真实数据源</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-update-realMediaSource" name="realDbIds" class="form-update-realMediaSource col-xs-10 col-sm-5"
                                data-placeholder="Click to Choose..." style="width:350px;height:35px">
                            <c:forEach items="${realMediaSource}" var="bean">
                                <option value="${bean.id}">${bean.name} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

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


    $('#form-update-real-type').val('${mediaSourceInfo.simulateMsType}').select2({allowClear: false, maximumSelectionLength: 1});

    var srcMediaSourceType = $('#form-update-real-type').val();
    $.ajax({
        type: "post",
        url: "${basePath}/virtual/getMediaSources",
        async: true,
        dataType: "json",
        data: "&mediaSourceType=" + srcMediaSourceType,
        success: function (result) {
            if (result != null && result != '') {

                $('#form-update-realMediaSource').html('');
                if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                    for (var i = 0; i < result.mediaSourceList.length; i++) {
                        $("#form-update-realMediaSource").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                    }
                }
            }

            var pe = '${mediaSourceInfo.realDbIds}'.split(",");
            $('.form-update-realMediaSource').val(pe).select2({
                allowClear: false,
                maximumSelectionLength: 2
            });

        }
    });


    function doEdit() {
        var name = $.trim($("#form-update-name").val());
        var desc = $.trim($("#form-update-desc").val());
        var realType = $.trim($("#form-update-real-type").val());
        var realMediaSource = $.trim($("#form-update-realMediaSource").val());

        if (name == "") {
            alert("虚拟数据源名称不能为空!");
            return false;
        }
        if (desc == "") {
            alert("虚拟数据源描述不能为空!");
            return false;
        }
        if (realType == "") {
            alert("数据源类型不能为空!");
            return false;
        }
        if (realMediaSource == "") {
            alert("真实数据源不能为空!");
            return false;
        }

        $.ajax({
            type: "post",
            url: "${basePath}/virtual/doEdit",
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

    $('#form-update-real-type').change(function () {

        $('#form-update-realMediaSource').html('');
        $(".form-update-realMediaSource").val('').select2({allowClear: false, maximumSelectionLength: 2});
        var srcMediaSourceType = $('#form-update-real-type').val();
        if (srcMediaSourceType == null) {
            $(".form-update-realMediaSource").val('').select2({allowClear: false, maximumSelectionLength: 2});
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/virtual/getMediaSources",
            async: true,
            dataType: "json",
            data: "&mediaSourceType=" + srcMediaSourceType,
            success: function (result) {
                if (result != null && result != '') {
                    $('#form-update-realMediaSource').html('');
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#form-update-realMediaSource").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                    }
                }
            }
        });
    });


    function refresh() {
        $("#edit").hide();
        $("#mainContentInner").show();
        virtualListMyTable.draw(false);
    }
</script>
