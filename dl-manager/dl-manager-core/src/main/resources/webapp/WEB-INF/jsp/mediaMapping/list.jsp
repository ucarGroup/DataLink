<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="view" class="main-container">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">

            <div id="addColumn-wizard" class="modal">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div id="queue-wizard-container">
                            <div class="modal-header">
                                <div class="modal-header no-padding">
                                    <div class="table-header">
                                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                            <span class="white">&times;</span>
                                        </button>
                                        手动增加字段
                                    </div>
                                </div>
                            </div>

                            <div class="modal-body">
                                <form id="addColumnForm" class="form-horizontal" role="form">
                                    <input type="hidden" name="queue_form_start_jobId" id="queue_form_start_jobId"/>


                                    <div class="form-group">
                                        <label class="col-sm-3 control-label no-padding-right"
                                               for="queue_form-start"> 映射id </label>

                                        <div class="col-sm-9" id="queue_form-start" style="margin-bottom: 5px;">
                                            <input type="text" id="mappingIdText" name="mappingId" class="col-sm-8" />
                                        </div>


                                        <label class="col-sm-3 control-label no-padding-right"
                                               for="queue_form-start"> 要添加的字段名 </label>

                                        <div class="col-sm-9" id="queue_form-start1" style="margin-top: 5px;">
                                            <input type="text" id="columnNameText" name="columnName" class="col-sm-8" />
                                        </div>
                                    </div>

                                </form>
                            </div>
                        </div>

                        <div class="modal-footer wizard-actions">
                            <button class="btn btn-success" type="button" onclick="doAddColumn()">
                                <i class="ace-icon fa fa-save"></i>
                                确定
                            </button>
                            <button class="btn btn-danger" type="button" data-dismiss="modal">
                                取消
                                <i class="ace-icon fa fa-times"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xs-12">

                <div class="row">
                    <form class="form-horizontal">

                        <div class="row">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label">同步模式</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="basic-taskSyncMode"
                                            style="width:100%">
                                        <option value="-1">全部</option>
                                        <c:forEach items="${taskSyncModeList}" var="bean">
                                            <option value="${bean.code}">${bean.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label">源端数据源</label>

                                <div class="col-sm-8">
                                    <select class="mediaSourceId width-100 chosen-select" id="mediaSourceId"
                                            style="width:100%">
                                        <option value="-1">全部</option>
                                        <c:forEach items="${sourceMediaSourceList}" var="item">
                                            <option value="${item.id}">${item.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label">目标端数据源</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="targetMediaSourceId" style="width:100%">
                                        <option value="-1">全部</option>
                                        <c:forEach items="${targetMediaSourceList}" var="item">
                                            <option value="${item.id}">${item.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label no-padding-right"
                                       for="taskId">任务名称</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="taskId"
                                            style="width:100%">
                                        <option value="-1">全部</option>
                                        <c:forEach items="${taskList}" var="item">
                                            <option value="${item.id}">${item.taskName}</option>
                                        </c:forEach>
                                    </select>

                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label">源端表名</label>

                                <div class="col-sm-8">
                                    <input id="srcMediaName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-3 control-label">目标端表名</label>

                                <div class="col-sm-8">
                                    <input id="targetMediaName" type="text" style="width:100%;">
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                            </div>

                            <div class="form-group col-xs-3" id="addColumnPanel">

                            </div>

                        </div>

                    </form>
                </div>

                <div class="col-xs-12"  id="OperPanel">

                </div>

                <div class="row">
                    <table id="mediaMappingTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>任务名称</td>
                            <td>源端数据源</td>
                            <td>源端表名</td>
                            <td>目标端数据源</td>
                            <td>目标端表名</td>
                            <td>优先级</td>
                            <td>是否有效</td>
                            <td>创建时间</td>
                            <td>操作</td>
                        </tr>
                        </thead>
                    </table>
                </div>

                <div id="dataCompare-wizard" class="modal">

                    <div class="modal-dialog">
                        <div class="modal-content" style="width:800px;height:400px">
                            <div id="modal-wizard-container">
                                <div class="modal-header" style="width:800px;height:70px">

                                    <div class="modal-header no-padding">
                                        <div class="table-header">
                                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                <span class="white">&times;</span>
                                            </button>
                                            数据检测
                                        </div>
                                    </div>
                                </div>

                                <div class="modal-body" style="width:800px;height:300px">
                                    <form id="check_form" class="form-horizontal" role="form">
                                        <input type="hidden" name="mappingId" id="form-check-mappingId"/>

                                        <div class="form-group well">
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-startId">起始Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-startId" name="startId"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-endId">截止Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-endId" name="endId" placeholder=""
                                                           class="col-sm-12"/>
                                                </div>
                                            </div>
                                            <div class="col-sm-2">
                                                <input type="button" value="检 测" class="btn btn-sm btn-info"
                                                       onClick="doDataCheck();"/>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-fromDsCount">源表数据量</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-fromDsCount" name="fromDsCount"
                                                           vplaceholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-toDsCount">目标表数据量</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-toDsCount" name="toDsCount"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-fromMinId">源表最小Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-fromMinId" name="fromMinId"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-toMinId">目标表最小Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-toMinId" name="toMinId"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-fromMaxId">源表最大Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-fromMaxId" name="fromMaxId"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                            <div class="col-sm-5">
                                                <label class="col-sm-4 control-label no-padding-right"
                                                       for="form-check-toMaxId">目标表最大Id</label>

                                                <div class="col-sm-8">
                                                    <input type="text" id="form-check-toMaxId" name="toMaxId"
                                                           placeholder="" class="col-sm-12"/>
                                                </div>
                                            </div>
                                        </div>
                                    </form>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
        <!-- /.page-content -->

    </div>
</div>
<script type="text/javascript">
    var msgAlarmListMyTable;
    $(".chosen-select").chosen();

    getButtons([{
        code:"004020200",
        html:'<div class="pull-left tableTools-container" style="padding-top: 10px;">'+
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>'+
        '</div>'
    }],$("#OperPanel"));

    getButtons([{
        code:"004020900",
        html:'<input style="border:0px;"></input>'+
        '<button type="button" id="addColumn" class="btn btn-sm btn-purple">添加字段</button>'
    }],$("#addColumnPanel"));

    msgAlarmListMyTable = $('#mediaMappingTable').DataTable({
        processing: true,
        filter: true,
        ordering: true,
        "sInfoEmpty": "No entries to show",
        serverSide: true,//开启服务器模式:启用服务器分页
        paging: true,//是否分页
        pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax": {
            "url": "${basePath}/mediaMapping/initMediaMapping",
            "data": function (d) {
                d.mediaSourceId = $("#mediaSourceId").val();
                d.targetMediaSourceId = $("#targetMediaSourceId").val();
                d.srcMediaName = $("#srcMediaName").val();
                d.targetMediaName = $("#targetMediaName").val();
                d.taskId = $("#taskId").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "taskName"},
            {"data": "srcMediaSourceName"},
            {"data": "srcMediaName"},
            {"data": "targetMediaSourceName"},
            {"data": "targetMediaName"},
            {"data": "writePriority"},
            {
                "data": "valid",
                render: function (data, type, row) {
                    if (data == true) {
                        return "是";
                    } else {
                        return "否";
                    }
                }
            },
            {
                "data": "createTime",
                render: function (data, type, row) {
                    return formatTs(data);
                }
            }
        ],
        language: {
            "sUrl": "${basePath}/assets/json/zh_CN.json",
            "infoEmpty": ""
        },
        "drawCallback": function (settings) {
            var api = this.api();
            var rows = api.rows({page: 'current'}).nodes();
            var last = null;
            var tr = null;
            var preTd = null;

            api.column(0, {page: 'current'}).data().each(function (group, i) {

                tr = $(rows[i]);
                if (last !== group) {
                    preTd = $("td:first", tr);
                    preTd.attr("rowspan", 1);
                    preTd.text(group);
                    last = group;
                } else {
                    preTd.attr("rowspan", parseInt(preTd.attr("rowspan")) + 1);
                    $("td:first", tr).remove();
                }
            });
        },
        columnDefs: [{
            "aTargets": [9],
            "mData": null,
            "bSortable": false,
            "bSearchable": false,
            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {

                getButtons([
                    {
                        code:'004020400',
                        html:function() {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:toEdit(" + oData.id + ")' class='blue'  title='修改'>" +
                            "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    },
                    {
                        code:'004020800',
                        html:function() {
                            var str;
                            str = "<div class='radio'>" +
                                "<a href='javascript:toView(" + oData.id + ")' class='black'  title='查看'>" +
                                "<i class='fa fa-info bigger-130' aria-hidden='true'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    },
                    {
                        code:'004020600',
                        html:function() {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                            "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    },
                    {
                        code:'004020700',
                        html:function() {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:dataCheck(" + oData.id + ")' class='green'  title='数据校验'>" +
                            "<i class='ace-icon fa fa-hand-o-up bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    }
                ],$(nTd));

            }

        }]
    });

    $("#addColumn").click(function () {
        debugger;
        $('#addColumn-wizard').modal('show');
    })

    function doAddColumn() {

        if(!$('#mappingIdText').val()){
            alert("映射id不能为空");
            return;
        }
        if(!$('#columnNameText').val()){
            alert("字段名不能为空");
            return;
        }

        debugger;
        $.ajax({
            type: "post",
            url: "${basePath}/sync/relation/generateAddColumnSql",
            data: $("#addColumnForm").serialize(),
            dataType: "json",
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data.success == "true") {
                    var data = {
                        sql:data.sql,
                        "mediaSourceId":data.mediaSourceId,
                        "mappingId":data.mappingId
                    };
                    $.ajax({
                        type: "post",
                        url: "${basePath}/sync/relation/sync_to_es",
                        data: data,
                        dataType: "json",
                        async: false,
                        error: function (xhr, status, err) {
                            alert(err);
                        },
                        success: function (data) {
                            debugger;
                            var json = JSON.parse(data);
                            if (json.code == 200) {
                                alert("添加成功");
                                $('#addColumn-wizard').modal('hide');
                                $('#mappingIdText').val("");
                                $('#columnNameText').val("");
                            } else {
                                alert(json.message);
                            }
                        }
                    });

                } else {
                    alert(data);
                }
            }
        });
    }

    $("#mediaSourceId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#targetMediaSourceId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#taskId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/mediaMapping/toAdd?random=" + Math.random());
        $("#add").show();
        $("#mainContentInner").hide();
    }
    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/mediaMapping/toEdit?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function toView(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#view").load("${basePath}/mediaMapping/toView?id=" + id + "&random=" + Math.random());
        $("#view").show();
        $("#mainContentInner").hide();
    }

    function reset() {
        $("#add").empty();
        $("#edit").empty();
        $("#view").empty();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/mediaMapping/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        msgAlarmListMyTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function dataCheck(id) {
        $("#form-check-mappingId").val(id);
        $('#dataCompare-wizard').modal('show');
    }

    function doDataCheck() {
        document.getElementById('form-check-fromDsCount').value = '';
        document.getElementById('form-check-toDsCount').value = '';
        document.getElementById('form-check-fromMinId').value = '';
        document.getElementById('form-check-toMinId').value = '';
        document.getElementById('form-check-fromMaxId').value = '';
        document.getElementById('form-check-toMaxId').value = '';

        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/dataCheck",
            dataType: "json",
            data: $("#check_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "fail") {
                    alert("校验失败！");
                } else {
                    var obj = JSON.parse(data);
                    document.getElementById('form-check-fromDsCount').value = obj.fromDsCount;
                    document.getElementById('form-check-toDsCount').value = obj.toDsCount;
                    document.getElementById('form-check-fromMinId').value = obj.fromMinId;
                    document.getElementById('form-check-toMinId').value = obj.toMinId;
                    document.getElementById('form-check-fromMaxId').value = obj.fromMaxId;
                    document.getElementById('form-check-toMaxId').value = obj.toMaxId;
                }
            }
        });
    }

    $('#basic-taskSyncMode').change(function () {
        debugger;
        $("#mediaSourceId").val(-1);

        var taskSyncMode = $('#basic-taskSyncMode').val();
        var data = "&taskSyncMode=" + taskSyncMode;
        $.ajax({
            type: "post",
            url: "${basePath}/mediaMapping/findMediaSourcesBySyncMode",
            async: true,
            dataType: "json",
            data: data,
            success: function (result) {
                if (result != null && result != '') {
                    if (result.mediaSourceList != null && result.mediaSourceList.length > 0) {
                        document.getElementById("mediaSourceId").innerHTML = "";
                        $("<option value=\"-1\">全部</option>").appendTo(".mediaSourceId");
                        for (var i = 0; i < result.mediaSourceList.length; i++) {
                            $("#mediaSourceId").append("<option value=" + "'" + result.mediaSourceList[i].id + "'" + ">" + result.mediaSourceList[i].name + "</option>");
                        }
                        $("#mediaSourceId").trigger("chosen:updated");
                    }
                }
            }
        });

    });


</script>