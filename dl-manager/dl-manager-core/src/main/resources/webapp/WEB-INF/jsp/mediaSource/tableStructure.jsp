<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<div class="main-container" >
    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="row">
                    <form class="form-horizontal">
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">数据源类型</label>

                            <div class="col-sm-8">
                                <select class="width-100 chosen-select" id="dbType"
                                        style="width:100%">
                                    <option selected="selected" value="-1">全部</option>
                                    <option value="MYSQL">MYSQL</option>
                                    <option value="SQLSERVER">SQLSERVER</option>
                                    <option value="ORACLE">ORACLE</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">数据源名称</label>

                            <div class="col-sm-8">
                                <input id="dbName" type="text" style="width:100%;">
                            </div>
                        </div>
                        <div class="form-group col-xs-3">
                            <label class="col-sm-4 control-label">表名称</label>
                            <div class="col-sm-8">
                                <input id="tableName" type="text" style="width:100%;">
                            </div>
                        </div>
                        <div class="col-xs-2">
                            <button type="button" id="search" class="btn btn-sm btn-purple">查询</button>
                        </div>
                    </form>
                </div>

                <div class="col-xs-12" id="OperPanel">

                </div>
                <div class="row">
                    <table id="mediaSourceTable" class="table table-striped table-bordered table-hover"
                           style="text-align: left;width:100%">
                        <thead>
                        <tr>
                            <td>字段名称</td>
                            <td>类型</td>
                            <td>是否主键</td>
                            <td>长度</td>
                            <td>精度</td>
                            <td>备注</td>
                        </tr>
                        </thead>
                        <tbody id="columnBody"></tbody>
                    </table>
                </div>

            </div>
        </div>
        <!-- /.page-content -->

    </div>
    <div class="clearfix form-actions">
        <div class="col-md-offset-5 col-md-7">
            <button class="btn" type="reset" onclick="back2Main();">
                返回
                <i class="ace-icon fa fa-undo bigger-110"></i>
            </button>
        </div>
    </div>
</div>
<script type="text/javascript">

    $("#search").click(function () {
       // msgAlarmListMyTable.ajax.reload();
        var dbType = $("#dbType").val();
        var dbName = $("#dbName").val();
        var tableName = $("#tableName").val();
        $.ajax({
            type: "post",
            url: "${basePath}/metaData/tableInfo?dbName="+dbName+"&dbType="+dbType+"&tableName="+tableName,
            dataType: 'json',
            data: $("#add_form").serialize(),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data.code == 200) {
                    $("#columnBody").html("");
                    var columnMetaInfos = data.data.columnMetaInfos;
                    if(columnMetaInfos != null) {
                        for (var i =0 ;i <columnMetaInfos.length;i++) {
                            var columnMeta = columnMetaInfos[i];
                            var isPrimaryKey = "false";
                            if(columnMeta.isPrimaryKey!=undefined) {
                                isPrimaryKey = columnMeta.isPrimaryKey;
                            }
                            var str = "<tr>" +
                                "<td>"+columnMeta.name+"</td>" +
                                " <td>"+columnMeta.type+"</td>" +
                                "<td>"+isPrimaryKey+"</td>" +
                                "<td>"+columnMeta.length+"</td>" +
                                "<td>"+columnMeta.decimalDigits+"</td>" +
                                "<td>"+columnMeta.columnDesc+"</td>" +
                                "</tr>";
                            $("#columnBody").append(str);
                        }
                    }
                } else{
                    alert("没有查到表信息");
                }
            }
        });

    })

    function reset() {
        $("#add").empty();
        $("#edit").empty();
        $("#addQuick").empty();
    }

    function back2Main() {
        $("#tableStructure").hide();
        $("#mainContentInner").show();
    }

</script>