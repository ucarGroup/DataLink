<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTabMonitor">
                    <li class="active">
                        <a data-toggle="tab" href="#jvmMonitor">JVM监控</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#systemMonitor">系统监控</a>
                    </li>
                </ul>

                <div class="tab-content" style="border: 0px">
                    <!--JVM监控-->
                    <div id="jvmMonitor" class="tab-pane in active">
                        <jsp:include page="jvmMonitor.jsp"/>
                    </div>

                    <!--系统监控-->
                    <div id="systemMonitor" class="tab-pane">
                        <jsp:include page="systemMonitor.jsp"/>
                    </div>

                </div>
            </div>
        </div>
        <div class="clearfix" align="center">
            <button class="btn" type="reset" onclick="back2Main();">
                返回
                <i class="ace-icon fa fa-undo bigger-110"></i>
            </button>
        </div>
    </div>
    <!-- /.page-content -->
</div>

<script type="text/javascript">
    function back2Main() {
        $("#workerMonitor").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
