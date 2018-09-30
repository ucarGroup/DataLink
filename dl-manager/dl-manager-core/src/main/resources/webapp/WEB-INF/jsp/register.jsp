<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.request.contextPath }"/>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta charset="utf-8"/>
    <title>用户注册</title>

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

                    <div class="space-6"></div>


                        <div class="modal-dialog">
                            <div class="modal-content" style="width:500px;height:400px">
                                <div id="modal-wizard-container">
                                    <div class="modal-header" style="width:500px;height:70px">

                                        <div class="modal-header no-padding">
                                            <div class="table-header">
                                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                                    <span class="white">&times;</span>
                                                </button>
                                                用户注册
                                            </div>
                                        </div>
                                    </div>

                                    <div class="modal-body" style="width:500px;height:300px">
                                        <form id="check_form" class="form-horizontal" role="form">
                                            <input type="hidden" name="mappingId" id="form-check-mappingId"/>

                                            <div class="form-group">
                                                <div class="col-sm-10">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="userName">用户名</label>

                                                    <div class="col-sm-8">
                                                        <input type="text" id="userName" name="userName"
                                                               vplaceholder="" class="col-sm-12"/>
                                                    </div>
                                                </div>

                                            </div>
                                            <div class="form-group">
                                                <div class="col-sm-10">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="ucarEmail">邮箱前缀</label>

                                                    <div class="col-sm-8">
                                                        <input type="text" id="ucarEmail" name="ucarEmail"
                                                               placeholder="" class="col-sm-12"/>
                                                    </div>
                                                </div>

                                            </div>
                                            <div class="form-group">
                                                <div class="col-sm-10">
                                                    <label class="col-sm-4 control-label no-padding-right"
                                                           for="phone">手机号</label>

                                                    <div class="col-sm-8">
                                                        <input type="text" id="phone" name="phone"
                                                               placeholder="" class="col-sm-12"/>
                                                    </div>
                                                </div>

                                            </div>

                                            <div class="space"></div>
                                            <div class="clearfix" align="center">
                                                <button type="button" id="register"
                                                        class="width-35 btn btn-sm btn-primary">
                                                    <i class="ace-icon fa fa-key"></i>
                                                    <span class="bigger-110">注册</span>
                                                </button>
                                            </div>

                                            <div class="space"></div>

                                        </form>

                                    </div>
                                </div>
                            </div>
                        </div>


                </div>
            </div>
        </div>
    </div>
    <!-- /.main-content -->
</div>
<!-- /.main-container -->
<!-- inline scripts related to this page -->
<script type="text/javascript">

    jQuery(function ($) {

        $("#register").click(function () {
            var userName = $("#userName").val();
            if (userName == '') {
                alert("用户名不能为空");
                return false;
            }
            var ucarEmail = $("#ucarEmail").val();
            if (ucarEmail == '') {
                alert("邮箱不能为空");
                return false;
            }
            var phone = $("#phone").val();
            if (phone == '') {
                alert("手机号不能为空");
                return false;
            }
            var r = /^1[3|4|5|7|8]\d{9}$/;
            var flag = r.test(phone);
            if (!flag) {
                alert("手机号格式不正确");
                return;
            }
            $.ajax({
                type: 'post',
                url: CAR_PATH + '/userReq/doRegister',
                data: {
                    userName: userName,
                    ucarEmail: ucarEmail,
                    phone: phone
                },
                cache: false,
                dataType: 'json',
                success: function (data) {
                    if (data == "success") {
                        window.location.href = CAR_PATH + "/userReq/login";
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
