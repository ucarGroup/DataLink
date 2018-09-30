<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<style>
    .showTable {
        display: block !important;
        position: absolute;
        left: -20000px;
        top: -20000px;
        visibility: hidden;
    }
</style>
<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <div class="tabbable">
                <input type="hidden" name="id" id="id" value="${roleInfo.id}">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                    <li>
                        <a data-toggle="tab" href="#authorityId">权限配置</a>
                    </li>
                </ul>

                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <jsp:include page="roleBasic.jsp"/>
                    </div>

                    <!--权限配置-->
                    <div id="authorityId" class="tab-pane showTable">
                        <jsp:include page="roleAuthority.jsp"/>
                    </div>

                </div>
            </div>
        </div>

    </div>
    <script>
        $("#myTab4 li").mouseover(function () {
            $("#authorityId").removeClass("showTable").css("visibility", "visible");
        })
    </script>
    <!-- /.page-content -->
</div>
