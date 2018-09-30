<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#readerId">Reader配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#writerId">Writer配置</a>
                    </li>
                </ul>

                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <jsp:include page="taskBasic.jsp"/>
                    </div>

                    <!--Reader配置-->
                    <div id="readerId" class="tab-pane">
                        <jsp:include page="mysqlTaskReader.jsp"/>
                    </div>

                    <!--Writer配置-->
                    <div id="writerId" class="tab-pane">
                        <jsp:include page="taskWriter.jsp"/>
                    </div>
                </div>
            </div>
        </div>
        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn btn-info" type="button" onclick="add();">
                    <i class="ace-icon fa fa-check bigger-110"></i>
                    增加
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
</div>

<script type="text/javascript">
    function back2Main() {
        $("#mysqlTaskAdd").empty();
        $("#main-container").show();
    }

    function add() {
        var obj = {};
        obj.taskBasicInfo = getBasicObj();
        obj.mysqlReaderParameter = getMysqlReaderObj();
        obj.writerParameterMap = getWritersObj();

        $.ajax({
            type: "post",
            url: "${basePath}/mysqlTask/doAddMysqlTask",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(obj),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("添加成功！");
                    back2Main();
                    oTable.ajax.reload();
                } else {
                    alert(data);
                }
            }
        });
    }
</script>
