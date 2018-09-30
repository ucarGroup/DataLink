<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<!--css link-->
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap.min.css"/>
<link rel="stylesheet" href="${basePath}/assets/font-awesome/4.5.0/css/font-awesome.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap-datepicker3.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap-timepicker.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/daterangepicker.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/chosen.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap-datetimepicker.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap-colorpicker.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/fonts.googleapis.com.css" />
<link rel="stylesheet" href="${basePath}/assets/css/ace.min.css" class="ace-main-stylesheet" id="main-ace-style"/>
<link rel="stylesheet" href="${basePath}/assets/css/ace-skins.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/ace-rtl.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/select2.min.css" />
<link rel="stylesheet" href="${basePath}/assets/css/bootstrap-duallistbox.min.css" />


<!--js source-->
<script src="${basePath}/assets/js/ace-extra.min.js"></script>
<script src="${basePath}/assets/js/jquery-2.1.4.min.js"></script>
<script src="${basePath}/assets/js/bootstrap.min.js"></script>
<script src="${basePath}/assets/js/bootstrap-datepicker.min.js"></script>
<script src="${basePath}/assets/js/bootstrap-timepicker.min.js"></script>
<script src="${basePath}/assets/js/moment.min.js"></script>
<script src="${basePath}/assets/js/daterangepicker.min.js"></script>
<script src="${basePath}/assets/js/bootstrap-datetimepicker.min.js"></script>
<script src="${basePath}/assets/js/bootstrap-colorpicker.min.js"></script>
<script src="${basePath}/assets/js/jquery.dataTables.min.js"></script>
<script src="${basePath}/assets/js/jquery.dataTables.bootstrap.min.js"></script>
<script src="${basePath}/assets/js/dataTables.buttons.min.js"></script>
<script src="${basePath}/assets/js/buttons.flash.min.js"></script>
<script src="${basePath}/assets/js/buttons.html5.min.js"></script>
<script src="${basePath}/assets/js/buttons.print.min.js"></script>
<script src="${basePath}/assets/js/buttons.colVis.min.js"></script>
<script src="${basePath}/assets/js/dataTables.select.min.js"></script>
<script src="${basePath}/assets/js/jquery-ui.custom.min.js"></script>
<script src="${basePath}/assets/js/jquery.easypiechart.min.js"></script>
<script src="${basePath}/assets/js/jquery.sparkline.index.min.js"></script>
<script src="${basePath}/assets/js/chosen.jquery.min.js"></script>
<script src="${basePath}/assets/js/bootbox.js"></script>
<script src="${basePath}/assets/js/ace-elements.min.js"></script>
<script src="${basePath}/assets/js/ace.min.js"></script>
<script src="${basePath}/assets/js/jquery.ui.touch-punch.min.js"></script>
<script src="${basePath}/assets/js/jquery.flot.min.js"></script>
<script src="${basePath}/assets/js/jquery.flot.pie.min.js"></script>
<script src="${basePath}/assets/js/jquery.flot.resize.min.js"></script>
<script src="${basePath}/assets/common/common.js"></script>
<script src="${basePath}/assets/js/select2.min.js"></script>
<script src="${basePath}/assets/js/jquery.bootstrap-duallistbox.min.js"></script>

<script type="text/javascript">

    jQuery(function($){
        // 备份jquery的ajax方法
        var _ajax=$.ajax;
        // 重写ajax方法，
        $.ajax=function(opt){
            var _error = opt && opt.error || function(a, b){};
            var _opt = $.extend(opt, {
                error:function(data, textStatus){
                    // 如果后台将请求重定向到了登录页，则data里面存放的就是登录页的源码，这里需要判断是登录页的证据(标记)
                    // 这儿的{49cdd9d3-a473-4aef-8190-5dc5bf7b3984}是用Guid.NewGuid()生成的，是一个标识，可以随意，但是Login页面也要有
                    if(data.responseText.indexOf('49cdd9d3-a473-4aef-8190-5dc5bf7b3984') != -1){
                        top.location.href= "${basePath}/userReq/login";
                        return;
                    }
                    _error(data, textStatus);
                }
            });
            return _ajax(_opt);
        };
    });
    //前端校验角色的按钮显示权限
    function checkIsAuth(code) {
        var authValue = "";
        var authCode = "";
        var cookieArr = document.cookie.split("; ");
        for(var i = 0;i < cookieArr.length;i ++){
            var pos = cookieArr[i].indexOf("=");
            //然后获取=前面的name
            var c_name = cookieArr[i].substring(0,pos);
            //获取=后面的value
            var c_value = cookieArr[i].substring(pos+2,cookieArr[i].length-1);
            if(c_name == "roleAuth"){
                authValue = c_value;
                authCode = authValue.split(",");
            }
        }

        if(authCode.indexOf(code)>=0) {
            return true;
        } else{
            return false;
        }
    }
    function getButtons(buttons,$el) {
        var newButtons=[];
        for(var i=0;i<buttons.length;i++) {
            var button = buttons[i];
            if(checkIsAuth(button.code)) {
                if(typeof button.html == 'function') {
                    newButtons.push(button.html());
                } else {
                    newButtons.push(button.html);
                }
            }
        }
        $el.html(newButtons.join(""));
    }

</script>


