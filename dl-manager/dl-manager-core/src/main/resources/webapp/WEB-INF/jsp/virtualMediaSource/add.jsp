<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-name">虚拟数据源名称</label>

                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="name" class="col-xs-10 col-sm-5"
                               id="form-add-name"/>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-desc">虚拟数据源描述</label>
                    <div class="col-sm-9">
                        <input type="text" style="width:350px;height:35px" name="desc" class="col-xs-10 col-sm-5"
                               id="form-add-desc"/>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-real-type">数据源类型</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-add-real-type" name="simulateMsType" class="form-add-real-type col-xs-10 col-sm-5"
                                data-placeholder="Click to Choose..." style="width:350px;height:35px">
                            <c:forEach items="${mediaSourceTypeList}" var="bean">
                                <option value="${bean}">${bean} </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-realMediaSource">真实数据源</label>

                    <div class="col-sm-9">
                        <select multiple="" id="form-add-realMediaSource" name="realDbIds" class="realMediaSource col-xs-10 col-sm-5"
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

    $('#form-add-real-type').select2({allowClear: false, maximumSelectionLength: 1});
    $('.realMediaSource').select2({allowClear: false, maximumSelectionLength: 2});

    function doAdd() {

        var name = $.trim($("#form-add-name").val());
        var desc = $.trim($("#form-add-desc").val());
        var realType = $.trim($("#form-add-real-type").val());
        var realMediaSource = $.trim($("#form-add-realMediaSource").val());

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
            url: "${basePath}/virtual/doAdd",
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

    $('#form-add-real-type').change(function () {
        var srcMediaSourceType = $('#form-add-real-type').val();
        if (srcMediaSourceType == null) {
            $(".form-add-realMediaSource").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
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
                    $('#form-add-realMediaSource').html('');
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#form-add-realMediaSource").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                    }
                }
            }
        });
    });

    function refresh() {
        $("#add").hide();
        $("#mainContentInner").show();
        virtualListMyTable.ajax.reload();
    }

</script>
