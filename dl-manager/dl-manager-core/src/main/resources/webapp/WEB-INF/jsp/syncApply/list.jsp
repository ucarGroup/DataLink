<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div id="process" class="main-container">
</div>
<div id="addMediaMapping" class="modal">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="row">
                    <form class="form-horizontal">
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">申请状态</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="status"
                                        style="width:100%">
                                    <option value="">全部</option>
                                    <c:forEach items="${SyncApplyStatusList}" var="item">
                                        <option value="${item}">${item}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">同步类型</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="type" name="type"
                                        style="width:100%">
                                    <option value="" selected="selected">全部</option>
                                    <option value="Full">全量</option>
                                    <option value="Increment">增量</option>
                                </select>
                            </div>
                        </div>

                        <c:if test="${roleType == 'SUPER' || roleType == 'APPROVER'}">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">申请人</label>

                                <div class="col-sm-8">
                                    <select class="width-100 chosen-select" id="applyUserId" style="width:100%">
                                        <option value="">全部</option>
                                        <c:forEach items="${applyUserIdList}" var="bean">
                                            <option value="${bean.id}">${bean.userName} </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </c:if>
                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>
                    </form>
                </div>

                <div class="col-xs-12" id="OperPanel">

                </div>

                <div class="row">
                    <table id="syncApplyTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>申请ID</td>
                            <td>申请状态</td>
                            <td>申请类型</td>
                            <td>全量初始化</td>
                            <td>源库名称</td>
                            <td>目标库名称</td>
                            <td>申请人</td>
                            <td>创建时间</td>
                            <td>操作</td>
                        </tr>
                        </thead>
                    </table>
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
        code: "003001002",
        html: '<div class="pull-left tableTools-container" style="padding-top: 10px;">' +
        '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增申请</button> </p>' +
        '</div>'
    }], $("#OperPanel"));

    $("#addMediaMapping").on('hide.bs.modal', function () {
        msgAlarmListMyTable.ajax.reload();
    });

    msgAlarmListMyTable = $('#syncApplyTable').DataTable({
        processing: true,
        filter: true,
        ordering: true,
        "sInfoEmpty": "No entries to show",
        serverSide: true,//开启服务器模式:启用服务器分页
        paging: true,//是否分页
        pagingType: "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "ajax": {
            "url": "${basePath}/sync/apply/initSyncApply",
            "data": function (d) {
                d.applyStatus = $("#status").val();
                d.applyUserId = $("#applyUserId").val();
                d.applyType = $("#type").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "applyStatus"},
            {"data": "applyType"},
            {
                "data": "isInitialData",
                render: function (data, type, row) {
                    if (data == 1) {
                        return "是";
                    } else {
                        return "否";
                    }
                }
            },
            {"data": "srcMediaSourceName"},
            {"data": "targetMediaSourceName"},
            {"data": "applyUserName"},
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
            "aTargets": [8],
            "mData": null,
            "bSortable": false,
            "bSearchable": false,
            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
//            "mRender": function (data, type, full) {
                getButtons([
                    {
                        code: '003001003',
                        html: function () {
                            var str;
                            if (oData.applyStatus == "SUBMITTED" || oData.applyStatus == "REJECTED" || oData.loginRoleType == "SUPER") {
                                str = "<div class='radio'>" +
                                "<a href='javascript:toEdit(" + oData.id + ")' class='blue'  title='修改'>" +
                                "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            }
                            return str;
                        }
                    },
                    {
                        code: '003001008',
                        html: function () {
                            var str;
                            if (oData.applyStatus == "SUBMITTED" || oData.applyStatus == "APPROVED") {
                                str = "<div class='radio'>" +
                                "<a href='javascript:doCancel(" + oData.id + ")' class='red'  title='撤销'>" +
                                "<i class='ace-icon fa fa-undo bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            }
                            return str;
                        }
                    },
                    {
                        code: '003001004',
                        html: function () {
                            var str;
                            if (oData.canApprove == true || (oData.loginRoleType == "SUPER" && oData.applyStatus == "SUBMITTED")) {
                                str = "<div class='radio'>" +
                                "<a href='javascript:toApprove(" + oData.id + ")' class='green'  title='审批'>" +
                                "<i class='ace-icon fa fa-pencil-square-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            }
                            return str;
                        }
                    },
                    /*{
                     code:'003001011',
                     html:function() {
                     var str;
                     if (oData.loginRoleType == "SUPER" && oData.applyStatus == "EXECUTING" && oData.isAutoKeeper == false) {
                     str = "<div class='radio'>" +
                     "<a href='javascript:doCreateJobConfig(" + oData.id + ")' class='green'  title='生成job'>" +
                     "<i class='ace-icon fa fa-plus bigger-130'></i>" + "</a>" +
                     "</div> &nbsp; &nbsp;"
                     }
                     return str;
                     }
                     },*/
                    {
                        code: '003001012',
                        html: function () {
                            var str;
                            if (oData.loginRoleType == "SUPER" && oData.applyType == "Increment" && (oData.applyStatus == "INCREMENT_EXECUTING" || oData.applyStatus == "INCREMENT_FAILED")) {
                                str = "<div class='radio'>" +
                                "<a href='javascript:toAddMediaMapping(" + oData.id + ")' class='green'  title='配置映射'>" +
                                "<i class='ace-icon fa fa-plus-circle bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            }
                            return str;
                        }
                    },
                    {
                        code: '003001009',
                        html: function () {
                            var str;
                            if (oData.loginRoleType == "SUPER" && (oData.applyStatus == "INCREMENT_FAILED" || oData.applyStatus == "FULL_FAILED" || oData.applyStatus == "INCREMENT_EXECUTING" || oData.applyStatus == "FULL_EXECUTING")) {
                                str = "<div class='radio'>" +
                                "<a href='javascript:toProcess(" + oData.id + ")' class='blue'  title='处理'>" +
                                "<i class='ace-icon fa fa-cog bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                            }
                            return str;
                        }
                    },
                    {
                        code: '003001014',
                        html: function () {
                            var str;
                            str = "<div class='radio'>" +
                            "<a href='javascript:toDetail(" + oData.id + ")' class='grey'  title='详情'>" +
                            "<i class='ace-icon fa fa-file-text-o bigger-130'></i>" + "</a>" +
                            "</div> &nbsp; &nbsp;"
                            return str;
                        }
                    }
                ], $(nTd));


            }
        }]
    });

    $("#status").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#applyUserId").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#type").change(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    $("#search").click(function () {
        msgAlarmListMyTable.ajax.reload();
    })

    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/sync/apply/toAdd?random=" + Math.random());
        $("#add").show();
        $("#mainContentInner").hide();
    }
    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/sync/apply/toEdit?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function toApprove(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/sync/apply/toApprove?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function toProcess(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#process").load("${basePath}/sync/apply/toProcess?id=" + id + "&random=" + Math.random());
        $("#process").show();
        $("#mainContentInner").hide();
    }

    function toDetail(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/sync/apply/toDetail?id=" + id + "&random=" + Math.random());
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function doCreateJobConfig(id) {
        if (confirm("确定要生成申请ID=" + id + "的所有job吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/sync/apply/doCreateJobConfig?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("job创建成功！");
                        msgAlarmListMyTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function toAddMediaMapping(id) {
        var addMediaMappingDiv = $("#addMediaMapping");
        addMediaMappingDiv.empty();
        addMediaMappingDiv.load("${basePath}/sync/apply/toAddMediaMapping?id=" + id + "&random=" + Math.random());
        addMediaMappingDiv.modal('show');
    }

    function doCancel(id) {
        if (confirm("确定要撤销该同步申请吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/sync/apply/doCancel?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("撤销成功！");
                        msgAlarmListMyTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function reset() {
        $("#add").empty();
        $("#edit").empty();
        $("#process").empty();
    }

</script>