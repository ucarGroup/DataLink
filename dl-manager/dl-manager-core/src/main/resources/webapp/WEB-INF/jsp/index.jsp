<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>Datalink - Admin</title>
    <%@ include file="/WEB-INF/jsp/include.jsp" %>
</head>

<body class="no-skin">
<%@ include file="/WEB-INF/jsp/header.jsp" %>


<style>

    .changeHeight {
        display:block;
        list-style-type:none;
        margin:15px;
    }

</style>

<div class="main-container ace-save-state" id="main-container">
    <script type="text/javascript">
        try {
            ace.settings.loadState('main-container')
        } catch (e) {
        }
    </script>

    <%@ include file="/WEB-INF/jsp/sidebar.jsp" %>

    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="row">
                    <div class="tabbable">
                        <ul class="nav nav-tabs" role="tablist">

                        </ul>
                        <div class="tab-content" style="padding: 0px;">

                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="contactUsPage" class="modal">

        <div class="modal-dialog">
            <div class="modal-content" style="width:800px;height:400px">
                <div id="modal-wizard-container2">
                    <div class="modal-header" style="width:800px;height:70px">

                        <div class="modal-header no-padding">
                            <div class="table-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                    <span class="white">&times;</span>
                                </button>
                                联系我们
                            </div>
                        </div>
                    </div>

                    <div class="modal-body" style="width:800px;height:300px">

                        <div id="contact" class="page-body">
                            <h5>
                                <ul>
                                    <li class="changeHeight">
                                        李晓阳 : 13911972711  <a href="mailto:xiaoyang.li01@ucarinc.com">xiaoyang.li01@ucarinc.com</a>
                                    </li>
                                    <li class="changeHeight">
                                        宋文彬 : 13241643559  <a href="mailto:wenbin.song@ucarinc.com">wenbin.song@ucarinc.com</a>
                                    </li>
                                    <li class="changeHeight">
                                        王阳 : 15010085750  <a href="mailto:yang.wang09@ucarinc.com">yang.wang09@ucarinc.com</a>
                                    </li>
                                    <li class="changeHeight">
                                        代俊建 : 13717663266  <a href="mailto:junjian.dai@ucarinc.com">junjian.dai@ucarinc.com</a>
                                    </li>
                                    <li class="changeHeight">
                                        刘译繁 : 17611498126  <a href="mailto:yifan.liu02@ucarinc.com">yifan.liu02@ucarinc.com</a>
                                    </li>
                                    <li class="changeHeight">
                                        组邮箱 : <a href="mailto:tech_plat_data@ucarinc.com">tech_plat_data@ucarinc.com</a>
                                    </li>
                                </ul>
                            </h5>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="instruction-wizard" class="modal">

        <div class="modal-dialog">
            <div class="modal-content" style="width:800px;height:400px">
                <div id="modal-wizard-container">
                    <div class="modal-header" style="width:800px;height:70px">

                        <div class="modal-header no-padding">
                            <div class="table-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                                    <span class="white">&times;</span>
                                </button>
                                说明文档
                            </div>
                        </div>
                    </div>

                    <div class="modal-body" style="width:800px;height:300px">
                        <div id="document" class="page-body">
                            <h5>
                                <li class="changeHeight"><a href="http://wiki.10101111.com/pages/viewpage.action?pageId=107840774" target="_blank">使用说明
                                </a></li>
                                <li class="changeHeight"><a href="http://wiki.10101111.com/pages/viewpage.action?pageId=113410822" target="_blank">常见问题
                                </a></li>
                                <li class="changeHeight"><a href="http://wiki.10101111.com/pages/viewpage.action?pageId=107840764" target="_blank">架构介绍
                                </a></li>
                            </h5>
                        </div>


                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">
    $(function () {
        $(".nav-tabs").on("click", "[tabclose]", function (e) {
            id = $(this).attr("tabclose");
            closeTab(id);
        });

        addTabs('0', '主页', '/main', false);
    });

    var addTabs = function (id, title, url, closeable) {
        var baseUrl = window.location.protocol + '//' + window.location.host;
        var tabUrl = baseUrl + url;
        var tabId = "tab_" + id;

        $(".active").removeClass("active");

        //如果TAB不存在，创建一个新的TAB
        if (!$("#" + tabId)[0]) {
            var tabTitle =
                    ' <li role="presentation" id="tab_' + tabId + '">' +
                    ' <a href="#' + tabId + '" aria-controls="' + tabId + '" role="tab" data-toggle="tab">';
            if (closeable == true) {
                tabTitle += ' <i class="glyphicon glyphicon-remove" tabclose="' + tabId + '"></i>';
            }
            tabTitle += title + ' </a></li>';

            var tabContent =
                    '<div role="tabpanel" class="tab-pane" id="' + tabId + '">' +
                    '<iframe src="' + tabUrl + '" width="100%" height="8000px' +
                    '" frameborder="0" border="0" marginwidth="0" marginheight="0" scrolling="no" allowtransparency="yes">' +
                    '</iframe></div>';


            //加入TABS
            $(".nav-tabs").append(tabTitle);
            $(".tab-content").append(tabContent);
            observe(document.querySelector("#" + tabId));
        }
        //激活TAB
        $("#tab_" + tabId).addClass('active');
        $("#" + tabId).addClass("active");
    };
    var closeTab = function (id) {
        //如果关闭的是当前激活的TAB，激活他的前一个TAB
        if ($("li.active").attr('id') == "tab_" + id) {
            if ($("#tab_" + id).prev().length) {
                $("#tab_" + id).prev().addClass('active');
                $("#" + id).prev().addClass('active');
            } else if ($("#tab_" + id).next().length) {
                $("#tab_" + id).next().addClass('active');
                $("#" + id).next().addClass('active');
            }
        }
        //关闭TAB
        $("#tab_" + id).remove();
        $("#" + id).remove();
    };

    function observe(iframe) {
        //observe和onContentMutated，解决google浏览器下面，切换页签时滚动条消失的问题
        //参考链接：http://stackoverflow.com/questions/38557971/iframe-scrollbar-disappears-on-chrome-when-visibility-changes
        var observer = new MutationObserver(onContentMutated);
        var options = {
            attributes: true,
            childList: false,
            characterData: false,
            subtree: false,
            attributeFilter: ['class']
        };
        observer.observe(iframe, options);
    }

    function onContentMutated(mutations) {
        for (var i = 0; i < mutations.length; i++) {
            var m = mutations[i];

            var thisIsNowAnActiveTab = m.target.classList.contains('active');
            if (thisIsNowAnActiveTab) {
                // get the corresponding iframe and fiddle with its DOM

                var iframes = m.target.getElementsByTagName("iframe");
                if (iframes.length == 0) continue;
                var iframe = iframes[0];

                iframe.contentWindow.document.documentElement.style.overflow = 'hidden';

                // the timeout is to trigger Chrome to recompute the necessity of the scrollbars, which makes them visible again. Because the timeout period is 0 there should be no visible change to users.
                setTimeout(function (s) {
                    s.overflow = 'auto';
                }, 0, iframe.contentWindow.document.documentElement.style);
            }

            console.log(m.type);
        }
    }

    jQuery(function ($) {
        $("#logout").click(function () {
            $.ajax({
                type: "post",
                url: "${basePath}/userReq/logout",
                dataType: "json",
                data: "",
                async: true,
                success: function (data) {
                    if (data == "success") {
                        alert("logout成功");
                        window.location.href = "${basePath}/userReq/login";
                    }else if (data == "admin_success") {
                        alert("logout成功");
                         window.location.href = "${basePath}/admin";
                     } else {
                        alert(data);
                    }
                }
            });
        });
        $("#instruction").click(function () {
            $('#instruction-wizard').modal('show');
        });

        $("#contactUs").click(function () {
            $('#contactUsPage').modal('show');
        });

    });

</script>

</body>
</html>
