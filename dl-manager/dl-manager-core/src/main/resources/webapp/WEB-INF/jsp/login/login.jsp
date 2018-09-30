<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.request.contextPath }"/>

<!DOCTYPE html>
<!--{49cdd9d3-a473-4aef-8190-5dc5bf7b3984}-->
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta charset="utf-8"/>
    <title>Login Page - Datalink集中管理平台</title>

    <meta name="description" content="User login page"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
    <%@ include file="/WEB-INF/jsp/include.jsp" %>
    <script type="text/javascript">
        var CAR_PATH = '${basePath}';
    </script>
</head>

<body class="login-layout blur-login"
      style="background-size:100%;background-image: url(${basePath}/assets/images/wallpaper/1.jpg);background-repeat:no-repeat">
<div class="main-container">
    <div class="main-content">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1">
                <div class="login-container">
                    <div class="center">
                        <h1>
                            <i class="ace-icon fa fa-automobile light-blue"></i>
                            <span class="red"></span>
                            <span class="white" id="id-text2">Datalink集中管理平台</span>
                        </h1>
                    </div>

                    <div class="space-6"></div>

                    <div class="position-relative">
                        <div id="login-box" class="login-box visible widget-box no-border">
                            <div class="widget-body">
                                <div class="widget-main">
                                    <h4 class="header blue lighter bigger">
                                        <i class="ace-icon fa fa-coffee green"></i>
                                        登录
                                    </h4>

                                    <div class="space-6"></div>

                                    <form id="login-form">
                                        <fieldset>
                                            <label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="text" id="loginEmail" name="loginEmail"
                                                                   class="form-control" placeholder="LoginEmail"/>
															<i class="ace-icon fa fa-user"></i>
														</span>
                                            </label>

                                            <label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="password" id="password" name="password"
                                                                   class="form-control" placeholder="Password"/>
															<i class="ace-icon fa fa-lock"></i>
														</span>
                                            </label>

                                            <div class="space"></div>

                                            <div class="space"></div>
                                            <div class="clearfix" align="center">
                                                <button type="button" id="login"
                                                        class="width-35 btn btn-sm btn-primary">
                                                    <i class="ace-icon fa fa-key"></i>
                                                    <span class="bigger-110">Login</span>
                                                </button>
                                            </div>

                                            <div class="space-4"></div>
                                        </fieldset>
                                    </form>
                                </div>
                                <!-- /.widget-main -->

                            </div>
                            <!-- /.widget-body -->
                        </div>
                        <!-- /.login-box -->
                    </div>
                    <!-- /.position-relative -->

                    <div class="center">
                        <h4 class="light-blue" id="id-company-text">&copy; 神州优车</h4>
                    </div>
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
<script type="text/javascript">
    //you don't need this, just used for changing background
    jQuery(function ($) {

        $("#login").click(function () {
            var loginEmail = $("#loginEmail").val();
            if (loginEmail == '') {
                alert("用户名不能为空");
                return false;
            }
            var password = $("#password").val();
            if (password == '') {
                alert("密码不能为空");
                return false;
            }
            $.ajax({
                type: 'post',
                url: CAR_PATH + '/userReq/doLogin',
                data: {
                    loginEmail: loginEmail,
                    password: password
                },
                cache: false,
                dataType: 'json',
                success: function (data) {
                    if (data == "success") {
                        window.location.href = CAR_PATH + "/";
                    } else if (data == "toRegister") {
                        window.location.href = CAR_PATH + "/userReq/toRegister";
                    } else {
                        alert(data);
                    }
                }
            });
        });

    });

    if (top != window)
        top.location.href = window.location.href;
</script>
</body>
</html>
