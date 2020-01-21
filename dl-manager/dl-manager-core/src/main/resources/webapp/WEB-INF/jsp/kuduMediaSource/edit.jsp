<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="page-content ace-save-state">
    <div class="row">
        <form id="update_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                </ul>
                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-name">数据源名称</label>

                                <div class="col-sm-7">
                                    <input type="text" name="name" class="col-sm-12"
                                           id="form-add-name" value="${kuduMediaSourceView.name}"/>
                                    <input type="hidden" name="id" value="${kuduMediaSourceView.id}">
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-labId">所属机房</label>

                                <div class="col-sm-7">
                                    <select multiple="" id="form-add-labId" name="labId"
                                            class="labId col-xs-10 col-sm-12"
                                            data-placeholder="Click to Choose...">
                                        <c:forEach items="${labInfoList}" var="bean">
                                            <option value="${bean.id}">${bean.labName} </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                        </div>


                    <c:forEach items="${kuduMediaSourceView.kuduMediaSrcParameter.kuduMasterConfigs}" var="bean" begin="0"
                                                             varStatus="status">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-writerHost">master_host</label>

                                <div class="col-sm-7">
                                    <input type="text" name="kuduMediaSrcParameter.kuduMasterConfigs.host"
                                           class="col-sm-12" id="form-add-writerHost" value="${bean.host}"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerUserName">kudu_port</label>
                                <div class="col-sm-7">
                                    <input type="text" name="kuduMediaSrcParameter.kuduMasterConfigs.port"
                                           class="col-sm-12" id="form-add-writerUserName" value="${bean.port}"/>
                                </div>

                                <label class="col-sm-2 control-label no-padding-right">
                                    <c:if test="${status.index==0}">
                                        <a href="javascript:void(0)" onclick="buildReadDataSource()">新增</a>
                                    </c:if>
                                    <c:if test="${status.index>0}">
                                        <a href="javascript:void(0)" onclick="deleteReadDataSource(this)">删除</a>
                                    </c:if>
                                </label>
                            </div>
                        </div>
                    </c:forEach>

                        <div id="copyBaseSourceDivId"></div>

                        <c:forEach items="${kuduMediaSourceView.kuduMediaSrcParameter.impalaCconfigs}" var="bean" begin="0"
                                   varStatus="status">
                            <div class="col-sm-12">
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-3 control-label no-padding-right"
                                           for="form-add-writerImpalaHost">impala_host</label>

                                    <div class="col-sm-7">
                                        <input type="text" name="kuduMediaSrcParameter.impalaCconfigs.host"
                                               class="col-sm-12" id="form-add-writerImpalaHost" value="${bean.host}"/>
                                    </div>
                                </div>
                                <div class="col-sm-4 form-group">
                                    <label class="col-sm-3 control-label no-padding-right" for="form-add-writerImpalaPort">impala_port</label>
                                    <div class="col-sm-7">
                                        <input type="text" name="kuduMediaSrcParameter.impalaCconfigs.port"
                                               class="col-sm-12" id="form-add-writerImpalaPort" value="${bean.port}"/>
                                    </div>

                                    <label class="col-sm-2 control-label no-padding-right">
                                        <c:if test="${status.index==0}">
                                            <a href="javascript:void(0)" onclick="buildImpalaReadDataSource()">新增</a>
                                        </c:if>
                                        <c:if test="${status.index>0}">
                                            <a href="javascript:void(0)" onclick="deleteReadDataSource(this)">删除</a>
                                        </c:if>
                                    </label>
                                </div>
                            </div>
                        </c:forEach>

                        <div id="copyImpalaBaseSourceDivId"></div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerUserName">数据库名称</label>

                                <div class="col-sm-7">
                                    <input type="text" name="kuduMediaSrcParameter.database" id="form-add-database"
                                           class="col-sm-12" value="${kuduMediaSourceView.kuduMediaSrcParameter.database}"/>
                                </div>
                            </div>

                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-desc">描述</label>

                                <div class="col-sm-7">
                                    <textarea type="text" name="desc" class="col-sm-12"
                                              id="form-add-desc"
                                              style="margin: 0px;height: 106px;width: 100%;" >${kuduMediaSourceView.desc}</textarea>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
            </div>



        </form>
    </div>
    <div id="copyReadSourceBase" style="display: none;">
        <div class="col-sm-12">
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right"
                       for="form-add-writerHost">master_host</label>

                <div class="col-sm-7">
                    <input type="text" name="kuduMediaSrcParameter.kuduMasterConfigs.host"
                           class="col-sm-12"  value="${bean.host}"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerUserName">kudu_port</label>
                <div class="col-sm-7">
                    <input type="text" name="kuduMediaSrcParameter.kuduMasterConfigs.port"
                           class="col-sm-12"  value="${bean.port}"/>
                </div>

                <label class="col-sm-2 control-label no-padding-right">
                        <a href="javascript:void(0)" onclick="deleteReadDataSource(this)">删除</a>
                </label>
            </div>
        </div>
    </div>


    <div id="copyImpalaReadSourceBase" style="display: none;">
        <div class="col-sm-12">
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right"
                       for="form-add-writerImpalaHost">impala_host</label>

                <div class="col-sm-7">
                    <input type="text" name="kuduMediaSrcParameter.impalaCconfigs.host"
                           class="col-sm-12"  value="${bean.host}"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerImpalaPort">impala_port</label>
                <div class="col-sm-7">
                    <input type="text" name="kuduMediaSrcParameter.impalaCconfigs.port"
                           class="col-sm-12"  value="${bean.port}"/>
                </div>

                <label class="col-sm-2 control-label no-padding-right">
                    <a href="javascript:void(0)" onclick="deleteReadDataSource(this)">删除</a>
                </label>
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

    $('.labId').val('${labId}').select2({allowClear: false, maximumSelectionLength: 1});

    $("#form-update-desc").val('${mediaSourceView.desc}');
    $("#form-update-mediaSourceType").val('${mediaSourceView.mediaSourceType}');

    function buildReadDataSource() {
        var e = $("#copyReadSourceBase>div").clone(true);
        $('#copyBaseSourceDivId').append(e);
    }

    function buildImpalaReadDataSource() {
        var e = $("#copyImpalaReadSourceBase>div").clone(true);
        $('#copyImpalaBaseSourceDivId').append(e);
    }

    function deleteReadDataSource(e) {
        $(e).parent().parent().parent().remove();
    }

    function doEdit() {
        if (!validateForm()) {
            return;
        }
        var hosts = $('input[name="kuduMediaSrcParameter.kuduMasterConfigs.host"]');
        var ports = $('input[name="kuduMediaSrcParameter.kuduMasterConfigs.port"]');
        if (hosts != null && hosts.length > 0) {
            var size = hosts.length - 1;
            for (var i = 0; i < size; i++) {
                hosts.eq(i).attr('name', 'kuduMediaSrcParameter.kuduMasterConfigs[' + i + '].host');
                ports.eq(i).attr('name', 'kuduMediaSrcParameter.kuduMasterConfigs[' + i + '].port');
            }
        }

        var impala_hosts = $('input[name="kuduMediaSrcParameter.impalaCconfigs.host"]');
        var impala_ports = $('input[name="kuduMediaSrcParameter.impalaCconfigs.port"]');
        if (impala_hosts != null && impala_hosts.length > 0) {
            for (var i = 0; i < impala_hosts.length - 1; i++) {
                impala_hosts.eq(i).attr('name', 'kuduMediaSrcParameter.impalaCconfigs[' + i + '].host');
                impala_ports.eq(i).attr('name', 'kuduMediaSrcParameter.impalaCconfigs[' + i  + '].port');
            }
        }



        $.ajax({
            type: "post",
            url: "${basePath}/kudu/doEdit",
            dataType: 'json',
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
        kuduListMyTable.ajax.reload();
    }



    function validateForm() {
        if ($.trim($('#form-add-name').val()) == '') {
            alert('数据源名称不能为空');
            return false;
        }
        if ($.trim($('#form-add-labId').val()) == '') {
            alert('所属机房不能为空');
            return false;
        }

        if ($.trim($('#form-add-database').val()) == '') {
            alert('数据库名称不能为空');
            return false;
        }

        var count = $('#add_form').find('input[kuduMediaSrcParameter\\.kuduMasterConfigs\\.host]').length;
        for (var i = 0; i < count; i++) {
            var host = $('#copyBaseSourceDivId').find('input[kuduMediaSrcParameter\\.kuduMasterConfigs\\.host]').eq(i).val();
            var port = $('#copyBaseSourceDivId').find('input[kuduMediaSrcParameter\\.kuduMasterConfigs\\.port]').eq(i).val();
            if ($.trim(host) == '') {
                alert('host不能为空!');
                return false;
            }
            if ($.trim(port) == '') {
                alert('port不能为空!');
                return false;
            }else{
                var r = /^\+?[1-9][0-9]*$/;　　//正整数
                var flag = r.test(port);
                if (!flag) {
                    alert("端口号必须为正整数");
                    return false;
                }
            }

        }

        count = $('#add_form').find('input[kuduMediaSrcParameter\\.impalaCconfigs\\.host]').length;
        for (var i = 0; i < count; i++) {
            var host = $('#copyImpalaBaseSourceDivId').find('input[kuduMediaSrcParameter\\.impalaCconfigs\\.host]').eq(i).val();
            var port = $('#copyImpalaBaseSourceDivId').find('input[kuduMediaSrcParameter\\.impalaCconfigs\\.port]').eq(i).val();
            if ($.trim(host) == '') {
                alert('host不能为空!');
                return false;
            }
            if ($.trim(port) == '') {
                alert('port不能为空!');
                return false;
            }else{
                var r = /^\+?[1-9][0-9]*$/;　　//正整数
                var flag = r.test(port);
                if (!flag) {
                    alert("端口号必须为正整数");
                    return false;
                }
            }

        }
        return true;
    }


</script>
