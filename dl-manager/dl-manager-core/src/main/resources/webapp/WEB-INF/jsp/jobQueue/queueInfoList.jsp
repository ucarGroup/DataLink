<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


<div class="main-container" >
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <!-- 查询 选项 -->
                <div class="row">
                    <form class="form-horizontal">

                        <button class="btn" type="reset" onclick="back2Main();">
                            返回
                            <i class="ace-icon fa fa-undo bigger-110"></i>
                        </button>

                        <!--
                        <div class="col-xs-2">
                            <button type="flush_button" id="queuInfoList_search" class="btn btn-sm btn-purple">刷新</button>
                        </div>
                        -->
                    </form>
                </div>




                <div class="row">
                    <table id="jobRunQueueTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>id</td>
                            <td>队列名称</td>
                            <td>创建队列人的邮箱</td>
                            <td>队列状态</td>
                            <td>任务失败中断执行</td>
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
    var jobQueueListTable;
    //$(".chosen-select").chosen();

    $("#queuInfoList_search").click(function () {
        jobRunQueueTable.ajax.reload();
    })

    function back2Main() {
        $("#showQueueInfoList").hide();
        $("#mainContentInner").show();
        jobRunQueueTable.ajax.reload();
    }




    jobRunQueueTable = $('#jobRunQueueTable').DataTable({
        "bAutoWidth": true,
        "serverSide": true,//开启服务器模式:启用服务器分页
        "paging": true,//是否分页
        "pagingType": "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite": "true",
        "sScrollX": "100%",
        "ajax": {
            "url": "${basePath}/jobQueue/initJobQueueInfoList",
            "data": function (d) {
                //d.srcName = $("#srcName").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "queueName"},
            {"data": "mail"},
            {"data": "queueState"},
            {"data": "failToStop"},
            {"data": "createTime"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    getButtons([
                        {
                            code: '005003002',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                        "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        },
                        {
                            code: '005003002',
                            html: function () {
                                var str;
                                str = "<div class='radio'>" +
                                        "<a href='javascript:toEdit(" + oData.id + ")' class='blue'  title='编辑'>" +
                                        "<i class='ace-icon fa fa-pencil bigger-130'></i>" + "</a>" +
                                        "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        }
                    ], $(nTd));


                }
            }
        ]
    });

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobQueue/doDeleteJobQueueInfo?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        jobRunQueueTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }


    function toEdit(id) {
        $.ajaxSetup({cache: true});
        $("#edit").load("${basePath}/jobQueue/editJobQueueInfo?id="+id);
        $("#edit").show();
        $("#showQueueInfoList").hide();
    }




</script>