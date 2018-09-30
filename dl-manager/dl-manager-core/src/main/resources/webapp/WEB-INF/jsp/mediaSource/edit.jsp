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
                    <li>
                        <a data-toggle="tab" href="#specialId">特殊配置</a>
                    </li>
                </ul>
                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right">数据源名称</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.name}" name="name" class="col-sm-12"
                                           id="form-update-srcMediaName"/>
                                    <input type="hidden" name="mediaSourceId" value="${mediaSourceId}">
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-namespace">schema</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.namespace}" name="namespace"
                                           class="col-sm-12" id="form-update-namespace"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-update-mediaSourceType">数据源类型</label>

                                <div class="col-sm-7">
                                    <select name="mediaSourceType" id="form-update-mediaSourceType"
                                            class="chosen-select col-sm-12">
                                        <option value="MYSQL">MYSQL</option>
                                        <option value="SQLSERVER">SQLSERVER</option>
                                        <option value="POSTGRESQL">POSTGRESQL</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-update-encoding">编码</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.encoding}" name="encoding"
                                           class="col-sm-12" id="form-update-encoding" value="utf-8"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="form-update-port">端口号</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.port}" name="port" class="col-sm-12"
                                           id="form-update-port" value="3306"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-writerHost">写库host</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.writeConfig.writeHost}"
                                           name="writeConfig.writeHost" class="col-sm-12" id="form-update-writerHost"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-writerUserName">写库用户名</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.writeConfig.username}"
                                           name="writeConfig.username" class="col-sm-12"
                                           id="form-update-writerUserName"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-writerPassWord">写库密码</label>

                                <div class="col-sm-7">
                                    <input type="password" value="${mediaSourceView.writeConfig.password}"
                                           name="writeConfig.password" class="col-sm-12"
                                           id="form-update-writerPassWord"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12" id="readSourceBaseDiv">

                            <c:forEach items="${mediaSourceView.readConfig.hosts}" var="bean" begin="0"
                                       varStatus="status">
                                <div>
                                    <div class="col-sm-4 form-group">
                                        <label class="col-sm-3 control-label no-padding-right"
                                               for="form-update-readerHost">读库host</label>

                                        <div class="col-sm-7">
                                            <input type="text" value="${bean}" name="readConfig.hosts" class="col-sm-12"
                                                   id="form-update-readerHost"/>
                                        </div>
                                    </div>
                                    <div class="col-sm-4 form-group">
                                        <label class="col-sm-3 control-label no-padding-right"
                                               for="form-update-readerUserName">读库用户名</label>

                                        <div class="col-sm-7">
                                            <c:if test="${status.index==0}">
                                                <input type="text" class="col-sm-12" name="readConfig.username"
                                                       value="${mediaSourceView.readConfig.username}"
                                                       id="form-update-readerUserName"/>
                                            </c:if>
                                            <c:if test="${status.index>0}">
                                                <input type="text" class="col-sm-12" readonly="readonly"
                                                       id="form-add-readerUserName"/>
                                            </c:if>
                                        </div>
                                    </div>
                                    <div class="col-sm-4 form-group">
                                        <label class="col-sm-3 control-label no-padding-right"
                                               for="form-update-readerPassWord">读库密码</label>

                                        <div class="col-sm-7">
                                            <c:if test="${status.index==0}">
                                                <input type="password" class="col-sm-12" name="readConfig.password"
                                                       value="${mediaSourceView.readConfig.password}"
                                                       id="form-update-readerPassWord"/>
                                            </c:if>
                                            <c:if test="${status.index>0}">
                                                <input type="password" class="col-sm-12" readonly="readonly"
                                                       id="form-update-readerPassWord"/>
                                            </c:if>
                                        </div>
                                        <label class="col-sm-2 control-label no-padding-right"
                                               for="form-update-readerPassWord">
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

                        </div>
                        <div id="copyBaseSourceDivId"></div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-etlHost">Etl-Host</label>

                                <div class="col-sm-7">
                                    <input type="text" value="${mediaSourceView.readConfig.etlHost}"
                                           name="readConfig.etlHost" class="col-sm-12" id="form-update-etlHost"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-etlUserName">读库用户名</label>

                                <div class="col-sm-7">
                                    <input type="text" name="xxx.etlUsername" class="col-sm-12"
                                           id="form-update-etlUserName" readonly="readonly"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-etlPassWord">读库密码</label>

                                <div class="col-sm-7">
                                    <input type="password" name="xxx.etlPassword" class="col-sm-12"
                                           id="form-update-etlPassWord" readonly="readonly"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-update-desc">描述</label>

                                <div class="col-sm-7">
                                    <textarea type="text" name="desc" class="col-sm-12" id="form-update-desc"
                                              style="margin: 0px; width: 755px; height: 106px;"></textarea>
                                </div>
                            </div>

                        </div>
                    </div>
                    <div id="specialId" class="tab-pane">
                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-maxWait">超时等待时间</label>

                                <div class="col-sm-5">
                                    <input type="text" value="${mediaSourceView.dataSourceConfig.maxWait}"
                                           name="maxWait" class="col-sm-12" id="form-update-maxWait" value="60000"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-minIdle">最小空闲连接</label>

                                <div class="col-sm-5">
                                    <input type="text" value="${mediaSourceView.dataSourceConfig.minIdle}"
                                           name="minIdle" class="col-sm-12" id="form-update-minIdle" value="1"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-add-initialSize">初始化连接数</label>

                                <div class="col-sm-5">
                                    <input type="text" value="${mediaSourceView.dataSourceConfig.initialSize}"
                                           name="initialSize" class="col-sm-12" id="form-add-initialSize" value="1"/>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right" for="form-update-maxActive">最大连接数量</label>

                                <div class="col-sm-5">
                                    <input type="text" value="${mediaSourceView.dataSourceConfig.maxActive}"
                                           name="maxActive" class="col-sm-12" id="form-update-maxActive" value="32"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-maxIdle">最大空闲连接</label>

                                <div class="col-sm-5">
                                    <input type="text" value="${mediaSourceView.dataSourceConfig.maxIdle}"
                                           name="maxIdle" class="col-sm-12" id="form-update-maxIdle" value="32"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-numTestsPerEvictionRun">numTestsPerEvictionRun</label>

                                <div class="col-sm-5">
                                    <input type="text"
                                           value="${mediaSourceView.dataSourceConfig.numTestsPerEvictionRun}"
                                           name="numTestsPerEvictionRun" class="col-sm-12"
                                           id="form-update-numTestsPerEvictionRun" value="-1"/>
                                </div>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-timeBetweenEvictionRunsMillis">timeBetweenEvictionRunsMillis</label>

                                <div class="col-sm-5">
                                    <input type="text"
                                           value="${mediaSourceView.dataSourceConfig.timeBetweenEvictionRunsMillis}"
                                           name="timeBetweenEvictionRunsMillis" class="col-sm-12"
                                           id="form-update-timeBetweenEvictionRunsMillis" value="60000"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-removeAbandonedTimeout">removeAbandonedTimeout</label>

                                <div class="col-sm-5">
                                    <input type="text"
                                           value="${mediaSourceView.dataSourceConfig.removeAbandonedTimeout}"
                                           name="removeAbandonedTimeout" class="col-sm-12"
                                           id="form-update-removeAbandonedTimeout" value="300"/>
                                </div>
                            </div>
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-7 control-label no-padding-right"
                                       for="form-update-minEvictableIdleTimeMillis">minEvictableIdleTimeMillis</label>

                                <div class="col-sm-5">
                                    <input type="text"
                                           value="${mediaSourceView.dataSourceConfig.minEvictableIdleTimeMillis}"
                                           name="minEvictableIdleTimeMillis" class="col-sm-12"
                                           id="form-update-minEvictableIdleTimeMillis" value="3000000"/>
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
                <label class="col-sm-3 control-label no-padding-right" for="form-update-readerHost">读库host</label>

                <div class="col-sm-7">
                    <input type="text" id="readConfigHosts" name="readConfig.hosts" class="col-sm-12"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-update-readerUserName">读库用户名</label>

                <div class="col-sm-7">
                    <input type="text" readonly="readonly" class="col-sm-12"/>
                </div>
            </div>
            <div class="col-sm-4 form-group">
                <label class="col-sm-3 control-label no-padding-right" for="form-update-readerPassWord">读库密码</label>

                <div class="col-sm-7">
                    <input type="text" readonly="readonly" class="col-sm-12"/>
                </div>
                <label class="col-sm-2 control-label no-padding-right" for="form-update-readerPassWord">
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
    $("#form-update-desc").val('${mediaSourceView.desc}');
    $("#form-update-mediaSourceType").val('${mediaSourceView.mediaSourceType}');
    function buildReadDataSource() {
        9
        var e = $("#copyReadSourceBase>div").clone(true);
        $('#copyBaseSourceDivId').append(e);
    }
    function deleteReadDataSource(e) {
        $(e).parent().parent().parent().remove();
    }

    function doEdit() {
        if (!validateForm()) {
            return;
        }
        var hosts = $('input[name="readConfig.hosts"]');
        if (hosts != null && hosts.length > 0) {
            for (var i = 0; i < hosts.length; i++) {
                hosts.eq(i).attr('name', 'readConfig.hosts[' + (i) + ']');
            }
        }

        $.ajax({
            type: "post",
            url: "${basePath}/mediaSource/doEdit",
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
        msgAlarmListMyTable.ajax.reload();
    }

    $('#form-update-mediaSourceType').change(function () {
        if ($(this).val() == 'SQLSERVER') {
            $('#form-update-port').val(1433);
        } else {
            $('#form-update-port').val(3306);
        }
    });


    function validateForm() {
        if ($.trim($('#form-update-srcMediaName').val()) == '') {
            alert('数据源名称不能为空');
            return false;
        }
        if ($.trim($('#form-update-namespace').val()) == '') {
            alert('schema不能为空');
            return false;
        }

        if ($.trim($('#form-update-encoding').val()) == '') {
            alert('编码不能为空');
            return false;
        }
        var r = /^\+?[1-9][0-9]*$/;　　//正整数
        var flag = r.test($('#form-update-port').val());
        if (!flag) {
            alert("端口号必须为正整数");
            return;
        }

        if ($.trim($('#form-update-desc').val()) == '') {
            alert('数据源描述不能为空');
            return false;
        }

        if ($.trim($('#form-update-writerHost').val()) == '') {
            alert('写库host不能为空');
            return false;
        }

        if ($.trim($('#form-update-writerUserName').val()) == '') {
            alert('写库用户名不能为空');
            return false;
        }

        if ($.trim($('#form-update-writerPassWord').val()) == '') {
            alert('写库密码不能为空');
            return false;
        }

        if ($.trim($('#form-update-etlHost').val()) == '') {
            alert('etlHost不能为空');
            return false;
        }

        var hosts = $('#readSourceBaseDiv').find('input[name^=readConfig\\.hosts]');
        var username = $('#readSourceBaseDiv').find('input[name^=readConfig\\.username]');
        var password = $('#readSourceBaseDiv').find('input[name^=readConfig\\.password]');
        if ($.trim(hosts.eq(0).val()) == '') {
            alert('读库host不能为空');
            return false;
        }
        if ($.trim(username.eq(0).val()) == '') {
            alert('读库用户名不能为空');
            return false;
        }
        if ($.trim(password.eq(0).val()) == '') {
            alert('读库密码不能为空');
            return false;
        }
        for (var i = 0; i < $('#readSourceBaseDiv').find('input[name^=readConfig\\.hosts]').length; i++) {
            var e = $('#readSourceBaseDiv').find('input[name^=readConfig\\.hosts]').eq(i).val();
            if ($.trim(e) == '') {
                alert('读库host不能为空!');
                return false;
            }
        }
        for (var i = 0; i < $('#copyBaseSourceDivId').find('input[name^=readConfig\\.hosts]').length; i++) {
            var e = $('#copyBaseSourceDivId').find('input[name^=readConfig\\.hosts]').eq(i).val();
            if ($.trim(e) == '') {
                alert('读库host不能为空!');
                return false;
            }
        }
        return true;
    }
</script>
