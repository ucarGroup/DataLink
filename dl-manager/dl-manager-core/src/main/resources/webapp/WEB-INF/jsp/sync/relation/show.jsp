<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container ace-save-state" id="main-container">
    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="row">
                    <div class="col-xs-12 pull-left">
                        <div class="row">
                            <form class="form-horizontal">
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">数据源</label>

                                    <div class="col-sm-8">
                                        <select class="width-20 chosen-select" id="mediaSourceId"
                                                style="width:100%">
                                            <option value="-1">全部</option>
                                            <c:forEach items="${mediaSourceList}" var="item">
                                                <option value="${item.id}">${item.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group col-xs-3">
                                    <label class="col-sm-4 control-label">数据表</label>

                                    <div class="col-sm-8">
                                        <input id="mediaName" type="text" style="width:100%;">
                                    </div>
                                </div>
                                <div class="col-xs-2" id="OperPanel">

                                </div>
                            </form>
                        </div>
                        <div class="col-sm-12 form-group">
                            <span id="span1"> </span>

                            <p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${basePath}/assets/echarts2/echarts.js"></script>
<script type="text/javascript">
    $(document).ready(function () {
        $('.chosen-select').chosen({allow_single_deselect: true, width: "100%"});
    });

    getButtons([{
        code:"003003001",
        html:'<div>'+
        '<p> <button type="button" id="checkBtn" class="btn btn-sm btn-purple">查询</button> </p>'+
        '</div>'
    }],$("#OperPanel"));

    require.config({
        paths: {
            'echarts': '${basePath}/assets/echarts2',
            'jquery': '${basePath}/assets/js/jquery-1.11.3.min'
        }
    });

    require(
            [
                'jquery',
                'echarts',
                'echarts/chart/chord',
                'echarts/chart/tree',
                'echarts/chart/force'
            ],

            function ($, ec) {
                var button = document.getElementById('checkBtn');
                if(button != null) {
                    button.onclick = function (obj) {
                        check();
                    };
                }

                function check() {
                    if ($.trim($('#mediaName').val()) == '') {
                        alert('数据表为必输项!');
                        return false;
                    }

                    //将上次的检测结果清空
                    var divs = document.getElementById("span1").childNodes;
                    var length = divs.length;
                    for (var i = 0; i < length; i++) {
                        document.getElementById("span1").removeChild(
                                divs[0]);
                    }

                    //构造查询对象
                    var obj = {};
                    obj.mediaSourceId = $("#mediaSourceId").val();
                    obj.mediaName = $("#mediaName").val();

                    $.ajax({
                        type: "post",
                        url: "${basePath}/sync/relation/getTrees",
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        data: JSON.stringify(obj),
                        async: false,
                        error: function (xhr, status, err) {
                            alert(err);
                        },
                        success: function (data) {
                            if (data.length > 0) {
                                showTree(data);
                            } else {
                                showMsg("未配置同步.");
                            }
                        }
                    });
                }

                function showMsg(msg) {
                    var mydiv = document.createElement("div");
                    mydiv.setAttribute("id", "itemdiv");
                    mydiv.setAttribute("name", "itemdiv");
                    mydiv.innerHTML = "<p style=\"text-indent:0;font-size:15px;\">"
                            + msg + "</p>";
                    mydiv.style.width = "1200px";
                    mydiv.style.height = "500px";
                    mydiv.style.textAlign = "center";
                    mydiv.style.border = "1px solid lightgray";
                    document.getElementById("span1").appendChild(mydiv);
                }

                function showTree(data) {
                    for (var i = 0; i < data.length; i++) {
                        var obj = data[i];
                        var option = buildOption();
                        option.series[0].data = getSeriedata(obj);

                        //同步关系树
                        var mydivChart = document.createElement("div");
                        mydivChart.setAttribute("id", "divChart" + i);
                        mydivChart.setAttribute("name", "divChart" + i);
                        mydivChart.style.width = "1200px";
                        mydivChart.style.height = "500px";
                        mydivChart.style.border = "1px solid lightgray";
                        document.getElementById("span1").appendChild(mydivChart);
                        var myChart = ec.init(mydivChart);
                        myChart.setOption(option, true);
                    }
                }

                function buildOption() {
                    option = {
                        title: {
                            text: ''
                        },
                        series: [{
                            name: '检测树',
                            type: 'tree',
                            orient: 'horizontal',
                            rootLocation: {x: 100, y: 250},
                            nodePadding: 20,
                            layerPadding: 340,
                            roam: true,
                            symbolSize: 17,
                            itemStyle: {
                                normal: {
                                    color: '#4883b4',
                                    label: {
                                        show: true,
                                        position: 'right',
                                        formatter: "{b}",
                                        textStyle: {
                                            color: '#000',
                                            fontSize: 14
                                        }
                                    },
                                    lineStyle: {
                                        color: '#ccc',
                                        type: 'curve' // 'curve'|'broken'|'solid'|'dotted'|'dashed'

                                    }
                                },
                                emphasis: {
                                    color: '#4883b4',
                                    label: {
                                        show: true
                                    },
                                    borderWidth: 8
                                }
                            },
                            data: []
                        }]
                    };
                    return option;
                }

                function getSeriedata(obj) {
                    var arr = [];
                    arr.push(obj);
                    return arr;
                }

                var BUILTIN_OBJECT = {
                    '[object Function]': 1,
                    '[object RegExp]': 1,
                    '[object Date]': 1,
                    '[object Error]': 1,
                    '[object CanvasGradient]': 1
                };

                var objToString = Object.prototype.toString;

                function isDom(obj) {
                    return obj && obj.nodeType === 1
                            && typeof (obj.nodeName) == 'string';
                }

                function clone(source) {
                    if (typeof source == 'object' && source !== null) {
                        var result = source;
                        if (source instanceof Array) {
                            result = [];
                            for (var i = 0, len = source.length; i < len; i++) {
                                result[i] = clone(source[i]);
                            }
                        } else if (!BUILTIN_OBJECT[objToString.call(source)]
                                    // 是否为 dom 对象
                                && !isDom(source)) {
                            result = {};
                            for (var key in source) {
                                if (source.hasOwnProperty(key)) {
                                    result[key] = clone(source[key]);
                                }
                            }
                        }
                        return result;
                    }
                    return source;
                }
            }
    );
</script>
