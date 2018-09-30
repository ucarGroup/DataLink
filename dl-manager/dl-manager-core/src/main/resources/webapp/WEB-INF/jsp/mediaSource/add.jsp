<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#specialId">特殊配置</a>
                    </li>
                </ul>
                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-name">数据源名称</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.name" class="col-sm-12"
                                           id="form-add-name"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-namespace">schema</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.namespace" class="col-sm-12"
                                           id="form-add-namespace"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-mediaSourceType">数据源类型</label>

                                <div class="col-sm-7">
                                    <select name="rdbMediaSrcParameter.mediaSourceType" id="form-add-mediaSourceType"
                                            class="chosen-select col-sm-12">
                                        <option value="MYSQL">MYSQL</option>
                                        <option value="SQLSERVER">SQLSERVER</option>
                                        <option value="POSTGRESQL">POSTGRESQL</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-encoding">编码</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.encoding" class="col-sm-12"
                                           id="form-add-encoding" value="utf-8"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-port">端口号</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.port" class="col-sm-12"
                                           id="form-add-port" value="3306"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-writerHost">写库host</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.writeConfig.writeHost"
                                           class="col-sm-12" id="form-add-writerHost"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerUserName">写库用户名</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.writeConfig.username"
                                           class="col-sm-12" id="form-add-writerUserName" value="ucar_dep"/>
                                </div>
                            </div>
                            <input type="hidden" id="mysqlWritePsw" name="mysqlWritePsw" value="${mysql_write_psw}"/>
                            <input type="hidden" id="mysqlReadPsw" name="mysqlReadPsw" value="${mysql_read_psw}"/>
                            <input type="hidden" id="sqlserverWritePsw" name="sqlserverWritePsw" value="${sqlserver_write_psw}"/>
                            <input type="hidden" id="sqlserverReadPsw" name="sqlserverReadPsw" value="${sqlserver_read_psw}"/>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-writerPassWord">写库密码</label>

                                <div class="col-sm-7">
                                    <input type="password" name="rdbMediaSrcParameter.writeConfig.password"
                                           class="col-sm-12" id="form-add-writerPassWord" value="${mysql_write_psw}"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12" id="readSourceBaseDiv">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-readerHost">读库host</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.readConfig.hosts[0]" class="col-sm-12"
                                           id="form-add-readerHost"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-readerUserName">读库用户名</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.readConfig.username" class="col-sm-12"
                                           id="form-add-readerUserName" value="canal"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-readerPassWord">读库密码</label>

                                <div class="col-sm-7">
                                    <input type="password" name="rdbMediaSrcParameter.readConfig.password"
                                           class="col-sm-12" id="form-add-readerPassWord" value="${mysql_read_psw}"/>
                                </div>
                                <label class="col-sm-2 control-label no-padding-right" for="form-add-readerPassWord">
                                    <a href="javascript:void(0)" onclick="buildReadDataSource()">新增</a>
                                </label>
                            </div>
                        </div>
                        <div id="copyBaseSourceDivId"></div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-etlHost">Etl-Host</label>

                                <div class="col-sm-7">
                                    <input type="text" name="rdbMediaSrcParameter.readConfig.etlHost" class="col-sm-12"
                                           id="form-add-etlHost"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-etlUserName">读库用户名</label>

                                <div class="col-sm-7">
                                    <input type="text" name="xxx.etlUserName" class="col-sm-12"
                                           id="form-add-etlUserName" value="" readonly="readonly"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-etlPassWord">读库密码</label>

                                <div class="col-sm-7">
                                    <input type="password" name="xxx.etlPassWord"
                                           class="col-sm-12" id="form-add-etlPassWord" readonly="readonly"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-add-desc">描述</label>

                                <div class="col-sm-7">
                                    <textarea type="text" name="rdbMediaSrcParameter.desc" class="col-sm-12"
                                              id="form-add-desc"
                                              style="margin: 0px;height: 106px;width: 100%;"></textarea>
                                </div>
                            </div>

                        </div>
                    </div>
                    <div id="specialId" class="tab-pane">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-maxWait">超时等待时间</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;" name="basicDataSourceConfig.maxWait"
                                           class="col-sm-12" id="form-add-maxWait" value="60000"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-minIdle">最小空闲连接</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;" name="basicDataSourceConfig.minIdle"
                                           class="col-sm-12" id="form-add-minIdle" value="1"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-initialSize">初始化连接数</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;" name="basicDataSourceConfig.initialSize"
                                           class="col-sm-12" id="form-add-initialSize" value="1"/>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-maxActive">最大连接数量</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;" name="basicDataSourceConfig.maxActive"
                                           class="col-sm-12" id="form-add-maxActive" value="32"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-maxIdle">最大空闲连接</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;" name="basicDataSourceConfig.maxIdle"
                                           class="col-sm-12" id="form-add-maxIdle" value="32"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-numTestsPerEvictionRun">numTestsPerEvictionRun</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;"
                                           name="basicDataSourceConfig.numTestsPerEvictionRun" class="col-sm-12"
                                           id="form-add-numTestsPerEvictionRun" value="-1"/>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-timeBetweenEvictionRunsMillis">timeBetweenEvictionRunsMillis</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;"
                                           name="basicDataSourceConfig.timeBetweenEvictionRunsMillis" class="col-sm-12"
                                           id="form-add-timeBetweenEvictionRunsMillis" value="60000"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-removeAbandonedTimeout">removeAbandonedTimeout</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;"
                                           name="basicDataSourceConfig.removeAbandonedTimeout" class="col-sm-12"
                                           id="form-add-removeAbandonedTimeout" value="300"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-minEvictableIdleTimeMillis">minEvictableIdleTimeMillis</label>

                                <div class="col-sm-5">
                                    <input type="text" style="width:100%;"
                                           name="basicDataSourceConfig.minEvictableIdleTimeMillis" class="col-sm-12"
                                           id="form-add-minEvictableIdleTimeMillis" value="3000000"/>
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
                <label class="col-sm-3 control-label no-padding-right" for="form-add-readerHost">读库host</label>

                <div class="col-sm-7">
                    <input type="text" name="rdbMediaSrcParameter.readConfig.hosts" class="col-sm-12"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-add-readerUserName">读库用户名</label>

                <div class="col-sm-7">
                    <input type="text" readonly="readonly" class="col-sm-12"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-add-readerPassWord">读库密码</label>

                <div class="col-sm-7">
                    <input type="text" readonly="readonly" class="col-sm-12"/>
                </div>
                <label class="col-sm-2 control-label no-padding-right" for="form-add-readerPassWord">
                    <a href="javascript:void(0)" onclick="deleteReadDataSource(this)">删除</a>
                </label>
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
    function buildReadDataSource() {
        var e = $("#copyReadSourceBase>div").clone(true);
        $('#copyBaseSourceDivId').append(e);

    }
    function deleteReadDataSource(e) {
        $(e).parent().parent().parent().remove();
    }

    $("#form-add-mediaSourceType").change(function () {
        var mediaSourceType = $("#form-add-mediaSourceType").val();
        var mysqlWritePsw = $("#mysqlWritePsw").val();
        var mysqlReadPsw = $("#mysqlReadPsw").val();
        var sqlserverWritePsw = $("#sqlserverWritePsw").val();
        var sqlserverReadPsw = $("#sqlserverReadPsw").val();
        if (mediaSourceType == "MYSQL") {
            $("#form-add-writerUserName").val("ucar_dep");
            $("#form-add-readerUserName").val("canal");
            $("#form-add-writerPassWord").val(mysqlWritePsw);
            $("#form-add-readerPassWord").val(mysqlReadPsw);
        }
        if (mediaSourceType == "SQLSERVER") {
            $("#form-add-writerUserName").val("ucar_dep_w");
            $("#form-add-readerUserName").val("ucar_dep_r");
            $("#form-add-writerPassWord").val(sqlserverWritePsw);
            $("#form-add-readerPassWord").val(sqlserverReadPsw);
        }
    });

    function doAdd() {
        if (!validateForm()) {
            return;
        }
        var hosts = $('input[name="rdbMediaSrcParameter.readConfig.hosts"]');
        if (hosts != null && hosts.length > 0) {
            for (var i = 0; i < hosts.length; i++) {
                hosts.eq(i).attr('name', 'rdbMediaSrcParameter.readConfig.hosts[' + (i + 1) + ']');
            }
        }

        $.ajax({
            type: "post",
            url: "${basePath}/mediaSource/doAdd",
            dataType: 'json',
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

    $('#form-add-mediaSourceType').change(function () {
        if ($(this).val() == 'SQLSERVER') {
            $('#form-add-port').val(1433);
        } else {
            $('#form-add-port').val(3306);
        }
    });


    function validateForm() {
        if ($.trim($('#form-add-name').val()) == '') {
            alert('数据源名称不能为空');
            return false;
        }
        if ($.trim($('#form-add-namespace').val()) == '') {
            alert('schema不能为空');
            return false;
        }
        if ($.trim($('#form-add-encoding').val()) == '') {
            alert('编码不能为空');
            return false;
        }
        var r = /^\+?[1-9][0-9]*$/;//正整数
        var flag = r.test($('#form-add-port').val());
        if (!flag) {
            alert("端口号必须为正整数");
            return;
        }

        if ($.trim($('#form-add-desc').val()) == '') {
            alert('数据源描述不能为空');
            return false;
        }

        if ($.trim($('#form-add-writerHost').val()) == '') {
            alert('写库host不能为空');
            return false;
        }

        if ($.trim($('#form-add-writerUserName').val()) == '') {
            alert('写库用户名不能为空');
            return false;
        }

        if ($.trim($('#form-add-writerPassWord').val()) == '') {
            alert('写库密码不能为空');
            return false;
        }

        if ($.trim($('#form-add-readerHost').val()) == '') {
            alert('读库host不能为空');
            return false;
        }
        if ($.trim($('#form-add-readerUserName').val()) == '') {
            alert('读库用户名不能为空');
            return false;
        }

        if ($.trim($('#form-add-readerPassWord').val()) == '') {
            alert('读库密码不能为空');
            return false;
        }

        var count = $('#copyBaseSourceDivId').find('input[name=rdbMediaSrcParameter\\.readConfig\\.hosts]').length;
        for (var i = 0; i < count; i++) {
            var e = $('#copyBaseSourceDivId').find('input[name=rdbMediaSrcParameter\\.readConfig\\.hosts]').eq(i).val();
            if ($.trim(e) == '') {
                alert('读库host不能为空!');
                return false;
            }
        }
        return true;
    }
</script>
