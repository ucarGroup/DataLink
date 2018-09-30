<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="<%=request.getContextPath()%>"/>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta charset="utf-8"/>
    <title>forbidden Page - Datalink集中管理平台</title>

    <meta name="description" content="User login page"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
    <%@ include file="/WEB-INF/jsp/include.jsp" %>
</head>

<body class="login-layout blur-login">
<div class="main-container">
    <div class="main-content">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1">
                <div class="center">
                    <h1>
                        <i class="ace-icon fa fa-automobile blue"></i>
                        <span class="red"></span>
                        <span class="light-blue" id="id-text2">禁止访问</span>
                    </h1>
                </div>

                <div class="space-6"></div>

                <div class="center">
                    <h4 class="light-blue" id="id-company-text">&copy; 神州优车</h4>
                </div>
            </div>
            <!-- /.col -->
        </div>
        <!-- /.row -->
    </div>
    <!-- /.main-content -->
</div>
<!-- /.main-container -->
<!-- inline scripts related to this page -->
</body>
</html>
