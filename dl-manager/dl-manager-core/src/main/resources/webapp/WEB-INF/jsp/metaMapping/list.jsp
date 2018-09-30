<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div id="add" class="main-container">
</div>
<div id="edit" class="main-container">
</div>
<div class="main-container" id="mainContentInner">
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <!-- 查询 选项 -->
                <div class="row">
                    <form class="form-horizontal">
                        <div class="row">
                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">源端类型</label>
                                <div class="col-sm-8">
                                    <select id="srcType" class="width-100 tag-input-style" id="srcType" style="width:100%">
                                        <option selected="selected" value="-1">全部</option>
                                        <option value="ElasticSearch">ElasticSearch</option>
                                        <option value="HBase">HBase</option>
                                        <option value="HDFS">HDFS</option>
                                        <option value="MySql">MySql</option>
                                        <option value="SqlServer">SqlServer</option>
                                        <option value="PostgreSql">PostgreSql</option>
                                        <option value="SDDL">SDDL</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group col-xs-3">
                                <label class="col-sm-4 control-label">目标端类型</label>
                                <div class="col-sm-8">
                                    ‍‍<select id="destType" class="width-100 tag-input-style" id="destType" style="width:100%">
                                    <option selected="selected" value="-1">全部</option>
                                    <option value="ElasticSearch">ElasticSearch</option>
                                    <option value="HBase">HBase</option>
                                    <option value="HDFS">HDFS</option>
                                    <option value="MySql">MySql</option>
                                    <option value="SqlServer">SqlServer</option>
                                    <option value="PostgreSql">PostgreSql</option>
                                    <option value="SDDL">SDDL</option>
                                </select>
                                </div>
                            </div>

                            <div class="col-xs-2">
                                <button type="button" id="search" class="btn btn-sm btn-purple">刷新</button>
                            </div>
                        </div><!-- end row -->
                    </form>
                </div>

                <div class="col-xs-12"  id="OperPanel">

                </div>

                <div class="row">
                    <table id="metaMappingTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>源端类型</td>
                            <td>目标端类型</td>
                            <td>源端字段类型</td>
                            <td>目标端字段类型</td>
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
    var metaMappingTable;
    $(".chosen-select").chosen();

    getButtons([
        {
            code:"002008002",
            html:'<div class="pull-left tableTools-container" style="padding-top: 10px;">'+
            '<p> <button class="btn btn-sm btn-info" onclick="toAdd();">新增</button> </p>'+
            '</div>'
        },
        {
            code:"002008007",
            html:'<div class="pull-left tableTools-container" style="padding-top: 10px;padding-left: 10px;">'+
            '<p> <button class="btn btn-sm btn-info" onclick="doReload();">重加载配置</button> </p>'+
            '</div>'
        }],$("#OperPanel"));

    metaMappingTable = $('#metaMappingTable').DataTable({
        "bAutoWidth": true,
        "serverSide" : false,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/metaMapping/initMapping",
            "data": function (d) {
        d.srcType = $("#srcType").val();
    d.destType = $("#destType").val();
    return JSON.stringify(d);
    },
    "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
    },
        "columns": [
            {"data": "srcMediaSourceType"},
            {"data": "targetMediaSourceType"},
            {"data": "srcMappingType"},
            {"data": "targetMappingType"},
            {
                "data": "id",
                "bSortable": false,
                "sWidth": "10%",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {

                    getButtons([
                        {
                            code:'002008004',
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
                            code:'002008006',
                            html:function() {
                                var str;
                                str = "<div class='radio'>" +
                                "<a href='javascript:doDelete(" + oData.id + ")' class='red'  title='删除'>" +
                                "<i class='ace-icon fa fa-trash-o bigger-130'></i>" + "</a>" +
                                "</div> &nbsp; &nbsp;"
                                return str;
                            }
                        }
                    ],$(nTd));

                }
            }
        ]
    });



    function reset() {
        $("#add").empty();
        $("#edit").empty();
    }


    function toAdd() {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#add").load("${basePath}/metaMapping/toAdd");
        $("#add").show();
        $("#mainContentInner").hide();
    }


    $("#srcType").change(function () {
        var type_name = $('#srcType').val();
        metaMappingTable.ajax.reload();
    })

    $("#destType").change(function () {
        var type_name = $('#destType').val();
        metaMappingTable.ajax.reload();
    })

    $("#search").click(function () {
        metaMappingTable.ajax.reload();
    })

    function toEdit(id) {
        reset();//每次必须先reset，把已开界面资源清理掉
        $("#edit").load("${basePath}/metaMapping/toEdit?id=" + id + "");
        $("#edit").show();
        $("#mainContentInner").hide();
    }

    function doDelete(id) {
        if (confirm("确定要删除数据吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/metaMapping/doDelete?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("删除成功！");
                        metaMappingTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

    function doReload() {
        if (confirm("确定要重新加载配置吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/metaMapping/doReload",
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("重新加载成功！");
                        metaMappingTable.ajax.reload();
                    } else {
                        alert(data);
                    }
                }
            });
        }
    }

</script>